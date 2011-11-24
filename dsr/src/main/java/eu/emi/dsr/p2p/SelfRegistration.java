/**
 * 
 */
package eu.emi.dsr.p2p;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.util.DateUtil;
import eu.emi.dsr.util.ServiceUtil;

/**
 * GSR Service periodically self registration
 * 
 * @author g.szigeti
 * 
 */
public class SelfRegistration implements Runnable {
	private static Logger logger = Log.getLogger(Log.DSR, SelfRegistration.class);
	private final WebResource selfRegisterClient;
	private JSONObject myInfos;
	private Boolean firstUsage;
	
	/**
	 * @throws Throwable 
	 * 
	 */
	public SelfRegistration(String myUrl) throws Throwable {
		DSRClient sc = new DSRClient(myUrl + "/serviceadmin");
		selfRegisterClient = sc.getClientResource();
		firstUsage = true;
		try {
			URL tmp = new URL(myUrl);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Fill the infos
		myInfos = new JSONObject();
		myInfos.put("Service_Endpoint_URL", myUrl);
		myInfos.put("Service_Name", "IndexService");
		myInfos.put("Service_Type", "GSR");
		// current time and last update should be same in the beginning
		JSONObject date = new JSONObject();
		date.put("$date", ServiceUtil.toUTCFormat(new Date()));
		myInfos.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
				.getAttributeName(), date);
		myInfos.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
				.getAttributeName(), date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true){
			if (logger.isTraceEnabled()) {
				logger.trace("self registration entry");
			}
			// update the expiration time
			DateUtil.setExpiryTime(myInfos, Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_EXPIRY_DEFAULT,
							"1")));
			JSONArray message = new JSONArray();
			message.put(myInfos);
			// message sending
			if (firstUsage){
				ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(ClientResponse.class, message);
				if ( res.getStatus() != Status.BAD_REQUEST.getStatusCode() ){
					// Next message will be send as UPDATE
					firstUsage = false;
				}
			} else {
				ClientResponse res = selfRegisterClient.accept(MediaType.APPLICATION_JSON_TYPE)
					.put(ClientResponse.class, message);
				if ( res.getStatus() == Status.BAD_REQUEST.getStatusCode() ){
					// Next message will be send as REGISTER
					firstUsage = true;
				}
			}
			
			try {
				Thread.sleep(60*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	

}
