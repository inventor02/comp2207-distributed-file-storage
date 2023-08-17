/**
 * A message to the client informing it that a remove operation has been completed.
 *
 * @author George Peppard
 */
public class RemoveCompleteMessage extends Message {
  
  public RemoveCompleteMessage() {
    super(Protocol.REMOVE_COMPLETE);
  }
}
