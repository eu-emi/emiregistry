/**
 * 
 */
package eu.emi.emir.validator;


/**
 * @author a.memon
 *
 */
public class InvalidServiceDescriptionException extends Exception{
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public InvalidServiceDescriptionException() {
		super();
	}
	public InvalidServiceDescriptionException(String message) {
		super(message);
	}
	public InvalidServiceDescriptionException(Throwable cause) {
		super(cause);
	}
	public InvalidServiceDescriptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
