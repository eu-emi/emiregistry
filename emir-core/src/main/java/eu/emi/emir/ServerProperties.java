/**
 * 
 */
package eu.emi.emir;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.jetty.EMIRJettyProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.DocumentationReferenceMeta;
import eu.unicore.util.configuration.DocumentationReferencePrefix;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import eu.unicore.util.httpclient.ClientProperties;

/**
 * @author a.memon
 * 
 */
public class ServerProperties extends PropertiesHelper {
	private static final Logger logger = Log.getLogger(Log.EMIR_SECURITY,
			ServerProperties.class);

	@DocumentationReferencePrefix
	public static final String PREFIX = "emir.";

	/**
	 * 
	 * This is the EMIR server URL, where clients can contact. The URL should
	 * not end with slash "/"
	 * 
	 */
	public static final String PROP_ADDRESS = "address";
	
	
	/**
	 * 
	 * This is the EMIR server URL, where clients can contact with public resources. The URL should
	 * not end with slash "/"
	 * 
	 */
	public static final String PROP_ANONYMOUS_PORT = "address.anonymousPort";

	// GSR Properties //
	/**
	 * Indicating whether the registry node is global. If set to true then it
	 * will replicate among peer global registries while ignoring the <b>
	 * emir.url </b> property. This implies the current instance will not have
	 * parent.
	 * */
	public static final String PROP_GLOBAL_ENABLE = "global.enable";

	public static final String PROP_GLOBAL_PROVIDERLIST = "global.providerList";

	public static final String PROP_GLOBAL_SPARSITY = "global.sparsity";

	public static final String PROP_GLOBAL_RETRY = "global.retry";

	public static final String PROP_GLOBAL_ETVALID = "global.etValid";

	public static final String PROP_GLOBAL_ETREMOVE = "global.etRemove";

	public static final String PROP_GLOBAL_SOFTSTATE_DELAY = "global.softStateDelay";

	// DSR Properties //
	
	public static final String PROP_PARENT_ADDRESS = "parentAddress";
	
	// MongoDB Properties //
	
	public static final String PROP_MONGODB_HOSTNAME = "mongodb.hostName";
	
	public static final String PROP_MONGODB_PORT = "mongodb.port";
	
	public static final String PROP_MONGODB_COLLECTION_NAME = "mongodb.collectionName";
	
	public static final String PROP_MONGODB_DB_NAME = "mongodb.dbName";
	
	public static final String PROP_MONGODB_USERNAME = "mongodb.userName";
	
	public static final String PROP_MONGODB_PASSWORD = "mongodb.password";
	
	// Service Record Management //
	
	public static final String PROP_RECORD_EXPIRY_MAXIMUM = "record.expiryMaximum";
	
	public static final String PROP_RECORD_EXPIRY_DEFAULT = "record.expiryDefault";
	
	public static final String PROP_RECORD_BLOCKLIST_INCOMING = "record.blockList.incoming";
	
	public static final String PROP_RECORD_BLOCKLIST_OUTGOING = "record.blockList.outgoing";
	
	public static final String PROP_RECORD_MAXIMUM = "record.maximum";
	
	public static final String PROP_RECORD_CHECKING_MODE = "record.attributeCheckingMode";
	
	// Advanced: HTTP Request interceptors  //
	
	public static final String PROP_REQUEST_INTERCEPTORS = "interceptors.request";
	
	public static final String PROP_RESPONSE_INTERCEPTORS = "interceptors.response";
	
	// Logger file configuration //
	
	public static final String PROP_LOGGER_CONFIGPATH = "logger.configPath";
	
	// Path for the temporary database //
	public static String PROP_H2_DBFILE_PATH = "h2.dbpath";
	
	enum ATTRIBUTE_CHECKING_MODE{
		strict, relaxed;
	};
	
