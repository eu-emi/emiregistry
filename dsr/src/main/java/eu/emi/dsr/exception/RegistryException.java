/**
 * 
 */
package eu.emi.dsr.exception;

/**
 * Root exception class
 * @author a.memon
 *
 */
public class RegistryException extends Exception{
	private static final long serialVersionUID=1l;
	/**
	 * 
	 */
	public RegistryException() {
		super();
	}
	public RegistryException(String message) {
		super(message);
	}
	public RegistryException(Throwable cause) {
		super(cause);
	}
	public RegistryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
