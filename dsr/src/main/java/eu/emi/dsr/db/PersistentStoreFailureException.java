package eu.emi.dsr.db;

public class PersistentStoreFailureException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;

	public PersistentStoreFailureException() {
		super();
	}

	public PersistentStoreFailureException(String message) {
		super(message);
	}

}
