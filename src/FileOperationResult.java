import java.lang.reflect.InvocationTargetException;

/**
 * The result of an operation on a file.
 *
 * @author George Peppard
 */
public enum FileOperationResult {
  /**
   * A successful operation.
   */
  SUCCESS,

  /**
   * An operation that failed due to the file already existing.
   */
  FILE_ALREADY_EXISTS(ErrorFileAlreadyExistsMessage.class),

  /**
   * An operation that failed due to the file already being removed.
   */
  FILE_ALREADY_REMOVING(ErrorFileAlreadyExistsMessage.class),

  /**
   * An operation that failed due to the file not existing.
   */
  FILE_NOT_EXISTS(ErrorFileDoesNotExistMessage.class);

  /**
   * The message that should be sent if this error occurred, or null.
   */
  private final Class<? extends Message> clazz;

  /**
   * Initialises a successful result.
   */
  FileOperationResult() {
    this(null);
  }

  /**
   * Initialises a failed result.
   *
   * @param clazz the message type that should be sent if the error occurred
   */
  FileOperationResult(Class<? extends Message> clazz) {
    this.clazz = clazz;
  }

  /**
   * Returns whether the operation was a success.
   */
  public boolean isSuccess() {
    return this == SUCCESS;
  }

  /**
   * Returns whether the operation was unsuccessful.
   */
  public boolean isFailure() {
    return !isSuccess();
  }

  /**
   * Instantiates a message for this failure.
   *
   * @return the message
   */
  public Message getMessage() {
    if (this.clazz == null) {
      throw new UnsupportedOperationException("cannot convert non-message result to a message");
    }

    try {
      return this.clazz.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
