package eu.emi.emir.p2p;

import org.apache.log4j.Logger;

import eu.emi.emir.DSRServer;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.core.RegistryThreadPool;
import eu.emi.emir.core.ServerConstants;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventTypes;

/**
 * @author g.szigeti
 * 
 */
public class StartStopMethods {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE, DSRServer.class);
	private static String myURL = "";

	public static void startGSRFunctions() {
		// Neighbors event receiver start
		RegistryThreadPool.getExecutorService().execute(
				new NeighborsEventReciever());

		// Message(s) send event receiver start
		RegistryThreadPool.getExecutorService().execute(
				new eu.emi.emir.p2p.ServiceEventReceiver());

		// Set own endpoint URL
		myURL = DSRServer.getProperty(ServerConstants.REGISTRY_SCHEME).toString() +"://"+
		        DSRServer.getProperty(ServerConstants.REGISTRY_HOSTNAME).toString() +":"+
		        DSRServer.getProperty(ServerConstants.REGISTRY_PORT).toString();
		
		// Remove own entry from the local database
		deleteOwnEntry();
		
		// Self registration start
		try {
			RegistryThreadPool.getExecutorService().execute(
					new SelfRegistration(myURL));
		} catch (Throwable e) {
			logger.warn("Has a problem with the self-registration.");
		}
		
		//Soft-State functions start
		int timedelay;
		// time delay for a validity methods, extend the time windows with this extra time
		try {
			timedelay = Integer.valueOf(DSRServer
					.getProperty(ServerConstants.REGISTRY_GLOBAL_SOFTSTATE_DELAY));
			if (timedelay < 0) {
				logger.info("Configured Soft-State timedelay value (" + timedelay + ") is very low. Min value: 0 Default value (2 hours) will be used.");
				timedelay = 2;
			}
			logger.info("Set the Soft-State timedelay to "+ timedelay);
		} catch (NumberFormatException e) {
			// set default value
			logger.info("Set the default (2 hours) value of Soft-State timedelay.");
			timedelay = 2;
		}
		try {
			RegistryThreadPool.getExecutorService().execute(
					new ValidityCheck(timedelay));
		} catch (Throwable e) {
			logger.warn("Has a problem with the validity check.");
		}
		try {
			RegistryThreadPool.getExecutorService().execute(
					new RemoveCheck(timedelay));
		} catch (Throwable e) {
			logger.warn("Has a problem with the remove check.");
		}
	}

	public static void stopGSRFunctions() {
		System.out.println("Send DELETE message to the neighbors.");
		logger.info("Send DELETE message to the neighbors.");
		
		Event event = new Event(EventTypes.SERVICE_DELETE, myURL);
		new eu.emi.emir.p2p.ServiceEventReceiver().recieve(event);
		deleteOwnEntry();
	}
	
	private static void deleteOwnEntry(){
		try {
			new MongoDBServiceDatabase().deleteByUrl(myURL);
		} catch (Exception e) {
			Log.logException("Error in the delete procedure ", e);
		}
	}
}
