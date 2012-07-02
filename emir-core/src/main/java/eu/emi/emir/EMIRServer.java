/**
 * 
 */
package eu.emi.emir;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.jetty.HttpsServer;
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

	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			System.err.println("Error shutting down the EMIR Server");
			Log.logException("Error shutting down the EMIR Server", e, logger);
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
