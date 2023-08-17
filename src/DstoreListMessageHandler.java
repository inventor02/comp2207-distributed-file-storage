/**
 * A handler for the LIST message.
 *
 * @author George Peppard
 */
public class DstoreListMessageHandler extends DstoreControllerMessageHandler<ListMessage> {

  public DstoreListMessageHandler(ListMessage message, DstoreServiceContainer services,
      DstoreControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Returns the list of files that are stored locally at this Dstore.
   */
  @Override
  public void handle() {
    var files = services.getLocalFileService().getLocalFiles().stream().map(LocalFile::getName)
        .toArray(String[]::new);

    handler.send(new ListResponseMessage(files));
  }
}
