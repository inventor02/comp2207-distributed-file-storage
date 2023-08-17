/**
 * A file that is stored on a Dstore.
 *
 * @author George Peppard
 */
public class StoredFile {

  /**
   * The Dstore with the file.
   */
  private final DstoreModel store;

  /**
   * The file.
   */
  private final IndexedFile file;

  /**
   * Creates a new instance of the model.
   *
   * @param store the Dstore with the file
   * @param file the file
   */
  public StoredFile(DstoreModel store, IndexedFile file) {
    this.store = store;
    this.file = file;
  }

  /**
   * Returns the Dstore with the file.
   */
  public DstoreModel getStore() {
    return store;
  }

  /**
   * Returns the file that is stored.
   */
  public IndexedFile getFile() {
    return file;
  }
}
