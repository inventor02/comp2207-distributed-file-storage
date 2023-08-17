import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A message instructing a Dstore to rebalance its files in a certain way.
 *
 * @author George Peppard
 */
public class RebalanceMessage extends Message {

  private final Map<String, int[]> filesToSend;
  private final String[] filesToRemove;

  public RebalanceMessage(Map<String, int[]> filesToSend, String[] filesToRemove) {
    super(Protocol.REBALANCE);

    this.filesToSend = filesToSend;
    this.filesToRemove = filesToRemove;
  }

  public static RebalanceMessage parse(String[] args) {
    var filesToSend = new HashMap<String, int[]>();
    var toSendCount = Integer.parseInt(args[0]);

    var i = 0;
    var offset = 1;
    while (i < toSendCount) {
      var fileName = args[offset++];
      var nodeCount = Integer.parseInt(args[offset++]);

      var nodes = Arrays.stream(Arrays.copyOfRange(args, offset, offset + nodeCount)).mapToInt(Integer::parseInt).toArray();
      filesToSend.put(fileName, nodes);
      offset += nodeCount;
      i++;
    }

    var toDeleteCount = Integer.parseInt(args[offset++]);
    var filesToDelete = Arrays.copyOfRange(args, offset, offset + toDeleteCount);

    return new RebalanceMessage(filesToSend, filesToDelete);
  }

  @Override
  public String toString() {
    var args = new StringBuilder();

    args.append(filesToSend.size());
    
    for (var fts : filesToSend.entrySet()) {
      args.append(" ")
        .append(fts.getKey())
        .append(" ")
        .append(fts.getValue().length)
        .append(" ")
        .append(Arrays.stream(fts.getValue()).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
    }

    args.append(" ").append(filesToRemove.length);
    for (var ftr : filesToRemove) {
      args.append(" ").append(ftr);
    }

    return super.toString() + " " + args.toString().trim();
  }

  public Map<String, int[]> getFilesToSend() {
    return filesToSend;
  }

  public String[] getFilesToRemove() {
    return filesToRemove;
  }
}