/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
 

package eu.emi.dsr.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;

import eu.emi.client.security.ISecurityProperties;
import eu.emi.dsr.util.Log;

/**
 * UAS security properties. For property lookup, this uses a 
 * hierarchy of property sources: <br/>
 *  - supplied input stream (or file name), if any <br/>
 *  - parent properties passed as constructor argument (e.g. {@link Kernel}'s)  <br/> 
 * @author schuller
 */
public class DSRSecurityProperties implements ISecurityProperties, Cloneable {
	
	private static final Logger logger=Log.getLogger(Log.SECURITY,DSRSecurityProperties.class);
	
	private Properties properties;
	
	private X509Certificate[] certChain;
	
	private volatile PrivateKey privateKey;
	
	private boolean signMessage=true;
	
	private Map<String,Object>extraSecurityTokens;
	
	private volatile X509Certificate gatewayCertificate;

	private ClassLoader classLoader;
	
	private Properties parent; 
	
	public DSRSecurityProperties(Properties parent, Properties local) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		properties=local;
		this.parent = parent;
		if (isSslEnabled())
			readKeystore();
	}

	public DSRSecurityProperties(Properties parent) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		this(parent, new Properties());
		if (isSslEnabled())
			readKeystore();
	}

	public DSRSecurityProperties(Properties parent, InputStream is) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		try {
			properties=new Properties();
			properties.load(is);
		} catch(Exception e){
			logger.fatal("Could not read properties.",e);
		}
		this.parent = parent;
		if (isSslEnabled())
			readKeystore();
	}
	
	public DSRSecurityProperties(Properties parent, String propertiesFileName)throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException{
		this(parent, new FileInputStream(propertiesFileName));
	}
	
	@Override
	public ISecurityProperties clone(){
		if(logger.isTraceEnabled())logger.trace("Copying security props: "+toString());
		DSRSecurityProperties clone;
		try {
			clone = (DSRSecurityProperties) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported n Cloneable class?!", e);
		} 
		clone.properties=(Properties)this.properties.clone();
		//Note: parentProperties reference is copied intentionally, it is not cloned.
		//as we need a live instance.
//		clone.etdSettings = etdSettings.clone();
		if(extraSecurityTokens!=null){
			clone.extraSecurityTokens=new HashMap<String,Object>();
			clone.extraSecurityTokens.putAll(extraSecurityTokens);
		}
		return clone;
	}
	
//	@Override
//	public String getInHandlerClassNames() {
//		return doGetProperty(ISecurityProperties.UAS_INHANDLER_NAME);
//	}
//
//	@Override
//	public String getOutHandlerClassNames() {
//		return doGetProperty(ISecurityProperties.UAS_OUTHANDLER_NAME);
//	}

	/**
	 * return named property. 
	 * Lookup order:
	 *   - own properties
	 *   - Kernel.getKernel().getProperty()
	 */
	protected String doGetProperty(String key) {
		String p=(String)properties.get(key);
		if(p==null){
			p=parent.getProperty(key);
		}
		return p;
	}
	
	@Override
	public String getProperty(String key){
		return doGetProperty(key);
	}
	/**
	 * set a property
	 * @param key
	 * @param value
	 */
	@Override
	public void setProperty(String key, String value){
		properties.put(key,value);
	}
	/**
	 * returns true if SSL mode is enabled.<br/>
	 * SSL can be enabled by setting a property 
	 * "unicore.wsrflite.ssl" to "true" in the wsrflite.xml file 
	 * or by defining a system property
	 * @return
	 */
	@Override
	public boolean isSslEnabled(){
		if(doGetProperty(REGISTRY_SSL_ENABLED) == null){
			//default scheme
//			return false;
//			return doGetProperty(ServerConstants.REGISTRY_SCHEME).equalsIgnoreCase("https");
		}
//		return "true".equalsIgnoreCase(doGetProperty(REGISTRY_SSL_ENABLED));
		return true;
	}

	@Override
	public boolean doSSLAuthn()
	{
		return isSslEnabled();
	}
	
	@Override
	public boolean requireClientAuthentication() {
		return "true".equalsIgnoreCase(doGetProperty(REGISTRY_SSL_CLIENTAUTH));
	}

	@Override
	public synchronized X509Certificate[] getCertificateChain() {
		return certChain;
	}
	
	@Override
	public X509Certificate getPublicKey() {
		return certChain != null ? getCertificateChain()[0] : null;
	}
	
	/**
	 * read the keys from the keystore
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 * @throws CertificateException 
	 */
	private void readKeystore() throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
		String keystoreName=getKeystore();
		if (keystoreName == null)
			throw new IOException("Keystore path is not defined in the security properties");
	    	String keystoreType=getKeystoreType();
	    	if(keystoreType==null)keystoreType="jks";
	    	String keystorePassword=getKeystorePassword();
	    	String keyPassword = getKeystoreKeyPassword();
	    	String keystoreAlias=getKeystoreAlias();

	    	KeyStore keyStore;
    		logger.debug("Reading from keystore: " + keystoreName);
    		keyStore = KeyStore.getInstance(keystoreType);
	    	FileInputStream f = new FileInputStream(keystoreName);
	    	try{
				keyStore.load(f, keystorePassword.toCharArray());
		} finally {
			f.close();
		}
	    	logger.debug("Keystore: " + keystoreName + " successfully loaded");
	    	if(keystoreAlias==null){
	    		keystoreAlias = KeystoreChecker.findAlias(keyStore);
		   		if(keystoreAlias==null){
		   			throw new KeyStoreException("Keystore "+keystoreName+" does not contain any key entries!");
		   		}
	    		logger.debug("No alias supplied, loading  <"+keystoreAlias+">");
	    	}
	    	else {
	    		logger.debug("Loading  <"+keystoreAlias+">");
	    	}
	    	Certificate[] path=keyStore.getCertificateChain(keystoreAlias);
	    	if (path==null)
	    		throw new KeyStoreException("Alias <"+keystoreAlias+"> cannot be found in keystore. Please check your configuration.");
	    	privateKey=(PrivateKey)keyStore.getKey(keystoreAlias, keyPassword.toCharArray());
	    	if(privateKey==null)
	    		throw new KeyStoreException("Alias <"+keystoreAlias+"> does not denote a key entry. Please check your configuration.");
	    	
	    	certChain=new X509Certificate[path.length];
	    	for(int i=0;i<path.length;i++){
	    		certChain[i]=(X509Certificate)path[i];
	    	}
