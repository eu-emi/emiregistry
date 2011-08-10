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
		ChildServer cs = new ChildServer();
		cs.start();
	    System.err.println("Main: adding shutdown hook");
	}
	
	public void start(){
		Configuration c = getConfiguration("localhost", 9000, "localhost",
				27017, "emiregistry-childdb", false, "http://localhost:9001");
		
		DSRServer client = new DSRServer(c);		
		
		
		client.startJetty();
		url = client.getBaseUrl();
	}
	
	
	
	
	
}
