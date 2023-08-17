/**
 * A handler for the REMOVE message.
 *
 * @author George Peppard
 */
public class ControllerRemoveMessageHandler extends ControllerMessageHandler<RemoveMessage> {

  public ControllerRemoveMessageHandler(RemoveMessage message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Starts a new remove operation at the index service.
   */
  @Override
  public void handle() {
    var result = services.getIndexService().removeFile(message.getFileName(), handler);

    if (result.isFailure()) {
      handler.send(result.getMessage());
    }
  }
}
