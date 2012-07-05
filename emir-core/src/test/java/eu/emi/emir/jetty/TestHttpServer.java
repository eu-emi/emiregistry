/**
 * 
 */
package eu.emi.emir.jetty;

import java.util.Properties;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.db.mongodb.MongoDBTestBase;

/**
 * @author a.memon
 *
 */
public class TestHttpServer extends MongoDBTestBase{
	HttpServer h = null;
	@Before
	public void setup() throws Exception{
		Properties p = new Properties();
		EMIRServer server = new EMIRServer(p);
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_ADDRESS, "http://localhost:54321");
		p.put(ServerProperties.PREFIX+ServerProperties.PROP_ANONYMOUS_PORT, "8080");
		h = new HttpServer(p);
	}
	
	@Test
	public void testRun() throws Exception{
		h.start();
		EMIRClient cr = new EMIRClient("http://localhost:8080");
		System.out.println(cr.getClientResource().path("/services").get(JSONArray.class).toString());
		System.out.println(cr.getClientResource().path("/ping").get(JSONObject.class).toString());
	}
	
	@After
	public void tearDown() throws Exception{
		h.stop();
	}
}
