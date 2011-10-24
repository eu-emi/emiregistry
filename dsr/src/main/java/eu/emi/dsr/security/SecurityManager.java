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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.pdp.PDPResult;
import eu.emi.dsr.pdp.PDPResult.Decision;
import eu.emi.dsr.pdp.RegistryPDP;
import eu.emi.dsr.pdp.local.LocalHerasafPDP;
import eu.emi.dsr.security.client.AuthSSLProtocolSocketFactory;
import eu.emi.dsr.security.util.KeystoreUtil;
import eu.emi.dsr.security.util.ResourceDescriptor;
import eu.emi.dsr.util.Log;

/**
 * access to security components
 * 
 * @author schuller
 * @author golbi
 */
public final class SecurityManager {

	private static final Logger logger=Log.getLogger(Log.SECURITY,SecurityManager.class);

	/**
	 * for unit testing, set this property to "true" to disable security
	 */
	public static final String DISABLE_SECURITY_AND_ACCESS_CONTROL="registry.security.disable";
	
	private SecurityManager(){}

	private static RegistryPDP pdp=null;

	private static Boolean isAccessControlEnabled=null;

	@SuppressWarnings("unused")
	private static Boolean isProxyModeEnabled=null;

	private static IAttributeSource attributeSource = null;

	private static X509Certificate serverCert=null;
	@SuppressWarnings("unused")
	private static X509Certificate gatewayCert=null;

	private static List<AuthNCheckingStrategy>authNCheckStrategies=new ArrayList<AuthNCheckingStrategy>();

	private static Set<AttributeHandlingCallback> attribHandlingCallbacks=new HashSet<AttributeHandlingCallback>();

	private static final List<String>actionsRequiringSignatures=new ArrayList<String>();

	/**
	 * add a callback class for dealing with additional security attributes 
	 * 
	 * @param aac - an {@link AttributeHandlingCallback}
	 */
	public static void addCallback(AttributeHandlingCallback aac){
		attribHandlingCallbacks.add(aac);
	}

	/**
	 * get the certificate of the server
	 * @return
	 */
	public static X509Certificate getServerCert(){
		if(serverCert==null){
			try{
				serverCert=DSRServer.getSecurityProperties().getCertificateChain()[0];
				logger.info("Server identity: "+serverCert.getSubjectX500Principal().toString());
			}catch(Exception e){
				Log.logException("Could not get server certificate",e,logger);
			}

		}
		return serverCert;
	}

	/**
	 * get the server's identity, or <code>null</code> if 
	 * no server keystore has been configured

	 * @see #getServerCert()
	 * @return an {@link X500Principal} or <code>null</code>
	 */
	public static X500Principal getServerIdentity(){
		ISecurityProperties secProps=DSRServer.getSecurityProperties();
		if(secProps.isSslEnabled() && secProps.getKeystore()!=null){
			return getServerCert().getSubjectX500Principal();
		}
		else{
			return null;
		}
	}

	/**
	 * get the X500 name of the server in human-friendly form,
	 * i.e. using X500Principal.toString() <br/>
	 *  
	 * @see #getServerIdentity()
	 * @return the human-friendly form of the server DN
	 */
	public static String getServerDistinguishedName(){
		X500Principal p=getServerIdentity();
		return p!=null?getServerIdentity().toString():null;
	}

//	/**
//	 * get the certificate of the gateway, if available, in the following way
//	 * 
//	 * <ul>
//	 * <li>it can be configured statically, by adding the certificate to the truststore and
//	 * setting the property {@link ISecurityProperties#UAS_GATEWAY_ALIAS} to the 
//	 * alias of the certificate</li>
//	 * <li>it is retrieved dynamically by opening an SSL connection to the server at the configured
//	 * base url</li>
//+	 * </ul>
//	 * @return the {@link X509Certificate} of the gateway
//	 */
//	public synchronized static X509Certificate getGatewayCert(){
//		if(gatewayCert==null){
//			ISecurityProperties secProps=DSRServer.getSecurityProperties();
//			gatewayCert=secProps.getGatewayCertificate();
//			if(gatewayCert==null){
//				gatewayCert=getPeerCertificate(DSRServer.getProperty(Kernel.WSRF_BASEURL), secProps);
//			}
//		}
//		return gatewayCert;
//	}
	
