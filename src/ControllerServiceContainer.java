/**
 * A container for services used by controller units.
 *
 * @author George Peppard
 */
public class ControllerServiceContainer {

  /**
   * The controller main class itself.
   */
  private final Controller controller;

  /**
   * The Dstore service.
   */
  private final DstoreService dstoreService = new DstoreService(this);

  /**
   * The file index service.
   */
  private final IndexService indexService;

  /**
   * The blocking operation service.
   */
  private final BlockingOperationsService blockingOperationsService = new BlockingOperationsService(
      this);

  /**
   * Initialises a new service container.
   *
   * @param controller the controller providing the container
   */
  public ControllerServiceContainer(Controller controller) {
    this.controller = controller;
    this.indexService = new IndexService(this);
    Logger.info("initialised controller service container - you should only see this message once");
  }

  /**
   * Returns the Dstore service.
   */
  public DstoreService getDstoreService() {
    return dstoreService;
  }

  /**
   * Returns the controller.
   */
  public Controller getController() {
    return controller;
  }

  /**
   * Returns the index service.
   */
  public IndexService getIndexService() {
    return indexService;
  }

  /**
   * Returns the blocking operations service.
   */
  public BlockingOperationsService getBlockingOperationsService() {
    return blockingOperationsService;
  }
}
