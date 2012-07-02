/**
 * 
 */
package eu.emi.emir.security;

import eu.emi.emir.pdp.RegistryPDP;
import eu.unicore.security.canl.DefaultAuthnAndTrustConfiguration;

/**
 * @author a.memon
 *
 */
public class DefaultServerSecurityConfiguration extends DefaultAuthnAndTrustConfiguration implements IServerSecurityConfiguration{
	
	private boolean sslEnabled;
	private boolean xacmlAccessControlEnabled;
	private boolean aclAccessControlEnabled;
	private RegistryPDP pdp;
	private IAttributeSource aip;
	private String pdpConfigurationFile;
	private String aclConfigurationFile;
	
	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#isSslEnabled()
	 */
	@Override
	public boolean isSslEnabled() {
		// TODO Auto-generated method stub
		return sslEnabled;
	}
	
	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}
	
	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#isAccessControlEnabled()
	 */
	@Override
	public boolean isXACMLAccessControlEnabled() {
		// TODO Auto-generated method stub
		return xacmlAccessControlEnabled;
	}
	
	public void setXACMLAccessControlEnabled(boolean xacmlAccessControlEnabled) {
		this.xacmlAccessControlEnabled = xacmlAccessControlEnabled;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#isAccessControlEnabled(java.lang.String)
	 */
	@Override
	public boolean isAccessControlEnabled(String service) {
		return isXACMLAccessControlEnabled();
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#getPdp()
	 */
	@Override
	public RegistryPDP getPdp() {
		return pdp;
	}
	
	public void setPdp(RegistryPDP pdp) {
		this.pdp = pdp;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#getPdpConfigurationFile()
	 */
	@Override
	public String getPdpConfigurationFile() {
		return pdpConfigurationFile;
	}
	
	public void setPdpConfigFile(String pdpConfigFile) {
		this.pdpConfigurationFile = pdpConfigFile;
	}

	/* (non-Javadoc)xacml
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#getAip()
	 */
	@Override
	public IAttributeSource getAip() {
		return aip;
	}
	
	public void setAip(IAttributeSource aip) {
		this.aip = aip;
	}
	
	public void setAclAccessControlEnabled(boolean aclAccessControlEnabled) {
		this.aclAccessControlEnabled = aclAccessControlEnabled;
	}
	
	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#isACLAccessControlEnabled()
	 */
	@Override
	public boolean isACLAccessControlEnabled() {
		return this.aclAccessControlEnabled;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.security.IServerSecurityConfiguration#getACLConfigurationFile()
	 */
	@Override
	public String getACLConfigurationFile() {
		return this.aclConfigurationFile;
	}
	
	public void setAclConfigurationFile(String aclConfigurationFile) {
		this.aclConfigurationFile = aclConfigurationFile;
	}
	
}
