/**
 * 
 */
package eu.emi.emir.p2p;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.MongoException;

import eu.emi.client.util.Log;
import eu.emi.emir.DSRServer;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.util.DateUtil;

/**
 * Periodically database entries remove checking by the GSR
 * 
 * @author g.szigeti
 * 
 */
public class RemoveCheck implements Runnable {
	private static Logger logger = Log.getLogger(Log.DSR, RemoveCheck.class);
	private int etremove;
	private int timedelay;
	private MongoDBServiceDatabase mongoDB;
	
	/** 
	 * Constructor for a Soft-State mechanism
	 * 
	 */
	public RemoveCheck(int delay) {
		timedelay = delay;
		mongoDB = new MongoDBServiceDatabase();

		try {
			etremove = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_ETREMOVE));
			if (etremove < 24) {
				logger.info("Configured etremove value (" + etremove + ") is very low. Min value: 24 Default value will be used.");
				etremove = 24;
			}
			logger.info("Set the etremove to "+ etremove);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (24hours) value of etremove.");
			etremove = 24;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		/** remove all GSRs from the database that are contains only the following elements:
		 *        Service_Endpoint_URL
		 *        updateSince
		 *  and not contains the following elements:
		 *        Service_CreationTime
		 *        Service_ExpireOn
		 */
		String query = "{ $and: [ "+
		"{ $and: [ { Service_Endpoint_URL : { $exists : true } }, { updateSince : { $exists : true } } ] }" +
		"," +
		"{ $and: [ { Service_CreationTime : { $exists : false } }, { Service_ExpireOn : { $exists : false } } ] }" +
		"] }";
		while(true){
			if (logger.isTraceEnabled()) {
				logger.trace("Soft-State thread (remove) started");
			}
			JSONObject currentTime = DateUtil.setExpiryTimeWithHours(new JSONObject(),timedelay);
			
			try {
				String extendedQuery = "{ $and: [ ";			
				extendedQuery += query;
				extendedQuery += ", { \"updateSince\" : { $lt: "+ currentTime.getString("Service_ExpireOn")+"} }";
				extendedQuery += "] }";
				logger.info(extendedQuery);

				mongoDB.findAndDelete(query);
			} catch (MongoException e) {
				logger.warn(e.getCause());
			} catch (JSONException e) {
				logger.warn(e.getCause());
			}
			
			try {
				Thread.sleep(etremove*60*60*1000);
			} catch (InterruptedException e) {
				Log.logException("", e);
			}
		}
	}

}
