import java.io.IOException;
import java.net.Socket;

/**
 * A connection handler for Dstores. This handler communicates with clients.
 *
 * @author George Peppard
 * @see DstoreControllerConnectionHandler the equivalent handler for communicating with the
 * controller
 */
public class DstoreConnectionHandler extends AbstractConnectionHandler {

  /**
   * The message handler factory for this connection.
   */
  private final DstoreMessageHandlerFactory messageHandlerFactory;

  /**
   * Initialises a new instance of the handler.
   *
   * @param socket   the socket we are connected to
   * @param services the service container
   * @throws IOException if there is an error with the socket
   */
  public DstoreConnectionHandler(Socket socket, DstoreServiceContainer services)
      throws IOException {
    super(socket);
    this.messageHandlerFactory = new DstoreMessageHandlerFactory(this, services);
  }

  @Override
  protected IMessageHandlerFactory getHandlerFactory() {
    return messageHandlerFactory;
  }

  @Override
  protected Message parseMessage(String type, String[] args) throws UnsupportedOperationException {
    return switch (type) {
      case Protocol.STORE -> StoreMessage.parse(args);
      case Protocol.LOAD_DATA -> LoadDataMessage.parse(args);
      case Protocol.REBALANCE_STORE -> RebalanceStoreMessage.parse(args);
      case Protocol.ACK -> new AckMessage();
      default -> super.parseMessage(type, args);
    };
  }
}
