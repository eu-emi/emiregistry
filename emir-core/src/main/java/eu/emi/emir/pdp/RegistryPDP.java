/**
 * 
 */
package eu.emi.emir.pdp;

import eu.emi.emir.security.Client;
import eu.emi.emir.security.util.ResourceDescriptor;

/**
 * @author a.memon
 *
 */
public interface RegistryPDP {
	public PDPResult checkAuthorisation(Client c, String action, ResourceDescriptor d) 
	throws Exception;
}
