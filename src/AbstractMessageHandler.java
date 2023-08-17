/**
 * A message handler.
 *
 * @param <T> the type of message that this handler handles
 *
 * @author George Peppard
 */
public abstract class AbstractMessageHandler<T extends Message> implements IMessageHandler<T> {

  /**
   * The message that is being handled.
   */
  protected final T message;

  /**
   * Initialises a new instance of the handler.
   *
   * @param message the message to handle
   */
  public AbstractMessageHandler(T message) {
    this.message = message;
  }
}
