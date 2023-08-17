/**
 * A listener for when the queue is held.
 *
 * @author George Peppard
 */
public interface QueueHoldEventListener {

  /**
   * Handles the queue being held.
   */
  void onQueueOpened();
}
