/**
 * An "ACK" message which is sent on a successful operation.
 *
 * @author George Peppard
 */
public class AckMessage extends Message {
  
  public AckMessage() {
    super(Protocol.ACK);
  }
}
