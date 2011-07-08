/**
 * 
 */
package eu.emi.dsr.infrastructure;

/**
 * @author szigeti
 *
 */
public class EmptyIdentifierFailureException extends Exception {

	/**
	 * he serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public EmptyIdentifierFailureException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public EmptyIdentifierFailureException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EmptyIdentifierFailureException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EmptyIdentifierFailureException(String message, Throwable cause) {
		super(message, cause);
	}

}
