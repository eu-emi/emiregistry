/**
 * 
 */
package eu.emi.emir.exception;

/**
 * Thrown when service with a given url is not found in the registry
 * @author a.memon
 *
 */
public class UnknownServiceException extends RegistryException{
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public UnknownServiceException() {
		super();
	}
	public UnknownServiceException(String message) {
		super(message);
	}
	public UnknownServiceException(Throwable cause) {
		super(cause);
	}
	public UnknownServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