//	    	etdSettings.setIssuerCertificateChain(certChain);
	    	//load gateway cert if configured
//	    	String gwAlias=getProperty(UAS_GATEWAY_ALIAS);
//	    	if(gwAlias!=null){
//	    		loadGWCert(gwAlias);
//	    	}
	}
	
	@SuppressWarnings("unused")
	private void loadGWCert(String alias){
		String truststoreName=getTruststore();
		String truststoreType=getTruststoreType();
		if(truststoreType==null)truststoreType="jks";
		try{
			KeyStore trustStore;
			trustStore = KeyStore.getInstance(truststoreType);
			FileInputStream f = new FileInputStream(truststoreName);
			try{
				trustStore.load(f, getTruststorePassword().toCharArray());
			} finally {
				f.close();
			}
			logger.debug("Truststore: " + truststoreName + " successfully loaded");
			gatewayCertificate=(X509Certificate)trustStore.getCertificate(alias);
			if(gatewayCertificate==null)throw new IllegalArgumentException("Alias <"+alias+"> cannot be found in truststore. Please check your configuration.");
			logger.debug("Using gateway certificate <"+gatewayCertificate.getSubjectX500Principal().getName()+">");
		}catch(Exception e){ 
			String msg="Gateway alias is defined, but I could not load a gateway certificate from the truststore at "+truststoreName;
			throw new IllegalArgumentException(msg,e);
		}
	}

	
	/**
	 * returns the private key entry identified by getKeystoreAlias() from the keystore 
	 */
	@Override
	public synchronized PrivateKey getPrivateKey() {
		return privateKey;
	}

	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(this.getClass().getName()).append("\n");
		sb.append("Keystore <").append(doGetProperty(REGISTRY_SSL_KEYSTORE)).append(">\n");
		sb.append("Truststore <").append(doGetProperty(REGISTRY_SSL_TRUSTSTORE)).append(">\n");
		try{
			sb.append("Identity: ").append(getCertificateChain()[0].getSubjectX500Principal().getName()).append("\n");
		}catch(Exception e){
			sb.append("User not set.\n");
		}
		//if(getGatewayCertificate()!=null){
			//sb.append("Gateway identity: ").append(getGatewayCertificate().getSubjectX500Principal().getName()).append("\n");
		//}
		sb.append("Signing messages: ").append(doSignMessage()).append("\n");
//		sb.append("Extend trust delegation: ").append(etdSettings.isExtendTrustDelegation()).append("\n");
		return sb.toString();
	}
	@Override
	public boolean doSignMessage() {
		return signMessage;
	}
	@Override
	public void setSignMessage(boolean signMessage) {
		this.signMessage = signMessage;
	}
	@Override
	public synchronized X509Certificate getGatewayCertificate(){
		return gatewayCertificate;
	}
	
	@Override
	public Map<String, Object> getExtraSecurityTokens() {
		return extraSecurityTokens;
	}
	@Override
	public void setExtraSecurityTokens(Map<String, Object> extraSecurityTokens) {
		this.extraSecurityTokens = extraSecurityTokens;
	}
	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public boolean doHttpAuthn() {
		return false;
	}
	@Override
	public String getHttpUser() {
		return null;
	}
	@Override
	public String getHttpPassword() {
		return null;
	}
	@Override
	public SSLContext getSSLContext() {
		return null;
	}
	@Override
	public String getKeystorePassword() {
		return doGetProperty(REGISTRY_SSL_KEYPASS);
	}
	@Override
	public String getKeystoreKeyPassword() {
		return doGetProperty(REGISTRY_SSL_KEYPASS);
	}
	@Override
	public String getKeystore() {
		return doGetProperty(REGISTRY_SSL_KEYSTORE);
	}
	@Override
	public String getKeystoreType() {
		return doGetProperty(REGISTRY_SSL_KEYTYPE);
	}
	@Override
	public String getKeystoreAlias() {
		return doGetProperty(REGISTRY_SSL_KEYALIAS);
	}
	
	@Override
	public String getTruststore() {
		return doGetProperty(REGISTRY_SSL_TRUSTSTORE);
	}
	
	@Override
	public String getTruststoreType() {
		return doGetProperty(REGISTRY_SSL_TRUSTTYPE);
	}

	@Override
	public String getTruststorePassword() {
		return doGetProperty(REGISTRY_SSL_TRUSTPASS);
	}

	@Override
	public Properties getExtraSettings() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.security.client.IClientProperties#getOutHandlerClassNames()
	 */
	@Override
	public String getOutHandlerClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.security.client.IClientProperties#getInHandlerClassNames()
	 */
	@Override
	public String getInHandlerClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.security.ISecurityProperties#getProperties()
	 */
	@Override
	public Properties getProperties() {
		Properties p = (Properties) properties.clone();
		return p;
	}

//	@Override
//	public synchronized ETDClientSettings getETDSettings() {
//		return etdSettings;
//	}	
}
