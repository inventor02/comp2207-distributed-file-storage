/**
 * An error message sent when the requested file already exists somewhere.
 *
 * @author George Peppard
 */
public class ErrorFileAlreadyExistsMessage extends Message {
  
  public ErrorFileAlreadyExistsMessage() {
    super(Protocol.ERROR_FILE_ALREADY_EXISTS);
  }
}
