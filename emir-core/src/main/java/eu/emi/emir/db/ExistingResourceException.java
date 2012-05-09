package eu.emi.emir.db;

public class ExistingResourceException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;
	
	public ExistingResourceException() {
		super();
	}
	
	public ExistingResourceException(String message) {
		super(message);
	}
	public ExistingResourceException(String message, Throwable throwable){
		super(message, throwable);
	}
	public ExistingResourceException(Throwable t){
		super(t);
	}
}
