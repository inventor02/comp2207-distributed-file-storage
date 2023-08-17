/**
 * A message that has been sent or received.
 */
public class Message {

  /**
   * The first part of the message. This denotes the type of message.
   */
  private final String token;

  /**
   * Creates a new message.
   *
   * @param token the message token
   */
  public Message(String token) {
    this.token = token;
  }

  /**
   * Serializes the message for sending over the communication protocol.
   *
   * @return the string representation of the message
   */
  @Override
  public String toString() {
    return token;
  }
}
