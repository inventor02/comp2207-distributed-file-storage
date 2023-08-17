import java.util.ArrayList;
import java.util.List;

/**
 * A service that orchestrates and manages operations that block other operations.
 *
 * @author George Peppard
 */
public class BlockingOperationsService {

  /**
   * The service container.
   */
  private final ControllerServiceContainer services;

  /**
   * The number of current store operations that are ongoing.
   */
  private int currentStoreOperations = 0;

  /**
   * The number of current remove operations that are ongoing.
   */
  private int currentRemoveOperations = 0;

  /**
   * Whether a rebalance operation is ongoing.
   */
  private boolean isRebalancing = false;

  /**
   * Whether compatible handlers should queue messages instead of processing them straight away.
   */
  private boolean shouldQueueMessages = false;

  /**
   * The listeners that are registered to be notified about message queue holds.
   */
  private final List<QueueHoldEventListener> queueHoldStartListeners = new ArrayList<>();

  /**
   * The listeners that are registered to be notified when message queue holds are removed.
   */
  private final List<QueueHoldEndEventListener> queueHoldEndListeners = new ArrayList<>();

  /**
   * Initialises a new instance of the service.
   *
   * @param services the service container
   */
  public BlockingOperationsService(ControllerServiceContainer services) {
    this.services = services;
  }

  /**
   * Record a store operation as having started. This will block rebalance operations.
   */
  public synchronized void startStore() {
    currentStoreOperations++;
    Logger.info("Store operation started, there are now {} running", currentStoreOperations);
  }

  /**
   * Record a store operation as having finished.
   */
  public synchronized void finishStore() {
    currentStoreOperations--;
    this.notify();
    Logger.info("Store operation finished, there are now {} running", currentStoreOperations);
  }

  /**
   * Record a remove operation as having started. This will block rebalance operations.
   */
  public synchronized void startRemove() {
    currentRemoveOperations++;
    Logger.info("Remove operation started, there are now {} running", currentRemoveOperations);
  }

  /**
   * Record a remove operation as having finished.
   */
  public synchronized void finishRemove() {
    currentRemoveOperations--;
    this.notify();
    Logger.info("Remove operation finished, there are now {} running", currentRemoveOperations);
  }

  /**
   * Record a rebalance operation as having started. This method will block until store and remove
   * messages are all finished, and will start a queue hold so no further operations start.
   *
   * @throws AlreadyRebalancingException if there is already an ongoing rebalance operation
   * @throws InterruptedException        if the wait is interrupted
   */
  public synchronized void startRebalance()
      throws AlreadyRebalancingException, InterruptedException {
    if (isRebalancing()) {
      throw new AlreadyRebalancingException();
    }

    startQueueHold();
    while (currentStoreOperations > 0 || currentRemoveOperations > 0) {
      Logger.info("Waiting for store or remove operations to finish before rebalancing");
      this.wait();
    }

    isRebalancing = true;
  }

  /**
   * Record a rebalance operation as having finished.
   */
  public synchronized void finishRebalance() {
    endQueueHold();
    isRebalancing = false;
  }

  /**
   * Returns whether there is currently a rebalance operation happening.
   */
  public synchronized boolean isRebalancing() {
    return isRebalancing;
  }

  /**
   * Requests a queue hold from compatible handlers.
   */
  public synchronized void startQueueHold() {
    Logger.info("Queue hold started");
    shouldQueueMessages = true;
    for (QueueHoldEventListener listener : queueHoldStartListeners) {
      listener.onQueueOpened();
    }
  }

  /**
   * Ends the running queue hold.
   */
  private synchronized void endQueueHold() {
    Logger.info("Queue hold ended");
    shouldQueueMessages = false;
    for (QueueHoldEndEventListener listener : queueHoldEndListeners) {
      listener.onQueueClosed();
    }
  }

  /**
   * Registers some queue hold listeners.
   *
   * @param holdListener    the listener to be notified when a queue hold is started
   * @param holdEndListener the listener to be notified when a queue hold ends
   */
  public void registerListeners(QueueHoldEventListener holdListener,
      QueueHoldEndEventListener holdEndListener) {
    if (holdListener != null) {
      queueHoldStartListeners.add(holdListener);
    }

    if (holdEndListener != null) {
      queueHoldEndListeners.add(holdEndListener);
    }
  }

  /**
   * Deregisters a queue hold listener.
   *
   * @param listener the listener to deregister
   */
  public void deregisterListener(QueueHoldEventListener listener) {
    queueHoldStartListeners.remove(listener);
  }

  /**
   * Deregisters a queue hold end listener.
   *
   * @param listener the listener to deregister
   */
  public void deregisterListener(QueueHoldEndEventListener listener) {
    queueHoldEndListeners.remove(listener);
  }

  /**
   * Returns whether there is a queue hold at the moment.
   */
  public synchronized boolean shouldQueueMessages() {
    return shouldQueueMessages;
  }
}
