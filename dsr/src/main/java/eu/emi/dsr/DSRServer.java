/**
 * 
 */
package eu.emi.dsr;

import java.io.IOException;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.service.ConverterService;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerProperties;

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

	/**
	 * @param path
	 *            configuration file
	 */
	public DSRServer(String path) {
		conf = new Configuration(path);
		conf.bootstrapProperties();
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

	public void start() {
		if (!started) {
			// create Component (as ever for Restlet)
			comp = new Component();
			Server server = comp
					.getServers()
					.add(Protocol.HTTP,
							Configuration
									.getIntegerProperty(ServerProperties.REGISTRY_PORT));

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
