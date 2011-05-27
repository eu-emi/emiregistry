package eu.emi.dsr.core.db;

public class ExistingResourceException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = -2026577295929610025L;
	
	public ExistingResourceException() {
		super();
	}
	
	public ExistingResourceException(String message) {
		super(message);
	}
}
