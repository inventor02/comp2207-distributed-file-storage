/**
 * A message acknowledging the start of a store operation which will be followed by the file data.
 *
 * @author George Peppard
 */
public class StoreAckMessage extends Message {

  private final String fileName;

  public StoreAckMessage(String fileName) {
    super(Protocol.STORE_ACK);

    this.fileName = fileName;
  }

  public static StoreAckMessage parse(String[] args) {
    return new StoreAckMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
