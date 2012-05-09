/**
 * 
 */
package eu.emi.emir.infrastructure;

/**
 * @author szigeti
 *
 */
public class NullPointerFailureException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public NullPointerFailureException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NullPointerFailureException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NullPointerFailureException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NullPointerFailureException(String message, Throwable cause) {
		super(message, cause);
	}

}
