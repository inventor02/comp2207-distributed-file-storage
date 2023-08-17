/**
 * The message handler factory for controllers.
 *
 * @author George Peppard
 */
public class ControllerMessageHandlerFactory implements IMessageHandlerFactory {

  /**
   * The handler that received the message.
   */
  private final ControllerConnectionHandler handler;

  /**
   * The service container.
   */
  private final ControllerServiceContainer sc;

  /**
   * Initialises a new instance of the factory.
   *
   * @param handler the handler that is receiving messages
   * @param sc the controller's service container
   */
  public ControllerMessageHandlerFactory(ControllerConnectionHandler handler,
      ControllerServiceContainer sc) {
    this.handler = handler;
    this.sc = sc;
  }

  @Override
  public IMessageHandler<? extends Message> create(Message message) {
    if (message instanceof JoinMessage) {
      return new ControllerJoinMessageHandler((JoinMessage) message, sc, handler);
    }

    if (message instanceof StoreMessage) {
      return new ControllerStoreMessageHandler((StoreMessage) message, sc, handler);
    }

    if (message instanceof StoreAckMessage) {
      return new ControllerStoreAckMessageHandler((StoreAckMessage) message, sc, handler);
    }

    if (message instanceof RemoveMessage) {
      return new ControllerRemoveMessageHandler((RemoveMessage) message, sc, handler);
    }

    if (message instanceof RemoveAckMessage) {
      return new ControllerRemoveAckMessageHandler((RemoveAckMessage) message, sc, handler);
    }

    if (message instanceof ListMessage) {
      return new ControllerListMessageHandler((ListMessage) message, sc, handler);
    }

    if (message instanceof LoadMessage) {
      return new ControllerLoadMessageHandler((LoadMessage) message, sc, handler);
    }

    if (message instanceof ReloadMessage) {
      return new ControllerReloadMessageHandler((ReloadMessage) message, sc, handler);
    }

    if (message instanceof ListResponseMessage) {
      return new ControllerListResponseMessageHandler((ListResponseMessage) message, sc, handler);
    }

    if (message instanceof RebalanceCompleteMessage) {
      return new ControllerRebalanceCompleteMessageHandler((RebalanceCompleteMessage) message, sc, handler);
    }

    throw new UnsupportedOperationException("no handler type defined for this message");
  }
}
