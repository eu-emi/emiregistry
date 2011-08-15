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
public class ParentServer extends AbstractServer{
	private static DSRServer client = null;
	
	public static void main(String[] args) {
		ParentServer cs = new ParentServer();
		cs.start();
	}
	
	public void start(){
		Configuration c = getConfiguration("localhost", 9001, "localhost",
				27017, "emiregistry-parentdb", false, null,
				"./Emiregistry","sa", "");
		
		client = new DSRServer(c);		
		
		
		client.startJetty();
		url = client.getBaseUrl();
		System.err.println("ParentServer started.");
	}
	
	public void stop(){
		if (client.isStarted()){
			client.stopJetty();
			System.err.println("ParentServer stopped");
		}	
	}
	
}
