import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Runnable that handles socket connections for a single connected client. It is extended by
 * implementations for each client type.
 *
 * @author George Peppard
 */
public abstract class AbstractConnectionHandler implements Runnable {

  /**
   * The socket connection we are handling.
   */
  protected final Socket socket;

  /**
   * The output text stream.
   */
  protected final PrintWriter out;

  /**
   * The input text stream.
   */
  protected final BufferedReader in;

  /**
   * The raw output stream.
   */
  protected final OutputStream outRaw;

  /**
   * The raw input stream.
   */
  protected final InputStream inRaw;

  /**
   * The message handler thread. It handles messages that have been parsed, and for some handler
   * types, can be paused.
   */
  protected Thread messageHandlerThread;

  /**
   * The queue of messages that are yet to be processed. The queue blocks the processing thread
   * until one is available.
   */
  protected final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

  /**
   * Whether the processing thread should pause message processing. Note that if
   * {@link #shouldBypassQueueBlock()} returns true, the message handler will continue to handle
   * messages, regardless of the state of this variable.
   */
  protected boolean shouldStopProcessing = false;

  /**
   * A monitor used to restart processing in the message queue.
   */
  protected final Object processingRestartMonitor = new Object();

  /**
   * The number of bytes that have been requested by another component. If this is greater than 0,
   * the bytes will not be passed and instead will be returned to the requesting unit using the
   * {@link #nextBytesFuture}.
   */
  protected int nextBytesCount = 0;

  /**
   * A future used to return {@link #nextBytesCount} bytes to a requesting unit.
   */
  protected CompletableFuture<byte[]> nextBytesFuture;

  /**
   * The handler's acknowledgement service.
   */
  protected final AcknowledgementService acknowledgementService = new AcknowledgementService();

  /**
   * A lock used when sending messages.
   */
  protected final Object sendLock = new Object();

  /**
   * Initialises a new instance of the handler.
   *
   * @param socket the socket we are handling
   * @throws IOException if there is an error constructing the streams
   */
  public AbstractConnectionHandler(Socket socket) throws IOException {
    this.socket = socket;

    this.outRaw = this.socket.getOutputStream();
    this.inRaw = this.socket.getInputStream();
    this.out = new PrintWriter(this.outRaw);
    this.in = new BufferedReader(new InputStreamReader(this.inRaw));
  }

