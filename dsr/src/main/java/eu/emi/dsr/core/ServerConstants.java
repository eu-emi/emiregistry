/**
 * 
 */
package eu.emi.dsr.core;

/**
 * @author a.memon
 * 
 */
public class ServerConstants {
	public static final String CLIENT_AUTHN = "registry.security.clientauthentication";
	public static String REGISTRY_SCHEME = "registry.scheme";
	public static String REGISTRY_HOSTNAME = "registry.hostname";
	public static String REGISTRY_PORT = "registry.port";
	public static String REGISTRY_SSL_ENABLED = "registry.ssl.enabled";
	public static String REGISTRY_ACCESSCONTROL = "registry.security.accesscontrol";
	public static String REGISTRY_ATTRIBUTESOURCE_TYPE = "registry.security.attributesource.location";
	public static String REGISTRY_ATTRIBUTESOURCE_LOCATION = "registry.security.attributesource.location";
	
	// Log4J
	public static String LOGGER_CONF_PATH = "logger.conf.path";

	// MongoDB configuration
	public static String MONGODB_PORT = "mongodb.port";
	public static String MONGODB_HOSTNAME = "mongodb.hostname";
	public static String MONGODB_DB_NAME = "mongodb.dbname";
	public static String MONGODB_COLLECTION_NAME = "mongodb.colname";
	public static String MONGODB_COLLECTION_USERNAME = "mongodb.username";
	public static String MONGODB_COLLECTION_PASSWORD = "mongodb.password";

	// Security Properties
	// key store
	public static String KEYSTORE_PATH = "keystore.path";
	public static String KEYSTORE_ALIAS = "keystore.alias";
	public static String KEYSTORE_TYPE = "keystore.type";
	public static String KEYSTORE_PASSWORD = "keystore.password";
	

	// trust store
	public static String TRUSTSTORE_PATH = "truststore.path";
	public static String TRUSTSTORE_PASSWORD = "truststore.password";
	public static String TRUSTSTORE_TYPE = "truststore.type";
	
	// Jetty
	public static String JETTY_MAXIDLETIME = "jetty.maxIdleTime";
	public static String JETTY_LOW_RESOURCE_MAXIDLETIME = "jetty.lowResourceMaxIdleTime";
	public static String JETTY_MAXTHREADS = "jetty.maxThreads";
	public static String JETTY_LOWTHREADS = "jetty.lowThreads";

}
