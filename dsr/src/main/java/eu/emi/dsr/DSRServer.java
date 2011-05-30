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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.service.ConverterService;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerProperties;
import eu.emi.dsr.jetty.JettyServer;
import eu.emi.dsr.util.Log;

/**
 * The main class for starting the server
 * 
 * @author a.memon
 * 
 * 
 */
public class DSRServer {
	private Component comp;
	private boolean started;
	private Configuration conf;
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

	public void asyncStart() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					start();
				} catch (Exception e) {

				}

			}
		});
		t.run();
		t.setName("DSRServer");

	}

	// TODO remove this
	public void start() {
		if (!started) {
			initLog4j();

			// create Component (as ever for Restlet)
			comp = new Component();
			Server server = comp.getServers().add(Protocol.HTTP,
					conf.getIntegerProperty(ServerProperties.REGISTRY_PORT));

			// create JAX-RS runtime environment
			// JaxRsApplication application = new
			// JaxRsApplication(comp.getContext());
			JaxRsApplication application = new JaxRsApplication(
					comp.getContext());
			// attach Application
			application.add(new DSRApplication());

			// Attach the application to the component and start it
			comp.getDefaultHost().attach(application);
			ConverterService cs = new ConverterService();
			try {
				cs.start();

				comp.getServices().add(cs);

				comp.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Server started on port " + server.getPort());
			started = true;
		}

	}

	public void startJetty() {
		if (!started) {
			jettyServer = new JettyServer(DSRJaxRsApplication.class, conf);
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
		String path = conf.getProperty(ServerProperties.LOGGER_CONF_PATH);
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

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Configuration getConfiguration() {
		return conf;
	}

	// TODO remove this
	public void shutdown() {
		try {
			this.comp.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		started = false;
		System.out.println("Server stopped");
	}

	public static void main(String[] args) {
		DSRServer server = new DSRServer("src/main/conf/dsr.config");
		server.start();
	}
}
