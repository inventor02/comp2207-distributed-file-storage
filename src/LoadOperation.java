import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An operation in which a client loads a file from a Dstore.
 *
 * @author George Peppard
 */
public class LoadOperation {

  /**
   * The file being loaded.
   */
  private final IndexedFile file;

  /**
   * The handler that is loading the file.
   */
  private final ControllerConnectionHandler client;

  /**
   * The next Dstore index to load from.
   */
  private int nextDstore = 0;

  /**
   * The Dstores that will be used to serve this request.
   */
  private final List<DstoreModel> dstores;

  /**
   * Initialises a new load operation.
   *
   * @param file   the file to load
   * @param client the client that is loading the file
   */
  public LoadOperation(IndexedFile file, ControllerConnectionHandler client) {
    this.file = file;
    this.client = client;

    this.dstores = new ArrayList<>(file.getDstores());
    Collections.shuffle(dstores);
  }

  /**
   * Returns the file which is being loaded.
   */
  public IndexedFile getFile() {
    return file;
  }

  /**
   * Returns the client loading the file.
   */
  public ControllerConnectionHandler getClient() {
    return client;
  }

  /**
   * Get the next Dstore that can serve the file. If there are no Dstores remaining, null is
   * returned.
   *
   * @return the next Dstore to fetch from, or null if there are none left
   */
  public DstoreModel getNextDstore() {
    if (nextDstore >= dstores.size()) {
      Logger.warn("Ran out of Dstores to serve file from!");
      return null;
    }

    Logger.info("File will be served from Dstore with index {}", nextDstore);
    return dstores.get(nextDstore++);
  }
}
