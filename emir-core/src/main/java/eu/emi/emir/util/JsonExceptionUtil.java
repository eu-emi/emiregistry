package eu.emi.emir.util;

import java.util.Arrays;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JsonExceptionUtil {
	public static JSONObject toJson(Exception e){
		JSONObject jo = new JSONObject();
		try {
			jo.put("ErrorMessage", e.getMessage());
			jo.put("ErrorTrace", Arrays.toString(e.getStackTrace()));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return jo;
	}
}
