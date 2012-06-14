/**
 * 
 */
package eu.emi.emir.infrastructure;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.DSRServer;
import eu.emi.emir.client.DSRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.security.ISecurityProperties;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.ServiceAdminManager;
import eu.emi.emir.db.mongodb.ServiceObject;

/**
 * Service periodically checkin to the parent DSR
 * 
 * @author g.szigeti
 * 
 */
public class ServiceCheckin implements Runnable {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, ServiceCheckin.class);
	private final WebResource childClient;
	private final WebResource synchClient;
	private String myURL;
	private Long max;
	private ServiceAdminManager sm;
	private Filters filters;
	
	/**
	 * Constructor for a service checkin mechanism
	 * @param URL of the parent DSR
	 * @param own URL
	 * @param maximum number of entries per message
	 * @throws Throwable 
	 * 
	 */
	public ServiceCheckin(String parentUrl, String url, Long maxmessage) throws Throwable {
		// configuration error handling
		String slash = "/";
		if (parentUrl.charAt(parentUrl.length()-1) == '/' ){
			slash = "";
		}
		DSRClient cc = new DSRClient(parentUrl + slash + "children");
		DSRClient sc = new DSRClient(parentUrl + slash + "serviceadmin");
		if ("true".equalsIgnoreCase(DSRServer.getProperty(ISecurityProperties.REGISTRY_SSL_ENABLED, "false"))) {

			cc = new DSRClient(parentUrl + slash + "children",
										DSRServer.getClientSecurityProperties());
			sc = new DSRClient(parentUrl + slash + "serviceadmin",
										DSRServer.getClientSecurityProperties());
		}
		childClient = cc.getClientResource();
		synchClient = sc.getClientResource();
		sm = new ServiceAdminManager();
		try {
			URL tmp = new URL(url);
			if (tmp.getProtocol().isEmpty()) {
				logger.error("The registry.scheme element is empty in the configuration!");
				this.finalize();
			}
			if (tmp.getHost().isEmpty()) {
				logger.error("The registry.hostname element is empty in the configuration!");
				this.finalize();
			}
			if (tmp.getHost().equals("localhost")) {
				logger.error("You added 'localhost' value in the registry.hostname element. Please modified it to the real DNS name or IP address!");			
				this.finalize();
			}
			if (tmp.getPort() == -1 ) {
				logger.error("The registry.post element is empty in the configuration!");		
				this.finalize();
			}
		} catch (MalformedURLException e) {
			Log.logException("", e);
		}
		myURL = url;
		max = maxmessage;
		filters = new Filters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true){
			logger.debug("checkin service entry");
			try {
				ClientResponse res = childClient.queryParam(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
								myURL).post(ClientResponse.class);
				if ( res.getStatus() == Status.BAD_REQUEST.getStatusCode() ||
						res.getStatus() == Status.NOT_FOUND.getStatusCode()){
					logger.error("Please modified the server's configuration, because the following error (" +res.getEntity(String.class) + ") given from the parent DSR. Checkin stopped!");
					return;
				}
				if ( res.hasEntity() && 
					 res.getEntity(String.class).equals("First registration")){
					// Full DB need to be send
					List<ServiceObject> dbList = new ArrayList<ServiceObject>();
					try {
						dbList = sm.findAll();
						JSONArray message = new JSONArray();
						logger.debug("Send synch messages.");
						for (int i=0; i<dbList.size(); i++){
							message.put(dbList.get(i).toJSON());
							// sending message size limit
							if ( (i>0 && ((i % max) == 0)) ||
								  i+1 == dbList.size()){
								// message sending
								synchClient.accept(MediaType.APPLICATION_JSON_TYPE)
											.post(ClientResponse.class, filters.outputFilter(message));
								// message cleaning
								message = new JSONArray();
							}
						}
					} catch (JSONException e) {
						Log.logException("", e);
					}
				}
			} catch (ClientHandlerException e){
				logger.info("The parent DSR is not available.");
			}
			try {
				Thread.sleep(60*60*1000);
			} catch (InterruptedException e) {
				Log.logException("", e);
			}
		}
	}
	
	

}
