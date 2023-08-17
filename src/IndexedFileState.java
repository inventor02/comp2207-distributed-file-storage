/**
 * The state of an indexed file.
 */
public enum IndexedFileState {
  /**
   * Files where the store operation is in progress.
   */
  STORE_IN_PROGRESS,

  /**
   * Files where the store operation is complete and that can be fetched. This is also known in some
   * places as STORE_COMPLETE.
   */
  AVAILABLE,

  /**
   * Files where a remove operation is in progress.
   */
  REMOVE_IN_PROGRESS,

  /**
   * Files that are no longer stored anywhere we know of. This is also known in some places as
   * REMOVE_COMPLETE.
   */
  GONE
};
