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
	public static String REGISTRY_PARENT_URL = "registry.parent.url";
	public static String REGISTRY_FILTERS_REQUEST = "registry.filters.request";
	public static String REGISTRY_FILTERS_RESPONSE = "registry.filters.response";
	public static String REGISTRY_EXPIRY_MAXIMUM = "registry.expiry.maximum";
	public static String REGISTRY_EXPIRY_DEFAULT = "registry.expiry.default";

	// Log4J
	public static String LOGGER_CONF_PATH = "logger.conf.path";

	// MongoDB configuration
	public static String MONGODB_PORT = "mongodb.port";
	public static String MONGODB_HOSTNAME = "mongodb.hostname";
	public static String MONGODB_DB_NAME = "mongodb.dbname";
	public static String MONGODB_COLLECTION_NAME = "mongodb.colname";
	public static String MONGODB_COLLECTION_USERNAME = "mongodb.username";
	public static String MONGODB_COLLECTION_PASSWORD = "mongodb.password";
	// create new on startup, drop the old one - type: boolean
	public static final String MONGODB_COL_CREATE = "mongodb.create";

	public static final String CLIENT = "client";

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

	// H2 database
	public static String H2_DBFILE_PATH = "h2.dbpath";
	public static String H2_USERNAME = "h2.username";
	public static String H2_PASSWORD = "h2.password";

}
