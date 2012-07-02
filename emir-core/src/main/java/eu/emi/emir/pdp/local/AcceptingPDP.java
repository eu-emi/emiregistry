/**
 * 
 */
package eu.emi.emir.pdp.local;

import eu.emi.emir.pdp.PDPResult;
import eu.emi.emir.pdp.PDPResult.Decision;
import eu.emi.emir.pdp.RegistryPDP;
import eu.emi.emir.security.Client;
import eu.emi.emir.security.util.ResourceDescriptor;

/**
 * This PDP always grants access
 * 
 * @author a.memon
 *
 */
public class AcceptingPDP implements RegistryPDP{

	/* (non-Javadoc)
	 * @see eu.emi.emir.pdp.RegistryPDP#checkAuthorisation(eu.emi.emir.security.Client, java.lang.String, eu.emi.emir.security.util.ResourceDescriptor)
	 */
	@Override
	public PDPResult checkAuthorisation(Client c, String action,
			ResourceDescriptor d) throws Exception {
		// TODO Auto-generated method stub
		return new PDPResult(Decision.PERMIT, "This PDP always grants access");
	}

}
