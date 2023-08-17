/**
 * A handler for the LIST message.
 *
 * @author George Peppard
 */
public class ControllerListMessageHandler extends ControllerMessageHandler<ListMessage> {

  public ControllerListMessageHandler(ListMessage message, ControllerServiceContainer services,
      ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Returns the available files that we have in the index.
   */
  @Override
  public void handle() {
    var files = services.getIndexService().getFiles().stream().map(IndexedFile::getName).toArray(String[]::new);
    var resp = new ListResponseMessage(files);

    handler.send(resp);
  }
}
