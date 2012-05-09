package eu.emi.emir.db;

public class NonExistingResourceException extends Exception {

	/**
	 *  The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;

	public NonExistingResourceException() {
		super();
	}

	public NonExistingResourceException(String message) {
		super(message);
	}
	public NonExistingResourceException(Throwable e) {
		super(e);
	}

}
