/**
 * 
 */
package eu.emi.dsr.jetty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.ext.servlet.ServerServlet;

import eu.emi.dsr.DSRApplication;
import eu.emi.dsr.DSRJaxRsApplication;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerProperties;
import eu.emi.dsr.util.Log;

/**
 * @author a.memon
 * 
 */
public class JettyServer {
	Logger logger = Log.getLogger(Log.DSR, JettyServer.class);

	Server server;
	boolean started = false;
	private Class<? extends JaxRsApplication> appClass;
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
	public JettyServer(Class<DSRJaxRsApplication> clazz, Configuration c) {
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

		scheme = conf.getProperty(ServerProperties.REGISTRY_SCHEME);
		hostName = conf.getProperty(ServerProperties.REGISTRY_HOSTNAME);
		portNumber = Integer.valueOf(conf
				.getProperty(ServerProperties.REGISTRY_PORT));

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
		ServletHolder sh = new ServletHolder(ServerServlet.class);
		sh.setInitParameter("org.restlet.application",
				appClass.getCanonicalName());
		server = new Server();

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(sh, "/*");
		server.addConnector(connector);
	}

	/**
	 * 
	 */
	private AbstractConnector createSecureConnector() {
		SslSelectChannelConnector ssl_connector = null;
		try {
			ssl_connector = new SslSelectChannelConnector();
			ssl_connector.setHost(conf.getProperty(ServerProperties.REGISTRY_HOSTNAME));
			ssl_connector.setPort(Integer.valueOf(conf.getProperty(ServerProperties.REGISTRY_PORT)));

			SslContextFactory cf = ssl_connector.getSslContextFactory();

			cf.setKeyStore(conf.getProperty(ServerProperties.KEYSTORE_PATH));// "src/main/certs/demo-server.p12"
			cf.setKeyStoreType(conf.getProperty(ServerProperties.KEYSTORE_TYPE));//"pkcs12";
			cf.setKeyManagerPassword(conf.getProperty(ServerProperties.KEYSTORE_PASSWORD));//("emi");
			cf.setKeyStorePassword(conf.getProperty(ServerProperties.KEYSTORE_PASSWORD));//("emi");

			cf.setTrustStore(conf.getProperty(ServerProperties.TRUSTSTORE_PATH));//("src/main/certs/demo-server.jks");
			cf.setTrustStorePassword(conf.getProperty(ServerProperties.TRUSTSTORE_PASSWORD));//("emi");
			cf.setTrustStoreType(conf.getProperty(ServerProperties.TRUSTSTORE_TYPE));//("jks");

			cf.setWantClientAuth(Boolean.valueOf(conf.getProperty(ServerProperties.CLIENT_AUTHN))); //true
			cf.setNeedClientAuth(Boolean.valueOf(conf.getProperty(ServerProperties.CLIENT_AUTHN)));

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
		plain_connector.setThreadPool(new QueuedThreadPool(Integer.valueOf(conf
				.getProperty(ServerProperties.JETTY_MAXTHREADS))));
		plain_connector.setMaxIdleTime(Integer.valueOf(conf
				.getProperty(ServerProperties.JETTY_MAXIDLETIME)));
		return plain_connector;
	}

	protected void configureConnector(AbstractConnector http) {
		http.setMaxIdleTime(Integer.valueOf(conf
				.getProperty(ServerProperties.JETTY_MAXIDLETIME)));
		int soLinger = Integer.valueOf(conf
				.getProperty(ServerProperties.JETTY_MAXIDLETIME));
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

	public static void main(String[] args) throws InterruptedException,
			MalformedURLException {
		Properties p = new Properties();
		p.put(ServerProperties.REGISTRY_HOSTNAME, "localhost");
		p.put(ServerProperties.REGISTRY_PORT, "8443");
		// p.put(ServerProperties.REGISTRY_SCHEME, "http");
		p.put(ServerProperties.REGISTRY_SCHEME, "https");
		p.put(ServerProperties.JETTY_LOW_RESOURCE_MAXIDLETIME, "10000");
		p.put(ServerProperties.JETTY_LOWTHREADS, "50");
		p.put(ServerProperties.JETTY_MAXIDLETIME, "30000");
		p.put(ServerProperties.JETTY_MAXTHREADS, "255");
		Configuration c = new Configuration(p);
		JettyServer s = new JettyServer(DSRJaxRsApplication.class, c);
		s.start();

	}

}
