/**
 * A message handler for Dstore messages from the controller.
 *
 * @param <T> the message type
 * @author George Peppard
 */
public abstract class DstoreControllerMessageHandler<T extends Message> extends
    DstoreMessageHandler<T> {

  /**
   * The controller connection handler.
   */
  protected final DstoreControllerConnectionHandler handler;

  /**
   * Initialises a new handler.
   *
   * @param message  the message to handle
   * @param services the service container
   * @param handler  the handler that received the message
   */
  public DstoreControllerMessageHandler(T message, DstoreServiceContainer services,
      DstoreControllerConnectionHandler handler) {
    super(message, services);
    this.handler = handler;
  }
}
