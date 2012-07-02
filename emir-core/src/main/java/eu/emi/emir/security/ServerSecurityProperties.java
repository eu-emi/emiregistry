/**
 * 
 */
package eu.emi.emir.security;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.pdp.RegistryPDP;
import eu.emi.emir.pdp.local.AcceptingPDP;
import eu.emi.emir.security.util.AttributeSourcesChain;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.security.canl.CredentialProperties;
import eu.unicore.security.canl.IAuthnAndTrustConfiguration;
import eu.unicore.security.canl.TruststoreProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;

/**
 * EMIR security configuration implementation using a {@link Properties} source.
 * 
 * @author a.memon
 * 
 */
public class ServerSecurityProperties extends
		DefaultServerSecurityConfiguration {
	private static final Logger logger = Log.getLogger(Log.EMIR_SECURITY,
			ServerSecurityProperties.class);

	@DocumentationReferencePrefix
	public static final String PREFIX = ServerProperties.PREFIX + "security.";

	/**
	 * property defining whether SSL is enabled
	 */
	public static final String PROP_SSL_ENABLED = "sslEnabled";

	/**
	 * do we check access?
	 */
	public static final String PROP_CHECKACCESS = "accesscontrol";

	/**
	 * access control PDP class name (implementing
	 * <code>eu.emir.security.RegistryPDP</code>)
	 */
	public static final String PROP_CHECKACCESS_PDP = "accesscontrol.pdp";

	/**
	 * configuration file for the PDP
	 */
	public static final String PROP_CHECKACCESS_PDPCONFIG = "accesscontrol.pdpConfig";

	/**
	 * base for AIP property names
	 */
	public static final String PROP_AIP_PREFIX = "attributes";

	/**
	 * attribute sources order property
	 */
	public static final String PROP_AIP_ORDER = PROP_AIP_PREFIX + ".order";

	/**
	 * attribute sources combining policy
	 */
	private static final String PROP_AIP_COMBINING_POLICY = PROP_AIP_PREFIX
			+ ".combiningPolicy";

	/**
	 * access control PDP class name (implementing
	 * <code>de.fzj.uas.security.XacmlPDP</code>)
	 */
	public static final String PROP_CHECKACCESS_ACL = "accesscontrol.acl";

	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();

	static {
		META.put(PROP_SSL_ENABLED, new PropertyMD("false")
				.setDescription("Controls whether secure SSL mode is enabled.").setBoolean());
		META.put(
				PROP_CHECKACCESS,
				new PropertyMD("false")
						.setDescription("Controls whether access checking (authorisation) is enabled."));
		META.put(
				PROP_CHECKACCESS_PDP,
				new PropertyMD("eu.emi.emir.pdp.local.LocalHerasafPDP")
						.setDescription("Controls which Policy Decision Point (PDP, the authorisation engine) should be used."));
		META.put(PROP_CHECKACCESS_PDPCONFIG, new PropertyMD().setPath()
				.setDescription("Path of the PDP configuration file"));
		META.put(PROP_AIP_ORDER, new PropertyMD("FILE")
				.setDescription("Attribute sources in invocation order."));
		META.put(
				PROP_AIP_COMBINING_POLICY,
				new PropertyMD(AttributeSourcesChain.MergeLastOverrides.NAME)
						.setDescription("What algorithm should be used for combining the attributes from "
								+ "multiple attribute sources (if more then one is defined)."));
		META.put(
				PROP_CHECKACCESS_ACL,
				new PropertyMD().setPath()
						.setDescription("Path of the acl file. Enabling this would initiate ACL file based authorisation instead of XACML"));

	}

	private PropertiesHelper properties;
	
	private Properties source;

	/**
	 * @throws Exception 
	 * 
	 */
	public ServerSecurityProperties(Properties p) throws Exception {
		this(p, null);
	}

	/**
	 * @param p
	 * @param authAndTrust
	 * @throws Exception 
	 */
	public ServerSecurityProperties(Properties source,
			IAuthnAndTrustConfiguration authAndTrust) throws Exception {

		//if url starts with https, the access control will be enabled
		boolean isHttps = source.getProperty(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS).startsWith("https");
		
		//have to do this again due to possible bug in property logging mechanisms
		if (isHttps) 
			setSslEnabled(true);
		this.source = source;
		properties = new PropertiesHelper(PREFIX, source, META, logger);
		
		
		
		if (isSslEnabled()) {
			if (properties.getValue(PROP_CHECKACCESS_ACL) != null) {
				setAclAccessControlEnabled(true);	
			} 
			
			if (properties.getValue(PROP_CHECKACCESS_PDPCONFIG) != null) {
				setXACMLAccessControlEnabled(true);
			} 
			
			if (properties.getValue(PROP_CHECKACCESS_ACL) != null && properties.getValue(PROP_CHECKACCESS_PDPCONFIG) != null){
				//if both are provided then use the acl version
				setAclAccessControlEnabled(true);
				setXACMLAccessControlEnabled(false);
			} else if (properties.getValue(PROP_CHECKACCESS_ACL) == null && properties.getValue(PROP_CHECKACCESS_PDPCONFIG) == null) {
				//if both are null
				Log.logException("Access control properties must be provided in the configuration file", new ConfigurationException(), logger);
			}
				
		}
		
		boolean credNeeded = isSslEnabled();
		boolean trustNeeded = isSslEnabled() || isXACMLAccessControlEnabled() || isACLAccessControlEnabled();

		if (authAndTrust == null) {
			authAndTrust = new AuthnAndTrustProperties(source, PREFIX
					+ TruststoreProperties.DEFAULT_PREFIX, PREFIX
					+ CredentialProperties.DEFAULT_PREFIX, !trustNeeded,
					!credNeeded);
		}
		if (isSslEnabled()) {
			setValidator(authAndTrust.getValidator());
			setCredential(authAndTrust.getCredential());
			
			if (isXACMLAccessControlEnabled()) {
				setAip(createAttributeSource(source));
				setPdp(createPDP(properties));
			}
			
			if (isACLAccessControlEnabled()) {
				setAclConfigurationFile(properties.getValue(PROP_CHECKACCESS_ACL));
			}
		}
		
	}
	
	public Properties getRawProperties(){
		return source; 
	}

	/**
	 * @param properties 
	 * @return {@link RegistryPDP}
	 */
	private RegistryPDP createPDP(PropertiesHelper properties) {
		if (!isXACMLAccessControlEnabled())
			return new AcceptingPDP();
			
		if (properties.isSet(PROP_CHECKACCESS_PDPCONFIG)) {
			String conf=properties.getValue(PROP_CHECKACCESS_PDPCONFIG);
			setPdpConfigFile(conf);
		}
		
		String pdpClass=properties.getValue(PROP_CHECKACCESS_PDP);
		Class<?> pdpClazz; 
		try{
			pdpClazz = Class.forName(pdpClass);
		}catch(ClassNotFoundException cfe){
			throw new ConfigurationException("Cannot load PDP class <"+pdpClass+">: ", cfe);
		}
		
		try {
			Constructor<?> constructor = pdpClazz.getConstructor(String.class);
			logger.info("Using PDP class <"+pdpClass+">");
			RegistryPDP pdp = (RegistryPDP)constructor.newInstance(getPdpConfigurationFile());
			return pdp;
		}catch(Exception e) {
			throw new ConfigurationException("Can't create a PDP.", e);
		}
	}

	/**
	 * @param source
	 * @return
	 * @throws Exception 
	 */
	private IAttributeSource createAttributeSource(Properties raw) throws Exception {
		String order = properties.getValue(PROP_AIP_ORDER);
		if (order == null) {
			logger.info("No attribute source is defined in the configuration, "
					+ "users won't have any authorisation attributes assigned");
			return new SecurityManager.NullAuthoriser();
		}

		logger.debug("Creating main attribute sources chain");
		AttributeSourcesChain ret = new AttributeSourcesChain();
		ret.setCombiningPolicy(properties.getValue(PROP_AIP_COMBINING_POLICY));
		ret.setOrder(order);
		ret.setProperties(raw);
		ret.init(null);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see eu.emi.emir.security.DefaultServerSecurityConfiguration#isAccessControlEnabled(java.lang.String)
	 */
	@Override
	public boolean isAccessControlEnabled(String service) {
		return properties.getSubkeyBooleanValue(PROP_CHECKACCESS, service);
	}

}
