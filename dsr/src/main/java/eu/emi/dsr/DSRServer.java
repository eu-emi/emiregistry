/**
 * 
 */
package eu.emi.dsr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.jetty.JettyServer;
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

	/**
	 * @param path
	 *            configuration file
	 */
	public DSRServer(String path) {
		conf = new Configuration(path);
	}

	/**
	 * @param conf
	 */
	public DSRServer(Configuration conf) {
		this.conf = conf;
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
		System.out.println("DSR server started");
		logger.info("DSR server started");
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
		return conf;
	}
	
	

	public static void main(String[] args) {
		DSRServer server = new DSRServer("src/main/conf/dsr.config");
		server.startJetty();
	}
}
