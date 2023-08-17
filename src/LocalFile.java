import java.util.Objects;

/**
 * A locally stored file to a Dstore.
 *
 * @author George Peppard
 */
public class LocalFile {

  /**
   * The name of the file.
   */
  private final String name;

  /**
   * The file size in bytes.
   */
  private final int size;

  /**
   * Creates a new file.
   * @param name the name of the file
   * @param size the size of the file in bytes
   */
  public LocalFile(String name, int size) {
    this.name = name;
    this.size = size;
  }

  /**
   * Returns the name of the file.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the size of the file.
   */
  public int getSize() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalFile localFile = (LocalFile) o;
    return Objects.equals(name, localFile.name);
  }
}
