/**
 * 
 */
package eu.emi.dsr.infrastructure;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;

/**
 * @author a.memon
 *         g.szigeti
 *
 */
public class ChildServer extends AbstractServer{
	private static DSRServer client = null;
	
	public static void main(String[] args) {
		ChildServer cs = new ChildServer();
		cs.start();
	}
	
	public void start(){
		Configuration c = getConfiguration("localhost", 9000, "localhost",
				27017, "emiregistry-childdb", false, "http://localhost:9001");
		
		client = new DSRServer(c);		
		
		
		client.startJetty();
		url = client.getBaseUrl();
		System.err.println("ChildServer started.");
	}
	
	public void stop(){
		if (client.isStarted()){
			client.stopJetty();
			System.err.println("ChildServer stopped");
		}
	}
	
	
}
