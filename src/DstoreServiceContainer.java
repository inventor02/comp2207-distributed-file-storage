/**
 * A service container for Dstores.
 */
public class DstoreServiceContainer {

  /**
   * The Dstore itself.
   */
  private final Dstore dstore;

  /**
   * The local file management service.
   */
  private final LocalFileService localFileService = new LocalFileService(this);

  /**
   * Initialises the container.
   *
   * @param dstore the Dstore
   */
  public DstoreServiceContainer(Dstore dstore) {
    this.dstore = dstore;
    Logger.info("initialised dstore service container - you should only see this message once");
  }

  /**
   * Returns the Dstore main class.
   */
  public Dstore getDstore() {
    return dstore;
  }

  /**
   * Returns the local file service.
   */
  public LocalFileService getLocalFileService() {
    return localFileService;
  }
}
