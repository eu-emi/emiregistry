/**
 * 
 */
package eu.emi.dsr.pdp;

import eu.emi.dsr.security.Client;
import eu.emi.dsr.security.util.ResourceDescriptor;

/**
 * @author a.memon
 *
 */
public interface RegistryPDP {
	public PDPResult checkAuthorisation(Client c, String action, ResourceDescriptor d) 
	throws Exception;
}
