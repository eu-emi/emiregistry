/**
 * 
 */
package eu.emi.emir.jetty;

import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

import eu.emi.emir.client.util.Log;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.security.canl.AuthnAndTrustProperties;
import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.jetty.JettyServerBase;

/**
 * Jetty server class
 * 
 * @author a.memon
 * 
 */

public class JettyServer extends JettyServerBase{
	private static final Logger logger = Log.getLogger(Log.HTTP_SERVER, JettyServer.class);
	Map<String, String> jerseyParams = null;
	
	public JettyServer(URL listenUrl, ServerSecurityProperties securityCfg, 
			EMIRJettyProperties jettyCfg, Map<String, String> map) throws Exception {
		super(listenUrl, securityCfg, jettyCfg);
		this.jerseyParams = map;
		initServer();
	}	
	

	public JettyServer(URL[] listenUrl, ServerSecurityProperties securityCfg, 
			EMIRJettyProperties jettyCfg, Map<String, String> map) throws Exception {
		super(listenUrl, securityCfg, jettyCfg, EMIRJettyLogger.class);
		this.jerseyParams = map;
		initServer();
	}	
	

	/* (non-Javadoc)
	 * @see eu.unicore.util.jetty.JettyServerBase#createRootHandler()
	 */
	@Override
	protected Handler createRootHandler() throws ConfigurationException {
		logger.info("Adding Jersey Application");
		ServletContextHandler root = new ServletContextHandler(getServer(), "/", ServletContextHandler.SESSIONS);
		ServletHolder sh = new ServletHolder(ServletContainer.class);
		sh.setInitParameters(jerseyParams);
		root.addServlet(sh, "/*");
		return root;
	}
	
}