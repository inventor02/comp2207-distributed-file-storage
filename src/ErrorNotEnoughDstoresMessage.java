/**
 * An error message that is sent when there are not enough Dstores connected to the controller to
 * fulfil the request.
 *
 * @author George Peppard
 */
public class ErrorNotEnoughDstoresMessage extends Message {
  
  public ErrorNotEnoughDstoresMessage() {
    super(Protocol.ERROR_NOT_ENOUGH_DSTORES);
  }
}
