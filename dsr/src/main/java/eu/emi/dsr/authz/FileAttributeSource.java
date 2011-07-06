/**
 * 
 */
package eu.emi.dsr.authz;

import java.security.Principal;
import java.util.Map;

/**
 * Fetching the attributes from file
 * 
 * @author a.memon
 *
 */
public class FileAttributeSource implements AttributeSource{

	/* (non-Javadoc)
	 * @see eu.emi.dsr.authz.AttributeSource#getAttributes(java.security.Principal)
	 */
	public Map<String, Object> getAttributes(Principal principal) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.authz.AttributeSource#getRole()
	 */
	public String getRole() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.authz.AttributeSource#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return "FILE";
	}

}
