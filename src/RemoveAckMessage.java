/**
 * A message acknowledging removal of a file.
 *
 * @author George Peppard
 */
public class RemoveAckMessage extends Message {
  
  private final String fileName;

  public RemoveAckMessage(String fileName) {
    super(Protocol.REMOVE_ACK);

    this.fileName = fileName;
  }

  public static RemoveAckMessage parse(String[] args) {
    return new RemoveAckMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
