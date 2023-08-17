/**
 * A listener for when the queue hold ends.
 *
 * @author George Peppard
 */
public interface QueueHoldEndEventListener {

  /**
   * Handles the queue hold being removed.
   */
  void onQueueClosed();
}
