/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.io.File;
import java.util.Properties;

import eu.emi.emir.EMIRServer;

/**
 * @author a.memon
 *         g.szigeti
 *
 */
public class ChildServer extends AbstractServer{
	private static EMIRServer client = null;
	private static ChildServer cs = null;
	
	public static void main(String[] args) {
		cs = new ChildServer();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("stopping now (shutdown hook)");
				try {
					Thread.sleep(1000);
					cs.stop();
				} catch (InterruptedException e) {
					System.out.println("failure");
				}
				System.out.println("stopped (shutdown hook)");
			}
		});
		cs.start();
	}
	
	public void start(){
		//Remove the H2 database
		String h2FileName = "./data/Emiregistry-child";
		   // A File object to represent the filename
	    File f = new File(h2FileName.substring(0, 6));

	    // Make sure the file or directory exists and isn't write protected
	    if (!f.exists()) {
	    	System.out.println("Delete: no such file or directory: " + h2FileName.substring(0, 6));
	    }
	
	    // If it is a directory, make sure it is empty
	    if (f.isDirectory()) {
	    	String[] files = f.list();
	    	if (files.length > 0) {
	    		System.out.println("Delete: directory not empty: " + h2FileName.substring(0, 6));
	    	}
	    	for (int i=0; i< files.length; i++){
	    		// Attempt to delete it
	    		if (f.listFiles()[i].canWrite() && f.listFiles()[i].toString().subSequence(0, 24).equals(h2FileName)) {
		    		System.out.println("deleted: "+ f.listFiles()[i]);
	    			f.listFiles()[i].delete();
	    		}
	    	}
	    }
	    Properties c = getConfiguration("localhost", 9000, "localhost",
				27017, "emiregistry-childdb", false, "http://localhost:9001",
				h2FileName);
		
		client = new EMIRServer();	
		
		client.run(c);
		System.err.println("ChildServer started.");
	}
	
	public void stop(){
		client.stop();
		System.err.println("ChildServer stopped");
	}

}
