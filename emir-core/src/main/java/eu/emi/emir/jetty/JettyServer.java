/**
 * 
 */
package eu.emi.emir.jetty;

import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.jetty.JettyServerBase;

/**
 * Jetty server class
 * 
 * @author a.memon
 * 
 */

public class JettyServer extends JettyServerBase{
	private static final Logger logger = Log.getLogger(Log.HTTP_SERVER, JettyServer.class);
	Map<String, String> jerseyParams = null;
	
//	public JettyServer(URL[] listenUrls, AuthnAndTrustProperties securityCfg, 
//			EMIRJettyProperties jettyCfg, Map<String, String> map) throws Exception {
//		super(listenUrls, securityCfg, jettyCfg, EMIRJettyLogger.class);
//		this.jerseyParams = map;
//		initServer();
//	}
	
	public JettyServer(URL listenUrl, ServerSecurityProperties securityCfg, 
			EMIRJettyProperties jettyCfg, Map<String, String> map) throws Exception {
		super(listenUrl, securityCfg, jettyCfg);
		this.jerseyParams = map;
		initServer();
	}	
	

	public JettyServer(URL[] listenUrl, ServerSecurityProperties securityCfg, 
			EMIRJettyProperties jettyCfg, Map<String, String> map) throws Exception {
		super(listenUrl, securityCfg, jettyCfg, EMIRJettyLogger.class);
		this.jerseyParams = map;
		initServer();
	}	
	

	/* (non-Javadoc)
	 * @see eu.unicore.util.jetty.JettyServerBase#createRootHandler()
	 */
	@Override
	protected Handler createRootHandler() throws ConfigurationException {
		logger.info("Adding Jersey Application");
		ServletContextHandler root = new ServletContextHandler(getServer(), "/", ServletContextHandler.SESSIONS);
		ServletHolder sh = new ServletHolder(ServletContainer.class);
		sh.setInitParameters(jerseyParams);
		root.addServlet(sh, "/*");
		return root;
	}
	
}

//public class JettyServer {
//	Logger logger = Log.getLogger(Log.EMIR_CORE, JettyServer.class);
//
//	Server server;
//	boolean started = false;
//	@SuppressWarnings("rawtypes")
//	private Class appClass;
//	private String hostName;
//	private Integer portNumber;
//	private String scheme;
//	private AbstractConnector connector;
//	private Configuration conf;

	/**
	 * @
	 */

	/**
	 * @param class1
	 * @param c
	 */
