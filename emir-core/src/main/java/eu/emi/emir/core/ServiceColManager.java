/**
 * 
 */
package eu.emi.emir.core;

import java.text.ParseException;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.mongodb.MongoException;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.glue2.Glue2Mapper;
import eu.emi.emir.glue2.JSONToGlue2MappingException;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class ServiceColManager {
	@SuppressWarnings("unused")
	private static final Logger logger = Log.getLogger(Log.EMIR_CORE,
			ServiceColManager.class);
	private ServiceDatabase serviceDB = null;

	/**
	 * @throws DatabaseUnavilableException 
	 * 
	 */
	public ServiceColManager(){
		serviceDB = new MongoDBServiceDatabase();
	}
	
	public ServiceColManager(MongoDBServiceDatabase mongodb){
		serviceDB = mongodb;
	}

	public JSONArray getServiceReferences() throws JSONException, QueryException,
			PersistentStoreFailureException {
		JSONArray arr = new JSONArray();

		arr = serviceDB
			.queryDistinctJSON(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName());
		return arr;
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
	 * @return {@link JSONArray}
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * @throws JSONException 
	 * @throws MongoException 
	 */
	public JSONArray query(JSONObject queryDoc) throws QueryException,
			PersistentStoreFailureException, MongoException, JSONException {
		return serviceDB.queryJSON(queryDoc.toString());
	}
	
	/**
	 * @param m
	 * @return
	 * @throws PersistentStoreFailureException 
	 * @throws QueryException 
	 * @throws ParseException 
	 * @throws DatatypeConfigurationException 
	 * @throws JSONException 
	 * @throws JSONToGlue2MappingException 
	 */
	public QueryResult queryGlue2(Map<String, Object> m) throws QueryException, PersistentStoreFailureException, JSONException, DatatypeConfigurationException, ParseException, JSONToGlue2MappingException {
		JSONArray ja = query(m);		
		Glue2Mapper gm = new Glue2Mapper();
		QueryResult qr = gm.toQueryResult(ja);
		return qr;
	}
	
	
	public QueryResult queryGlue2(JSONObject queryDoc) throws QueryException, PersistentStoreFailureException, JSONException, DatatypeConfigurationException, ParseException, JSONToGlue2MappingException {
		Glue2Mapper gm = new Glue2Mapper();
		QueryResult qr = gm.toQueryResult(serviceDB.queryJSON(queryDoc.toString()));
		return qr;
	}
	
	
	/**
	 * Add pagination to the query interface 
	 * */
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
		
		// when the query response is empty 
		if (jArr.length() == 0) {
			return new JSONObject();
		}
		
		JSONObject doc = new JSONObject(jArr.get(jArr.length()-1).toString());
		
		jObj.put("ref", doc.getJSONObject("_id").get("$oid"));
		jObj.put("result", jArr);
		return jObj;
	}

	
}
