/**
 * A message requesting a file to be loaded from a different Dstore.
 *
 * @author George Peppard
 */
public class ReloadMessage extends Message {

  private final String fileName;

  public ReloadMessage(String fileName) {
    super(Protocol.RELOAD);
    this.fileName = fileName;
  }

  public static ReloadMessage parse(String[] args) {
    return new ReloadMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
