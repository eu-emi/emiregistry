/**
 * 
 */
package eu.emi.emir.core;

/**
 * @author a.memon
 * 
 */
public class ServerConstants {
	public static final String CLIENT_AUTHN = "registry.security.clientauthentication";

	public static String REGISTRY_GLOBAL_ENABLE = "registry.global.enable";
	public static String REGISTRY_GLOBAL_SPARSITY = "registry.global.sparsity";
	public static String REGISTRY_GLOBAL_RETRY = "registry.global.retry";
	public static String REGISTRY_GLOBAL_ETVALID = "registry.global.etvalid";
	public static String REGISTRY_GLOBAL_ETREMOVE = "registry.global.etremove";
	public static String REGISTRY_GLOBAL_SOFTSTATE_DELAY = "registry.global.softstate.delay";
	public static String REGISTRY_GLOBAL_PROVIDERLIST = "registry.global.providerlist";
	public static String REGISTRY_SCHEME = "registry.scheme";
	public static String REGISTRY_HOSTNAME = "registry.hostname";
	public static String REGISTRY_PORT = "registry.port";
	public static String REGISTRY_PARENT_URL = "registry.parent.url";
	public static String REGISTRY_FILTERS_REQUEST = "registry.filters.request";
	public static String REGISTRY_FILTERS_RESPONSE = "registry.filters.response";
	public static String REGISTRY_FILTERS_INPUTFILEPATH = "registry.filters.input";
	public static String REGISTRY_FILTERS_OUTPUTFILEPATH = "registry.filters.output";	
	public static String REGISTRY_EXPIRY_MAXIMUM = "registry.expiry.maximum";
	public static String REGISTRY_EXPIRY_DEFAULT = "registry.expiry.default";
	public static String REGISTRY_MAX_REGISTRATIONS = "registry.max.registration";

	// Log4J
	public static String LOGGER_CONF_PATH = "logger.conf.path";

	// MongoDB configuration
	public static String MONGODB_PORT = "mongodb.port";
	public static String MONGODB_HOSTNAME = "mongodb.hostname";
	public static String MONGODB_DB_NAME = "mongodb.dbname";
	public static String MONGODB_COLLECTION_NAME = "mongodb.colname";
	public static String MONGODB_USERNAME = "mongodb.username";
	public static String MONGODB_PASSWORD = "mongodb.password";
	// create new on startup, drop the old one - type: boolean
	public static final String MONGODB_COL_CREATE = "mongodb.create";

	public static final String CLIENT = "client";

	// Jetty
	public static String JETTY_MAXIDLETIME = "jetty.maxIdleTime";
	public static String JETTY_LOW_RESOURCE_MAXIDLETIME = "jetty.lowResourceMaxIdleTime";
	public static String JETTY_MAXTHREADS = "jetty.maxThreads";
	public static String JETTY_LOWTHREADS = "jetty.lowThreads";

	// H2 database
	public static String H2_DBFILE_PATH = "h2.dbpath";
}
