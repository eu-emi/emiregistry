/**
 * 
 */
package eu.emi.emir.db.mongodb;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.db.mongodb.ServiceObject;
import eu.emi.emir.db.mongodb.ServiceObjectBuilder;

import static org.junit.Assert.*;
/**
 * @author a.memon
 *
 */
public class TestServiceObjectBuilder {
	@Test
	public void test1() throws JSONException{
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "http://service1");
		JSONObject o = new JSONObject(map);
		ServiceObject so = ServiceObjectBuilder.build(o);
		assertEquals("http://service1", so.getEndpointID());
	}
}

