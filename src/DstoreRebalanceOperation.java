import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * A rebalance operation on the Dstore level.
 *
 * @author George Peppard
 */
public class DstoreRebalanceOperation implements Runnable {

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * The rebalance message.
   */
  private final RebalanceMessage rebalance;

  /**
   * Creates the operation.
   *
   * @param services  the service container
   * @param rebalance the rebalance message
   */
  public DstoreRebalanceOperation(DstoreServiceContainer services, RebalanceMessage rebalance) {
    this.services = services;
    this.rebalance = rebalance;
  }

  /**
   * Runs the rebalance operation.
   */
  @Override
  public void run() {
    Logger.info("Started rebalance");

    var filesForStores = new HashMap<Integer, List<LocalFile>>();
    rebalance.getFilesToSend().forEach((file, port) -> {
      var localFile = services.getLocalFileService().getLocalFileByName(file);
      if (localFile == null) {
        Logger.warn("Cannot rebalance nonexistent local file {}", file);
        return;
      }

      for (int p : port) {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        var thisStore = filesForStores.getOrDefault(p, new ArrayList<>());
        thisStore.add(localFile);
        filesForStores.put(p, thisStore);
      }
    });

    Logger.info("Rebalance operation summary:");
    filesForStores.forEach((port, files) -> Logger.info("Port {}: files {}", port,
        files.stream().map(LocalFile::getName).collect(
            Collectors.joining(" "))));
    Arrays.stream(rebalance.getFilesToRemove()).forEach(f -> Logger.info("Remove: file {}", f));

    var latch = new CountDownLatch(filesForStores.size());
    Logger.info("Starting inter-Dstore rebalance");
    filesForStores.forEach((port, files) -> {
      new Thread(() -> interStoreRebalance(latch, port, files),
          "DS Rebalance Orchestration Worker for " + port).start();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      Logger.error("Interrupted waiting for inter-store rebalances, won't delete any files");
      return;
    }

    for (String fileName : rebalance.getFilesToRemove()) {
      var file = services.getLocalFileService().getLocalFileByName(fileName);

      if (file == null) {
        Logger.warn("Cannot remove nonexistent file {}", fileName);
        continue;
      }

      Logger.info("Removing file {}", file.getName());
      try {
        services.getLocalFileService().removeFile(file);
      } catch (IOException e) {
        Logger.warn("Failed to remove file {}: {}", fileName, e.getMessage());
      }
    }

    Logger.info("Rebalance is complete, informing controller");
    services.getDstore().getControllerHandler().send(new RebalanceCompleteMessage());
  }

  /**
   * Performs one Dstore->Dstore rebalance, which may contain multiple files.
   *
   * @param latch a count down latch
   * @param port  the port to send the files to
   * @param files the files to send
   */
  private void interStoreRebalance(CountDownLatch latch, int port, List<LocalFile> files) {
    DstoreConnectionHandler handler;
    try {
      var conn = new Socket(WellKnownHosts.LOCALHOST, port);
      handler = new DstoreConnectionHandler(conn, services);
      new Thread(handler, "Rebalance Connection Handler for " + port).start();
    } catch (IOException e) {
      Logger.error("Failed to connect to Dstore at port {}: {}", port, e.getMessage());
      return;
    }

    for (var file : files) {
      Logger.info("Sending file {}", file.getName());

      byte[] fileContent;
      try {
        fileContent = services.getLocalFileService().getFileContent(file);
      } catch (IOException e) {
        Logger.error("Failed to get file {} from local storage: {}", file.getName(),
            e.getMessage());
        continue;
      }

      var msg = new RebalanceStoreMessage(file.getName(), file.getSize());
      var future = handler.getAcknowledgementService().getFuture();

      handler.send(msg);
      try {
        future.get(services.getDstore().getTimeoutMs(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        Logger.warn("Failed to send file {}", file.getName());
        continue;
      }

      handler.sendBytes(fileContent);
      Logger.info("Sent file {} to :{}", file.getName(), port);
    }

    try {
      handler.close();
    } catch (IOException e) {
      Logger.error("Could not close Dstore connection handler: {}", e.getMessage());
    }

    latch.countDown();
  }
}
