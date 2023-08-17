/**
 * A handler for the REBALANCE_COMPLETE message.
 *
 * @author George Peppard
 */
public class ControllerRebalanceCompleteMessageHandler extends ControllerMessageHandler<RebalanceCompleteMessage> {

  /**
   * Initialises a new instance of the handler.
   *
   * @param message  the message to handle
   * @param services the service container
   * @param handler  the handler the message came from
   */
  public ControllerRebalanceCompleteMessageHandler(RebalanceCompleteMessage message,
      ControllerServiceContainer services, ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  @Override
  public void handle() {
    var dstore = services.getDstoreService().getDstore(handler.getDstorePort());
    if (dstore == null) return;

    services.getIndexService().handleRebalanceComplete(dstore);
  }
}
