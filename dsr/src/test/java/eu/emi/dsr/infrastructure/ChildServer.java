/**
 * 
 */
package eu.emi.dsr.infrastructure;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;

/**
 * @author a.memon
 *
 */
public class ChildServer extends AbstractServer{
	public static void main(String[] args) {
		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase("localhost", 27017, "emiregistry", "client-services");
		ChildServer cs = new ChildServer();
		cs.start();
		Runtime rt = Runtime.getRuntime();
	    System.err.println("Main: adding shutdown hook");
	    
	    rt.addShutdownHook(new Thread() {
	      public void run() {
	        parentDB.dropCollection();
	      }
	    });
	    System.err.println("Main: calling Runtime.exit()");
	    
	}
	
	public void start(){
		Configuration c = getConfiguration("localhost", 9000, "locahost",
				27017, "emiregistry-childdb", false, "http://localhost:9001");
		
		DSRServer client = new DSRServer(c);		
		
		
		client.startJetty();
		url = client.getBaseUrl();
	}
	
	
	
	
	
}
