/**
 * 
 */
package eu.emi.dsr.authz;

import java.security.Principal;
import java.util.Map;

/**
 * @author a.memon
 *
 */
public interface AttributeSource {
	public Map<String, Object> getAttributes(Principal principal);
	public String getRole();
	public String getName();
}
