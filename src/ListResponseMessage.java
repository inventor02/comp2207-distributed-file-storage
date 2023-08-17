/**
 * A message containing a list of files.
 *
 * @author George Peppard
 */
public class ListResponseMessage extends Message {
  
  private final String[] files;

  public ListResponseMessage(String[] files) {
    super(Protocol.LIST);

    this.files = files;
  }

  public static ListResponseMessage parse(String[] args) {
    return new ListResponseMessage(args);
  }

  @Override
  public String toString() {
    return super.toString() + " " + String.join(" ", files);
  }

  public String[] getFiles() {
    return files;
  }
}
