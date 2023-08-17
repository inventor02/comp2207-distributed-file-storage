/**
 * A message handler for Dstores.
 *
 * @param <T> the type of message
 */
public abstract class DstoreMessageHandler<T extends Message> extends AbstractMessageHandler<T> {

  /**
   * The service container.
   */
  protected final DstoreServiceContainer services;

  /**
   * Initialises the handler.
   *
   * @param message  the message that was received
   * @param services the service container
   */
  public DstoreMessageHandler(T message, DstoreServiceContainer services) {
    super(message);
    this.services = services;
  }
}