	private static String[]trustedCertDNs=null;
	public static synchronized String[] getTrustedCertificateDNs(){
		if(trustedCertDNs==null){
			ISecurityProperties sp=DSRServer.getSecurityProperties();
			if(sp.getTruststore()!=null){
				try{
					String type=sp.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE);
					String file=sp.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE);
					String password=sp.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTPASS);
					KeyStore ks = KeystoreUtil.createKeyStore(file, password, type);
					trustedCertDNs=KeystoreUtil.getTrustedCertDNs(ks);
				}
				catch(Exception e){
					logger.error("Can't load trusted certs from truststore",e);
				}
			}
		}
		return trustedCertDNs;
	}

	/**
	 * get the XACML policy decision point
	 * @return {@link XacmlPDP}
	 */
	public static synchronized RegistryPDP getPDP(){
		if(pdp==null){
			String conf=DSRServer.getProperty(DSRSecurityProperties.REGISTRY_CHECKACCESS_PDPCONFIG, null);
			String pdpClass=DSRServer.getProperty(DSRSecurityProperties.REGISTRY_CHECKACCESS_PDP);
			String def=LocalHerasafPDP.class.getName();
			if(pdpClass==null)
			{
				//fallback to default
				pdpClass=def;
				logger.info("Using default value for property <"+DSRSecurityProperties.REGISTRY_CHECKACCESS_PDP+">");
			}
			try{
				Class.forName(pdpClass);
			}catch(ClassNotFoundException cfe){
				logger.error("Cannot find PDP class <"+pdpClass+"> fallback to default : "+def);
				pdpClass=def;
			}
			
			try {
				Class<?> pdpClazz = Class.forName(pdpClass);
				Constructor<?> constructor = null;
				if (conf != null) {
					constructor = pdpClazz.getConstructor(String.class);
					pdp = (RegistryPDP)constructor.newInstance(conf);
				} else {
					constructor = pdpClazz.getConstructor();
					pdp = (RegistryPDP)constructor.newInstance();
				}

				
				logger.info("Using PDP class <"+pdpClass+"> and config file <"+conf+">");	
			}catch(Exception e){
				logger.fatal("Can't create PDP.", e);
				throw new RuntimeException("Can't create a PDP: ",e);
			}
		}
		return pdp;
	}

	/**
	 * Returns an attribute map for a set of security tokens from the configured Attribute Source
	 * @param tokens
	 * @return attributes
	 */
	public static SubjectAttributesHolder establishAttributes(final SecurityTokens tokens) 
			throws Exception {
		if (attributeSource==null) 
			createAttributeSource();
		return attributeSource.getAttributes(tokens,null);
	}
	
	/**
	 * Creates AttributeSource used for establishing clients' attributes.
	 * @throws Exception
	 */
	public static synchronized void createAttributeSource()throws Exception{
		attributeSource=new AttributeSourceFactory(DSRServer.getConfiguration().getProperties()).makeAttributeSource();
	}
	
	
	/**
	 * Sets up the Xlogin object in the Client.
	 * @param client
	 * @param preferences
	 * @param validAttributes
	 * @param defaultAttributes
	 */
