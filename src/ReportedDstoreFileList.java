import java.util.Collections;
import java.util.List;

/**
 * A reported state of the local index from a Dstore.
 *
 * @author George Peppard
 */
public class ReportedDstoreFileList {

  private final DstoreModel dstore;
  private final List<IndexedFile> files;

  public ReportedDstoreFileList(DstoreModel dstore, List<IndexedFile> files) {
    this.dstore = dstore;
    this.files = files;
  }

  public DstoreModel getDstore() {
    return dstore;
  }

  public List<IndexedFile> getFiles() {
    return Collections.unmodifiableList(files);
  }
}
