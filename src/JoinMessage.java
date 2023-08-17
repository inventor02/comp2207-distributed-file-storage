/**
 * A message that is sent when a new Dstore joins.
 *
 * @author George Peppard
 */
public class JoinMessage extends Message {
  
  private final int port;

  public JoinMessage(int port) {
    super(Protocol.JOIN);

    this.port = port;
  }

  public static JoinMessage parse(String[] args) {
    return new JoinMessage(Integer.parseInt(args[0]));
  }

  @Override
  public String toString() {
    return super.toString() + " " + port;
  }

  public int getPort() {
    return port;
  }
}
