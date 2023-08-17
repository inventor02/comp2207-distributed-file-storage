import java.io.IOException;
import java.net.ServerSocket;

/**
 * The main class for the controller.
 *
 * @author George Peppard
 */
public class Controller {

  /**
   * The main method. It checks arguments for validity and then constructs an instance of the class
   * with them.
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    if (args.length < 4) {
      System.err.println(
          "Usage: Controller [port] [replication factor] [timeout ms] [rebalance period s]");
      System.exit(ExitCodes.EXIT_INVALID_ARG_COUNT);
      return;
    }

    int port, replicationFactor, timeoutMs, rebalancePeriodSecs;
    try {
      port = Integer.parseInt(args[0]);
      replicationFactor = Integer.parseInt(args[1]);
      timeoutMs = Integer.parseInt(args[2]);
      rebalancePeriodSecs = Integer.parseInt(args[3]);
    } catch (NumberFormatException e) {
      System.err.println(
          "Usage: Controller [port] [replication factor] [timeout ms] [rebalance period s]");
      System.exit(ExitCodes.EXIT_INVALID_ARGS);
      return;
    }

    var controller = new Controller(port, replicationFactor, timeoutMs, rebalancePeriodSecs);
    controller.run();
  }

  // ---------------------------------------------------------------------------------------

  /**
   * The port the controller is running on.
   */
  private final int port;

  /**
   * The replication factor.
   */
  private final int replicationFactor;

  /**
   * The timeout in milliseconds.
   */
  private final int timeoutMs;

  /**
   * How often a rebalance should be requested, in seconds.
   */
  private final int rebalancePeriodSecs;

  /**
   * The controller's service container.
   */
  private final ControllerServiceContainer sc;

  /**
   * Initialise a new controller.
   *
   * @param port                the port to run the server socket on
   * @param replicationFactor   the replication factor
   * @param timeoutMs           the timeout in ms
   * @param rebalancePeriodSecs the rebalance period in seconds
   */
  public Controller(int port, int replicationFactor, int timeoutMs, int rebalancePeriodSecs) {
    this.port = port;
    this.replicationFactor = replicationFactor;
    this.timeoutMs = timeoutMs;
    this.rebalancePeriodSecs = rebalancePeriodSecs;

    Logger.startup();
    Logger.info("Bootstrapping on :{}, R={}, T={}ms, RP={}s", port, replicationFactor, timeoutMs,
        rebalancePeriodSecs);
    sc = new ControllerServiceContainer(this);
  }

  /**
   * Runs the controller.
   */
  private void run() {
    try (var serverSocket = new ServerSocket(port)) {
      Logger.info("Ready to accept connections!");

      for (; ; ) {
        try {
          var socket = serverSocket.accept();
          new Thread(new ControllerConnectionHandler(socket, sc),
              "CLR Connection Handler").start();
        } catch (IOException e) {
          Logger.error("Failed to accept connection: {}", e.getMessage());
        }
      }
    } catch (IOException e) {
      Logger.error("Failed to listen on port {}: {}", port, e.getMessage());
      System.exit(ExitCodes.EXIT_SOCKET_ERR);
      return;
    }
  }

  /**
   * Returns the port of the controller.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the replication factor.
   */
  public int getReplicationFactor() {
    return replicationFactor;
  }

  /**
   * Returns the rebalance period.
   */
  public int getRebalancePeriodSecs() {
    return rebalancePeriodSecs;
  }

  /**
   * Returns the timeout in milliseconds.
   */
  public int getTimeoutMs() {
    return timeoutMs;
  }
}
