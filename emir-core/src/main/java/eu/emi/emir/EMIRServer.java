/**
 * 
 */
package eu.emi.emir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.RegistryThreadPool;
import eu.emi.emir.infrastructure.ServiceCheckin;
import eu.emi.emir.infrastructure.ServiceEventReceiver;
import eu.emi.emir.jetty.HttpServer;
import eu.emi.emir.jetty.HttpsServer;
import eu.emi.emir.lease.ServiceReaper;
import eu.emi.emir.p2p.GSRHelper;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.util.configuration.FilePropertiesHelper;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class EMIRServer {
	private static final Logger logger = Log.getLogger(Log.EMIR_HTTPSERVER,
			EMIRServer.class);

	private static ServerSecurityProperties secProps;

	private static ServerProperties serverProps;

	private static ClientSecurityProperties clientSecProps;

	private static Date runningSince = null;

	private Server server;
	
	private static Properties rawProps;
	
	
	/**
	 * 
	 */
	public EMIRServer() {
		initLogging();
	}
	//Following constructor is there is to keep the tests running
	public EMIRServer(Properties p){
		initLogging();
		serverProps = new ServerProperties(p, false);
	}
	
	public static void main(String[] args) {
		initLogging();
		if (args.length == 0)
			printUsage();
		else {
			EMIRServer s = new EMIRServer();
			s.run(args[0]);
			// Shutdown hook
			final EMIRServer serverPointer = s;

			Runtime.getRuntime().addShutdownHook(new Thread() {
				private Logger threadLogger = Log.getLogger(Log.EMIR_CORE,
						EMIRServer.class);

				public void run() {
					threadLogger
							.debug("EMIR server is stopping now (shutdown hook)");
					if (EMIRServer.getServerProperties().isGlobalEnabled()) {
						try {
							// serverPointer.finalize();
							serverPointer.stop();
						} catch (NullPointerException e) {
							System.err.println("Unknown (NULL) failure");
						} catch (Throwable e) {
							Log.logException("shutdown hook", e);
						} finally {
							GSRHelper.stopGSRFunctions();
						}
					}
					threadLogger.debug("EMIR server stopped (shutdown hook)");
				}
			});
			// end of Shutdown hook
		}

	}

	/**
	 * 
	 */
	public void run(String propsFilePath) {
		try {
			this.run(rawProps = FilePropertiesHelper.load(propsFilePath));
		} catch (IOException e) {
			logger.error("Problem starting the server", e);
			e.printStackTrace();
			System.out.println("Cannot start EMIR.");
			System.exit(1);
		}

	}

	public void run(Properties props) {
		HttpsServer server = null;
		HttpServer anonymousServer = null;
		try {
			
			server = new HttpsServer(props);
			
			secProps = server.getServerSecProps();

			serverProps = server.getServerProps();
			
			clientSecProps = server.getClientSecProps();

			rawProps = props;
			
			server.start();
			
			if (serverProps.isAnonymousAccessEnabled()
					&& secProps.isSslEnabled()) {
				anonymousServer = new HttpServer(props);
				anonymousServer.start();	
			}
			

			this.server = server.getJettyServer().getServer();
			
			
			
		} catch (Exception e) {
			logger.error("Problem starting the server", e);
			e.printStackTrace();
			System.out.println("Cannot start EMIR.");
			System.exit(1);
		}
		//should not hinder server start process
		handleGSR();
		
		startServiceReaper();

	}
	
	private static void initLogging(){
		runningSince = new Date();
		final String logConfig=System.getProperty("log4j.configuration");
		if(logConfig==null){
			logger.debug("No log4j config defined.");
			return;
		} else {
			PropertyConfigurator.configure(logConfig);
			LogManager l = LogManager.getLogManager();
			try {
				l.readConfiguration(new FileInputStream(new File(logConfig)));
			} catch (SecurityException e) {
				Log.logException("", e);
			} catch (FileNotFoundException e) {
				Log.logException("", e);
			} catch (IOException e) {
				Log.logException("", e);
			}
		}	
	}
	
	
	
	/**
	 * 
	 */
	private void handleGSR() {
		String type = "DSR";
		ServerProperties sp = EMIRServer.getServerProperties();
		if (sp.isGlobalEnabled()) {
			type = "GSR";
			GSRHelper.startGSRFunctions();
		} else {
			addParentDSR();
		}
		String startMessage = "EMIR Server Started [TYPE: "+type+"] [URL:"+sp.getValue(ServerProperties.PROP_ADDRESS)+"]";
		System.out.println(startMessage);
		logger.info(startMessage);
	}

	/**
	 * Starts the service reaper thread to purge the expired service entries
	 */
	private void startServiceReaper(){
		try {
			//this operation is penalty to the performance, should have greater interval 
			RegistryThreadPool.getScheduledExecutorService()
			.scheduleWithFixedDelay(new ServiceReaper(), 10, 300,
					TimeUnit.SECONDS);	
		} catch (Exception e) {
			logger.warn("Cannot start service record reaper", e);
		}
		
		
	}
	
	public void addParentDSR() {

		String parentUrl = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_PARENT_ADDRESS);
		String serverUrl = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_ADDRESS);
		if (parentUrl != null) {
			logger.debug("Configured server's URL: "+serverUrl);
			logger.info("The parent EMIR URL is set to: " + parentUrl);
			RegistryThreadPool.getExecutorService().execute(
					new ServiceEventReceiver(parentUrl));
			Long max = EMIRServer.getServerProperties().getLongValue(ServerProperties.PROP_RECORD_MAXIMUM);
			try {
				RegistryThreadPool.getExecutorService().execute(
						new ServiceCheckin(parentUrl, serverUrl, max));
			} catch (Throwable e) {
				logger.warn("The parent EMIR is not available.");
			}

		}

	}

	public void stop() {
		try {
			server.stop();
			this.finalize();
			System.out.println("EMIR server stopped");
			logger.info("EMIR server stopped");
		} catch (Exception e) {
			System.err.println("Error shutting down the EMIR Server");
			Log.logException("Error shutting down the EMIR Server", e, logger);
		}
	}
	
	protected void finalize(){
		if (EMIRServer.getServerProperties().isGlobalEnabled()) {
			GSRHelper.stopGSRFunctions();
		}
	}

	public static Date getRunningSince() {
		return runningSince;
	}

	public static ServerSecurityProperties getServerSecurityProperties() {
		return secProps;
	}

	public static ClientSecurityProperties getClientSecurityProperties() {
		return clientSecProps;
	}

	public static void printUsage() {
		System.out.println("\n" + "Usage: start_cmd  <command>  [parameters]\n"
				+ "	<configfile>  starts EMIR server");
	}

	/**
	 * 
	 */
	public Server getJettyServer() {
		return server;

	}

	public static ServerProperties getServerProperties() {
		return serverProps;

	}
	
	public static Properties getRawProperties(){
		return rawProps;
	}
	
	
}
