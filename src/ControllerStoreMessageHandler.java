/**
 * A handler for the STORE message.
 *
 * @author George Peppard
 */
public class ControllerStoreMessageHandler extends ControllerMessageHandler<StoreMessage> {

  public ControllerStoreMessageHandler(StoreMessage message, ControllerServiceContainer services, ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Initialises a file in our index, finds a Dstore to store the file to, and tells the client
   * to send the file to the Dstore directly. The index service will keep track of the operation.
   */
  @Override
  public void handle() {
    var name = message.getFileName();
    var size = message.getFileSize();

    if (!services.getDstoreService().hasEnoughDstores()) {
      handler.send(new ErrorNotEnoughDstoresMessage());
      return;
    }

    var result = services.getIndexService().addFile(name, size, handler);
    if (result.isFailure()) {
      handler.send(result.getMessage());
      return;
    }

    var file = services.getIndexService().getFileByName(name);
    var stores = file.getDstores();
    var ports = stores.stream().map(DstoreModel::getPort).mapToInt(Integer::intValue).toArray();

    var response = new StoreToMessage(ports);
    handler.send(response);
  }
}
