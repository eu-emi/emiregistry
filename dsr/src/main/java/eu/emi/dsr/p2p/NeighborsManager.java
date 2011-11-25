package eu.emi.dsr.p2p;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.mongodb.MongoException;

import eu.emi.client.util.Log;
import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.event.EventTypes;



public class NeighborsManager {
	private static Logger logger = Log.getLogger(Log.DSR,
								NeighborsManager.class);
	private static NeighborsManager instance = null;
	private List<String> neighbors;
	int neighbors_count;
	private Hashtable<String, String> hash;
	private ServiceDatabase serviceDB = null;
	private String myURL;
	private int sparsity;

	protected NeighborsManager() {
		neighbors = new ArrayList<String>();
		neighbors_count = 0;
		myURL = DSRServer.getProperty(ServerConstants.REGISTRY_SCHEME).toString() +"://"+
				DSRServer.getProperty(ServerConstants.REGISTRY_HOSTNAME).toString() +":"+
				DSRServer.getProperty(ServerConstants.REGISTRY_PORT).toString();
		hash = new Hashtable<String, String>();
		serviceDB = new MongoDBServiceDatabase();
		// config parse
		/*
		 * retry
		 * sparsity
		 * etvalid
		 * etremove
		 */
		sparsity = 2;
		// connect to the network
		// Connection to the cloud in 6 steps.
        // 1. step: Put it's own InfoProvider URL(s) from configuration in the set of providers.
		// 2.-6. steps are in the BootStrap function.
		//BootStrap(retry);
	}
	
	public static synchronized NeighborsManager getInstance() {
	      if(instance == null) {
	         instance = new NeighborsManager();
	      }
	      return instance;
	}
	
	/**
	 * Clear the hash table and set the neighbors count to 0.
	 *  
	 * @param None
	 */
	public void hashClear(){
		hash.clear();
		neighbors_count =0;
	}
	
	/**
	 * Get list of neighbors.
	 *  
	 * @param None
	 * @return list of neighbors URLs or own URL
	 */

	public synchronized List<String> getNeighbors(){
		if (neighbors.isEmpty()){
			List<String> tmp = new ArrayList<String>();	
			tmp.add(myURL);
			return tmp;
		}
		return neighbors;
	}

	/**
	 * Add neighbors DSRs.
	 *  
	 * @param URL of the global DSRs
	 */

	public synchronized void addNeighborsDSRs(JSONArray entries, String type){
		if (logger.isDebugEnabled()) {
			logger.debug("addNeighborsDSRs called");
			logger.debug("hash: "+ hash.toString());
		}
		boolean updateNeed = false;
		if (type.equalsIgnoreCase(EventTypes.SERVICE_DELETE)){
			updateNeed = true;
		} else {
			for (int i=0; i<entries.length(); i++){
				try {
					if (!hash.containsValue(entries.getJSONObject(i).get("Service_Endpoint_URL"))){
						updateNeed = true;
					}
				} catch (JSONException e) {
					logger.warn(e.getCause());
				}
			}
		}
        if (updateNeed){
        	Neighbors_Update();
        }
	}
	
	/**
	 * Set unavailable neighbor DSR.
	 *  
	 * @param URL of the unavailable neighbor DSR
	 */
	public synchronized void setUnavailableNeighbor(String url){
		logger.warn("Unavailable neighbor: " + url);
	}

	
	private void Neighbors_Update(){
		if (logger.isDebugEnabled()) {
			logger.debug("Neighbors_Update called");
		}

		JSONArray gsrList = new JSONArray();
		// get the list of GSRs from the database
		try {
			gsrList = serviceDB.queryJSON("{\"Service_Type\" : \"GSR\"}");
		} catch (MongoException e) {
			logger.warn(e.getCause());
		} catch (QueryException e) {
			logger.warn(e.getCause());
		} catch (PersistentStoreFailureException e) {
			logger.warn(e.getCause());
		} catch (JSONException e) {
			logger.warn(e.getCause());
		}
		
		// hash_table recalculate
		hash.clear();
		for(int i=0; i<gsrList.length(); i++){
			String url;
			try {
				url = gsrList.getJSONObject(i).getString("Service_Endpoint_URL");
				// hash calculation
				byte[] bytesOfMessage = url.getBytes("UTF-8");
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] digest = md.digest(bytesOfMessage);
				BigInteger bigInt = new BigInteger(1,digest);
				String hashtext = bigInt.toString(16);
				
				// put an entry into the hash table
				hash.put(hashtext, url);

			} catch (JSONException e) {
				logger.warn(e.getCause());
			} catch (NoSuchAlgorithmException e) {
				logger.warn(e.getCause());
			} catch (UnsupportedEncodingException e) {
				logger.warn(e.getCause());
			}
		}
		// neighbors count update
        // log(2)x = (log(10)x)/(log(10)2)
		int new_neighbors_count = 0;
        if (hash.size() > 0){
            new_neighbors_count = (int)Math.ceil(Math.log10((double)hash.size())
            		/Math.log10((double)sparsity));
        }
        logger.debug("Neighbors count recalculate from " + neighbors_count +
        		                                " to "+ new_neighbors_count);

		// neighbors list filling
        Neighbors_Calculate(new_neighbors_count);
        neighbors_count = new_neighbors_count;
	}
	
	private void Neighbors_Calculate(int count){
		if (logger.isDebugEnabled()) {
			logger.debug("Neighbors_Calculate called");
		}

		// Find own position in the hash table
        Collection<String> c = hash.values();
        Iterator<String> itr = c.iterator();
        Iterator<String> itrFirst = c.iterator();
        while(itr.hasNext()){ 
        	if (itr.next().equals(myURL)){
        		break;
        	}
        }

        int sum_step = 1;
        int previous_step = 0;
        String currentEntry = "";
        neighbors.clear();
         
        for (int i=0; i<count; i++) {
             //calculate the next neighbors
            for (int step=0; step<(sum_step-previous_step); step++){
                if (!itr.hasNext()){
            	    itr = itrFirst;
                }
            	currentEntry = itr.next();
            }
            previous_step = sum_step;
            sum_step = sum_step*sparsity;
            neighbors.add(currentEntry);
        }
	}
	
	private void BootStrap(int retry_count){
		// 2. step: goto InfoProviderGSR (one of the list)
		// 3. step: Send Query message to the providerGSR with Filter
		// 4. step: Hash table and neighbors filling
		// 5. step: Connect message send to one ISIS of the neighbors
		// 6. step: response data processing (DB sync, Config saving)
	}
}
