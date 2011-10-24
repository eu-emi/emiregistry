/**
 * 
 */
package eu.emi.dsr.jetty;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import eu.emi.client.security.ISecurityProperties;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class JettyServer {
	Logger logger = Log.getLogger(Log.DSR, JettyServer.class);

	Server server;
	boolean started = false;
	@SuppressWarnings("rawtypes")
	private Class appClass;
	private String hostName;
	private Integer portNumber;
	private String scheme;
	private AbstractConnector connector;
	private Configuration conf;

	/**
	 * @
	 */

	/**
	 * @param class1
	 * @param c
	 */
	public JettyServer(@SuppressWarnings("rawtypes") Class clazz,
			Configuration c) {
		if (clazz == null) {
			throw new IllegalArgumentException("Application ");
		}
		appClass = clazz;
		this.conf = c;
		init();
	}

	protected static final HashMap<String, Integer> defaults = new HashMap<String, Integer>();

	private static final String MAX_THREADS = null;

	private static final String MIN_THREADS = null;

	private static final String LOW_THREADS = null;

	private static final String MAX_IDLE_TIME = null;

	private static final String LOW_RESOURCE_MAX_IDLE_TIME = null;

	private static final String SO_LINGER_TIME = null;

	static {
		defaults.put(MAX_THREADS, 255);
		defaults.put(MIN_THREADS, 1);
		defaults.put(LOW_THREADS, 50);
		defaults.put(MAX_IDLE_TIME, 30000);
		defaults.put(LOW_RESOURCE_MAX_IDLE_TIME, 5000);
		defaults.put(SO_LINGER_TIME, -1);
	}

	public boolean isStarted() {
		return started;
	}

	public void init() {

		scheme = conf.getProperty(ServerConstants.REGISTRY_SCHEME);
		hostName = conf.getProperty(ServerConstants.REGISTRY_HOSTNAME);
		portNumber = Integer.valueOf(conf
				.getProperty(ServerConstants.REGISTRY_PORT));

		if ((scheme.equals("http") || (scheme == null))) {
			connector = createConnector();
		} else if (scheme.equals("https")) {
			connector = createSecureConnector();
		} else {
			try {
				throw new Exception();
			} catch (Exception e) {
				Log.logException(
						"Unknown scheme should either be http or https", e);
			}
		}

		// initialising the server
		ServletHolder sh = new ServletHolder(ServletContainer.class);

		setInitParams(sh);

		server = new Server();

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(sh, "/*");

		server.setHandler(context);
		
		QueuedThreadPool pool = new QueuedThreadPool();
		pool.setMaxThreads(500);
		pool.setMinThreads(50);
		
		server.setThreadPool(pool);
				
		server.setConnectors(new Connector[] { connector });

	}

	/**
	 * @param sh
	 */
	private void setInitParams(ServletHolder sh) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("javax.ws.rs.Application", appClass.getCanonicalName());
		addFilterClasses(map);
		sh.setInitParameters(map);
	}

	/**
	 * Adding request and response filters to the jersey servlet
	 * */
	private void addFilterClasses(Map<String, String> map) {
		String reqClasses = DSRServer
				.getProperty(ServerConstants.REGISTRY_FILTERS_REQUEST);
		map.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, reqClasses);
		String resClasses = DSRServer
				.getProperty(ServerConstants.REGISTRY_FILTERS_RESPONSE);
		map.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, resClasses);
	}

	/**
	 * 
	 */
	private AbstractConnector createSecureConnector() {
		SslSelectChannelConnector ssl_connector = null;
		try {
			ssl_connector = new SslSelectChannelConnector();
			ssl_connector.setHost(conf
					.getProperty(ServerConstants.REGISTRY_HOSTNAME));
			ssl_connector.setPort(Integer.valueOf(conf
					.getProperty(ServerConstants.REGISTRY_PORT)));

			SslContextFactory cf = ssl_connector.getSslContextFactory();

			cf.setKeyStore(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYSTORE));// "src/main/certs/demo-server.p12"
			cf.setKeyStoreType(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYTYPE));// "pkcs12";
			cf.setKeyManagerPassword(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYPASS));// ("emi");
			cf.setKeyStorePassword(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_KEYPASS));// ("emi");

			cf.setTrustStore(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE));// ("src/main/certs/demo-server.jks");
			cf.setTrustStorePassword(conf
					.getProperty(ISecurityProperties.REGISTRY_SSL_TRUSTPASS));// ("emi");
			cf.setTrustStoreType(conf.getProperty(
					ISecurityProperties.REGISTRY_SSL_TRUSTTYPE, "jks"));// ("jks");

			cf.setWantClientAuth(conf.getBooleanProperty(
					ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "false")); // true
			cf.setNeedClientAuth(conf.getBooleanProperty(
					ISecurityProperties.REGISTRY_SSL_CLIENTAUTH, "false"));
		} catch (Exception e) {
			Log.logException("Error creating secure connectore", e, logger);
		}

		configureConnector(ssl_connector);
		return ssl_connector;
	}

	/**
	 * @return
	 * 
	 */
	private AbstractConnector createConnector() {
		SelectChannelConnector plain_connector = new SelectChannelConnector();
		plain_connector.setHost(hostName);
		plain_connector.setPort(portNumber);
		
		plain_connector.setMaxIdleTime(Integer.valueOf(conf
				.getProperty(ServerConstants.JETTY_MAXIDLETIME)));
		
		return plain_connector;
	}

	protected void configureConnector(AbstractConnector http) {
		http.setMaxIdleTime(Integer.valueOf(conf
				.getProperty(ServerConstants.JETTY_MAXIDLETIME)));
		int soLinger = Integer.valueOf(conf
				.getProperty(ServerConstants.JETTY_MAXIDLETIME));
		if (soLinger > 0)
			http.setSoLingerTime(soLinger);
	}

	public void start() {
		try {
			server.start();
		} catch (Exception e) {
			Log.logException("cannot start the server", e);
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			Log.logException("cannot to stop the server", e);
		}
	}

	public Server getServer() {
		return server;
	}

}
