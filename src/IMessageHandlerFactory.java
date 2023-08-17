/**
 * A factory that creates {@link IMessageHandler}s from parsed messages.
 *
 * @author George Peppard
 */
public interface IMessageHandlerFactory {

  /**
   * Returns the correct handler for the type of the message, instantiated.
   *
   * @param message the message to handle
   * @return an instance of the correct handler type for the message type
   */
  IMessageHandler<? extends Message> create(Message message);
}
