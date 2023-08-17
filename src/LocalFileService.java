import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A service that manages locally stored files at a Dstore.
 *
 * @author George Peppard
 */
public class LocalFileService {

  /**
   * The service container.
   */
  private final DstoreServiceContainer services;

  /**
   * The list of local files.
   */
  private final List<LocalFile> files = new ArrayList<>();

  /**
   * Creates an instance of this service.
   *
   * @param services the service container
   */
  public LocalFileService(DstoreServiceContainer services) {
    this.services = services;
  }

  /**
   * Adds a new local file, saving it and adding it to the local index.
   *
   * @param name    the name of the file
   * @param size    the size of the file in bytes
   * @param content the content of the file as a byte array
   * @throws IOException if there is an error storing the file
   */
  public void addFile(String name, int size, byte[] content) throws IOException {
    var file = new LocalFile(name, size);
    files.add(file);

    var path = getPathToLocalFile(file);
    Files.write(path, content);
  }

  /**
   * Removes a locally stored file.
   *
   * @param file the file to remove
   * @throws IOException if there is an error removing the file
   */
  public void removeFile(LocalFile file) throws IOException {
    var path = getPathToLocalFile(file);
    Files.deleteIfExists(path);

    files.remove(file);
  }

  /**
   * Returns all local files.
   */
  public List<LocalFile> getLocalFiles() {
    return Collections.unmodifiableList(files);
  }

  /**
   * Returns a specific local file.
   *
   * @param name the name of the file
   * @return the requested file, or null if it does not exist
   */
  public LocalFile getLocalFileByName(String name) {
    return getLocalFiles().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst()
        .orElse(null);
  }

  /**
   * Returns the content of a file.
   *
   * @param file the file to fetch content for
   * @return the content of the file as a byte array
   * @throws IOException if there is an error fetching the file
   */
  public byte[] getFileContent(LocalFile file) throws IOException {
    var path = getPathToLocalFile(file);
    return Files.readAllBytes(path);
  }

  /**
   * Returns the qualified path to a local file.
   *
   * @param file the file to get the path for
   * @return the qualified path to the file
   */
  private Path getPathToLocalFile(LocalFile file) {
    return Paths.get(services.getDstore().getFileStorageDirectory().toString(), file.getName());
  }
}
