/**
 * A handler for the ACK message.
 *
 * @author George Peppard
 */
public class DstoreAckMessageHandler extends DstoreClientMessageHandler<AckMessage> {

  public DstoreAckMessageHandler(AckMessage message, DstoreServiceContainer services,
      DstoreConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Tells the acknowledgement service there has been an ACK.
   */
  @Override
  public void handle() {
    handler.getAcknowledgementService().handleAck();
  }
}
