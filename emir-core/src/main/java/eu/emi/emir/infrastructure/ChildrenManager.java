package eu.emi.emir.infrastructure;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.emir.DSRServer;
import eu.emi.emir.client.DSRClient;
import eu.emi.emir.client.security.ISecurityProperties;



public class ChildrenManager {
	private static ChildrenManager instance = null;
	private Map<String, Date> childServices;
	private long hour = 60*60*1000;

	/** 
	 * Default constructor if you don't want to use as a singleton class 
	 * @param None
	 */
	public ChildrenManager() {
		childServices = new HashMap<String, Date>();
	}

	/**
	 * Get only one instance for the children manager class.
	 * Use this operation if you want to use this class as a Singleton.
	 * @param None
	 * @return ChildrenManager instance
	 */
	public static synchronized ChildrenManager getInstance() {
	      if(instance == null) {
	         instance = new ChildrenManager();
	      }
	      return instance;
	}
	
	/**
	 * Get list of children's.
	 *  
	 * @param None
	 * @return list of child URLs
	 */

	public synchronized List<String> getChildDSRs() {
		List<String> result = new ArrayList<String>();
		Date currentTime = new Date();
		
		Set<String> s=childServices.keySet();
		Iterator<String> it=s.iterator();
		while(it.hasNext()) {
            String key=it.next();
            Date value=childServices.get(key);
            if ( value.getTime()+hour > currentTime.getTime()) {
				result.add(key);
			} else {
				// expired checkin entry
				childServices.remove(key);
			}
		}
		return result;
	}

	/**
	 * Add children DSR.
	 *  
	 * @param URL of the child DSR
	 * @return true if the identifier put first time into the list
	 */

	public synchronized boolean addChildDSR(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null)
			throw new NullPointerFailureException();
		if (identifier.isEmpty())
			throw new EmptyIdentifierFailureException();

		boolean retval = false;
		if (childServices.containsKey(identifier)) {
			Date currentTime = new Date();
			Date value = childServices.get(identifier);
			if ( value.getTime()+hour <= currentTime.getTime()) {
				// expired checkin entry
				retval = true;
			}
			childServices.remove(identifier);
		} else {
			// First time put the identifier into the list
			retval = true;
		}
        childServices.put(identifier, new Date());
        return retval;
	}
	
	/**
	 * Checked every child DSR with ping message.
	 * If the child is not available, remove it from the list.
	 * 
	 * @param None
	 * @return None
	 */
	public synchronized void childLiveCheck() {
		Set<String> s=childServices.keySet();
		Iterator<String> it=s.iterator();
		while(it.hasNext()) {
            String key=it.next();
			DSRClient c = new DSRClient(key + "/ping");
			if ("true".equalsIgnoreCase(DSRServer.getProperty(ISecurityProperties.REGISTRY_SSL_ENABLED, "false"))) {

				c = new DSRClient(key + "/ping",
											DSRServer.getClientSecurityProperties());
			}
			ClientResponse res = c.getClientResource().accept(MediaType.TEXT_PLAIN)
					.get(ClientResponse.class);
			if ( res.getStatus() != Status.OK.getStatusCode() ){
				childServices.remove(key);
			}
		}
	}

}
