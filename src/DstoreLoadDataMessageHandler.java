import java.io.IOException;

/**
 * A handler for the LOAD_DATA message.
 *
 * @author George Peppard
 */
public class DstoreLoadDataMessageHandler extends DstoreClientMessageHandler<LoadDataMessage> {

  public DstoreLoadDataMessageHandler(LoadDataMessage message, DstoreServiceContainer services,
      DstoreConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Finds the file in the local index and sends its contents raw over the socket.
   */
  @Override
  public void handle() {
    var fName = message.getFileName();
    var file = services.getLocalFileService().getLocalFileByName(fName);

    if (file == null) {
      handler.send(new ErrorFileDoesNotExistMessage());
      return;
    }

    try {
      var content = services.getLocalFileService().getFileContent(file);
      handler.sendBytes(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
