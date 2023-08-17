/**
 * A handler for the JOIN message.
 *
 * @author George Peppard
 */
public class ControllerJoinMessageHandler extends ControllerMessageHandler<JoinMessage> {

  public ControllerJoinMessageHandler(JoinMessage message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Tells interested services that we are a new Dstore and requests a rebalance so this Dstore can
   * get some files.
   */
  @Override
  public void handle() {
    var port = message.getPort();
    services.getDstoreService().join(port, handler);
    handler.setDstorePort(port);

    if (services.getDstoreService().hasEnoughDstores()) {
      services.getIndexService().runAdHocRebalance();
    } else {
      Logger.info("Not running rebalance as not enough Dstores");
    }
  }
}
