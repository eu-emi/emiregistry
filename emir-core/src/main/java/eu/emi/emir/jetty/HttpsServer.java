/**
 * 
 */
package eu.emi.emir.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;

import eu.emi.emir.EMIRApplication;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.FileListener;
import eu.emi.emir.core.RegistryThreadPool;
import eu.emi.emir.infrastructure.InputFilter;
import eu.emi.emir.security.ACLFilter;
import eu.emi.emir.security.AccessControlFilter;
import eu.emi.emir.security.ClientSecurityProperties;
import eu.emi.emir.security.ServerSecurityProperties;
import eu.unicore.util.configuration.FilePropertiesHelper;

/**
 * @author a.memon
 * 
 */
public class HttpsServer {
	private static Logger logger = Log.getLogger(Log.EMIR_HTTPSERVER,
			HttpsServer.class);

	JettyServer server = null;

	ServerProperties serverProps = null;

	ServerSecurityProperties serverSecProps = null;
	
	ClientSecurityProperties clientSecProps = null;

	EMIRJettyProperties jettyProperties = null;
	
	Properties props = null;

	public HttpsServer(String propsFilePath) throws Exception {
		this(FilePropertiesHelper.load(propsFilePath));	
	}
	
	public HttpsServer(Properties props) throws Exception {
		startLogConfigWatcher();
		
		
		
		String v=ServerProperties.class.getPackage().getImplementationVersion();
		
		logger.info("");
		logger.info("******************************************");
		logger.info("*    EMI Service Registry");
		if(v!=null){
			logger.info("*    Version "+v);
		}		
		logger.info("******************************************");
		
		//if ssl is enabled then set the authentication properties
		if (props.getProperty(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS).startsWith("https")) {
			props.setProperty(EMIRJettyProperties.PREFIX+EMIRJettyProperties.REQUIRE_CLIENT_AUTHN, "true");
			props.setProperty(EMIRJettyProperties.PREFIX+EMIRJettyProperties.REQUIRE_CLIENT_AUTHN, "true");
			props.setProperty(ServerSecurityProperties.PREFIX+ServerSecurityProperties.PROP_SSL_ENABLED, "true");
			
		} else {
			
		}
		
		serverSecProps = new ServerSecurityProperties(props);
		
		if (serverSecProps.isSslEnabled()) {
			clientSecProps = new ClientSecurityProperties(props, serverSecProps);
		}
		
		
		this.props = props;
		
		serverProps = new ServerProperties(props, serverSecProps.isSslEnabled());
		
		jettyProperties = new EMIRJettyProperties(props);

		String address = serverProps.getValue(ServerProperties.PROP_ADDRESS);
		
		URL[] url = {new URL(address)};

		server = new JettyServer(url[0], serverSecProps, jettyProperties, getJerseyInitParams());
		
		

	}
	
	public void start() throws Exception{
		server.start();
		
	}	
	

	/**
	 * sets up a watchdog that checks for changes to the log4j configuration file,
	 * and re-configures log4j if that file has changed
	 */
	private void startLogConfigWatcher(){
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
		
		try{
			Runnable r=new Runnable(){
				public void run(){
					logger.info("LOG CONFIG MODIFIED, re-configuring.");
					PropertyConfigurator.configure(logConfig);
				}
			};
			File logProperties=logConfig.startsWith("file:")?new File(new URI(logConfig)):new File(logConfig);
			FileListener fw = new FileListener(logProperties, r);
			RegistryThreadPool.getScheduledExecutorService()
					.scheduleWithFixedDelay(fw, 5, 5, TimeUnit.SECONDS);
			logger.info("Monitoring log configuration at <"+logProperties.getAbsolutePath()+">");
		
		}catch(FileNotFoundException fex){
			System.err.println("Log configuration file <"+logConfig+"> not found.");
		}
		catch(URISyntaxException use){
			System.err.println("Not a valid URI: <"+logConfig+">.");
		}
	}

	private Map<String, String> getJerseyInitParams() {

		Map<String, String> map = new HashMap<String, String>();

		map.put("javax.ws.rs.Application",
				EMIRApplication.class.getCanonicalName());
		map.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, getRequestFilters());
		map.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, getResponseFilters());
		return map;
	}

	/**
	 * 
	 */
	private String getResponseFilters() {
		StringBuilder sb = new StringBuilder();
		
		String s = serverProps
				.getValue(ServerProperties.PROP_REQUEST_INTERCEPTORS);
		
		sb.append(GZIPContentEncodingFilter.class.getName());
		if (s != null) {
			sb.append(";").append(s);
		}

		return sb.toString();

	}

	/**
	 * 
	 */
	private String getRequestFilters() {
		StringBuilder sb = new StringBuilder();
		String s = serverProps
				.getValue(ServerProperties.PROP_RESPONSE_INTERCEPTORS);
		
		//always add access control filters when ssl is enabled
		if (serverSecProps.isSslEnabled()) {
			// checking whether to use xacml for the authorization	
			if (serverSecProps.getPdpConfigurationFile() != null){
				sb.append(AccessControlFilter.class.getName()).append(";");
			} else {
				// setting ACL filter
				sb.append(ACLFilter.class.getName()).append(";");
			}	
		}
		
		
		// adding the service record filter
		
		if (serverProps.getValue(ServerProperties.PROP_RECORD_BLOCKLIST_INCOMING) != null) {
			sb.append(InputFilter.class.getName()).append(";");
		}
		
		sb.append(GZIPContentEncodingFilter.class.getName());

		if (s != null) {
			sb.append(";").append(s);
		}

		return sb.toString();

	}

	public ServerProperties getServerProps() {
		return serverProps;
	}

	public ServerSecurityProperties getServerSecProps() {
		return serverSecProps;
	}
	
	public ClientSecurityProperties getClientSecProps() {
		return clientSecProps;
	}

	public Properties getRawProperties(){
		return props;
	}
	
	
	public JettyServer getJettyServer() {
		return server;
	}
	
	
}
