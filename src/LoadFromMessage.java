/**
 * A message describing where data is to be loaded from.
 *
 * @author George Peppard
 */
public class LoadFromMessage extends Message {
  
  private final int port;
  private final int fileSize;

  public LoadFromMessage(int port, int fileSize) {
    super(Protocol.LOAD_FROM);

    this.port = port;
    this.fileSize = fileSize;
  }

  public static LoadFromMessage parse(String[] args) {
    return new LoadFromMessage(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" %d %d", port, fileSize);
  }

  public int getPort() {
    return port;
  }

  public int getFileSize() {
    return fileSize;
  }
}
