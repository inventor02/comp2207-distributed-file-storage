/**
 * A message requesting a file to be stored.
 *
 * @author George Peppard
 */
public class StoreMessage extends Message {
  
  private final String fileName;
  private final int fileSize;

  public StoreMessage(String fileName, int fileSize) {
    super(Protocol.STORE);

    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  public static StoreMessage parse(String[] args) {
    return new StoreMessage(args[0], Integer.parseInt(args[1]));
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
