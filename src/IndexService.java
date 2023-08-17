import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service to manage the central file index.
 */
public class IndexService {

  /**
   * The controller service container.
   */
  private final ControllerServiceContainer services;

  /**
   * The list of all files stored in the index.
   */
  private final List<IndexedFile> index = new ArrayList<>();

  /**
   * The list of all file load operations.
   */
  private final List<LoadOperation> loadOperations = new ArrayList<>();

  /**
   * The latch used for refreshing files from a Dstore-provided LIST.
   */
  private CountDownLatch refreshLatch;

  /**
   * The lists of reported states of Dstores.
   */
  private List<ReportedDstoreFileList> refreshFileLists;

  /**
   * The service that executes rebalance operations periodically.
   */
  private final ScheduledExecutorService rebalanceExecutorService = Executors.newSingleThreadScheduledExecutor();

  /**
   * The currently executing rebalance operation, if any.
   */
  private RebalanceOperation currentRebalanceOperation;

  /**
   * Initialises the service and sets up scheduled tasks.
   *
   * @param services the service container
   */
  public IndexService(ControllerServiceContainer services) {
    this.services = services;

    Logger.info("Initialising periodic rebalance with period {}s",
        services.getController().getRebalancePeriodSecs());
    rebalanceExecutorService.scheduleAtFixedRate(this::runScheduledRebalance,
        services.getController().getRebalancePeriodSecs(),
        services.getController().getRebalancePeriodSecs(), TimeUnit.SECONDS);
  }

  /**
   * Adds a file to the index.
   *
   * @param name   the name of the file
   * @param size   the size of the file in bytes
   * @param client the client that is storing the file
   * @return the result
   */
  public synchronized FileOperationResult addFile(String name, int size,
      ControllerConnectionHandler client) {
    if (getFileByName(name) != null) {
      return FileOperationResult.FILE_ALREADY_EXISTS;
    }

    services.getBlockingOperationsService().startStore();
    Logger.info("Adding file {} of size {}", name, size);

    var stores = services.getDstoreService()
        .getNext(services.getController().getReplicationFactor());
    var file = new IndexedFile(name, size, stores);
    index.add(file);

    Runnable stateUpdater = () -> {
      try {
        var success = file.getStoreLatch()
            .await(services.getController().getTimeoutMs(), TimeUnit.MILLISECONDS);

        if (!success) {
          Logger.error("Store operation timeout for file {}", file.getName());
          file.setState(IndexedFileState.GONE);
          return;
        }

        file.setState(IndexedFileState.AVAILABLE);
        client.send(new StoreCompleteMessage());

        Logger.info("Store complete for {}", file.getName());
      } catch (InterruptedException e) {
        Logger.error("Store operation interrupted for file {}", file.getName());
        file.setState(IndexedFileState.GONE);
      } finally {
        services.getBlockingOperationsService().finishStore();
      }
    };

    new Thread(stateUpdater, "CLR IS File " + file.getName() + " StateUpdater").start();

    return FileOperationResult.SUCCESS;
  }

  /**
   * Removes a file by name, contacting any Dstores that have it to remove the file as well.
   *
   * @param fileName the name of the file
   * @param client   the client that requested the removal
   * @return the result
   */
  public synchronized FileOperationResult removeFile(String fileName,
      ControllerConnectionHandler client) {
    var file = getAvailableFileByName(fileName);

    if (file == null) {
      return FileOperationResult.FILE_NOT_EXISTS;
    }

    return removeFile(file, client);
  }

  /**
   * Removes a file, contacting any Dstores that have it to remove the file as well.
   *
   * @param file   the file
   * @param client the client that requested the removal
   * @return the result
   */
  public synchronized FileOperationResult removeFile(IndexedFile file,
      ControllerConnectionHandler client) {
    if (file.getState() == IndexedFileState.REMOVE_IN_PROGRESS) {
      return FileOperationResult.FILE_ALREADY_REMOVING;
    } else if (file.getState() != IndexedFileState.AVAILABLE) {
      return FileOperationResult.FILE_NOT_EXISTS;
    }

    services.getBlockingOperationsService().startRemove();
    Logger.info("Removing file {}", file.getName());

    file.startRemove();
    var dstoreHandlers = file.getDstores().stream().map(DstoreModel::getHandler).toList();

    Runnable removeOperation = () -> {
      for (var handler : dstoreHandlers) {
        handler.send(new RemoveMessage(file.getName()));
      }

      try {
        var success = file.getRemoveLatch()
            .await(services.getController().getTimeoutMs(), TimeUnit.MILLISECONDS);

        if (!success) {
          Logger.error("Remove operation timeout for file {}", file.getName());
          file.setState(IndexedFileState.AVAILABLE);
          return;
        }

        file.setState(IndexedFileState.GONE);
        client.send(new RemoveCompleteMessage());
      } catch (InterruptedException e) {
        Logger.error("Remove operation interrupted for file {}", file.getName());
        file.setState(IndexedFileState.AVAILABLE);
      } finally {
        services.getBlockingOperationsService().finishRemove();
      }
    };

    new Thread(removeOperation, "CLR IS File " + file.getName() + " remove operation").start();
    return FileOperationResult.SUCCESS;
  }

