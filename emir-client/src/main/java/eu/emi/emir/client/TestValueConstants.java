/**
 * 
 */
package eu.emi.emir.client;

import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;

/**
 * @author a.memon
 *
 */
public class TestValueConstants {
	public static JSONObject getJSONWithMandatoryAttributes() throws JSONException{
		JSONObject jo = new JSONObject();
		JSONArray capJa = new JSONArray();
		capJa.put("1");
		DateUtil.setExpiryTime(jo, 365);
		jo.put(ServiceBasicAttributeNames.SERVICE_ID.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://1");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(), capJa);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER.getAttributeName(), "1");
		jo.put(ServiceBasicAttributeNames.SERVICE_OWNER_DN.getAttributeName(), "cn=anonym,o=org");
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY.getAttributeName(), "webservices");
		return jo;
	}
	
	public static JSONArray getSingleJSONArrayWithMandatoryAttributes() throws JSONException{
		JSONArray ja = new JSONArray();
		ja.put(getJSONWithMandatoryAttributes());
		return ja;
	}
	
	public static JSONArray getDummyJSONArrayWithMandatoryAttributes(int index) throws JSONException{
		if (index <= 0) {
			throw new NullPointerException();
		}
		JSONArray ja = new JSONArray();
		for (int i = 0; i < index; i++) {
			JSONObject j = getJSONWithMandatoryAttributes();
			String id = UUID.randomUUID().toString();
			j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), id);
			j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName(), "http://url/"+id);
			ja.put(j);	
		}
		
		return ja;
	}
}
