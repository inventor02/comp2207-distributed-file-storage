import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A centrally indexed file.
 *
 * @author George Peppard
 */
public class IndexedFile {

  /**
   * The name of the file.
   */
  private final String name;

  /**
   * The size, in bytes, of the file.
   */
  private final int size;

  /**
   * The list of Dstores that have a copy of this file.
   */
  private final List<DstoreModel> dstores = new ArrayList<>();

  /**
   * The latch that determines when the store operation has finished for this file.
   */
  private final CountDownLatch storeLatch;

  /**
   * The latch that determines when the file has been completely removed.
   */
  private CountDownLatch removeLatch;

  /**
   * When the file was created.
   */
  private final LocalDateTime createdAt = LocalDateTime.now();

  /**
   * The state of the file.
   */
  private IndexedFileState state;

  /**
   * Creates a new file, with a store latch and the {@link IndexedFileState#STORE_IN_PROGRESS}
   * state.
   *
   * @param name    the name of the file
   * @param size    the size in bytes of the file
   * @param dstores the list of Dstores that will initially hold this file
   */
  public IndexedFile(String name, int size, List<DstoreModel> dstores) {
    this.name = name;
    this.size = size;
    this.dstores.addAll(dstores);

    this.storeLatch = new CountDownLatch(this.dstores.size());
    this.state = IndexedFileState.STORE_IN_PROGRESS;
  }

  /**
   * Returns the name of the file.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the size in bytes of the file.
   */
  public int getSize() {
    return size;
  }

  /**
   * Returns the Dstores that have this file.
   */
  public List<DstoreModel> getDstores() {
    return dstores;
  }

  /**
   * Returns the store latch for this file.
   */
  public CountDownLatch getStoreLatch() {
    return storeLatch;
  }

  /**
   * Returns the state of this file.
   */
  public synchronized IndexedFileState getState() {
    return state;
  }

  /**
   * Sets the state of this file.
   *
   * @param state the new state
   */
  public synchronized void setState(IndexedFileState state) {
    this.state = state;
  }

  /**
   * Records the initiation of the removal of this file.
   */
  public synchronized void startRemove() {
    setState(IndexedFileState.REMOVE_IN_PROGRESS);
    removeLatch = new CountDownLatch(dstores.size());
  }

  /**
   * Returns the remove latch.
   */
  public CountDownLatch getRemoveLatch() {
    return removeLatch;
  }

  /**
   * Handles a new Dstore storing this file.
   *
   * @param dstore the dstore that now holds the file
   */
  public synchronized void addDstore(DstoreModel dstore) {
    dstores.add(dstore);
  }

  /**
   * Handles a Dstore that holds this file no longer holding it.
   *
   * @param dstore the dstore that no longer holds this file
   */
  public synchronized void removeDstore(DstoreModel dstore) {
    dstores.remove(dstore);

    if (dstores.size() < 1) {
      Logger.warn("File {} has been lost as no Dstores have a copy, so marking as gone", getName());
      setState(IndexedFileState.GONE);
    }
  }
}