  /**
   * The main handler method. It spins up a message handler thread, and then enters a loop that
   * receives messages from the socket, parses them and forwards them to the handler thread for
   * execution.
   */
  @Override
  public void run() {
    Logger.info(log("(connected)"));
    hookConnected();

    messageHandlerThread = new Thread(() -> {
      try {
        Logger.info(log("Message handler thread started"));
        messageHandlerThread();
      } catch (InterruptedException e) {
        Logger.error(log("Message handler thread interrupted"));
      }
    }, "ACH Message Processor");
    messageHandlerThread.start();

    try {
      int firstByte;
      String message;
      while ((firstByte = inRaw.read()) != -1) {
        if (nextBytesCount > 0) {
          nextBytesFuture.complete(
              ByteBuffer.allocate(nextBytesCount)
                  .put((byte) firstByte)
                  .put(inRaw.readNBytes(nextBytesCount - 1))
                  .array()
          );

          nextBytesCount = 0;
          nextBytesFuture = null;

          continue;
        }

        message = ((char) firstByte) + in.readLine();
        Logger.info(log("> in > {}"), message);

        var parts = message.split(" ");
        Message parsedMessage = null;
        try {
          parsedMessage = parseMessage(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
        } catch (UnsupportedOperationException e) {
          Logger.warn(log("no handler for message type, will do nothing: {}"), e.getMessage());
        } catch (Exception e) {
          Logger.warn(log("failed to parse message, perhaps it was malformed? {}"), e.getMessage());
        }

        if (parsedMessage != null) {
          socket.setSoTimeout(0);
          messageQueue.add(parsedMessage);
        }
      }
    } catch (SocketTimeoutException e) {
      Logger.warn(log("read timeout exception"));
    } catch (Exception e) {
      if (!socket.isClosed()) { // if we closed the socket, we expect this
        Logger.error(log("caught exception {}: {}\n"), e.getClass().getSimpleName(),
            e.getMessage());
      }
    } finally {
      Logger.info(log("(disconnecting)"));
      hookDisconnected();
      try {
        socket.close();
      } catch (Exception ignored) {
      }
    }

    messageQueue.add(new StopProcessingMessage());

    if (!socket.isClosed()) {
      Logger.warn("socket remains open despite best effort");
    }
  }

  /**
   * Returns whether this handler should allow a queue hold.
   */
  protected boolean shouldBypassQueueBlock() {
    return false;
  }

  /**
   * Requests that the message handler stops processing messages.
   */
  public void stopProcessing() {
    this.shouldStopProcessing = true;
  }

  /**
   * Requests that the message handler starts processing messages.
   */
  public void startProcessing() {
    synchronized (processingRestartMonitor) {
      this.shouldStopProcessing = false;
      processingRestartMonitor.notify();
    }
  }

  /**
   * Requests the next n bytes of data from the input stream. These will be returned with a
   * {@link Future} and will not be parsed or handled further by the standard communication
   * services.
   *
   * @param n the number of bytes to get
   * @return the future which will contain the requested bytes
   */
  public Future<byte[]> getNextNBytes(int n) {
    if (nextBytesCount > 0) {
      return null;
    }

    nextBytesFuture = new CompletableFuture<>();
    nextBytesCount = n;

    return nextBytesFuture;
  }

  /**
   * The thread method for the message handling logic.
   *
   * @throws InterruptedException if the thread is interrupted
   */
  protected void messageHandlerThread() throws InterruptedException {
    while (true) {
      var message = messageQueue.take(); // blocks until available

      if (shouldStopProcessing) {
        if (shouldBypassQueueBlock()) {
          Logger.info("Queue pause requested, but this type of handler does not listen to them");
        } else {
          Logger.info("Message handler will wait");

          synchronized (processingRestartMonitor) {
            processingRestartMonitor.wait();
          }

          Logger.info("Restart processing messages");
        }
      }

      if (message instanceof StopProcessingMessage) {
        Logger.info("Message handler thread stopped gracefully");
        return;
      }

      Logger.info("Now processing {}", message.getClass().getSimpleName());

      try {
        handleMessage(message);
      } catch (Exception e) {
        Logger.error("exception while handling message: {}", e.getMessage());
      }
    }
  }

  /**
   * Returns the message handler factory for this handler.
   */
  protected abstract IMessageHandlerFactory getHandlerFactory();

  /**
   * The hook that will be called when the handler is connected. This is empty by default, but can
   * be overridden by an implementation to perform some logic when the state transitions.
   */
  protected void hookConnected() {
    // implementations should override this method if they require it
  }

  /**
   * The hook that will be called when the handler is disconnected. This is empty by default, but
   * can be overridden by an implementation to perform some logic when the state transitions.
   */
  protected void hookDisconnected() {
    // implementations should override this method if they require it
  }

  /**
   * Parses a message from the raw text content. Implementations must override this method to parse
   * the messages they can handle.
   *
   * @param type the type of message (i.e. "JOIN")
   * @param args the arguments of the message (i.e. [8001])
   * @return the parsed message
   * @throws UnsupportedOperationException if the message cannot be parsed
   */
  protected Message parseMessage(String type, String[] args) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Finds the handler for a message and processes it.
   *
   * @param message the message to process
   */
  protected void handleMessage(Message message) {
    var handler = getHandlerFactory().create(message);
    handler.handle();
  }

  /**
   * Sends a message.
   *
   * @param message the message to send
   */
  public void send(Message message) {
    send(message, 0);
  }

  /**
   * Sends a message and sets a timeout on the socket.
   *
   * @param message the message to send
   * @param timeout the timeout to set
   */
  public void send(Message message, int timeout) {
    synchronized (sendLock) {
      Logger.info(log("< out < {}"), message);

      try {
        socket.setSoTimeout(timeout);
        out.println(message.toString());
        out.flush();
      } catch (Exception e) {
        Logger.error(log("caught exception while sending message: {}"), e.getMessage());
      }
    }
  }

  /**
   * Sends some bytes as raw data across the socket.
   *
   * @param bytes the byte array to send
   */
  public void sendBytes(byte[] bytes) {
    synchronized (sendLock) {
      Logger.info(log("< out < {} bytes"), bytes.length);

      try {
        outRaw.write(bytes);
      } catch (IOException e) {
        Logger.error(log("caught exception while sending message: {}"), e.getMessage());
      }
    }
  }

  /**
   * Returns a formatted log message containing information about this connection.
   *
   * @param string the message that is being logged
   */
  protected String log(String string) {
    return socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " - " + string;
  }

  /**
   * Closes the connection.
   */
  public void close() throws IOException {
    Logger.info(log("closing connection upon request"));
    socket.close();
  }

  /**
   * Returns this handler's acknowledgement service.
   */
  public AcknowledgementService getAcknowledgementService() {
    return acknowledgementService;
  }
}
