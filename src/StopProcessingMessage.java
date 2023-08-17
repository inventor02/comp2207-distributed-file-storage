/**
 * A "pseudo-message" used to tell a message handler that there will be no more messages to process.
 *
 * @author George Peppard
 */
public class StopProcessingMessage extends Message {

  /**
   * Creates a new message.
   */
  public StopProcessingMessage() {
    super("");
  }
}
