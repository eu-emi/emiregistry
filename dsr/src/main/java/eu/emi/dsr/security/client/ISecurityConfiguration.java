package eu.emi.dsr.security.client;


/**
 * Implementation of this interface provides keystore details with a specified key.
 * @author K. Benedyczak
 */
public interface ISecurityConfiguration extends Cloneable
{
	/**
	 * Returns keystore location.
	 * @return
	 */
	public String getKeystore();
	/**
	 * Returns keystore password.
	 * @return
	 */
	public String getKeystorePassword();
	/**
	 * Returns key alias in keystore.
	 * @return
	 */
	public String getKeystoreAlias();
	/**
	 * Returns key password in keystore.
	 * @return
	 */
	public String getKeystoreKeyPassword();
	/**
	 * Returns keystore type.
	 * @return
	 */
	public String getKeystoreType();
	/**
	 * Cloning support is mandatory
	 */
	public ISecurityConfiguration clone();
}