//	private static void handleXlogin(Client client, Map<String, String[]> preferences, 
//			Map<String, String[]> validAttributes, Map<String, String[]> defaultAttributes) {
//		String[] validXlogins = validAttributes.get(IAttributeSource.ATTRIBUTE_XLOGIN);
//		String[] defaultXlogin = defaultAttributes.get(IAttributeSource.ATTRIBUTE_XLOGIN);
//		
//		String[] validPGroups = validAttributes.get(IAttributeSource.ATTRIBUTE_GROUP);
//		if (validPGroups == null)
//			validPGroups = new String[0];
//		String[] defaultPGroup = defaultAttributes.get(IAttributeSource.ATTRIBUTE_GROUP);
//		if (defaultPGroup == null)
//			defaultPGroup = new String[0];
//		
//		String[] validSupGroups = validAttributes.get(IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS);
//		if (validSupGroups == null)
//			validSupGroups = new String[0];
//		String[] defaultSupGroups = defaultAttributes.get(IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS);
//		if (defaultSupGroups == null)
//			defaultSupGroups = new String[0];
//		
//		String[] validGroups = new String[validPGroups.length + validSupGroups.length];
//		for (int i=0; i<validPGroups.length; i++)
//			validGroups[i] = validPGroups[i];
//		for (int i=0; i<validSupGroups.length; i++)
//			validGroups[i+validPGroups.length] = validSupGroups[i];
//		String[] pAddDefaultGids = defaultAttributes.get(IAttributeSource.ATTRIBUTE_ADD_DEFAULT_GROUPS);		
//		
//		//uid must be always set. XLogin object wraps uid, gid and supp. gids.
//		if (validXlogins!=null && validXlogins.length > 0){
//			//create XLogin with valid values
//			Xlogin xlogin = new Xlogin(validXlogins, validGroups);
//			
//			//set defaults from AS
//			xlogin.setSelectedLogin(defaultXlogin[0]);
//			if (defaultPGroup.length > 0)
//				xlogin.setSelectedGroup(defaultPGroup[0]);
//			if (defaultSupGroups.length > 0)
//				xlogin.setSelectedSupplementaryGroups(defaultSupGroups);
//			
//			//handle user preferences
//			String[] reqXlogin = preferences.get(IAttributeSource.ATTRIBUTE_XLOGIN);
//			if (reqXlogin!=null && reqXlogin.length > 0)
//				xlogin.setSelectedLogin(reqXlogin[0]);
//			
//			String[] reqGroup = preferences.get(IAttributeSource.ATTRIBUTE_GROUP);
//			if (reqGroup!=null && reqGroup.length > 0)
//				xlogin.setSelectedGroup(reqGroup[0]);
//			
//			String[] reqSupGroups = preferences.get(IAttributeSource.ATTRIBUTE_SUPPLEMENTARY_GROUPS);
//			if (reqSupGroups != null && reqSupGroups.length > 0)
//				xlogin.setSelectedSupplementaryGroups(reqSupGroups);
//			
//			//This is special - we always allow for changing this by the user.
//			String[] reqAddDefaultGroups = preferences.get(IAttributeSource.ATTRIBUTE_ADD_DEFAULT_GROUPS);
//			if (reqAddDefaultGroups != null && reqAddDefaultGroups.length > 0) {
//				if (reqAddDefaultGroups[0].equalsIgnoreCase("true"))
//					xlogin.setAddDefaultGroups(true);
//				else if (reqAddDefaultGroups[0].equalsIgnoreCase("false"))
//					xlogin.setAddDefaultGroups(false);
//				else
//					throw new SecurityException("Requested value <"+reqAddDefaultGroups[0]+
//						"> is invalid for " + IAttributeSource.ATTRIBUTE_ADD_DEFAULT_GROUPS +
//						" attribute; use 'true' or 'false'.");
//			} else if (pAddDefaultGids != null && pAddDefaultGids.length > 0) {
//				if (pAddDefaultGids[0].equalsIgnoreCase("true"))
//					xlogin.setAddDefaultGroups(true);
//				else if (pAddDefaultGids[0].equalsIgnoreCase("false"))
//					xlogin.setAddDefaultGroups(false);
//			}
//			
//			client.setXlogin(xlogin);
//		}
//	}
	
	/**
	 * Sets up the role object in the Client.
	 * @param client
	 * @param preferences
	 * @param validAttributes
	 * @param defaultAttributes
	 */
	private static void handleRole(Client client, Map<String, String[]> preferences, 
			Map<String, String[]> validAttributes, Map<String, String[]> defaultAttributes) {
		Role r = new Role();
		String[] validRoles = validAttributes.get(IAttributeSource.ATTRIBUTE_ROLE);
		String[] defaultRole = defaultAttributes.get(IAttributeSource.ATTRIBUTE_ROLE);
		if (defaultRole!=null && defaultRole.length > 0) {
			String[] prefRole = preferences.get(IAttributeSource.ATTRIBUTE_ROLE);
			if (prefRole != null && prefRole.length > 0) {
				boolean roleOk = false;
				for (String valid: validRoles)
					if (valid.equals(prefRole[0])) {
						r.setName(valid);
						r.setDescription("user's preferred role");
						roleOk = true;
						break;
					}
				if (!roleOk)
					throw new SecurityException("Requested role <"+prefRole[0]+"> is not available.");
			} else {
				r.setName(defaultRole[0]);
				r.setDescription("role from attribute source");
			}
		} else {
			r.setName(IAttributeSource.ROLE_ANONYMOUS);
			r.setDescription("default role");
		}
		client.setRole(r);
	}

	/**
	 * Sets up the queue object in the Client. Only small part of the job is done here
	 * as available queues are further refined by IDB settings and only then user 
	 * preferences are checked 
	 * @param client
	 * @param preferences
	 * @param validAttributes
	 * @param defaultAttributes
	 */
