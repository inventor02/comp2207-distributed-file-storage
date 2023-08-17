import java.io.File;
import java.io.IOException;

public class RvsTestOneSim {

  private static final Client client = new Client(12345, 1000, Logger.LoggingType.ON_FILE_AND_TERMINAL);

  public static void main(String[] args) throws IOException {
    client.connect();
    client.store(new File("test1.txt"));
  }
}