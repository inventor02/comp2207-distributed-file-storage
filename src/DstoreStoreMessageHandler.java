import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A handler for the STORE message.
 *
 * @author George Peppard
 */
public class DstoreStoreMessageHandler extends DstoreClientMessageHandler<StoreMessage> {

  public DstoreStoreMessageHandler(StoreMessage message, DstoreServiceContainer services,
      DstoreConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Creates the local file record, stores the file and then tells the controller what we have
   * done.
   */
  @Override
  public void handle() {
    var file = message.getFileName();
    var size = message.getFileSize();

    var future = handler.getNextNBytes(size);
    if (future == null) {
      Logger.info("Another operation is waiting on bytes");
      return;
    }

    handler.send(new AckMessage());

    byte[] bytes;
    try {
      bytes = future.get();
    } catch (ExecutionException | InterruptedException e) {
      Logger.warn("Store operation interrupted: {}", e.getMessage());
      return;
    }

    try {
      Logger.info("Got {} bytes from client", bytes.length);

      services.getLocalFileService().addFile(file, size, bytes);
      services.getDstore().getControllerHandler().send(new StoreAckMessage(file));
    } catch (IOException e) {
      Logger.error("Failed to store file: {}", e.getMessage());
    }
  }
}