//	private static void handleQueue(Client client, Map<String, String[]> preferences, 
//			Map<String, String[]> validAttributes, Map<String, String[]> defaultAttributes) {
//		Queue q = new Queue();
//		String[] validQueues = validAttributes.get(IAttributeSource.ATTRIBUTE_QUEUES);
//		String[] defQueue = defaultAttributes.get(IAttributeSource.ATTRIBUTE_QUEUES);
//		if (validQueues != null && validQueues.length > 0)
//		{
//			q.setValidQueues(validQueues);
//			if (defQueue != null && defQueue.length > 0)
//				q.setSelectedQueue(defQueue[0]);
//		}
//		client.setQueue(q);
//	}

	
	/**
	 * Sets up incarnation attributes, xlogin, groups and role.
	 * @param client
	 * @param tokens
	 */
	private static void assembleClientAttributes(Client client, SecurityTokens tokens) {
		if (isServer(client)) {
			Role r=getServerRole();
			client.setRole(r);
		} else {
			//setup client with authorisation attributes
			SubjectAttributesHolder subAttributes;
			try {
				subAttributes = establishAttributes(tokens);
			} catch (Exception e) {
				throw new SecurityException("Exception when getting " +
						"attributes for the client.", e);
			}
			if (subAttributes != null) 
				client.setSubjectAttributes(subAttributes);
			//get the list of user preferences from the User assertion
			@SuppressWarnings("unchecked")
			Map<String, String[]> preferences = (Map<String, String[]>) 
				tokens.getContext().get(UserAttributeCallback.USER_PREFERENCES_KEY);
			if (preferences == null)
				preferences = Collections.emptyMap(); 
			
			Map<String, String[]> validAttributes = client.getSubjectAttributes().getValidIncarnationAttributes();
			Map<String, String[]> defaultAttributes = client.getSubjectAttributes().getDefaultIncarnationAttributes();
			
//			handleXlogin(client, preferences, validAttributes, defaultAttributes);
			handleRole(client, preferences, validAttributes, defaultAttributes);
//			handleQueue(client, preferences, validAttributes, defaultAttributes);
//			@SuppressWarnings("unused")
//			String[] vos = validAttributes.get(IAttributeSource.ATTRIBUTE_VOS);
//			if (vos != null)
//				client.setVos(vos);
		}
		
		//handle additional attributes
		for(AttributeHandlingCallback a: attribHandlingCallbacks){
			Map<String, Serializable> attribs=a.extractAttributes(tokens);
			if(attribs!=null){
				client.getExtraAttributes().putAll(attribs);
			}
		}		
	}
	
	
	/**
	 * Typical authorisation is done here (when security is ON and we don't handle a local call).
	 *
	 * @param tokens - Security tokens
	 * @return authorised Client object 
	 */
	private static Client createSecureClient(final SecurityTokens tokens) {
		Client client=new Client();
		String dn=null;
		
		//if (tokens.getEffectiveUserName() == null){
			//no security info at all -> can't authorise
			//throw new AuthorisationException("Can't authorise: no user cert available, " +
				//"no trust delegation found, no consignor cert.");
		//}

//		if(isProxyModeEnabled()){
//			try {
//				dn=handleProxyCert(tokens);
//			} catch (CertificateException e) {
//				throw new SecurityException("Exception during proxy certificate handling.", e);
//			}
//		}
		//if proxy cert was not used we use UNICORE established effective user. With proxy certs proxy initiator is set as the user.
		// FIXME Should be handled in security handlers!
		if (dn==null) {
			dn=tokens.getUserName().toString();
		}
		client.setDistinguishedName(dn);		
		client.setSecurityTokens(tokens);
		
		assembleClientAttributes(client, tokens);

//		if(logger.isDebugEnabled()){
//			logger.debug("Client info:\n" + client.toString());
//						
//			try{
//				SecurityTokens st=client.getSecurityTokens();
//				if (st!=null)
//					logger.debug("TD Chain length=" + 
//							st.getTrustDelegationTokens().size());
//			}catch(Exception e){
//				logger.debug("No TD.");
//			}
//		}
		
		return client;
	}
	
	/**
	 * Create an authorised Client object. This will use the supplied
	 * security tokens to make a call to an authoriser (such as the XUUDB)
	 * and set client attributes such as role, xlogin, etc based on the
	 * authoriser's reply.<br/>
	 * 
	 * 
	 * @param tokens - Security tokens
	 * @return authorised Client object 
	 */
	public static Client createAndAuthoriseClient(final SecurityTokens tokens){
		Client client=new Client();
		
		// 4 cases: local call, no authorisation material, security is ON and security is OFF
		if(isLocalCall())
			client = makeAnonymousClient("CN=Local_call");
		else if (tokens == null)
			client = makeAnonymousClient(null);
		else if(!DSRServer.getConfiguration().getBooleanProperty(SecurityManager.DISABLE_SECURITY_AND_ACCESS_CONTROL, false))
			client = createSecureClient(tokens);
		else
			client = makeAnonymousClient("CN=Security_is_disabled");
	
		//now we know the client name we can put it into the log context
		MDC.put("clientName",client.getDistinguishedName());
		return client;
	}

	/**
	 * gets the DN in case of a proxy cert. As a "side effect", the user and user name fields 
	 * in the Securitytokens are modified, so that later stages in the authz get the proper 
	 * (i.e. non-proxy)user DN.
	 * 
	 * @param tokens
	 * @return the real DN, or null if the user cert path is not a proxy
	 * @throws CertificateException
	 */
