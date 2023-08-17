/**
 * A message requesting a file.
 *
 * @author George Peppard
 */
public class LoadMessage extends Message {
  
  private final String fileName;

  public LoadMessage(String fileName) {
    super(Protocol.LOAD);
    
    this.fileName = fileName;
  }

  public static LoadMessage parse(String[] args) {
    return new LoadMessage(args[0]);
  }

  @Override
  public String toString() {
    return super.toString() + " " + fileName;
  }

  public String getFileName() {
    return fileName;
  }
}
