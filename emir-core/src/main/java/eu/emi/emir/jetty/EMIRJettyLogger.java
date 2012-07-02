/**
 * 
 */
package eu.emi.emir.jetty;

import eu.emi.emir.client.util.Log;
import eu.unicore.util.jetty.JettyLogger;

/**
 * @author a.memon
 *
 */
public class EMIRJettyLogger extends JettyLogger{
	/* (non-Javadoc)
	 * @see eu.unicore.util.jetty.JettyLogger#getName()
	 */
	@Override
	public String getName() {
		return Log.EMIR_HTTPSERVER+ "." +EMIRJettyLogger.class.getName();
	}

}
