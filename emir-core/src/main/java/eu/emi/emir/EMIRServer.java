/**
 * 
 */
package eu.emi.emir;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.RegistryThreadPool;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventTypes;
import eu.emi.emir.infrastructure.ServiceCheckin;
import eu.emi.emir.infrastructure.ServiceEventReceiver;
import eu.emi.emir.jetty.HttpsServer;
import eu.emi.emir.lease.ServiceReaper;
import eu.emi.emir.p2p.GSRHelper;
import eu.emi.emir.p2p.StartStopMethods;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.util.configuration.FilePropertiesHelper;

/**
 * @author a.memon
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
	}
	//Following constructur is there is to keep the tests running
	public EMIRServer(Properties p){
		serverProps = new ServerProperties(p, false);
	}
	
	public static void main(String[] args) {
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
							StartStopMethods.stopGSRFunctions();
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
		try {
			
			server = new HttpsServer(props);

			secProps = server.getServerSecProps();

			serverProps = server.getServerProps();
			
			clientSecProps = server.getClientSecProps();

			runningSince = new Date();
			
			rawProps = props;
			
			server.start();

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
	
	/**
	 * 
	 */
	private void handleGSR() {
		String type = "DSR";
		if (EMIRServer.getServerProperties().isGlobalEnabled()) {
			type = "GSR";
			new GSRHelper().startGSRFunctions();
		} else {
			addParentDSR();
		}	
	}

	/**
	 * Starts the servicereaper thread to purge the expired service entries
	 */
	private void startServiceReaper(){
		try {
			RegistryThreadPool.getScheduledExecutorService()
			.scheduleWithFixedDelay(new ServiceReaper(), 10, 5,
					TimeUnit.SECONDS);	
		} catch (Exception e) {
			logger.warn("Cannot start service record reaper", e);
		}
		
		
	}
	
	public void addParentDSR() {

		String parentUrl = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_PARENT_ADDRESS);
		String serverUrl = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_ADDRESS);
		if (parentUrl != null) {
			logger.info("adding parent dsr : " + parentUrl + " to: " + serverUrl);
			RegistryThreadPool.getExecutorService().execute(
					new ServiceEventReceiver(parentUrl));
			Long max = EMIRServer.getServerProperties().getLongValue(ServerProperties.PROP_RECORD_MAXIMUM);
			try {
				RegistryThreadPool.getExecutorService().execute(
						new ServiceCheckin(parentUrl, serverUrl, max));
			} catch (Throwable e) {
				logger.warn("The parent DSR is not available.");
			}

		}

	}

	public void stop() {
		try {
			server.stop();
			cleanUp();
			System.out.println("DSR server stopped");
			logger.info("DSR server stopped");
		} catch (Exception e) {
			System.err.println("Error shutting down the EMIR Server");
			Log.logException("Error shutting down the EMIR Server", e, logger);
		}
	}
	
	protected void cleanUp(){
		System.out.println("Send DELETE message to the neighbors.");
		logger.info("Send DELETE message to the neighbors.");
		String myURL = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_ADDRESS);

		Event event = new Event(EventTypes.SERVICE_DELETE, myURL);
		new eu.emi.emir.p2p.ServiceEventReceiver().recieve(event);
		try {
			new MongoDBServiceDatabase().deleteByUrl(myURL);
		} catch (Exception e) {
			Log.logException("Error in the delete procedure ", e);
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