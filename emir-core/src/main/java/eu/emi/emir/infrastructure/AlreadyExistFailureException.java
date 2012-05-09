/**
 * 
 */
package eu.emi.emir.infrastructure;

/**
 * @author szigeti
 *
 */
public class AlreadyExistFailureException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;
	
	public AlreadyExistFailureException() {
		super();
	}
	
	public AlreadyExistFailureException(String message) {
		super(message);
	}

}
