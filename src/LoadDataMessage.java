/**
 * A message requesting the data contained in a file to be sent.
 *
 * @author George Peppard
 */
public class LoadDataMessage extends Message {
  
  private final String fileName;

  public LoadDataMessage(String fileName) {
    super(Protocol.LOAD_DATA);

    this.fileName = fileName;
  }

  public static LoadDataMessage parse(String[] args) {
    return new LoadDataMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
