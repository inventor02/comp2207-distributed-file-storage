/**
 * A handler for the REBALANCE message.
 *
 * @author George Peppard
 */
public class DstoreRebalanceMessageHandler extends DstoreControllerMessageHandler<RebalanceMessage> {

  /**
   * Initialises a new handler.
   *
   * @param message  the message to handle
   * @param services the service container
   * @param handler  the handler that received the message
   */
  public DstoreRebalanceMessageHandler(RebalanceMessage message, DstoreServiceContainer services,
      DstoreControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Spins up a rebalance operation which orchestrates the rebalance.
   */
  @Override
  public void handle() {
    Logger.info("Starting rebalance on a new thread");

    var op = new DstoreRebalanceOperation(services, message);
    new Thread(op, "Rebalancer").start();
  }
}
