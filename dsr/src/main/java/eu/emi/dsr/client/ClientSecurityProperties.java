/**
 * 
 */
package eu.emi.dsr.client;

import java.io.Serializable;

/**
 * @author a.memon
 *
 */
public class ClientSecurityProperties implements Serializable{
	private static final long serialVersionUID = 1L;
	private String truststorePath;
	private String truststorePassword;
	private String truststoreType;
	private String keystoreType;
	private String keystorePassword;
	private String keystorePath;
	public String getTrustStorePath() {
		return truststorePath;
	}
	public void setTruststorePath(String trustStrorePath) {
		this.truststorePath = trustStrorePath;
	}
	public String getTruststorePassword() {
		return truststorePassword;
	}
	public void setTruststorePassword(String trustStrorePassword) {
		this.truststorePassword = trustStrorePassword;
	}
	public String getTruststoreType() {
		return truststoreType;
	}
	public void setTruststoreType(String trustStoreType) {
		this.truststoreType = trustStoreType;
	}
	public String getKeystoreType() {
		return keystoreType;
	}
	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getKeystorePath() {
		return keystorePath;
	}
	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}
	
}
