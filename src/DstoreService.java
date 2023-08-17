import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A service that manages connected Dstores.
 *
 * @author George Peppard
 */
public class DstoreService {

  /**
   * The service container.
   */
  private final ControllerServiceContainer services;

  /**
   * A list of connected Dstores.
   */
  private final List<DstoreModel> dstores = new ArrayList<>();

  /**
   * A number representing the next Dstore that will be allocated.
   */
  private int nextAllocation = 0;

  /**
   * Initialises the service.
   *
   * @param services the service container
   */
  public DstoreService(ControllerServiceContainer services) {
    this.services = services;
  }

  /**
   * Adds a connected Dstore.
   *
   * @param port    the port the Dstore says it is listening on
   * @param handler the Dstore's connection handler
   * @return the created Dstore model
   */
  public synchronized DstoreModel join(int port, ControllerConnectionHandler handler) {
    var dstore = new DstoreModel(port, handler);
    dstores.add(dstore);

    Logger.info("New Dstore at port {} added", port);

    return dstore;
  }

  /**
   * Handles a Dstore disconnecting from the controller.
   *
   * @param port the port of the Dstore
   */
  public synchronized void leave(int port) {
    var dstore = getDstore(port);
    if (dstore == null) {
      return; // we never knew about this Dstore anyway
    }

    services.getIndexService().removeDstore(dstore);
    dstores.remove(dstore);

    Logger.info("Lost Dstore at port {}", port);
  }

  /**
   * Returns the Dstore on the specified port.
   */
  public synchronized DstoreModel getDstore(int port) {
    return dstores.stream()
        .filter(p -> p.getPort() == port)
        .findFirst().orElse(null);
  }

  /**
   * Returns the next Dstore for allocation.
   */
  public synchronized List<DstoreModel> getNext(int count) {
    var stores = new ArrayList<DstoreModel>();

    for (int i = 0; i < count; i++) {
      stores.add(dstores.get(nextAllocation++ % dstores.size()));
    }

    return Collections.unmodifiableList(stores);
  }

  /**
   * Returns whether there are enough Dstores to perform operations.
   */
  public synchronized boolean hasEnoughDstores() {
    return dstores.size() >= services.getController().getReplicationFactor();
  }

  /**
   * Returns all Dstores.
   */
  public synchronized List<DstoreModel> getAllDstores() {
    return Collections.unmodifiableList(dstores);
  }
}
