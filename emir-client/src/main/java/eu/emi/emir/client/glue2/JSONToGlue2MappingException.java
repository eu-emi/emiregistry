/**
 * 
 */
package eu.emi.emir.client.glue2;

/**
 * @author a.memon
 * 
 */
public class JSONToGlue2MappingException extends Exception {

	private static final long serialVersionUID = -3155320259236068720L;

	/**
	 * 
	 */
	public JSONToGlue2MappingException() {

	}

	public JSONToGlue2MappingException(String message) {
		super(message);
	}

	public JSONToGlue2MappingException(String message, Throwable t) {
		super(message, t);
	}

	public JSONToGlue2MappingException(Throwable t) {
		super(t);
	}
}
