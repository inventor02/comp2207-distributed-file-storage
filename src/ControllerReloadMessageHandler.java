/**
 * A handler for the RELOAD operation.
 *
 * @author George Peppard
 */
public class ControllerReloadMessageHandler extends ControllerMessageHandler<ReloadMessage> {

  public ControllerReloadMessageHandler(ReloadMessage message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Find a new Dstore to load the file from, if we can, or fail.
   */
  @Override
  public void handle() {
    var loadOp = services.getIndexService().getLoadOperationForClientAndFileName(handler, message.getFileName());

    if (loadOp == null) {
      handler.send(new ErrorLoadMessage());
      return;
    }

    var nextDstore = loadOp.getNextDstore();

    if (nextDstore == null) {
      handler.send(new ErrorLoadMessage());
      return;
    }

    handler.send(new LoadFromMessage(nextDstore.getPort(), loadOp.getFile().getSize()));
  }
}
