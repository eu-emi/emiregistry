/**
 * 
 */
package eu.emi.dsr.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
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
		System.out.println((DBObject) JSON.parse(jo.toString()));
	}

	@Test
	public void testArray() throws JSONException {
		Map<String, String> map = new HashMap<String, String>();
		Character c = new Character('/');
		UnicodeBlock d = null;
		map.put("serviceurl", "http:/");
		JSONObject jo = new JSONObject(map);

		JSONArray j = new JSONArray();
		j.put(map);
		j.put(map);
		j.put(map);
		JSONObject arr = new JSONObject();
		arr.put("services", j);

		System.out.println(arr);
	}
	
	@Test
	public void testJSON() throws Exception {

		/*
		 * Get input stream of our data file. This file can be in the root of
		 * you application folder or inside a jar file if the program is packed
		 * as a jar.
		 */
		InputStream is = new FileInputStream(new File("src/test/resources/serviceinfo.json"));
		
		/*
		 * Call the method to convert the stream to string
		 */
		JSONObject jo = new JSONObject(convertStreamToString(is));
		assertNotNull(jo.get("Service.Endpoint.URL"));
	}

	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