	@DocumentationReferenceMeta
	public final static Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();
	static
	{
		META.put(PROP_ADDRESS, new PropertyMD("http://localhost:0").setDescription(""));
		META.put(PROP_ANONYMOUS_PORT, new PropertyMD("").setDescription(""));
		META.put(PROP_GLOBAL_ENABLE, new PropertyMD("false").setBoolean().setDescription(""));
		META.put(PROP_GLOBAL_PROVIDERLIST, new PropertyMD().setDescription(""));
		META.put(PROP_GLOBAL_SPARSITY, new PropertyMD("2").setInt().setBounds(2, 100).setDescription(""));
		META.put(PROP_GLOBAL_RETRY, new PropertyMD("5").setDescription(""));
		META.put(PROP_GLOBAL_ETVALID, new PropertyMD("12").setDescription(""));
		META.put(PROP_GLOBAL_ETREMOVE, new PropertyMD("24").setDescription(""));
		META.put(PROP_GLOBAL_SOFTSTATE_DELAY, new PropertyMD("2").setDescription(""));
		META.put(PROP_PARENT_ADDRESS, new PropertyMD().setDescription(""));
		META.put(PROP_MONGODB_HOSTNAME, new PropertyMD("localhost").setDescription("MongoDB hostname"));
		META.put(PROP_MONGODB_PORT, new PropertyMD("27017").setInt().setDescription("MongoDB port number"));
		META.put(PROP_MONGODB_DB_NAME, new PropertyMD("emiregistry").setDescription("MongoDB database name"));
		META.put(PROP_MONGODB_COLLECTION_NAME, new PropertyMD("services").setDescription("MongoDB collection name"));
		META.put(PROP_MONGODB_USERNAME, new PropertyMD().setDescription(""));
		META.put(PROP_MONGODB_PASSWORD, new PropertyMD().setSecret().setDescription(""));
		META.put(PROP_RECORD_EXPIRY_DEFAULT, new PropertyMD("365").setDescription(""));
		META.put(PROP_RECORD_EXPIRY_MAXIMUM, new PropertyMD("3650").setDescription(""));
		META.put(PROP_RECORD_MAXIMUM, new PropertyMD("100").setDescription("Maximum allowed number of Service Endpoints in a registration request"));
		META.put(PROP_RECORD_BLOCKLIST_INCOMING, new PropertyMD().setDescription(""));
		META.put(PROP_RECORD_BLOCKLIST_OUTGOING, new PropertyMD().setDescription(""));
		META.put(PROP_RECORD_CHECKING_MODE, new PropertyMD("strict").setDescription("There are two possible modes: 'strict' or 'flexible'. If set to 'strict' the emir server will check mandatory attributes in the record being updated or registered. If set to 'flexible' only "+ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.toString()+" will be checked."));
		META.put(PROP_REQUEST_INTERCEPTORS, new PropertyMD().setDescription(""));
		META.put(PROP_RESPONSE_INTERCEPTORS, new PropertyMD().setDescription(""));
		META.put(PROP_LOGGER_CONFIGPATH, new PropertyMD().setPath().setDescription(""));
		META.put(PROP_H2_DBFILE_PATH, new PropertyMD("/var/lib/emi/emir/data/Emiregistry").setDescription("Path for the temporary database"));
		
		META.put(ServerSecurityProperties.SECURITY_PREFIX, new PropertyMD().setCanHaveSubkeys().setDescription("").setHidden());
		META.put(ClientProperties.DEFAULT_PREFIX, new PropertyMD().setCanHaveSubkeys().setDescription("").setHidden());
		META.put(EMIRJettyProperties.JETTY_PREFIX, new PropertyMD().setCanHaveSubkeys().setDescription("").setHidden());
		
	}
	
	public Boolean isGlobalEnabled(){
		return getBooleanValue(PROP_GLOBAL_ENABLE);
	}
	
	public Boolean isAnonymousAccessEnabled(){
		if (getValue(PROP_ANONYMOUS_PORT).isEmpty() || getValue(PROP_ANONYMOUS_PORT) == null) {
			return false;
		}
		return true;
	}
	
	public String getAnonymousPortNumber(){
		String port = getValue(PROP_ANONYMOUS_PORT);
		if (port.isEmpty() || port == null) {
			return "UNKNOWN";
		}
		return port;
	}
	
	public String parentAddress(){
		return getValue(PROP_PARENT_ADDRESS);
	}
	
	public String getAttributeCheckingMode(){
		return getValue(PROP_RECORD_CHECKING_MODE);
	}
	
	public Long getMaxRecordInARequest(){
		return getLongValue(PROP_RECORD_MAXIMUM);
	}
	
	/**
	 * @param prefix
	 * @param properties
	 * @param propertiesMD
	 * @param log
	 * @throws ConfigurationException
	 */
	public ServerProperties(Properties props, boolean sslMode)
			throws ConfigurationException {
		super(PREFIX, props, META, logger);		
	}

}
