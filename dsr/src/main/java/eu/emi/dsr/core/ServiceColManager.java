/**
 * 
 */
package eu.emi.dsr.core;

import java.text.ParseException;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.mongodb.MongoException;

import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.glue2.Glue2Mapper;
import eu.emi.dsr.util.Log;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon
 * 
 */
public class ServiceColManager {
	private static Logger logger = Log.getLogger(Log.DSR,
			ServiceColManager.class);
	private ServiceDatabase serviceDB = null;

	/**
	 * 
	 */
	public ServiceColManager() {
		serviceDB = new MongoDBServiceDatabase();
	}

	public JSONObject getServiceReferences() throws JSONException {
		JSONArray j = new JSONArray();
		j.put("http://1");
		j.put("http://1");
		JSONObject o = new JSONObject();
		o.put("references", j);
		return o;
	}

	public JSONArray getServicesByType(String serviceType)
			throws JSONException, QueryException,
			PersistentStoreFailureException {
		if (serviceType.isEmpty()) {
			throw new IllegalArgumentException("\"servicetype\" is not defined");
		}
		JSONArray arr = new JSONArray();

		arr = serviceDB.queryJSON("{\""
				+ ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName()
				+ "\":\"" + serviceType + "\"}");
		return arr;
	}

	public JSONArray getDistinctTypes() throws JSONException, QueryException,
			PersistentStoreFailureException {
		JSONArray arr = new JSONArray();

		arr = serviceDB
				.queryDistinctJSON(ServiceBasicAttributeNames.SERVICE_TYPE
						.getAttributeName());
		return arr;
	}

	/**
	 * @param jo
	 * @throws JSONException
	 */
	public JSONObject queryServiceCollection(JSONObject jo)
			throws JSONException {
		JSONArray ja = new JSONArray();
		JSONObject resultSet = new JSONObject();
		for (int i = 0; i < 50; i++) {
			JSONObject j = new JSONObject();
			j.put("serviceurl", "http://url-" + i);
			j.put("servicetype", "type-" + i);
			j.put("servicetype", "1type-" + i);
			j.put("servicetype", "2type-" + i);
			j.put("servicetype", "3type-" + i);
			j.put("servicetype", "4type-" + i);
			j.put("servicetype", "5type-" + i);
			j.put("servicetype", "6type-" + i);
			j.put("servicetype", "7type-" + i);
			ja.put(j);
		}
		resultSet.put("result", ja);
		return resultSet;
	}

	/**
	 * @param m
	 * @return {@link JSONArray}
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * @throws JSONException 
	 * @throws MongoException 
	 */
	public JSONArray query(Map<String, Object> m) throws QueryException,
			PersistentStoreFailureException, MongoException, JSONException {

		Integer limit = 0;
		Integer skip = 0;
		String keyLimit = "limit";
		String keySkip = "skip";
		limit = (m.get(keyLimit) != null) ? Integer.valueOf(m.get(keyLimit)
				.toString()) : 0;
		skip = (m.get(keySkip) != null) ? Integer.valueOf(m.get(keySkip)
				.toString()) : 0;
		JSONArray jArr;
		JSONObject j = new JSONObject(m);
		if ((limit != 0) && (skip != 0)) {
			j.remove("limit");
			j.remove("skip");
			jArr = serviceDB.queryJSON(j.toString(), limit, skip);
		} else if (limit != 0) {
			j.remove("limit");
			jArr = serviceDB.queryJSONWithLimit(j.toString(), limit);
		} else if (skip != 0) {
			j.remove("skip");
			jArr = serviceDB.queryJSON(j.toString(), skip);
		} else {
			jArr = serviceDB.queryJSON(j.toString());
		}

		return jArr;
	}
	
	/**
	 * @param m
	 * @return
	 * @throws PersistentStoreFailureException 
	 * @throws QueryException 
	 * @throws ParseException 
	 * @throws DatatypeConfigurationException 
	 * @throws JSONException 
	 */
	public QueryResult queryGlue2(Map<String, Object> m) throws QueryException, PersistentStoreFailureException, JSONException, DatatypeConfigurationException, ParseException {
		JSONArray ja = query(m);
		logger.debug("array"+ja);
		Glue2Mapper gm = new Glue2Mapper();
		QueryResult qr = gm.toQueryResult(ja);
		return qr;
	}
	
	

	public JSONObject pagedQuery(Map<String, Object> m) throws JSONException {
		Integer pageSize = 0;
		String ref = null;
		String keyPageSize = "pageSize";
		String keyRef = "ref";
		JSONObject jObj = new JSONObject();
		pageSize = (m.get(keyPageSize) != null) ? Integer.valueOf(m.get(
				keyPageSize).toString()) : 100;
		
		ref = (m.get(keyRef) != null) ? m.get(keyRef).toString() : null;
		JSONArray jArr;
		JSONObject j = new JSONObject(m);
		j.remove(keyPageSize);
		j.remove(keyRef);
		if (j.length() > 0)
			jArr = serviceDB.paginatedQuery(j.toString(), pageSize, ref);
		else
			jArr = serviceDB.paginatedQuery("{}", pageSize, ref);
		
		
		
		JSONObject doc = new JSONObject(jArr.get(jArr.length()-1).toString());
		
		jObj.put("ref", doc.getJSONObject("_id").get("$oid"));
		jObj.put("result", jArr);
		return jObj;
	}

	
}
