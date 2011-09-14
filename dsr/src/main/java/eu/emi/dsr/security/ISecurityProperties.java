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

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;

import eu.emi.dsr.security.client.IClientProperties;


/**
 * Interface extends {@link IClientProperties} with general security 
 * settings: whether client authentication is required and whether SSL is turned on
 * or not at all, whether signatures are required etc
 *  
 * @author schuller
 * @author a.memon
 */
public interface ISecurityProperties extends IClientProperties {
	/**
	 * property defining whether SSL is enabled
	 */
	public static final String REGISTRY_SSL_ENABLED = "registry.ssl.enabled";

	/**
	 * property defining whether SSL is client authenticated
	 */
	public static final String REGISTRY_SSL_CLIENTAUTH = "registry.ssl.clientauth";

	/**
	 * property defining the SSL keystore
	 */
	public static final String REGISTRY_SSL_KEYSTORE = "registry.ssl.keystore";

	/**
	 * property defining the SSL keystore password
	 */
	public static final String REGISTRY_SSL_KEYPASS = "registry.ssl.keypass";

	/**
	 * property defining the SSL keystore type (e.g. JKS)
	 */
	public static final String REGISTRY_SSL_KEYTYPE = "registry.ssl.keytype";

	/**
	 * property defining the alias of the key to use
	 */
	public static final String REGISTRY_SSL_KEYALIAS = "registry.ssl.keyalias";

	/**
	 * property defining the SSL trust store
	 */
	public static final String REGISTRY_SSL_TRUSTSTORE = "registry.ssl.truststore";

	/**
	 * property defining the SSL trust tore password
	 */
	public static final String REGISTRY_SSL_TRUSTPASS = "registry.ssl.truststorepass";

	/**
	 * property defining the SSL trust store type (e.g. JKS)
	 */
	public static final String REGISTRY_SSL_TRUSTTYPE = "registry.ssl.truststoretype";

//	/**
//	 * set to "true" to enable proxy certificate support, i.e. proper handling of the proxy DNs
//	 * (does NOT mean that proxies can be used for the SSL connection to the UNICORE/X container)
//	 */
//	public static final String UAS_AUTHORISER_PROXY_SUPPORT="uas.security.authoriser.proxysupport";
	
	/**
	 * do we check access on the WSRF level? 
	 */
	public static final String REGISTRY_CHECKACCESS="registry.security.accesscontrol";

	/**
	 * access control PDP class name 
	 * (implementing <code>de.fzj.uas.security.XacmlPDP</code>)
	 */
	public static final String REGISTRY_CHECKACCESS_PDP="registry.security.accesscontrol.pdp";
	
	/**
	 * configuration file for the PDP
	 */
	public static final String REGISTRY_CHECKACCESS_PDPCONFIG="registry.security.accesscontrol.pdp.config";
	
	/**
	 * for CLIENT use: space separated name(s) of any extra UAS outbound security handler class(es)
	 */
	public final static String REGISTRY_OUTBOUND_FILTER_NAME="registry.security.out.handler.classname";
	
	/**
	 * for CLIENT use: space-separated names of any extra UAS incoming security handler classes
	 */
	public final static String REGISTRY_INBOUND_FILTER_NAME="registry.security.in.handler.classname";
	
	
	/**
	 * do we check if the consignor assertion is signed?
	 */
//	public final static String UAS_CHECK_CONSIGNOR_SIGNATURE="uas.security.consignor.checksignature";

	/**
	 * do we require signatures on certain messages 
	 */
	public static final String REGISTRY_REQUIRE_SIGNATURES="registry.security.signatures";

	/**
	 * for stricter security, define which certificate from the truststore
	 * will be used to verify gateway consignor assertions 
	 */
//	public static final String UAS_GATEWAY_ALIAS="uas.security.gateway.alias";
	
	/**
	 * make a copy of these properties
	 */
	public ISecurityProperties clone();
	
	/**
	 * set a property
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value);

	/**
	 * get a property
	 * @param key
	 */
	public String getProperty(String key);


	/**
	 * returns true if SSL client must be authenticated.<br/>
	 * It can be enabled by setting a property 
	 * "unicore.wsrflite.ssl.clientauth" to "true" in the configuration file.
	 * @return
	 */
	public boolean requireClientAuthentication();

	/**
	 * 
	 * @return local user's certificate chain
	 */
	public X509Certificate[] getCertificateChain();
	
	/**
	 * select whether the outgoing message should be signed
	 * @param what
	 */
	public void setSignMessage(boolean what);

	/**
	 * get the certificate of the gateway, or <code>null</code> if this is not
	 * configured (using the {@link #UAS_GATEWAY_ALIAS} property)
	 */
	public X509Certificate getGatewayCertificate();
	
	/**
	 * set extra security tokens to be used in outgoing calls
	 * @param tokens
	 */
	public void setExtraSecurityTokens(Map<String,Object> tokens);
	
	/**
	 * set the classloader to be used e.g. for dynamically loading security handlers
	 * 
	 * @param classLoader - the classloader to use
	 */
	public void setClassLoader(ClassLoader classLoader);
	
	/**
	 * get the raw properties
	 * */
	public Properties getProperties();
}