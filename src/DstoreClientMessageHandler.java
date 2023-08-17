/**
 * A message handler for messages from the client to a Dstore.
 *
 * @param <T> the type of message
 * @author George Peppard
 */
public abstract class DstoreClientMessageHandler<T extends Message> extends
    DstoreMessageHandler<T> {

  /**
   * The handler that received the message.
   */
  protected final DstoreConnectionHandler handler;

  /**
   * Initialises a new instance of the handler.
   *
   * @param message  the message that was received
   * @param services the service container
   * @param handler  the handler that received the message
   */
  public DstoreClientMessageHandler(T message, DstoreServiceContainer services,
      DstoreConnectionHandler handler) {
    super(message, services);
    this.handler = handler;
  }

}
