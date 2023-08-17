/**
 * An error message that is sent when the file cannot be loaded (for example if none of the
 * Dstores that store the file were capable of serving it).
 *
 * @author George Peppard
 */
public class ErrorLoadMessage extends Message {
  
  public ErrorLoadMessage() {
    super(Protocol.ERROR_LOAD);
  }
}
