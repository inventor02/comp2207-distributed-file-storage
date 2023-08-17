/**
 * A message handler factory for messages from the client to the Dstore.
 *
 * @author George Peppard
 * @see DstoreControllerMessageHandlerFactory the equivalent factory for controller messages
 */
public class DstoreMessageHandlerFactory implements IMessageHandlerFactory {

  /**
   * The handler that receives messages.
   */
  private final DstoreConnectionHandler handler;

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * Initialises the factory.
   *
   * @param handler  the handler that receives messages
   * @param services the service container
   */
  public DstoreMessageHandlerFactory(DstoreConnectionHandler handler,
      DstoreServiceContainer services) {
    this.handler = handler;
    this.services = services;
  }

  @Override
  public IMessageHandler<? extends Message> create(Message message) {
    if (message instanceof StoreMessage) {
      return new DstoreStoreMessageHandler((StoreMessage) message, services, handler);
    }

    if (message instanceof LoadDataMessage) {
      return new DstoreLoadDataMessageHandler((LoadDataMessage) message, services, handler);
    }

    if (message instanceof RebalanceStoreMessage) {
      return new DstoreRebalanceStoreMessageHandler((RebalanceStoreMessage) message, services, handler);
    }

    if (message instanceof AckMessage) {
      return new DstoreAckMessageHandler((AckMessage) message, services, handler);
    }

    throw new UnsupportedOperationException("no handler type defined for this message");
  }
}
