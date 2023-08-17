/**
 * A handler for a list response message from a Dstore.
 *
 * @author George Peppard
 */
public class ControllerListResponseMessageHandler extends ControllerMessageHandler<ListResponseMessage> {

  public ControllerListResponseMessageHandler(ListResponseMessage message,
      ControllerServiceContainer services, ControllerConnectionHandler handler) {
    super(message, services, handler);
  }

  /**
   * Sends the file list to the {@link IndexService} so it can be reconciled with the index.
   */
  @Override
  public void handle() {
    // ListResponseMessages are only parsed for Dstores, so isDstore() == true
    var dstore = services.getDstoreService().getDstore(handler.getDstorePort());
    services.getIndexService().handleUpdatedFileList(dstore, message.getFiles());
  }
}
