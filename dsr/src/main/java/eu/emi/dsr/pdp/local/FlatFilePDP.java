/**
 * 
 */
package eu.emi.dsr.pdp.local;

import eu.emi.dsr.pdp.PDPResult;
import eu.emi.dsr.pdp.RegistryPDP;
import eu.emi.dsr.security.Client;
import eu.emi.dsr.security.util.ResourceDescriptor;

/**
 * @author a.memon
 *
 */
public class FlatFilePDP implements RegistryPDP{

	/* (non-Javadoc)
	 * @see eu.emi.dsr.pdp.RegistryPDP#checkAuthorisation(eu.emi.dsr.security.Client, java.lang.String, eu.emi.dsr.security.util.ResourceDescriptor)
	 */
	@Override
	public PDPResult checkAuthorisation(Client c, String action,
			ResourceDescriptor d) throws Exception {
		if (c.getRole().getName().equalsIgnoreCase("admin") || c.getRole().getName().equalsIgnoreCase("serviceowner")) {
			return new PDPResult(PDPResult.Decision.PERMIT, "");
		}
		
		return new PDPResult(PDPResult.Decision.DENY, "");
	}

}
