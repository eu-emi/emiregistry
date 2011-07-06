package eu.emi.dsr.db;

public class PersistentStoreFailureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 407747781736391198L;

	/**
	 * The serial version UID expected at serializable classes
	 */

	public PersistentStoreFailureException() {
		super();
	}

	public PersistentStoreFailureException(String message) {
		super(message);
	}

	public PersistentStoreFailureException(Throwable t) {
		super(t);
	}
}
