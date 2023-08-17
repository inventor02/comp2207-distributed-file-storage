/**
 * A message representing the rebalance operation being complete.
 *
 * @author George Peppard
 */
public class RebalanceCompleteMessage extends Message {
  
  public RebalanceCompleteMessage() {
    super(Protocol.REBALANCE_COMPLETE);
  }
}
