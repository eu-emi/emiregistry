/**
 * 
 */
package eu.emi.emir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;

import eu.emi.emir.client.ClientSecurityProperties;
import eu.emi.emir.client.security.ISecurityProperties;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.Configuration;
import eu.emi.emir.core.FileListener;
import eu.emi.emir.core.RegistryThreadPool;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.infrastructure.InputFilter;
import eu.emi.emir.infrastructure.ServiceCheckin;
import eu.emi.emir.infrastructure.ServiceEventReceiver;
import eu.emi.emir.jetty.JettyServer;
import eu.emi.emir.lease.ServiceReaper;
import eu.emi.emir.p2p.StartStopMethods;
import eu.emi.emir.security.ACLFilter;
import eu.emi.emir.security.AccessControlFilter;
import eu.emi.emir.security.DSRSecurityProperties;

/**
 * The main class for starting the server
 * 
 * @author a.memon
 * @author g.szigeti
 * 
 * 
 */
public class DSRServer {
	private boolean started;
	private static Configuration conf;
	private JettyServer jettyServer;
	private Logger logger = Log.getLogger(Log.EMIR_CORE, DSRServer.class);
	private static ISecurityProperties sProps;
	private static Date runningSince;

	/**
	 * @param path
	 *            configuration file
	 */
	public DSRServer(String path) {
		conf = new Configuration(path);
		init();
	}

	/**
	 * @param conf
	 */
	public DSRServer(Configuration conf) {
		DSRServer.conf = conf;
		init();
	}

	/**
	 * 
	 */
	private void init() {
		setSecurityProperties();
	}

	private void setSecurityProperties() {
		try {
			if (getProperty(ServerConstants.REGISTRY_SCHEME, "http")
					.equalsIgnoreCase("https")) {
				conf.setProperty(ISecurityProperties.REGISTRY_SSL_ENABLED,
						"true");
			}
			if ("true".equalsIgnoreCase(getProperty(
					ISecurityProperties.REGISTRY_SSL_ENABLED, "false"))) {
				sProps = new DSRSecurityProperties(conf.getProperties());
			}

		} catch (UnrecoverableKeyException e) {
			Log.logException("", e);
		} catch (KeyStoreException e) {
			Log.logException("", e);
		} catch (NoSuchAlgorithmException e) {
			Log.logException("", e);
		} catch (CertificateException e) {
			Log.logException("", e);
		} catch (IOException e) {
			Log.logException("", e);
		}

	}

