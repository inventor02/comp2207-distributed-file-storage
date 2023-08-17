/**
 * A message handler.
 *
 * @param <T> the type of message that this handler will serve
 */
public interface IMessageHandler<T extends Message> {

  /**
   * Handles the message. A separate thread orchestrates message handling, so this method can
   * block. Messages are guaranteed to be handled in the order they were received.
   */
  void handle();
}