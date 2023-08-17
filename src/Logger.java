import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The level of a log message.
 *
 * @author George Peppard
 */
enum LogLevel {
  /**
   * An error or fatal error.
   */
  ERROR,

  /**
   * A warning.
   */
  WARN,

  /**
   * An informational message.
   */
  INFO
}

/**
 * Utility class that manages logging messages to stdout or stderr.
 *
 * @author George Peppard
 */
public class Logger {

  /**
   * The formatter used to log times.
   */
  private static final DateTimeFormatter LOG_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "uuuu-MM-dd HH:mm:ss");

  /**
   * Logs information about the program and environment at startup.
   */
  public static void startup() {
    info("COMP2207 Distributed File Storage System");
    info("(c) George Peppard and University of Southampton");
    info("JRE: {} Java Runtime Environment, v{}", System.getProperty("java.vendor"),
        System.getProperty("java.version"));
    info("JVM: {} {}, v{}", System.getProperty("java.vm.vendor"),
        System.getProperty("java.vm.name"), System.getProperty("java.vm.version"));
    info("ENV: {} ({}), version {}", System.getProperty("os.name"), System.getProperty("os.arch"),
        System.getProperty("os.version"));
  }

  /**
   * Sends a log message.
   *
   * @param level   the log level
   * @param message the message to log
   * @param args    any arguments to place in the message
   */
  private static void log(LogLevel level, String message, Object... args) {
    var finalStr = message;

    for (Object arg : args) {
      if (arg == null) {
        finalStr = finalStr.replaceFirst("\\{}", "(null)");
      } else {
        finalStr = finalStr.replaceFirst("\\{}", arg.toString());
      }
    }

    var caller = Thread.currentThread().getStackTrace()[3];
    var output = String.format("[%s %s] %s - %s - %s",
        LocalDateTime.now().format(LOG_DATE_TIME_FORMATTER), level,
        Thread.currentThread().getName(),
        caller.getClassName() + "." + caller.getMethodName(), finalStr);

    if (level == LogLevel.ERROR) {
      System.err.println(output);
    } else {
      System.out.println(output);
    }
  }

  /**
   * Logs an info message.
   *
   * @param message the message to log
   * @param args    arguments for the message
   */
  public static void info(String message, Object... args) {
    log(LogLevel.INFO, message, args);
  }

  /**
   * Logs a warning message.
   *
   * @param message the message to log
   * @param args    arguments for the message
   */
  public static void warn(String message, Object... args) {
    log(LogLevel.WARN, message, args);
  }

  /**
   * Logs an error message.
   *
   * @param message the message to log
   * @param args    arguments for the message
   */
  public static void error(String message, Object... args) {
    log(LogLevel.ERROR, message, args);
  }
}
