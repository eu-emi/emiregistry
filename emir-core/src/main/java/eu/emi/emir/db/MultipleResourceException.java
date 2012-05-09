package eu.emi.emir.db;

public class MultipleResourceException extends Exception {

	/**
	 *  The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;

	public MultipleResourceException() {
		super();
	}

	public MultipleResourceException(String message) {
		super(message);
	}

}
