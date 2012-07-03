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
public class ParentServer extends AbstractServer{
	private static EMIRServer client = null;
	
	public static void main(String[] args) {
		ParentServer cs = new ParentServer();
		cs.start();
	}
	
	public void start(){
		//Remove the H2 database
		String h2FileName = "./data/Emiregistry-parent";
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
	    Properties c = getConfiguration("localhost", 9001, "localhost",
				27017, "emiregistry-parentdb", false, null,
				h2FileName);    
		
		client = new EMIRServer();		
		
		
		client. run(c);
		System.err.println("ParentServer started.");
	}
	
	public void stop(){
		client.stop();
		System.err.println("ParentServer stopped");	
	}
	
}