//	public static String handleProxyCert(SecurityTokens tokens)throws CertificateException{
//		//get the real user certificate
//		X509Certificate xCert=getProxiedUserCert(tokens);
//		tokens.setUserName(xCert.getSubjectX500Principal());
//		CertPath proxyUser=CertificateFactory.getInstance("X.509").generateCertPath(Arrays.asList(new X509Certificate[]{xCert}));
//		tokens.setUser(proxyUser);
//		String dn=xCert.getSubjectX500Principal().toString();
//		if(logger.isDebugEnabled()){
//			logger.debug("Real User DN: "+dn);
//		}
//		return dn;
//	}
//	
	/**
	 * get the real user cert from either the user cert path or the consignor cert path
	 * 
	 * @param tokens
	 * @return X509Certificate - the user certificate 
	 * @throws CertificateException
	 */
//	public static X509Certificate getProxiedUserCert(SecurityTokens tokens)throws CertificateException{
//		CertPath cp=tokens.getUser()!=null?tokens.getUser():tokens.getConsignor();
//		X509Certificate xCert=getRealUserCertFromProxyCertPath(cp);
//		return xCert;
//	}

	/**
	 * for the given certificate path, return the first entry that is not a proxy cert,
	 * i.e. the real user certificate
	 * @param cp - the certpath to check
	 * @return the user X509 certificate
	 */
