/**
 * 
 */
package eu.emi.dsr.core;

import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestJSON {
	@Test
	public void test() throws JSONException {
		Map<String, String> map = new HashMap<String, String>();
		Character c = new Character('/');
		UnicodeBlock d = null;
		map.put("serviceurl", "http:/");
		JSONObject jo = new JSONObject(map);
		System.out.println(jo.get("serviceurl"));
		assertEquals("http:/", jo.get("serviceurl"));
		System.out.println((DBObject)JSON.parse(jo.toString()));
	}
}
