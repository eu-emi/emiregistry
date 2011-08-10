package eu.emi.dsr.security.client;

import javax.net.ssl.SSLContext;


/**
 * Implementation of this interface provides data necessary for 
 * setting up transport level security and HTTP authentication. SSL
 * can be set up in two ways: either by providing ready to use SSLContext
 * or by providing all the keystore and truststore details which are needed 
 * to create a new SSLContext.
 * 
 * @author K. Benedyczak
 */
public interface IAuthenticationConfiguration extends ISecurityConfiguration
{
	/**
	 * This method can return initialized {@link SSLContext} or null.
	 * If null is returned then {@link SSLContext} will be created by this library
	 * itself using the data from the other methods defined in this interface.
	 * If this method returns non-null value then most of the methods in this 
	 * interface are not used (only HTTP related are still used). 
	 *  
	 * @return initialized SSL context.
	 */
	public SSLContext getSSLContext();

	/**
	 * Returns true if the client-side TLS authentication should be done.
	 * If false then all (inherited from the super interface  
	 * {@link ISecurityConfiguration}) getKeystore* methods are 
	 * not used at all.
	 * @return
	 */
	public boolean doSSLAuthn();
	
	
	/**
	 * Returns truststore location. If this method returns null
	 * then (for SSL connections) client will trust ANY server certificate.
	 * @return
	 */
	public String getTruststore();
	/**
	 * Returns truststore password.
	 * @return
	 */
	public String getTruststorePassword();
	/**
	 * Returns truststore type.
	 * @return
	 */
	public String getTruststoreType();
	
	/**
	 * Returns true if HTTP BASIC Auth should be used.
	 * @return
	 */
	public boolean doHttpAuthn();
	/**
	 * Returns HTTP BASIC Auth user. Required if doHttpAuthn is true.
	 * @return
	 */
	public String getHttpUser();
	/**
	 * Returns HTTP BASIC Auth user's password. Required if doHttpAuthn is true.
	 * @return
	 */
	public String getHttpPassword();
	
	/**
	 * Cloning support is mandatory
	 */
	public IAuthenticationConfiguration clone();
}