//	public static X509Certificate getRealUserCertFromProxyCertPath(CertPath cp){
//		X509Certificate xCert=null;
//		//get a useful DN from the cert chain
//		if(cp!=null && cp.getCertificates().size()>0){
//			String dn=null;
//			List<? extends Certificate> certs=cp.getCertificates();
//			if(logger.isDebugEnabled()){
//				logger.debug("Checking certpath to extract real user cert.");
//				for(Certificate c: certs){
//					logger.debug("DN: "+((X509Certificate)c).getSubjectDN().toString());
//				}
//			}
//			for(Certificate c: certs){
//				xCert=(X509Certificate)c;
//				dn=xCert.getSubjectX500Principal().toString();
//				if(!isProxyDN(dn))break;
//			}
//			if(logger.isDebugEnabled()){
//				logger.debug("Real subject: "+dn);
//			}
//		
//		}
//		return xCert;
//	}

	/**
	 * checks if user+consignor DNs are equal (taking into account proxy mode)
	 *
	 * @param tokens
	 * @return
	 */
//	public static boolean checkConsignorEqualsUser(SecurityTokens tokens){
//		if(isProxyModeEnabled()){
//			String dnUser=getRealUserCertFromProxyCertPath(tokens.getUser()).getSubjectX500Principal().getName();
//			String dnConsignor=getRealUserCertFromProxyCertPath(tokens.getConsignor()).getSubjectX500Principal().getName();
//			return dnConsignor!=null && dnConsignor.equals(dnUser);
//		}
//		else{
//			return tokens.getConsignorCertificate().getSubjectX500Principal().equals(tokens.getUserName());
//		}
//	}
	
	/**
	 * server role
	 */
	protected static Role getServerRole(){
		Role r=new Role();
		r.setDescription("Server");
		r.setName("__server__");
		return r;
	}
	/**
	 * a special client useful for anonymous requests
	 * @return
	 */
	protected static Client makeAnonymousClient(String dn){
		Client c=new Client();
		if(dn==null)c.setDistinguishedName("CN=ANONYMOUS,O=UNKNOWN,OU=UNKNOWN");
		else c.setDistinguishedName(dn);
		c.setRole(new Role("anonymous","No authorisation information available."));
		return c;
	}

	/**
	 * check whether authentication is OK and conforms to this
	 * server's policies
	 *  
	 * @param tokens - SecurityTokens from the request
	 * @param action - the method being invoked
	 * @param d - the resource being accessed
	 */
	public static void checkAuthentication(SecurityTokens tokens, String action, ResourceDescriptor d){
		if(!DSRServer.getConfiguration().getBooleanProperty(ISecurityProperties.REGISTRY_REQUIRE_SIGNATURES))return;
		for(AuthNCheckingStrategy s: authNCheckStrategies){
			s.checkAuthentication(tokens, action, d);
		}
	}

	/**
	 * register checking strategies
	 * @param strategies - {@link AuthNCheckingStrategy} objects
	 */
	public static void registerAuthNCheckingStrategies(AuthNCheckingStrategy ... strategies){
		authNCheckStrategies.addAll(Arrays.asList(strategies));
	}

	private static Decision checkAuthzInternal(Client c, String action, ResourceDescriptor d) {
		PDPResult res;
		try {
			res = getPDP().checkAuthorisation(c, action, d);
		} catch(Exception e) {
			throw new AuthorisationException("Access denied due to PDP error: " + e);
		}

		if (res.getDecision().equals(PDPResult.Decision.UNCLEAR)) {
			logger.warn("The EMI Registry PDP was unable to make a definitive decision, " +
					"check your policy files and consult other log messages.");
		}
		return res.getDecision();
	}
	
	/**
	 * Check access by evaluating the XACML policies. 
	 * I access is DENIED then {@link AuthorisationException} is thrown. 
	 *  
	 * @param c
	 * @param action
	 * @param d
	 * @throws AuthorisationException
	 */
	public static void checkAuthorisation(Client c, String action, ResourceDescriptor d) 
			throws AuthorisationException {
		Decision decision = checkAuthzInternal(c, action, d);
		if (!decision.equals(PDPResult.Decision.PERMIT)){
			String msg="Access denied for "+c.getDistinguishedName()+" on resource "+d;
			logger.info(msg);
			throw new AuthorisationException(msg);
		}
	}

	/**
	 * Can the given client access the given (server-local!) endpoint?
	 * This will not give the correct result if the action plays a role
	 * 
	 * @param client - the client
	 * @param serviceName - can be <code>null</code>
	 * @param wsResourceID - can be <code>null</code>
	 * @return true if the 
	 */
	public static boolean isAccessible(Client client, String serviceName, String wsResourceID, String owner)
			throws Exception{
		if(!isAccessControlEnabled())
			return true;
		if(isServer(client))
			return true;
		ResourceDescriptor resource=new ResourceDescriptor(serviceName,wsResourceID,owner);
		Decision decision = checkAuthzInternal(client, null, resource);
		
		if (!decision.equals(PDPResult.Decision.PERMIT))
			return false;
		else
			return true;
	}

	private static final ThreadLocal<Boolean>localCalls=new ThreadLocal<Boolean>();
	
	
	/**
	 * for the current thread, set the "local call" flag. This should be used always in 
	 * using a try-finally construct, i.e.
	 * 
	 * <pre>
	 *  SecurityManager.setLocalCall();
	 *  try{
	 *    //... perform call
	 *  }
	 *  finally{
	 *    SecurityManager.clearLocalCall();
	 *  }
	 * </pre> 
	 */
	public static void setLocalCall(){
		localCalls.set(Boolean.TRUE);
	}
	
	/**
	 * for the current thread, clear the "local call" flag
	 * @return
	 */
	public static void clearLocalCall(){
		localCalls.set(null);
	}
	
	/**
	 * check whether the current request is local (i.e. made from within the same VM)
	 */
	public static boolean isLocalCall(){
		return Boolean.TRUE.equals(localCalls.get());
	}
	
	/**
	 * checks whether the given client has the server identity
	 */
	public static boolean isServer(Client c){
		try{
			if(c==null)return false;
			if(logger.isTraceEnabled()){
				logger.trace("Check server=<"+getServerCert().getSubjectX500Principal().getName()+"> "+
						" vs client=<"+c.getDistinguishedName()+">");
			}
			if(getServerDistinguishedName().equals(c.getDistinguishedName())){
				return true;
			}
		}catch(Exception e){
			Log.logException("Could not check certificate vs. server cert.",e,logger);
		}
		return false;
	}

	/**
	 * checks whether the given certificate is the server identity
	 */
	public static boolean isServer(X509Certificate cert){
		return getServerCert().equals(cert);
	}


	/**
	 * checks whether the given CertPath is the server identity
	 */
	public static boolean isServer(CertPath path){
		try{
			X509Certificate cert=(X509Certificate)(path.getCertificates().get(0));
			if(getServerCert().equals(cert)){
				return true;
			}
		}catch(Exception e){
			Log.logException("Could not check certificate vs. server cert",e,logger);
		}
		return false;
	}


