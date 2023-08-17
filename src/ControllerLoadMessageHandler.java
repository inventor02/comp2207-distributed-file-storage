/**
 * A handler for the LOAD message.
 *
 * @author George Peppard
 */
public class ControllerLoadMessageHandler extends ControllerMessageHandler<LoadMessage> {

  public ControllerLoadMessageHandler(LoadMessage message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Finds a Dstore to load the file from, if we can.
   */
  @Override
  public void handle() {
    if (!services.getDstoreService().hasEnoughDstores()) {
      handler.send(new ErrorNotEnoughDstoresMessage());
      return;
    }

    var file = services.getIndexService().getAvailableFileByName(message.getFileName());

    if (file == null) {
      handler.send(new ErrorFileDoesNotExistMessage());
      return;
    }

    var loadOp = services.getIndexService().startLoad(handler, file);
    handler.send(new LoadFromMessage(loadOp.getNextDstore().getPort(), loadOp.getFile().getSize()));
  }
}
