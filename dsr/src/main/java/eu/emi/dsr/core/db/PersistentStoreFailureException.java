package eu.emi.dsr.core.db;

public class PersistentStoreFailureException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 3528359506541883295L;

	public PersistentStoreFailureException() {
		super();
	}

	public PersistentStoreFailureException(String message) {
		super(message);
	}

}