//	/**
//	 * checks whether the given client has the "trusted agent" role
//	 */
//	public static boolean isTrustedAgent(Client c){
//		try{
//			return IAttributeSource.ROLE_TRUSTED_AGENT.equals(c.getRole().getName());
//		}catch(Exception e){
//			Log.logException("Could not check whether client is trusted agent.",e,logger);
//		}
//		return false;
//	}

	/**
	 * checks whether the given client has the "admin" role
	 */
	public static boolean isAdmin(Client c){
		try{
			return IAttributeSource.ROLE_ADMIN.equals(c.getRole().getName());
		}catch(Exception e){
			Log.logException("Could not check whether client is admin.",e,logger);
		}
		return false;
	}

	public static X509Certificate getPeerCertificate(String url, ISecurityProperties security){
		return getPeerCertificate(url, security, 0);
	}


	/**
	 * helper to get the certificate on the other side of a SSL connection to "url"
	 * @param url - the URL to connect to
	 * @param security - the {@link IUASSecurityProperties} to use
	 * @param timeout - the time to wait for a connection before giving up
	 */
	public static X509Certificate getPeerCertificate(String url, ISecurityProperties security, int timeout){
		SSLSocket s=null;
		try{
			URL u=new URL(url);
			int port=u.getPort()!=-1?u.getPort():u.getDefaultPort();
			s=(SSLSocket)new AuthSSLProtocolSocketFactory(security).createSocket(u.getHost(), port);
			X509Certificate peer=(X509Certificate)s.getSession().getPeerCertificates()[0];
			if(logger.isDebugEnabled()){
				try{
					logger.debug("Got peer cert of <"+url+">,\nName: "+peer.getSubjectX500Principal().getName()+"\n" +
							"Issued by: "+peer.getIssuerX500Principal().getName());
				}
				catch(Exception e){
					Log.logException("Problem with certificate for <"+url+">",e,logger);
					return null;
				}
			}
			return peer;
		}
		catch(Exception e){
			logger.debug("Can't get certificate for <"+url+">",e);
		}
		finally{
			try{
				if(s!=null)s.close();
			}catch(IOException ignored){}
		}
		return null;
	}

	public static class NullAuthoriser implements IAttributeSource{
		public NullAuthoriser(){}
		public String getStatusDescription(){ return "(No attribute source configured)"; }
		public void init(String name){}
		public String getName() { return "NULL source"; }
		@Override
		public SubjectAttributesHolder getAttributes(SecurityTokens tokens,
				SubjectAttributesHolder otherAuthoriserInfo) throws IOException		{
			return new SubjectAttributesHolder();
		}
		@Override
		public String[] getAcceptedVOs() { return null; }
	}

	public static boolean isAccessControlEnabled() {
		if(isAccessControlEnabled==null){
			isAccessControlEnabled=Boolean.parseBoolean(DSRServer.getProperty(ISecurityProperties.REGISTRY_CHECKACCESS,"false"));
		}
		return isAccessControlEnabled;
	}

	public static void setAccessControlEnabled(boolean isAccessControlEnabled) {
		SecurityManager.isAccessControlEnabled = isAccessControlEnabled;
		DSRServer.getConfiguration().setProperty(ISecurityProperties.REGISTRY_CHECKACCESS,"true");
	}

