/**
 * 
 */
package eu.emi.dsr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.FileListener;
import eu.emi.dsr.core.RegistryThreadPool;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.core.ServerSecurityProperties;
import eu.emi.dsr.jetty.JettyServer;
import eu.emi.dsr.lease.ServiceReaper;
import eu.emi.dsr.util.Log;

/**
 * The main class for starting the server
 * 
 * @author a.memon
 * 
 * 
 */
public class DSRServer{
	private boolean started;
	private static Configuration conf;
	private JettyServer jettyServer;
	private Logger logger = Log.getLogger(Log.DSR, DSRServer.class);
	private static ServerSecurityProperties sProps;

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
		this.conf = conf;
		init();
	}

	/**
	 * 
	 */
	private void init() {
		setSecurityProperties();		
	}
	
	/**
	 * 
	 */
	private void setSecurityProperties() {
	   sProps = new ServerSecurityProperties();
	   sProps.setAccessControl(conf.getBooleanProperty(ServerConstants.REGISTRY_ACCESSCONTROL));
	   sProps.setAttributeLocation(conf.getProperty(ServerConstants.REGISTRY_ATTRIBUTESOURCE_LOCATION));
	   sProps.setAttributeSource(conf.getProperty(ServerConstants.REGISTRY_ATTRIBUTESOURCE_TYPE));
	   sProps.setClientAuthn(conf.getBooleanProperty(ServerConstants.CLIENT_AUTHN));
	   sProps.setKeystorePassword(conf.getProperty(ServerConstants.KEYSTORE_PASSWORD));
	   sProps.setKeystorePath(conf.getProperty(ServerConstants.KEYSTORE_PATH));
	   sProps.setKeystoreType(conf.getProperty(ServerConstants.KEYSTORE_TYPE));
	   sProps.setTruststorePassword(conf.getProperty(ServerConstants.TRUSTSTORE_PASSWORD));
	   sProps.setTruststorePath(conf.getProperty(ServerConstants.TRUSTSTORE_PATH));
	   sProps.setTruststoreType(conf.getProperty(ServerConstants.TRUSTSTORE_TYPE));  
	   
	}
	
	public static ServerSecurityProperties getSecurityProperties(){
		ServerSecurityProperties sp = null;
		try {
			sp = sProps.clone();
		} catch (CloneNotSupportedException e) {
			Log.logException("error creating server security properties", e);
		}
		return sp;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	
	
	

	public void startJetty() {
		initLog4j();
		if (!started) {
			jettyServer = new JettyServer(DSRApplication.class, conf);
			jettyServer.start();
		}
		
		startLog4jFileListener();
		startServiceExpiryCheckcer();
		System.out.println("DSR server started");
		logger.info("DSR server started");
	}

	/**
	 * Starts the servicereaper thread to purge the expired service entries
	 */
	private void startServiceExpiryCheckcer() {
		RegistryThreadPool.getScheduledExecutorService().scheduleWithFixedDelay(new ServiceReaper(), 10, 5, TimeUnit.SECONDS);		
	}



	public void stopJetty() {
		jettyServer.stop();
		started = false;
		System.out.println("DSR server stopped");
		logger.info("DSR server stopped");

	}

	private void initLog4j() {
		String path = conf.getProperty(ServerConstants.LOGGER_CONF_PATH);
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
			server = new DSRServer("conf/dsr.config");	
		}
		
		server.startJetty();
	}
	
	
	/**
	 * sets up a watchdog that checks for changes to the log4j configuration file,
	 * and re-configures log4j if that file has changed
	 */
	private void startLog4jFileListener(){
		final String log4jConfig=System.getProperty("log4j.configuration");
		if(log4jConfig==null){
			logger.debug("No logger configuration found.");
			return;
		}
		try{
			Runnable r=new Runnable(){
				public void run(){
					logger.info("Log4j Configuration modified, re-configuring.");
					PropertyConfigurator.configure(log4jConfig);
				}
			};
			File logProperties=log4jConfig.startsWith("file:")?new File(new URI(log4jConfig)):new File(log4jConfig);
			FileListener fw=new FileListener(logProperties,r);
			RegistryThreadPool.getScheduledExecutorService().scheduleWithFixedDelay(fw, 5, 5, TimeUnit.SECONDS);
		}
		catch(URISyntaxException use){
			logger.warn("Location of log configuration is not an URI: <"+log4jConfig+">");
		} catch (FileNotFoundException e) {
			logger.warn("Invalid log location: <"+log4jConfig+">");
		}
	}
}
