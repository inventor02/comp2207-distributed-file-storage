import java.io.IOException;
import java.net.Socket;

/**
 * A connection handler that deals with connections between Dstores and the controller.
 *
 * @author George Peppard
 * @see DstoreConnectionHandler the equivalent handler for communicating with clients
 */
public class DstoreControllerConnectionHandler extends AbstractConnectionHandler {

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * The message handler factory.
   */
  private final DstoreControllerMessageHandlerFactory messageHandlerFactory;

  /**
   * Initialises a new instance of the handler.
   *
   * @param socket   the socket we are connected to
   * @param services the service container
   * @throws IOException if there is an error with the socket
   */
  public DstoreControllerConnectionHandler(Socket socket, DstoreServiceContainer services)
      throws IOException {
    super(socket);
    this.services = services;
    this.messageHandlerFactory = new DstoreControllerMessageHandlerFactory(this, services);
  }

  @Override
  protected void hookConnected() {
    super.hookConnected();

    Logger.info("Trying to join controller as Dstore");
    send(new JoinMessage(services.getDstore().getPort()));
  }

  @Override
  protected void hookDisconnected() {
    super.hookDisconnected();

    Logger.error("Controller has closed connection");
    Logger.error("Requires controller, exiting.");
    System.exit(ExitCodes.EXIT_SOCKET_ERR);
  }

  @Override
  protected IMessageHandlerFactory getHandlerFactory() {
    return messageHandlerFactory;
  }

  @Override
  protected Message parseMessage(String type, String[] args) throws UnsupportedOperationException {
    return switch (type) {
      case Protocol.REMOVE -> RemoveMessage.parse(args);
      case Protocol.LIST -> new ListMessage();
      case Protocol.REBALANCE -> RebalanceMessage.parse(args);
      default -> super.parseMessage(type, args);
    };
  }
}