//	public static synchronized boolean isProxyModeEnabled() {
//		if(isProxyModeEnabled==null){
//			isProxyModeEnabled=Boolean.parseBoolean(Kernel.getKernel().getProperty(ISecurityProperties.UAS_AUTHORISER_PROXY_SUPPORT,"false"));
//		}
//		return isProxyModeEnabled;
//	}

	/**
	 * get a human-readable description of the current status of the configured authorisers.
	 */
	public static synchronized String getAuthoriserConnectionStatus()throws Exception{
		if(attributeSource==null){
			createAttributeSource();
		}
		return attributeSource.getStatusDescription();
	}

	/**
	 * get the attribute source
	 * @throws Exception
	 */
	public static synchronized IAttributeSource getAtributeSource()throws Exception{
		if(attributeSource==null){
			createAttributeSource();
		}
		return attributeSource;
	}

	//pattern for checking if a DN represents a proxy cert
	final protected static String proxyRE= "(CN=([\\d]+))|(CN=proxy)";
	final public static Pattern pattern = Pattern.compile(proxyRE,Pattern.CASE_INSENSITIVE); 

	public static boolean isProxyDN(String dn){
		return pattern.matcher(dn).find();
	}

	/**
	 * add SOAP actions to the special list requiring signed messages
	 * @param actions
	 */
	public static void addSOAPActionsRequiringSignatures(String ... actions){
		actionsRequiringSignatures.addAll(Arrays.asList(actions));
	}
	
	public static boolean needSignature(String soapAction){
		if(soapAction==null)return false;
		boolean b=actionsRequiringSignatures.contains(soapAction);
		logger.debug("Check <"+soapAction+"> = "+b);
		return b;
	}
}
