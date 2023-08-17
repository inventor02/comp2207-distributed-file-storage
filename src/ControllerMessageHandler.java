/**
 * A message handler for controllers.
 *
 * @param <T> the type of message to be handled
 */
public abstract class ControllerMessageHandler<T extends Message> extends
    AbstractMessageHandler<T> {

  /**
   * The controller's service container.
   */
  protected final ControllerServiceContainer services;

  /**
   * The connection handler the message came from.
   */
  protected final ControllerConnectionHandler handler;

  /**
   * Initialises a new instance of the handler.
   *
   * @param message  the message to handle
   * @param services the service container
   * @param handler  the handler the message came from
   */
  public ControllerMessageHandler(T message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message);
    this.services = services;
    this.handler = handler;
  }
}
 