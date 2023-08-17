/**
 * A message requesting removal of a file.
 *
 * @author George Peppard
 */
public class RemoveMessage extends Message {
  
  private final String fileName;

  public RemoveMessage(String fileName) {
    super(Protocol.REMOVE);

    this.fileName = fileName;
  }

  public static RemoveMessage parse(String[] args) {
    return new RemoveMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
