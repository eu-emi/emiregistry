/**
 * 
 */
package eu.emi.emir;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.util.DateUtil;

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
		return jo;
	}
	
	public static JSONArray getJSONArrayWithMandatoryAttributes() throws JSONException{
		JSONArray ja = new JSONArray();
		ja.put(getJSONWithMandatoryAttributes());
		return ja;
	}
}
