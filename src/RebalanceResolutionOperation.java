import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An operation that happens on a Dstore that consists of several actions, taken to resolve a
 * difference noticed when rebalancing is in progress.
 *
 * @author George Peppard
 */
public class RebalanceResolutionOperation {

  /**
   * The files to send to other Dstores.
   */
  private final Map<IndexedFile, List<DstoreModel>> filesToSend = new HashMap<>();

  /**
   * The files to remove.
   */
  private final List<IndexedFile> filesToRemove = new ArrayList<>();

  /**
   * Returns whether the operation is empty; that is, nothing needs to be done on this Dstore.
   */
  public boolean isNullOperation() {
    return filesToSend.size() < 1 && filesToRemove.size() < 1;
  }

  /**
   * Tracks a file as to be sent to another Dstore.
   *
   * @param file   the file to be sent
   * @param target the target Dstore
   * @return the operation
   */
  public RebalanceResolutionOperation sendFileTo(IndexedFile file, DstoreModel target) {
    if (filesToSend.containsKey(file)) {
      filesToSend.get(file).add(target);
    } else {
      var stores = new ArrayList<DstoreModel>();
      stores.add(target);
      filesToSend.put(file, stores);
    }

    return this;
  }

  /**
   * Tracks a file as to be removed.
   *
   * @param file the file to be removed
   * @return the operation
   */
  public RebalanceResolutionOperation deleteFile(IndexedFile file) {
    filesToRemove.add(file);

    return this;
  }

  /**
   * Returns the files to be sent, and to which Dstore they are to be sent to.
   */
  public Map<IndexedFile, List<DstoreModel>> getFilesToSend() {
    return filesToSend;
  }

  /**
   * Returns the files to be removed.
   */
  public List<IndexedFile> getFilesToRemove() {
    return filesToRemove;
  }

  /**
   * Returns a REBALANCE message that will perform the correct operations.
   */
  public RebalanceMessage toRebalanceMessage() {
    var sends = new HashMap<String, int[]>();
    getFilesToSend().forEach((file, stores) -> sends.put(file.getName(), stores.stream().mapToInt(DstoreModel::getPort).toArray()));

    var removes = getFilesToRemove().stream().map(IndexedFile::getName).toArray(String[]::new);

    return new RebalanceMessage(sends, removes);
  }
}
