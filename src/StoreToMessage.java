import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A message telling a client where to send some files for storage.
 *
 * @author George Peppard
 */
public class StoreToMessage extends Message {

  private final int[] ports;

  public StoreToMessage(int[] ports) {
    super(Protocol.STORE_TO);
    this.ports = ports;
  }

  public static StoreToMessage parse(String[] args) {
    return new StoreToMessage(Arrays.stream(args).mapToInt(Integer::parseInt).toArray());
  }

  @Override
  public String toString() {
    return super.toString() + " " +
        String.join(" ", Arrays.stream(ports).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
  }

  public int[] getPorts() {
    return ports;
  }
}