  /**
   * Removes a Dstore from the index, changing all files that are stored on it to remove the
   * reference to this store.
   *
   * @param dstore the Dstore that is being removed
   */
  public void removeDstore(DstoreModel dstore) {
    Logger.info("Removing Dstore {} from all files that reference it", dstore);
    getFilesByDstore(dstore).forEach(f -> f.removeDstore(dstore));
  }

  /**
   * Handles a Dstore acknowledging a successful store operation.
   *
   * @param name the name of the file
   */
  public void acknowledgeSuccessfulStore(String name) {
    var file = getFileByName(name);

    if (file == null) {
      Logger.error("Cannot successfully store nonexistent file {}", name);
      return;
    }

    Logger.info("Successful store for file {} (latch +1)", name);
    file.getStoreLatch().countDown();
  }

  /**
   * Handles a Dstore acknowledging a successful remove operation.
   *
   * @param name       the name of the file
   * @param dstorePort the port of the Dstore that has removed the file
   */
  public void acknowledgeSuccessfulRemove(String name, int dstorePort) {
    var file = getFileByName(name);

    if (file == null) {
      Logger.error("Cannot successfully remove nonexistent file {}", name);
      return;
    }

    var dstore = services.getDstoreService().getDstore(dstorePort);

    if (dstore == null) {
      Logger.warn("Received RemoveAck message from a Dstore we do not know about, ignoring.");
      return;
    }

    if (!file.getDstores().contains(dstore)) {
      Logger.warn("Dstore acknowledged removal for orphaned file, ignoring it");
      return;
    }

    Logger.info("Successful remove for file {} (latch +1)", name);
    file.getDstores().remove(dstore);
    file.getRemoveLatch().countDown();
  }

  /**
   * Returns whether a file exists in the index. Files with a state of {@link IndexedFileState#GONE}
   * are ignored.
   *
   * @param name the name of the file
   * @return whether the file exists
   */
  public synchronized boolean fileExists(String name) {
    return getAllFiles().stream().anyMatch(f -> f.getName().equalsIgnoreCase(name));
  }

  /**
   * Returns the available files.
   */
  public synchronized List<IndexedFile> getFiles() {
    return index.stream().filter(f -> f.getState() == IndexedFileState.AVAILABLE).toList();
  }

  /**
   * Returns all files, apart from those that are {@link IndexedFileState#GONE}.
   */
  public List<IndexedFile> getAllFiles() {
    return getAllFiles(false);
  }

  /**
   * Returns all files we know of.
   *
   * @param withGone whether to return deleted files
   * @return a list of all files in the index
   */
  public synchronized List<IndexedFile> getAllFiles(boolean withGone) {
    var stream = index.stream();

    if (!withGone) {
      stream = stream.filter(f -> f.getState() != IndexedFileState.GONE);
    }

    return stream.toList();
  }

  /**
   * Returns an available file by its name, or null if it does not exist.
   *
   * @param name the name of the file
   * @return the file, or null if it does not exist
   */
  public IndexedFile getAvailableFileByName(String name) {
    return getFiles().stream().filter(f -> f.getName().equalsIgnoreCase(name))
        .findFirst().orElse(null);
  }

  /**
   * Returns a file by its name, or null if it does not exist.
   *
   * @param name the name of the file
   * @return the file, or null if it does not exist
   */
  public IndexedFile getFileByName(String name) {
    return getAllFiles().stream().filter(f -> f.getName().equalsIgnoreCase(name))
        .findFirst().orElse(null);
  }

  /**
   * Returns a list of all files that are held by a Dstore. Note that in some cases, the Dstore
   * might not actually have a copy of the file. If this is the case, this will be picked up the
   * next time the rebalance operation runs, and the file list is reconciled.
   *
   * @param dstore the Dstore holding the files
   * @return a list of all files the Dstore is attached to in the index
   */
  public List<IndexedFile> getFilesByDstore(DstoreModel dstore) {
    return getFiles().stream().filter(f -> f.getDstores().contains(dstore)).toList();
  }

  /**
   * Initiates a load operation for a file.
   *
   * @param client the client that wants the file
   * @param file   the file that the client wants
   * @return the operation that has been started
   */
  public synchronized LoadOperation startLoad(ControllerConnectionHandler client,
      IndexedFile file) {
    LoadOperation op;
    if ((op = getLoadOperationForClient(client)) != null) {
      Logger.info("removing old load operation from this client");
      loadOperations.remove(op);
    }

    op = new LoadOperation(file, client);
    loadOperations.add(op);

    Logger.info("Started new load operation for file {}", op.getFile());
    return op;
  }

  /**
   * Returns the most recent load operation for a client.
   *
   * @param client the client to query the operation list for
   * @return the operation that the client most recently initiated
   */
  public synchronized LoadOperation getLoadOperationForClient(ControllerConnectionHandler client) {
    return loadOperations.stream().filter(o -> o.getClient().equals(client)).findFirst()
        .orElse(null);
  }

