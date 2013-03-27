package eu.emi.emir.client;
import org.junit.Test;

import eu.emi.emir.client.EMIRClient;
import static org.junit.Assert.*;
/**
 * 
 */

/**
 * @author a.memon
 *
 */
public class TestIsReachable {
	@Test
	public void testReachable(){
		EMIRClient c = new EMIRClient("http://localhost:9127");
		assertFalse(c.isReachable());
	}
}
