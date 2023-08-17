import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The main Dstore class.
 *
 * @author George Peppard
 */
public class Dstore {

  /**
   * The main method. It checks arguments and then instantiates an instance of the class.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 4) {
      System.err.println(
          "Usage: Dstore [port] [controller port] [timeout ms] [file storage directory]");
      System.exit(ExitCodes.EXIT_INVALID_ARG_COUNT);
      return;
    }

    int port, controllerPort, timeoutMs;
    String fileStorageDirectory;

    try {
      port = Integer.parseInt(args[0]);
      controllerPort = Integer.parseInt(args[1]);
      timeoutMs = Integer.parseInt(args[2]);
      fileStorageDirectory = args[3];
    } catch (NumberFormatException e) {
      System.err.println(
          "Usage: Dstore [port] [controller port] [timeout ms] [file storage directory]");
      System.exit(ExitCodes.EXIT_INVALID_ARG_COUNT);
      return;
    }

    var dstore = new Dstore(port, controllerPort, timeoutMs, fileStorageDirectory);
    dstore.run();
  }

  // ---------------------------------------------------------------------------------------

  /**
   * The port to listen on.
   */
  private final int port;

  /**
   * The port the controller is running on.
   */
  private final int controllerPort;

  /**
   * The timeout in milliseconds.
   */
  private final int timeoutMs;

  /**
   * The directory to store files in.
   */
  private final Path fileStorageDirectory;

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * The connection handler to communicate with the controller.
   */
  private DstoreControllerConnectionHandler controllerHandler;

  /**
   * Initialises a new instance of the Dstore.
   *
   * @param port                 the port to listen on
   * @param controllerPort       the port to connect to the controller on
   * @param timeoutMs            the timeout in ms
   * @param fileStorageDirectory the directory to store files in
   */
  public Dstore(int port, int controllerPort, int timeoutMs, String fileStorageDirectory) {
    this.port = port;
    this.controllerPort = controllerPort;
    this.timeoutMs = timeoutMs;
    this.fileStorageDirectory = Path.of(fileStorageDirectory);

    Logger.startup();
    Logger.info("Bootstrapping on :{}, controller is at :{}, T={}ms, dir={}", port, controllerPort,
        timeoutMs, fileStorageDirectory);

    try {
      initFileStorageDir();
    } catch (IOException | IllegalArgumentException e) {
      Logger.error("Cannot initialise file storage directory: {}", e.getMessage());
      Logger.error("Cannot continue, exiting");
      System.exit(ExitCodes.EXIT_FILE_ERR);
    }

    this.services = new DstoreServiceContainer(this);
  }

  /**
   * Runs the Dstore. This connects to the controller, and then runs our own socket for clients to
   * connect to. If the Dstore cannot connect to the controller, the execution fails.
   */
  public void run() {
    // Connect to the controller's ServerSocket
    try {
      var controllerSock = new Socket(WellKnownHosts.LOCALHOST, controllerPort);
      Logger.info("Opened connection to controller on port {}", controllerPort);
      controllerHandler = new DstoreControllerConnectionHandler(controllerSock, services);
      new Thread(controllerHandler, "DS CLR Connection Handler").start();
    } catch (IOException e) {
      Logger.error("Failed to connect to controller; is it running? {}", e.getMessage());
      Logger.error("Cannot continue, exiting");
      System.exit(ExitCodes.EXIT_SOCKET_ERR);
    }

    // Run our own ServerSocket for clients to connect to
    try (var serverSocket = new ServerSocket(port)) {
      Logger.info("Listening for connections on :{}", port);

      for (; ; ) {
        try {
          var socket = serverSocket.accept();
          new Thread(new DstoreConnectionHandler(socket, services), "DS Connection Handler").start();
        } catch (IOException e) {
          Logger.error("Failed to accept connection: {}", e.getMessage());
        }
      }
    } catch (IOException e) {
      Logger.error("Failed to listen on port {}: {}", port, e.getMessage());
      System.exit(ExitCodes.EXIT_SOCKET_ERR);
    }
  }

  /**
   * Creates and clears the file storage directory.
   *
   * @throws IOException              if an IO exception occurred
   * @throws IllegalArgumentException if the path is not a directory
   */
  private void initFileStorageDir() throws IOException, IllegalArgumentException {
    if (!Files.exists(fileStorageDirectory)) {
      Logger.info("File storage directory does not exist, creating");
      Files.createDirectories(fileStorageDirectory);
    }

    if (!Files.isDirectory(fileStorageDirectory)) {
      throw new IllegalArgumentException("File storage directory is not a directory!");
    }

    Logger.info("Clearing storage directory");
    Files.list(fileStorageDirectory).forEach(f -> {
      try {
        Files.delete(f);
      } catch (IOException e) {
        Logger.error("Cannot delete file! {}", f.getFileName());
      }
    });
  }

  /**
   * Returns the port for the server socket.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the controller port.
   */
  public int getControllerPort() {
    return controllerPort;
  }

  /**
   * Returns the timeout.
   */
  public int getTimeoutMs() {
    return timeoutMs;
  }

  /**
   * Returns the file storage directory.
   */
  public Path getFileStorageDirectory() {
    return fileStorageDirectory;
  }

  /**
   * Returns the controller connection handler.
   */
  public DstoreControllerConnectionHandler getControllerHandler() {
    return controllerHandler;
  }
}
