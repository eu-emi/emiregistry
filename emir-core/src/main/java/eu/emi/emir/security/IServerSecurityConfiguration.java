/**
 * 
 */
package eu.emi.emir.security;

import eu.emi.emir.pdp.RegistryPDP;
import eu.unicore.security.canl.IAuthnAndTrustConfiguration;


/**
 * @author a.memon
 *
 */
public interface IServerSecurityConfiguration extends IAuthnAndTrustConfiguration{
	/**
	 * Returns true if SSL mode is enabled.
	 */
	public boolean isSslEnabled();
	
	/**
	 * @return do we check access on the web service level (using PDP)? 
	 */
	public boolean isXACMLAccessControlEnabled();
	
	/**
	 * @return do we check access on the web service level (using plain ACL file)? 
	 */
	public boolean isACLAccessControlEnabled();

	/**
	 * @return do we check access on the web service level (using PDP) for a particular service? 
	 */
	public boolean isAccessControlEnabled(String service);
	
	/**
	 * @return the configured PDP instance, null can be returned only if {@link #isXACMLAccessControlEnabled()}
	 * returns false
	 */
	public RegistryPDP getPdp();

	/**
	 * @return the PDP configuration file path
	 */
	public String getPdpConfigurationFile();
	
	/**
	 * @return the ACL configuration file path
	 */
	public String getACLConfigurationFile();
	
	/**
	 * @return the configured attribute source instance
	 */
	public IAttributeSource getAip();
}
