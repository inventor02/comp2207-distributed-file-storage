import java.io.IOException;

/**
 * A handler for the REMOVE message.
 *
 * @author George Peppard
 */
public class DstoreRemoveMessageHandler extends DstoreControllerMessageHandler<RemoveMessage> {

  public DstoreRemoveMessageHandler(RemoveMessage message, DstoreServiceContainer services,
      DstoreControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Tells the local file service to remove the file from our folder, if it can.
   */
  @Override
  public void handle() {
    var file = services.getLocalFileService().getLocalFileByName(message.getFileName());

    if (file == null) {
      handler.send(new ErrorFileDoesNotExistMessage());
      return;
    }

    try {
      services.getLocalFileService().removeFile(file);
      handler.send(new RemoveAckMessage(file.getName()));
    } catch (IOException e) {
      Logger.error("Failed to remove local file {}: {}", file.getName(), e.getMessage());
    }
  }
}
