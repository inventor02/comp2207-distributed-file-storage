import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An operation in which files are automatically rebalanced around the Dstore pool to achieve
 * system-wide goals.
 *
 * @author George Peppard
 */
public class RebalanceOperation implements Runnable {

  /**
   * The service container.
   */
  private final ControllerServiceContainer services;

  /**
   * The final list of rebalance resolution operations that will be performed.
   */
  private Map<DstoreModel, RebalanceResolutionOperation> finalOps;

  /**
   * A latch used to track completion of the rebalance operation.
   */
  private CountDownLatch completionLatch;

  /**
   * Creates a new operation.
   *
   * @param services the service container
   */
  public RebalanceOperation(ControllerServiceContainer services) {
    this.services = services;
  }

  /**
   * Executes the operation.
   */
  @Override
  public void run() {
    try {
      if (!services.getIndexService().refreshFileList()) {
        Logger.warn("Couldn't refresh file list for all Dstores, aborting rebalance");
        return;
      }
    } catch (InterruptedException e) {
      Logger.error("File list refresh was interrupted, aborting rebalance.");
      return;
    }

    var filesCount = services.getIndexService().getFiles().size();
    var dstores = services.getDstoreService().getAllDstores();

    Logger.info("Will rebalance {} files between {} Dstores", filesCount, dstores.size());

    var targetFileCountLower = getTargetFileCountLowerBound();
    var targetFileCountUpper = getTargetFileCountUpperBound();

    Logger.info("Target files for each Dstore is between {} and {}", targetFileCountLower,
        targetFileCountUpper);

    // Find all Dstores that store too few files or too many files
    var dstoresWithShortage = new HashMap<DstoreModel, Integer>();
    var dstoresWithOverage = new HashMap<DstoreModel, Integer>();

    for (DstoreModel store : dstores) {
      var filesOnStore = services.getIndexService().getFilesByDstore(store).size();

      if (filesOnStore < targetFileCountLower) { // shortage
        var diff = targetFileCountLower - filesOnStore;
        dstoresWithShortage.put(store, diff);

        Logger.info("{} has a shortage of {} files", store, diff);
      } else if (filesOnStore > targetFileCountUpper) { // overage
        var diff = filesOnStore - targetFileCountUpper;
        dstoresWithOverage.put(store, diff);

        Logger.info("{} has an overage of {} files", store, diff);
      }
    }

    // Find all files that are not stored on enough Dstores
    var filesWithShortages = new HashMap<IndexedFile, Integer>();
    services.getIndexService().getFiles().forEach(f -> {
      if (f.getDstores().size() < services.getController().getReplicationFactor()) {
        filesWithShortages.put(f,
            services.getController().getReplicationFactor() - f.getDstores().size());
      }
    });

    Logger.info("{} files have a replication shortage", filesWithShortages.size());

    var storeOps = new HashMap<DstoreModel, RebalanceResolutionOperation>();
    dstores.forEach(d -> storeOps.put(d, new RebalanceResolutionOperation()));

    Stack<StoredFile> fileStack = new Stack<>();

    // Add all files that need to be replicated more times to the stack n times
    filesWithShortages.forEach((f, count) -> {
      for (int i = 0; i < count; i++) {
        var candidateStore = f.getDstores().stream().findFirst();
        if (candidateStore.isEmpty()) {
          Logger.warn("Cannot rectify a file shortage if no Dstores have it: {}", f.getName());
          break;
        }

        fileStack.add(new StoredFile(candidateStore.get(), f));
      }
    });

    // Dstores with overage should push some arbitrary files to the stack
    dstoresWithOverage.forEach((store, count) -> {
      var op = storeOps.get(store);
      var filesOnStore = services.getIndexService().getFilesByDstore(store);
      filesOnStore.stream().limit(count).forEach(f -> {
        fileStack.push(new StoredFile(store, f));
        op.deleteFile(f);
      });
    });

    // Dstores with shortages should take files from the stack
    dstoresWithShortage.forEach((store, count) -> {
      for (int i = 0; i < count; i++) {
        var sf = fileStack.pop();
        var sfOp = storeOps.get(sf.getStore());
        sfOp.sendFileTo(sf.getFile(), store);
      }
    });

    Logger.info("Rebalance Resolution Summary:");
    finalOps = new HashMap<>();
    storeOps.forEach((store, op) -> {
      if (op.isNullOperation()) {
        Logger.info("{} will do nothing", store);
        return;
      }

      finalOps.put(store, op);
      Logger.info("{} will send {} files and lose {} files", store, op.getFilesToSend().size(),
          op.getFilesToRemove().size());
    });

    if (!fileStack.empty()) {
      Logger.warn("There are still {} files on the file stack!", fileStack.size());
    }

    // Set up the latch
    completionLatch = new CountDownLatch(finalOps.size());

    // Send the messages
    finalOps.forEach((store, op) -> store.getHandler().send(op.toRebalanceMessage()));

    // Wait for completion
    try {
      if (!completionLatch.await(services.getController().getTimeoutMs(), TimeUnit.MILLISECONDS)) {
        Logger.warn("Not all Dstores responded to rebalance in time!");
      }
    } catch (InterruptedException e) {
      Logger.error("Completion timeout interrupted");
    }

    // This line MUST be here to ensure the blocking operation is cleared off.
    // If the BOS is not told about the end of the rebalance operation, no messages will be
    // handled again!
    services.getBlockingOperationsService().finishRebalance();
    services.getIndexService().finishRebalance();

    Logger.info("Done at {}", LocalDateTime.now().toString());
  }

  /**
   * Handles a REBALANCE_COMPLETE message.
   *
   * @param dstore the store that completed the rebalance
   */
  public void handleCompleteMessage(DstoreModel dstore) {
    completionLatch.countDown();
    Logger.info("Got rebalance completion message, {} to go", completionLatch.getCount());

    var op = finalOps.get(dstore);

    op.getFilesToSend().forEach((file, stores) -> {
      stores.forEach(file::addDstore);
    });

    for (IndexedFile removedFile : op.getFilesToRemove()) {
      removedFile.removeDstore(dstore);
    }
  }

  /**
   * Returns the lower bound of the file target.
   */
  private int getTargetFileCountLowerBound() {
    return (int) Math.floor(getTargetFileCount());
  }

  /**
   * Returns the upper bound of the file target.
   */
  private int getTargetFileCountUpperBound() {
    return (int) Math.ceil(getTargetFileCount());
  }

  /**
   * Returns the target file count.
   */
  private double getTargetFileCount() {
    return (double) (services.getIndexService().getFiles().size() * services.getController()
        .getReplicationFactor()) / services.getDstoreService().getAllDstores().size();
  }
}
