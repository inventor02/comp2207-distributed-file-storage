import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A handler for the REBALANCE_STORE message.
 *
 * @author George Peppard
 */
public class DstoreRebalanceStoreMessageHandler extends
    DstoreMessageHandler<RebalanceStoreMessage> {

  /**
   * The connection handler in use.
   */
  private final DstoreConnectionHandler handler;

  public DstoreRebalanceStoreMessageHandler(RebalanceStoreMessage message,
      DstoreServiceContainer services, DstoreConnectionHandler handler) {
    super(message, services);
    this.handler = handler;
  }

  /**
   * Receives the requested file from the connected client.
   */
  @Override
  public void handle() {
    var fileContent = handler.getNextNBytes(message.getFileSize());

    handler.send(new AckMessage());

    byte[] bytes;
    try {
      bytes = fileContent.get(services.getDstore().getTimeoutMs(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      Logger.warn("Failed to get bytes: {}", e.getMessage());
      return;
    }

    try {
      services.getLocalFileService().addFile(message.getFileName(), message.getFileSize(), bytes);
    } catch (IOException e) {
      Logger.warn("Failed to store file {}: {}", message.getFileName(), e.getMessage());
    }

    Logger.info("Stored rebalanced file {} of size {}", message.getFileName(), message.getFileSize());
  }
}
