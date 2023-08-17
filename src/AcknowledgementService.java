import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * A service to handle acknowledgements of transmitted messages.
 *
 * @author George Peppard
 */
public class AcknowledgementService {

  /**
   * The monitor used to wake threads upon an ACK.
   */
  private CompletableFuture<Void> ackFuture;

  /**
   * Handles an ACK message being received.
   */
  public synchronized void handleAck() {
    if (ackFuture == null) {
      Logger.warn("Ignoring ACK as nothing is waiting");
      return;
    }

    ackFuture.complete(null);
    ackFuture = null;
  }

  /**
   * Returns the Future that can be used to wait for an ACK.
   *
   * @return a future that will be completed when an ACK is received
   */
  public synchronized Future<Void> getFuture() {
    if (ackFuture != null) {
      Logger.warn("Overwriting previous future");
    }

    ackFuture = new CompletableFuture<>();
    return ackFuture;
  }
}
