package eu.emi.dsr.core.db;

public class NonExistingResourceException extends Exception {

	/**
	 *  The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 515931301821963410L;

	public NonExistingResourceException() {
		super();
	}

	public NonExistingResourceException(String message) {
		super(message);
	}

}