	public static ISecurityProperties getSecurityProperties() {
		return sProps.clone();
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void startJetty() {
		runningSince = new Date();
		addDefaultFilterClasses();
		initLog4j();
		if (!started) {
			jettyServer = new JettyServer(DSRApplication.class, conf);
			jettyServer.start();
			setStarted(jettyServer.isStarted());
		}

		startLog4jFileListener();
		startServiceExpiryCheckcer();

		String type = "DSR";
		if (getGlobalRegistryEnabled()) {
			type = "GSR";
			StartStopMethods.startGSRFunctions();
		} else {
			addParentDSR();
		}
		System.out.println(type + " server started");
		logger.info(type + " server started");
	}

	/**
	 * The configured EMIR is global or federated component.
	 * 
	 * @return boolean
	 */
	private static boolean getGlobalRegistryEnabled() {
		String globalRegistryEnabled = conf
				.getProperty(ServerConstants.REGISTRY_GLOBAL_ENABLE);
		if (globalRegistryEnabled != null
				&& globalRegistryEnabled.toLowerCase().equals("true")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void addDefaultFilterClasses() {
		addRequestFilters();
		addResponseFilters();

	}

	/**
	 * 
	 */
	private void addResponseFilters() {
		StringBuilder sb = new StringBuilder();
		String s = conf.getProperty(ServerConstants.REGISTRY_FILTERS_RESPONSE);
		sb.append(GZIPContentEncodingFilter.class.getName());
		if (s != null) {
			sb.append(";").append(s);	
		}
		
		conf.setProperty(ServerConstants.REGISTRY_FILTERS_RESPONSE,
				sb.toString());

	}

	/**
	 * 
	 */
	private void addRequestFilters() {
		StringBuilder sb = new StringBuilder();
		String s = conf.getProperty(ServerConstants.REGISTRY_FILTERS_REQUEST);

		// checking whether to use xacml for the authorization
		if ((getProperty(ISecurityProperties.REGISTRY_CHECKACCESS_PDPCONFIG,
				null) != null)
				&& (!getProperty(
						ISecurityProperties.REGISTRY_CHECKACCESS_PDPCONFIG)
						.isEmpty())) {
			sb.append(AccessControlFilter.class.getName()).append(";");
		} else {
			// setting ACL filter
			sb.append(ACLFilter.class.getName()).append(";");
		}

		// adding the service record filter
		if ((getProperty(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH, null) != null)
				&& (!getProperty(ServerConstants.REGISTRY_FILTERS_INPUTFILEPATH)
						.isEmpty())) {
			sb.append(InputFilter.class.getName()).append(";");
		}
		
		sb.append(GZIPContentEncodingFilter.class.getName());
		
		if (s != null) {
			sb.append(";").append(s);	
		}
		
		conf.setProperty(ServerConstants.REGISTRY_FILTERS_REQUEST,
				sb.toString());

	}

	public static String getProperty(String key) {
		return conf.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		if (conf == null) {
			return null;
		}
		return conf.getProperty(key, defaultValue);
	}

	public Server getServer() {
		return jettyServer.getServer();
	}

	/**
	 * Starts the servicereaper thread to purge the expired service entries
	 */
	private void startServiceExpiryCheckcer() {
		RegistryThreadPool.getScheduledExecutorService()
				.scheduleWithFixedDelay(new ServiceReaper(), 10, 5,
						TimeUnit.SECONDS);
	}

	public void stopJetty() {
		jettyServer.stop();
		started = false;
		this.finalize();
		System.out.println("EMIR server stopped");
		logger.info("EMIR server stopped");
	}

	public String getBaseUrl() {
		StringBuffer b = new StringBuffer();
		if (conf != null) {
			b.append(conf.getProperty(ServerConstants.REGISTRY_SCHEME))
					.append("://")
					.append(conf.getProperty(ServerConstants.REGISTRY_HOSTNAME))
					.append(":")
					.append(conf.getProperty(ServerConstants.REGISTRY_PORT));
			return b.toString();
		}
		return null;
	}

	private void initLog4j() {
		String path = conf.getProperty(ServerConstants.LOGGER_CONF_PATH);
		if (path == null) {
			return;
		}
		PropertyConfigurator.configure(path);
		LogManager l = LogManager.getLogManager();
		try {
			l.readConfiguration(new FileInputStream(new File(path)));
		} catch (SecurityException e) {
			Log.logException("", e);
		} catch (FileNotFoundException e) {
			Log.logException("", e);
		} catch (IOException e) {
			Log.logException("", e);
		}
	}

	public static Configuration getConfiguration() {
		Configuration c = null;
		try {
			if (conf == null) {
				return null;
			}
			c = conf.clone();
		} catch (CloneNotSupportedException e) {
			Log.logException("error getting configuration", e);
		}
		return c;
	}

	public static void main(String... args) {
		DSRServer server = null;
		if (args[0] != null) {
			server = new DSRServer(args[0]);
		} else {
			server = new DSRServer("conf/emir.config");
		}
		// Shutdown hook
		final DSRServer serverPointer = server;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			private Logger threadLogger = Log.getLogger(Log.EMIR_CORE,
					DSRServer.class);

			public void run() {
				threadLogger
						.debug("EMIR server is stopping now (shutdown hook)");
				if (getGlobalRegistryEnabled()) {
					try {
						// serverPointer.finalize();
						serverPointer.stopJetty();
					} catch (NullPointerException e) {
						System.out.println("null failure");
					} catch (Throwable e) {
						Log.logException("shutdown hook", e);
					}
				}
				threadLogger.debug("EMIR server stopped (shutdown hook)");
			}
		});
		// end of Shutdown hook

		server.startJetty();
	}

	public static Date getRunningSince() {
		return runningSince;
	}

	/**
	 * sets up a watchdog that checks for changes to the log4j configuration
	 * file, and re-configures log4j if that file has changed
	 */
	private void startLog4jFileListener() {
		final String log4jConfig = System.getProperty("log4j.configuration");
		if (log4jConfig == null) {
			logger.debug("No logger configuration found.");
			return;
		}
		try {
			Runnable r = new Runnable() {
				public void run() {
					logger.info("Log4j Configuration modified, re-configuring.");
					PropertyConfigurator.configure(log4jConfig);
				}
			};
			File logProperties = log4jConfig.startsWith("file:") ? new File(
					new URI(log4jConfig)) : new File(log4jConfig);
			FileListener fw = new FileListener(logProperties, r);
			RegistryThreadPool.getScheduledExecutorService()
					.scheduleWithFixedDelay(fw, 5, 5, TimeUnit.SECONDS);
		} catch (URISyntaxException use) {
			logger.warn("Location of log configuration is not an URI: <"
					+ log4jConfig + ">");
		} catch (FileNotFoundException e) {
			logger.warn("Invalid log location: <" + log4jConfig + ">");
		}
	}

	public void addParentDSR() {

		String url = conf.getProperty(ServerConstants.REGISTRY_PARENT_URL);
		if (url != null) {
			logger.info("adding parent DSR : " + url + " to: " + getBaseUrl());
			RegistryThreadPool.getExecutorService().execute(
					new ServiceEventReceiver(conf
							.getProperty(ServerConstants.REGISTRY_PARENT_URL),
							conf));
			String myURL = conf.getProperty(ServerConstants.REGISTRY_SCHEME)
					.toString()
					+ "://"
					+ conf.getProperty(ServerConstants.REGISTRY_HOSTNAME)
							.toString()
					+ ":"
					+ conf.getProperty(ServerConstants.REGISTRY_PORT)
							.toString();
			Long max = Long.valueOf(DSRServer.getProperty(
					ServerConstants.REGISTRY_MAX_REGISTRATIONS, "100"));
			try {
				RegistryThreadPool
						.getExecutorService()
						.execute(
								new ServiceCheckin(
										conf.getProperty(ServerConstants.REGISTRY_PARENT_URL),
										myURL, max));
			} catch (Throwable e) {
				logger.warn("The parent DSR is not available.");
			}

		}

	}

	protected void finalize() {
		if (getGlobalRegistryEnabled()) {
			StartStopMethods.stopGSRFunctions();
		}
	}

	public static ClientSecurityProperties getClientSecurityProperties() {
		ClientSecurityProperties csp = null;

		Properties p = new Properties();
		p.put(ISecurityProperties.REGISTRY_SSL_CLIENTAUTH,
				sProps.requireClientAuthentication());
		p.put(ISecurityProperties.REGISTRY_SSL_KEYPASS,
				sProps.getKeystoreKeyPassword());
		p.put(ISecurityProperties.REGISTRY_SSL_KEYTYPE,
				sProps.getKeystoreType());
		p.put(ISecurityProperties.REGISTRY_SSL_KEYSTORE, sProps.getKeystore());
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTPASS,
				sProps.getTruststorePassword());
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTSTORE,
				sProps.getTruststore());
		p.put(ISecurityProperties.REGISTRY_SSL_TRUSTTYPE,
				sProps.getTruststoreType());

		try {
			csp = new ClientSecurityProperties(p);
		} catch (Exception e) {
			Log.logException(
					"Error in creating the client security properties", e);
		}
		return csp;
	}
}
