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
import eu.emi.emir.client.glue2.Glue2Mapper;
import eu.emi.emir.client.glue2.JSONToGlue2MappingException;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
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

	// query keywords for pagination
	public static final String SKIP = "skip";
	public static final String LIMIT = "limit";
	public static final String PAGE_SIZE = "pageSize";
	public static final String REF = "ref";

	/**
	 * @throws DatabaseUnavilableException
	 * 
	 */
	public ServiceColManager() {
		serviceDB = new MongoDBServiceDatabase();
	}

	public ServiceColManager(MongoDBServiceDatabase mongodb) {
		serviceDB = mongodb;
	}

	public JSONArray getServiceReferences() throws JSONException,
			QueryException, PersistentStoreFailureException {
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
	 * Single method to query for JSON objects
	 * 
	 * @param m
	 *            HTTP Query Parameters
	 * @return {@link JSONArray}
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * @throws JSONException
	 * @throws MongoException
	 */
	public JSONArray queryForJSON(Map<String, Object> m) throws QueryException,
			PersistentStoreFailureException, MongoException, JSONException {

		JSONArray jArr = queryForJSON(null, m);

//		if (m.get("limit") != null || m.get("skip") != null) {
//			jArr = queryWithSkipOrLimit(m);
//		} else {
//			jArr = pagedQueryForJSON(m);
//		}

		return jArr;
	}
	
	/**
	 * Single method to query for JSON objects
	 * 
	 * @param m
	 *            HTTP Query Parameters
	 * @param jo
	 *            Query in JSON format according to the MongoDB specification
	 * @return {@link JSONArray}
	 * @throws PersistentStoreFailureException
	 * @throws QueryException
	 * @throws JSONException
	 * @throws MongoException
	 */
	public JSONArray queryForJSON(JSONObject jo, Map<String, Object> m)
			throws QueryException, PersistentStoreFailureException,
			MongoException, JSONException {

		JSONArray jArr = null;

		if (m.get(LIMIT) != null || m.get(SKIP) != null) {
			jArr = queryWithSkipOrLimit(jo, m);
		} else {
			jArr = pagedQueryForJSON(jo, m);
		}

		return jArr;
	}

	/***
	 * Single method to query for Glue 2.0 XML documents
	 * 
	 * @param m
	 *            HTTP Query parameters
	 * @return A GLUE 2 XML Document
	 * @throws QueryException
	 * @throws PersistentStoreFailureException
	 * @throws JSONException
	 * @throws DatatypeConfigurationException
	 * @throws ParseException
	 * @throws JSONToGlue2MappingException
	 */
	public QueryResult queryForXML(Map<String, Object> m)
			throws QueryException, PersistentStoreFailureException,
			JSONException, DatatypeConfigurationException, ParseException,
			JSONToGlue2MappingException {
		
		QueryResult qr = queryForXML(null, m);
		
		return qr;
	}

	/***
	 * Single method to query for Glue 2.0 XML documents
	 * 
	 * @param m
	 *            HTTP Query parameters
	 * @param jo
	 *            Query in JSON format according to the MongoDB specification
	 * @return A GLUE 2 XML Document
	 * @throws QueryException
	 * @throws PersistentStoreFailureException
	 * @throws JSONException
	 * @throws DatatypeConfigurationException
	 * @throws ParseException
	 * @throws JSONToGlue2MappingException
	 */
	public QueryResult queryForXML(JSONObject jo, Map<String, Object> m)
			throws QueryException, PersistentStoreFailureException,
			JSONException, DatatypeConfigurationException, ParseException,
			JSONToGlue2MappingException {
		
		QueryResult qr = null;

		if (m.get(LIMIT) != null || m.get(SKIP) != null) {
			qr = queryGlue2(m);
		} else {
			qr = pagedQueryForXML(jo, m);
		}

		return qr;
	}

	/***
	 * Execute rich queries and also limits the returning result if containing
	 * skip or limit predicates
	 * 
	 * @param m
	 * @return
	 * @throws QueryException
	 * @throws PersistentStoreFailureException
	 * @throws MongoException
	 * @throws JSONException
	 */
	private JSONArray queryWithSkipOrLimit(JSONObject queryDoc,
			Map<String, Object> m) throws QueryException,
			PersistentStoreFailureException, MongoException, JSONException {
		Integer limit = 0;
		Integer skip = 0;
		String keyLimit = LIMIT;
		String keySkip = SKIP;
		limit = (m.get(keyLimit) != null) ? Integer.valueOf(m.get(keyLimit)
				.toString()) : 0;
		skip = (m.get(keySkip) != null) ? Integer.valueOf(m.get(keySkip)
				.toString()) : 0;
		JSONArray jArr;
		
		if (queryDoc == null) {
			queryDoc =new JSONObject(m);
		}
		
		if ((limit > 0) && (skip > 0)) {
			queryDoc.remove(LIMIT);
			queryDoc.remove(SKIP);
			jArr = serviceDB.queryJSON(queryDoc.toString(), limit, skip);
		} else if (limit > 0) {
			queryDoc.remove(LIMIT);
			jArr = serviceDB.queryJSONWithLimit(queryDoc.toString(), limit);
		} else if (skip > 0) {
			queryDoc.remove(SKIP);
			jArr = serviceDB.queryJSON(queryDoc.toString(), skip);
		} else {
			jArr = serviceDB.queryJSON(queryDoc.toString());
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
	 * @throws JSONToGlue2MappingException
	 */
	private QueryResult queryGlue2(Map<String, Object> m) throws QueryException,
			PersistentStoreFailureException, JSONException,
			DatatypeConfigurationException, ParseException,
			JSONToGlue2MappingException {
		JSONArray ja = queryForJSON(m);
		Glue2Mapper gm = new Glue2Mapper();
		QueryResult qr = gm.toQueryResult(ja);
		return qr;
	}

	/**
	 * Add pagination to the query interface
	 * 
	 * @param m
	 *            HTTP Query Parameters
	 * @return An array matching the JSON query
	 * */
	@Deprecated	
	public JSONArray pagedQueryForJSON(Map<String, Object> m)
			throws JSONException {
		JSONArray jArr = pagedQueryForJSON(null, m);
		return jArr;
	}

	/**
	 * Add pagination to the query interface
	 * 
	 * @return An array matching the JSON query
	 * */
	private JSONArray pagedQueryForJSON(JSONObject queryDoc,
			Map<String, Object> m) throws JSONException {
		Integer pageSize = 0;
		String ref = null;
		pageSize = (m.get(PAGE_SIZE) != null) ? Integer.valueOf(m
				.get(PAGE_SIZE).toString()) : 100;

		ref = (m.get(REF) != null) ? m.get(REF).toString() : null;
		JSONArray jArr;

		if (queryDoc == null) {
			queryDoc = new JSONObject(m);
		}

		queryDoc.remove(PAGE_SIZE);
		queryDoc.remove(REF);
		if (queryDoc.length() > 0)
			jArr = serviceDB.paginatedQuery(queryDoc.toString(), pageSize, ref);
		else
			jArr = serviceDB.paginatedQuery("{}", pageSize, ref);

		// when the query response is empty
		if (jArr.length() == 0) {
			return new JSONArray();
		}

        if ( jArr.length() >= pageSize) {
		    JSONObject doc = new JSONObject(jArr.get(jArr.length() - 1).toString());

		    JSONObject refObj = new JSONObject();

		    refObj.put(REF, doc.getJSONObject("_id").get("$oid"));
		    jArr.put(refObj);
        }

		return jArr;
	}	

	/**
	 * Add pagination to the query interface
	 * 
	 * @return GLUE 2 XML document
	 * 
	 * @throws JSONToGlue2MappingException
	 * */
	private QueryResult pagedQueryForXML(JSONObject queryDoc,
			Map<String, Object> m) throws JSONException,
			JSONToGlue2MappingException {
		Glue2Mapper gm = new Glue2Mapper();
		QueryResult qr = gm.toQueryResult(pagedQueryForJSON(queryDoc, m));
		return qr;
	}

}
