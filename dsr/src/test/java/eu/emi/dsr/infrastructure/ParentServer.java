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
public class ParentServer extends AbstractServer{
	public static void main(String[] args) {
		final MongoDBServiceDatabase parentDB = new MongoDBServiceDatabase("localhost", 27017, "emiregistry", "client-services");
		ParentServer cs = new ParentServer();
		cs.start();
		
		
		Runtime rt = Runtime.getRuntime();
	    System.err.println("Main: adding shutdown hook");
	    rt.addShutdownHook(new Thread() {
	      public void run() {
	        System.out.println("dropping parent collection");
	    	  parentDB.dropCollection();
	        
	      }
	    });
	    System.err.println("Main: calling Runtime.exit()");
//	    rt.exit(0);
	}
	
	public void start(){
		Configuration c = getConfiguration("localhost", 9001, "locahost",
				27017, "emiregistry-parentdb", false, null);
		
		DSRServer client = new DSRServer(c);		
		
		
		client.startJetty();
		url = client.getBaseUrl();
	}
	
}
