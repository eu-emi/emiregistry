/**
 * 
 */
package eu.emi.emir.jetty;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;

import eu.emi.emir.EMIRAnonymousApplication;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.Log;


/**
 * Runs annonymous query interface (on http port)
 * 
 * @author a.memon
 *
 */
public class HttpServer {
	private static Logger logger = Log.getLogger(Log.EMIR_HTTPSERVER, HttpServer.class);

	ServerProperties serverProps = null;
    
	JettyServer server = null;
	
	/**
	 * @throws Exception 
	 * 
	 */
	public HttpServer(Properties props) throws Exception {
        serverProps = new ServerProperties(props, false);
		
		EMIRJettyProperties jettyProperties = new EMIRJettyProperties(props);
		
		String httpsServerAddress = serverProps.getValue(ServerProperties.PROP_ADDRESS);
		
		URL httpsServerUrl = new URL(httpsServerAddress);
		
		String port = serverProps.getValue(ServerProperties.PROP_ANONYMOUS_PORT);
		
		String address = "http://"+httpsServerUrl.getHost()+":"+port;
		
		URL[] url = {new URL(address)};
		
		server = new JettyServer(url, null, jettyProperties, getJerseyInitParams());		
		
	}
	
	public void start() throws Exception{
		server.start();
		
	}
	
	public void stop() throws Exception{
		server.stop();
		
	}
	
	private Map<String, String> getJerseyInitParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("javax.ws.rs.Application",
				EMIRAnonymousApplication.class.getCanonicalName());
		map.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, getRequestFilters());
		map.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, getResponseFilters());
		return map;
	}
	
	/**
	 * 
	 */
	private String getResponseFilters() {
		return GZIPContentEncodingFilter.class.getName();

	}

	/**
	 * 
	 */
	private String getRequestFilters() {
		return GZIPContentEncodingFilter.class.getName();

	}
}
