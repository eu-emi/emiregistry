/**
 * 
 */
package eu.emi.dsr.db.mongodb;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author a.memon
 *
 */
public class ServiceObjectBuilder extends BasicDBObjectBuilder{
	public static ServiceObject build(JSONObject jobj) throws JSONException{
		ServiceObject s = new ServiceObject(jobj.toString());
		return s;
	}
	public static ServiceObject build(DBObject sobj) throws JSONException{
		ServiceObject s = new ServiceObject(sobj.toString());
		return s;
	}
	
	
}	
