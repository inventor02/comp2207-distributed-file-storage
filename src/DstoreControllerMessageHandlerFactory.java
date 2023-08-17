/**
 * A message handler factory for messages from the controller to a Dstore.
 */
public class DstoreControllerMessageHandlerFactory implements IMessageHandlerFactory {

  /**
   * The controller connection handler.
   */
  private final DstoreControllerConnectionHandler handler;

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * Initialises the factory.
   *
   * @param handler  the message handler that is receiving messages
   * @param services the service container
   */
  public DstoreControllerMessageHandlerFactory(DstoreControllerConnectionHandler handler,
      DstoreServiceContainer services) {
    this.handler = handler;
    this.services = services;
  }

  @Override
  public IMessageHandler<? extends Message> create(Message message) {
    if (message instanceof RemoveMessage) {
      return new DstoreRemoveMessageHandler((RemoveMessage) message, services, handler);
    }

    if (message instanceof ListMessage) {
      return new DstoreListMessageHandler((ListMessage) message, services, handler);
    }

    if (message instanceof RebalanceMessage) {
      return new DstoreRebalanceMessageHandler((RebalanceMessage) message, services, handler);
    }

    throw new UnsupportedOperationException("no handler type defined for this message");
  }
}
