import java.io.IOException;
import java.net.Socket;

/**
 * A connection handler for a controller.
 */
public class ControllerConnectionHandler extends AbstractConnectionHandler {

  /**
   * The host controller's service container.
   */
  protected final ControllerServiceContainer sc;

  /**
   * The message handler factory for this handler.
   */
  protected final ControllerMessageHandlerFactory handlerFactory;

  /**
   * The port the Dstore we are connected to is running on, or 0 if this is not a Dstore.
   */
  protected int dstorePort = 0;

  /**
   * Initialises a new handler.
   *
   * @param socket the socket to communicate over
   * @param sc     the service container
   * @throws IOException if there is an error initialising the handler
   */
  public ControllerConnectionHandler(Socket socket, ControllerServiceContainer sc)
      throws IOException {
    super(socket);
    this.sc = sc;
    this.handlerFactory = new ControllerMessageHandlerFactory(this, sc);
  }

  @Override
  protected Message parseMessage(String type, String[] args) throws UnsupportedOperationException {
    return switch (type) {
      case Protocol.JOIN -> JoinMessage.parse(args);
      case Protocol.STORE -> StoreMessage.parse(args);
      case Protocol.STORE_ACK -> StoreAckMessage.parse(args);
      case Protocol.LOAD -> LoadMessage.parse(args);
      case Protocol.RELOAD -> ReloadMessage.parse(args);
      case Protocol.REMOVE -> RemoveMessage.parse(args);
      case Protocol.REMOVE_ACK -> RemoveAckMessage.parse(args);
      case Protocol.LIST -> !isDstore() ? new ListMessage() : ListResponseMessage.parse(args);
      case Protocol.REBALANCE_COMPLETE -> new RebalanceCompleteMessage();
      default -> super.parseMessage(type, args);
    };
  }

  @Override
  protected void hookConnected() {
    if (sc.getBlockingOperationsService().shouldQueueMessages()) {
      stopProcessing();
    }

    sc.getBlockingOperationsService()
        .registerListeners(this::stopProcessing, this::startProcessing);
  }

  @Override
  protected void hookDisconnected() {
    sc.getBlockingOperationsService().deregisterListener(
        (QueueHoldEventListener) this::startProcessing);
    sc.getBlockingOperationsService().deregisterListener(
        (QueueHoldEndEventListener) this::stopProcessing);

    if (isDstore()) {
      sc.getDstoreService().leave(dstorePort);
      Logger.info(log("lost Dstore!"));
    }
  }

  @Override
  protected IMessageHandlerFactory getHandlerFactory() {
    return handlerFactory;
  }

  /**
   * Returns whether we are connected to a Dstore.
   */
  public boolean isDstore() {
    return dstorePort > 0;
  }

  /**
   * Sets the Dstore port for this connection.
   *
   * @param dstorePort the Dstore port
   */
  public void setDstorePort(int dstorePort) {
    this.dstorePort = dstorePort;
  }

  /** Returns this connection's Dstore port, or 0 if we are not connected to a Dstore. */
  public int getDstorePort() {
    return dstorePort;
  }

  /**
   * If we are a Dstore, we should always process messages regardless of any queue holds. Dstores
   * do not send unsolicited messages to the controller.
   */
  @Override
  protected boolean shouldBypassQueueBlock() {
    return isDstore();
  }
}
