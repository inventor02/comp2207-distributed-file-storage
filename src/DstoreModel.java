import java.time.LocalDateTime;

/**
 * A model representing a Dstore.
 */
public class DstoreModel {

  /**
   * The port it is listening on.
   */
  private final int port;

  /**
   * When the Dstore connected to the controller.
   */
  private final LocalDateTime createdAt;

  /**
   * The handler that can be used to communicate with the Dstore.
   */
  private final ControllerConnectionHandler handler;

  /**
   * Creates a new Dstore.
   *
   * @param port    the port the Dstore says it is listening on
   * @param handler the handler the Dstore is connected to
   */
  public DstoreModel(int port, ControllerConnectionHandler handler) {
    this.port = port;
    this.createdAt = LocalDateTime.now();
    this.handler = handler;
  }

  /**
   * Returns the port the Dstore says it is listening on.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns when the Dstore connected to the controller.
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Returns the connection handler the Dstore is connected to.
   */
  public ControllerConnectionHandler getHandler() {
    return handler;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DstoreModel other = (DstoreModel) obj;
    if (port != other.port) {
      return false;
    }
    if (createdAt == null) {
      return other.createdAt == null;
    } else {
      return createdAt.equals(other.createdAt);
    }
  }

  @Override
  public String toString() {
    return "Dstore[port=" + getPort() + "]";
  }
}
