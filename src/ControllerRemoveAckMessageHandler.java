/**
 * A handler for the REMOVE_ACK message.
 *
 * @author George Peppard
 */
public class ControllerRemoveAckMessageHandler extends ControllerMessageHandler<RemoveAckMessage> {

  public ControllerRemoveAckMessageHandler(RemoveAckMessage message,
      ControllerServiceContainer services, ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Tells the index service we have removed the file successfully from that Dstore.-
   */
  @Override
  public void handle() {
    services.getIndexService().acknowledgeSuccessfulRemove(message.getFileName(), handler.getDstorePort());
  }
}
