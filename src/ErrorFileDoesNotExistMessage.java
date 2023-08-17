/**
 * An error message sent when the requested file does not exist.
 *
 * @author George Peppard
 */
public class ErrorFileDoesNotExistMessage extends Message {
  
  public ErrorFileDoesNotExistMessage() {
    super(Protocol.ERROR_FILE_DOES_NOT_EXIST);
  }
}
