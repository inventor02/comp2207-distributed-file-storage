/**
 * A handler for the STORE_ACK message.
 *
 * @author George Peppard
 */
public class ControllerStoreAckMessageHandler extends ControllerMessageHandler<StoreAckMessage> {

  public ControllerStoreAckMessageHandler(StoreAckMessage message,
      ControllerServiceContainer services, ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Tells the index service that the Dstore has stored the file successfully.
   */
  @Override
  public void handle() {
    services.getIndexService().acknowledgeSuccessfulStore(message.getFileName());
  }
}
