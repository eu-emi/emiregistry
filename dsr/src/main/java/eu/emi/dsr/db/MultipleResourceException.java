package eu.emi.dsr.db;

public class MultipleResourceException extends Exception {

	/**
	 *  The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = -8742693398185127303L;

	public MultipleResourceException() {
		super();
	}

	public MultipleResourceException(String message) {
		super(message);
	}

}
