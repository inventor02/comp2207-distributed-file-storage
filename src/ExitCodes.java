/**
 * Commonly used exit codes for any invokable class.
 *
 * @author George Peppard
 */
public class ExitCodes {

  /**
   * The code returned when the argument count was invalid.
   */
  public static final int EXIT_INVALID_ARG_COUNT = 1;

  /**
   * The code returned when the arguments were not valid.
   */
  public static final int EXIT_INVALID_ARGS = 2;

  /**
   * The code returned when there is an error in some socket-based communications.
   */
  public static final int EXIT_SOCKET_ERR = 3;

  /**
   * The code returned due to a filesystem-related error.
   */
  public static final int EXIT_FILE_ERR = 4;
}
