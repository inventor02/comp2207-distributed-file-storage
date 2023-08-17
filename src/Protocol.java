/**
 * The message tokens that are used by the application.
 *
 * @author Leonardo Aniello
 * @author George Peppard
 */
public class Protocol {
	
	// Mainly Client
	public static final String STORE = "STORE";
	public static final String LOAD = "LOAD";
	public static final String LOAD_DATA = "LOAD_DATA";
	public static final String RELOAD = "RELOAD";
	
	// Mainly Controller
	public static final String LIST = "LIST";
	public static final String STORE_TO = "STORE_TO";
	public static final String STORE_COMPLETE = "STORE_COMPLETE";
	public static final String LOAD_FROM = "LOAD_FROM";
	public static final String REMOVE_COMPLETE = "REMOVE_COMPLETE";
	public static final String REBALANCE = "REBALANCE";
	public static final String ERROR_FILE_DOES_NOT_EXIST = "ERROR_FILE_DOES_NOT_EXIST";
	public static final String ERROR_FILE_ALREADY_EXISTS = "ERROR_FILE_ALREADY_EXISTS";
	public static final String ERROR_NOT_ENOUGH_DSTORES = "ERROR_NOT_ENOUGH_DSTORES";
	public static final String ERROR_LOAD = "ERROR_LOAD";
	public static final String REMOVE = "REMOVE";
	
	// Mainly Dstore
	public static final String ACK = "ACK";
	public static final String STORE_ACK = "STORE_ACK";
	public static final String REMOVE_ACK = "REMOVE_ACK";
	public static final String JOIN = "JOIN";
	public static final String REBALANCE_STORE = "REBALANCE_STORE";
	public static final String REBALANCE_COMPLETE = "REBALANCE_COMPLETE";
}