  /**
   * Returns the most recent load operation for a client and file name.
   *
   * @param client the client that initiated the operation
   * @param file   the name of the file
   * @return the operation the client most recently initiated for that file
   */
  public synchronized LoadOperation getLoadOperationForClientAndFileName(
      ControllerConnectionHandler client, String file) {
    return loadOperations.stream().filter(o -> o.getClient().equals(client))
        .filter(o -> o.getFile().getName().equalsIgnoreCase(file)).findFirst().orElse(null);
  }

  /**
   * Refreshes the indexed file list with the real state of affairs from each Dstore.
   *
   * @throws InterruptedException if the operation is interrupted
   * @returns whether all Dstores responded
   */
  public boolean refreshFileList() throws InterruptedException {
    var allResponses = true;
    Logger.info("File index refresh operation requested");

    if (refreshLatch != null) {
      Logger.warn("cannot start a file refresh request when one is already running");
    }

    refreshFileLists = new ArrayList<>();
    var dstores = services.getDstoreService().getAllDstores();
    refreshLatch = new CountDownLatch(dstores.size());

    for (DstoreModel dstore : dstores) {
      dstore.getHandler().send(new ListMessage());
    }

    if (!refreshLatch.await(services.getController().getTimeoutMs(), TimeUnit.MILLISECONDS)) {
      Logger.warn("Not all Dstores replied in time!");
      allResponses = false;

      var repliedDstores = refreshFileLists.stream().map(ReportedDstoreFileList::getDstore)
          .toList();
      var lostDstores = services.getDstoreService().getAllDstores().stream()
          .filter(d -> !repliedDstores.contains(d)).toList();

      lostDstores.forEach(this::removeDstore);
    }

    for (ReportedDstoreFileList state : refreshFileLists) {
      var indexState = getFilesByDstore(state.getDstore());
      Logger.info("Reconciliation for Dstore {}, reported file count {}, indexed count {}",
          state.getDstore(), state.getFiles().size(), indexState.size());

      // files we have but the Dstore does not
      indexState.stream().filter(f -> !state.getFiles().contains(f)).forEach(f -> {
        Logger.info("Reconciliation discrepancy: {} does not have {} but we do", state.getDstore(),
            f.getName());
        f.removeDstore(state.getDstore());
      });

      Logger.info("Reconciled {}", state.getDstore());
    }

    refreshLatch = null;
    refreshFileLists = null;

    return allResponses;
  }

  /**
   * Handles a Dstore's file list.
   *
   * @param dstore the dstore that sent the list
   * @param files  the list of files the Dstore says it has
   */
  public void handleUpdatedFileList(DstoreModel dstore, String[] files) {
    if (refreshLatch == null) {
      Logger.info(
          "Got an updated file list without a refresh operation to match it to. Is it too late?");
      return;
    }

    Logger.info("Got updated file list from {}", dstore);

    var updatedFiles = Arrays.stream(files).map(this::getFileByName).filter(Objects::nonNull)
        .toList();
    Logger.info("There are {} files successfully processed out of {}", updatedFiles.size(),
        files.length);

    var state = new ReportedDstoreFileList(dstore, updatedFiles);
    refreshFileLists.add(state);
    refreshLatch.countDown();
  }

  /**
   * Counts down the completion latch in the current rebalance operation.
   */
  public void handleRebalanceComplete(DstoreModel dstore) {
    if (currentRebalanceOperation == null) {
      Logger.info("Cannot complete a rebalance that is not running.");
      return;
    }

    currentRebalanceOperation.handleCompleteMessage(dstore);
  }

  /**
   * Request an ad-hoc rebalance operation is started on a new thread.
   */
  public void runAdHocRebalance() {
    try {
      services.getBlockingOperationsService().startRebalance();
    } catch (AlreadyRebalancingException e) {
      Logger.info("Refusing to start rebalancing as one is already happening.");
      return;
    } catch (InterruptedException e) {
      Logger.error("Rebalance operation interrupted. This will probably lead to errors!");
      return;
    }

    Logger.info("Submitting ad-hoc rebalance task");
    new Thread(getNewRebalanceOperation(), "CLR IS Ad-Hoc Rebalance Task").start();
  }

  /**
   * Request a scheduled rebalance operation is started on a new thread.
   */
  private void runScheduledRebalance() {
    try {
      services.getBlockingOperationsService().startRebalance();
    } catch (AlreadyRebalancingException e) {
      Logger.info("Refusing to start rebalancing as one is already happening.");
      return;
    } catch (InterruptedException e) {
      Logger.error("Rebalance operation interrupted. This will probably lead to errors!");
      return;
    }

    Logger.info("Submitting scheduled rebalance task");
    new Thread(getNewRebalanceOperation(), "CLR IS Scheduled Rebalance Task").start();
  }

  /**
   * Creates a new rebalance operation and returns it.
   */
  private RebalanceOperation getNewRebalanceOperation() {
    return (currentRebalanceOperation = new RebalanceOperation(services));
  }

  /**
   * Removes the currently executing rebalance operation.
   */
  public void finishRebalance() {
    currentRebalanceOperation = null;
  }
}