//	public JettyServer(@SuppressWarnings("rawtypes") Class clazz,
//			Configuration c) {
//		if (clazz == null) {
//			throw new IllegalArgumentException("Application ");
//		}
//		appClass = clazz;
//		this.conf = c;
//		init();
//	}
//
//	protected static final HashMap<String, Integer> defaults = new HashMap<String, Integer>();
//
//	private static final String MAX_THREADS = null;
//
//	private static final String MIN_THREADS = null;
//
//	private static final String LOW_THREADS = null;
//
//	private static final String MAX_IDLE_TIME = null;
//
//	private static final String LOW_RESOURCE_MAX_IDLE_TIME = null;
//
//	private static final String SO_LINGER_TIME = null;
//
//	static {
//		defaults.put(MAX_THREADS, 255);
//		defaults.put(MIN_THREADS, 1);
//		defaults.put(LOW_THREADS, 50);
//		defaults.put(MAX_IDLE_TIME, 30000);
//		defaults.put(LOW_RESOURCE_MAX_IDLE_TIME, 5000);
//		defaults.put(SO_LINGER_TIME, -1);
//	}
//
//	public boolean isStarted() {
//		return started;
//	}
//
//	public void init() {
//
//		scheme = conf.getProperty(ServerConstants.REGISTRY_SCHEME);
//		hostName = conf.getProperty(ServerConstants.REGISTRY_HOSTNAME, "localhost");
//		portNumber = Integer.valueOf(conf
//				.getProperty(ServerConstants.REGISTRY_PORT, "0"));
//
//		if ((scheme.equals("http") || (scheme == null))) {
//			connector = createConnector();
//		} else if (scheme.equals("https")) {
//			connector = createSecureConnector();
//		} else {
//			try {
//				throw new Exception();
//			} catch (Exception e) {
//				Log.logException(
//						"Unknown scheme should either be http or https", e);
//			}
//		}
//
//		// initialising the server
//		ServletHolder sh = new ServletHolder(ServletContainer.class);
//
//		setInitParams(sh);
//
//		server = new Server();
//
//		ServletContextHandler context = new ServletContextHandler(
//				ServletContextHandler.SESSIONS);
//		context.setContextPath("/");
//		context.addServlet(sh, "/*");
//
//		server.setHandler(context);
//
//		QueuedThreadPool pool = new QueuedThreadPool();
//		pool.setMaxThreads(500);
//		pool.setMinThreads(50);
//
//		server.setThreadPool(pool);
//
//		server.setConnectors(new Connector[] { connector });
//
//	}
//
//	/**
//	 * @param sh
//	 */
//	private void setInitParams(ServletHolder sh) {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("javax.ws.rs.Application", appClass.getCanonicalName());
//		addFilterClasses(map);
//		sh.setInitParameters(map);
//	}
//
//	/**
//	 * Adding request and response filters to the jersey servlet
//	 * */
//	private void addFilterClasses(Map<String, String> map) {
//		String reqClasses = DSRServer
//				.getProperty(ServerConstants.REGISTRY_FILTERS_REQUEST);
//		if (reqClasses != null)
//			map.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//					reqClasses);
//
//		String resClasses = DSRServer
//				.getProperty(ServerConstants.REGISTRY_FILTERS_RESPONSE);
//		if (resClasses != null)
//			map.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
//					resClasses);
//	}
//
//	/**
//	 * 
//	 */
//	private AbstractConnector createSecureConnector() {
//		SslSelectChannelConnector ssl_connector = null;
//		// SslSocketConnector ssl_connector = null;
//		try {
//			ssl_connector = new SslSelectChannelConnector();
//			// ssl_connector = new SslSocketConnector();
//			ssl_connector.setHost(conf
//					.getProperty(ServerConstants.REGISTRY_HOSTNAME));
//			ssl_connector.setPort(Integer.valueOf(conf
//					.getProperty(ServerConstants.REGISTRY_PORT)));
//
//			SslContextFactory cf = ssl_connector.getSslContextFactory();
//
//			cf.setKeyStore(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYSTORE));// "src/main/certs/demo-server.p12"
//			cf.setKeyStoreType(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYTYPE));// "pkcs12";
//			cf.setKeyManagerPassword(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYPASS));// ("emi");
//			cf.setKeyStorePassword(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYPASS));// ("emi");
//
//			cf.setTrustStore(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE));// ("src/main/certs/demo-server.jks");
//			cf.setTrustStorePassword(conf
//					.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTPASS));// ("emi");
//			cf.setTrustStoreType(conf.getProperty(
//					ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks"));// ("jks");
//
//			cf.setWantClientAuth(conf.getBooleanProperty(
//					ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "false")); // true
//			cf.setNeedClientAuth(conf.getBooleanProperty(
//					ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "false"));
//		} catch (Exception e) {
//			Log.logException("Error creating secure connectore", e, logger);
//		}
//
//		configureConnector(ssl_connector);
//		return ssl_connector;
//	}
//
//	/**
//	 * @return
//	 * 
//	 */
//	private AbstractConnector createConnector() {
//		SelectChannelConnector plain_connector = new SelectChannelConnector();
//		// SocketConnector plain_connector = new SocketConnector();
//		plain_connector.setHost(hostName);
//		plain_connector.setPort(portNumber);
//
//		plain_connector.setMaxIdleTime(Integer.valueOf(conf
//				.getProperty(ServerConstants.JETTY_MAXIDLETIME)));
//
//		return plain_connector;
//	}
//
//	protected void configureConnector(AbstractConnector http) {
//		http.setMaxIdleTime(Integer.valueOf(conf
//				.getProperty(ServerConstants.JETTY_MAXIDLETIME)));
//		int soLinger = Integer.valueOf(conf
//				.getProperty(ServerConstants.JETTY_MAXIDLETIME));
//		if (soLinger > 0)
//			http.setSoLingerTime(soLinger);
//	}
//
//	public void start() {
//		try {
//			server.start();
//			started = server.isStarted();
//		} catch (Exception e) {
//			Log.logException("cannot start the server", e);
//		}
//	}
//
//	public void stop() {
//		try {
//			server.stop();
//			started = !server.isStopped();
//		} catch (Exception e) {
//			Log.logException("cannot to stop the server", e);
//		}
//	}
//
//	public Server getServer() {
//		return server;
//	}
//
//}