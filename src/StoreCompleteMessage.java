/**
 * A message denoting completion of a store operation.
 *
 * @author George Peppard
 */
public class StoreCompleteMessage extends Message {
  
  public StoreCompleteMessage() {
    super(Protocol.STORE_COMPLETE);
  }
}
