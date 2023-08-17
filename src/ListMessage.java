/**
 * A message requesting a list of files.
 *
 * @author George Peppard
 */
public class ListMessage extends Message {
  
  public ListMessage() {
    super(Protocol.LIST);
  }
}
