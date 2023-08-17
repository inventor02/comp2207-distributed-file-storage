/**
 * A message between Dstores moving a file from one to the other as part of a rebalance.
 *
 * @author George Peppard
 * @see RebalanceOperation the orchestrating operation
 */
public class RebalanceStoreMessage extends Message {

  private final String fileName;
  private final int fileSize;

  public RebalanceStoreMessage(String fileName, int fileSize) {
    super(Protocol.REBALANCE_STORE);

    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  public static RebalanceStoreMessage parse(String[] args) {
    return new RebalanceStoreMessage(args[0], Integer.parseInt(args[1]));
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" %s %d", fileName, fileSize);
  }

  public String getFileName() {
    return fileName;
  }

  public int getFileSize() {
    return fileSize;
  }
}
