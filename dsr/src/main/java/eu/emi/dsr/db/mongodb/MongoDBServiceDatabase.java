package eu.emi.dsr.db.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.print.ServiceUI;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.event.Event;
import eu.emi.dsr.event.EventManager;
import eu.emi.dsr.event.EventTypes;
import eu.emi.dsr.util.Log;
import eu.emi.dsr.util.ServiceUtil;

import com.mongodb.*;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.util.JSON;

/**
 * @author martoni
 * @author a.memon
 * 
 */
public class MongoDBServiceDatabase implements ServiceDatabase {
	private static Logger logger = Log.getLogger(Log.DSRDB,
			MongoDBServiceDatabase.class);
	private Mongo connection;
	private DB database;
	private DBCollection serviceCollection;

	public MongoDBServiceDatabase() {
		if (DSRServer.getConfiguration() == null) {
			DSRServer s = new DSRServer(new Configuration(new Properties()));
		}
		String hostname = DSRServer.getConfiguration().getProperty(
				ServerConstants.MONGODB_HOSTNAME, "localhost");
		String port = DSRServer.getConfiguration().getProperty(
				ServerConstants.MONGODB_PORT, "27017");
		String dbName = DSRServer.getConfiguration().getProperty(
				ServerConstants.MONGODB_DB_NAME, "emiregistry");
		String colName = DSRServer.getConfiguration().getProperty(
				ServerConstants.MONGODB_COLLECTION_NAME, "services");
		try {
			connection = MongoConnection.get(hostname, Integer.valueOf(port));
			database = connection.getDB(dbName);
			serviceCollection = database.getCollection(colName);
			
			// setting index and uniquesness on "serviceUrl"
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
					"1");
			logger.info(obj);
			serviceCollection.ensureIndex(obj,
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(), true);
			
		} catch (UnknownHostException e) {
			Log.logException(e);
		} catch (MongoException e) {
			Log.logException(e);
		}

	}

	/**
	 * @param hostname
	 *            - default <b>localhost</b>
	 * @param port
	 *            - default <b>27017</b>
	 * @param dbName
	 *            - default <b>emiregistry</b>
	 * @param colName
	 *            - default <b>services</b>
	 * */
	public MongoDBServiceDatabase(String hostname, Integer port, String dbName,
			String colName) {
		try {
			if (DSRServer.getConfiguration() == null) {
				DSRServer s = new DSRServer(new Configuration(new Properties()));
			}
			connection = MongoConnection.get(hostname, port);
			database = connection.getDB(dbName);

			
			serviceCollection = database.getCollection(colName);
			
			

			// setting index and uniquesness on service url
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(),
					"1");
			logger.info(obj);
			serviceCollection.ensureIndex(obj,
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(), true);
			
		} catch (UnknownHostException e) {
			Log.logException(e);
		} catch (MongoException e) {
			Log.logException(e);
		}
	}

	@Override
	public void insert(ServiceObject item) throws ExistingResourceException,
			PersistentStoreFailureException {
		List<String> lstError = new CopyOnWriteArrayList<String>();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("inserting: " + item.toDBObject());
			}

			DBObject db = item.toDBObject();
			db.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), new Date());
			serviceCollection.insert(db, WriteConcern.SAFE);
			EventManager.notifyRecievers(new Event(EventTypes.SERVICE_ADD, db));
		} catch (MongoException e) {
			if (e instanceof DuplicateKey) {
				throw new ExistingResourceException("Service with URL: "
						+ item.getUrl() + " - already exists", e);
			} else {
				throw new PersistentStoreFailureException(e);
			}
			
		}

	}

	@Override
	public ServiceObject getServiceByUrl(String identifier)
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		ServiceObject so = null;
		try {

			DBObject db = serviceCollection.findOne(new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
							.getAttributeName(), identifier));
			if (db == null) {
				return null;
			}
			so = new ServiceObject(db);
			

		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			throw new NonExistingResourceException(e);
		}

		return so;
	}

	@Override
	public void deleteByUrl(String url) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		BasicDBObject query = new BasicDBObject();
		query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), url);
		DBObject d = serviceCollection.findAndRemove(query);
		if (d == null) {
			if (logger.isDebugEnabled()) {
				String msg = "No service description with the URL:" + url
						+ " exists";
				logger.debug(msg);
			}
			throw new NonExistingResourceException(
					"No service description with the URL:" + url + " exists");
		}

	}

	@Override
	public void update(ServiceObject sObj) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		DBObject dbObj = sObj.toDBObject();
		// change the update date
		dbObj.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
				.getAttributeName(), new Date());
		BasicDBObject query = new BasicDBObject();
		query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), sObj.getUrl());

		serviceCollection.update(query, dbObj);
		// sending update event to the recievers
		EventManager
				.notifyRecievers(new Event(EventTypes.SERVICE_UPDATE, dbObj));
	}

	@Override
	public List<ServiceObject> query(String query) throws QueryException,
			PersistentStoreFailureException {

		DBObject o = (DBObject) JSON.parse(query);
		DBCursor cur = serviceCollection.find(o);
		List<ServiceObject> resultCollection = new CopyOnWriteArrayList<ServiceObject>();

		try {
			while (cur.hasNext()) {

				ServiceObject s = new ServiceObject(cur.next().toString());
				resultCollection.add(s);
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Collections.unmodifiableList(resultCollection);
	}

	@Override
	public List<ServiceObject> query(String query, Integer limit, Integer skip)
			throws QueryException, PersistentStoreFailureException {

		// BasicDBObject queryObject = new BasicDBObject();
		// BasicDBObject predicate = new BasicDBObject("$lte",new Date());
		// queryObject.put("serviceExpireOn", predicate);
		// DBCursor cur = serviceCollection.find(queryObject);
		DBObject o = (DBObject) JSON.parse(query);
		// System.out.println(o);
		// skip and limit
		DBCursor cur = serviceCollection.find(o).skip(skip).limit(limit);
		if (logger.isDebugEnabled()) {
			logger.debug("result size: " + cur.size());
		}
		List<ServiceObject> resultCollection = new ArrayList<ServiceObject>();
		try {
			while (cur.hasNext()) {
				ServiceObject s = new ServiceObject(cur.next());
				resultCollection.add(s);
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableList(resultCollection);
	}

	@Override
	public List<ServiceObject> query(String query, Integer skip)
			throws QueryException, PersistentStoreFailureException {

		DBObject o = (DBObject) JSON.parse(query);
		DBCursor cur = serviceCollection.find(o).skip(skip);

		logger.debug(cur.getCursorId());

		if (logger.isDebugEnabled()) {
			logger.debug("result size: " + cur.size());
		}
		List<ServiceObject> resultCollection = new ArrayList<ServiceObject>();
		int i = 0;
		try {
			while (cur.hasNext()) {
				ServiceObject s = new ServiceObject(cur.next().toString());
				resultCollection.add(s);
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableList(resultCollection);
	}

	@Override
	public JSONArray queryJSON(String query) throws QueryException,
			PersistentStoreFailureException {
		DBObject o = (DBObject) JSON.parse(query);
		DBCursor cur = serviceCollection.find(o);
		JSONArray arr = new JSONArray();
		logger.info(cur.size());
		while (cur.hasNext()) {
			arr.put(JSON.serialize(cur.next()));
		}
		cur.close();
		return arr;
	}

	@Override
	public JSONArray queryJSON(String query, Integer limit, Integer skip)
			throws QueryException, PersistentStoreFailureException {

		// BasicDBObject queryObject = new BasicDBObject();
		// BasicDBObject predicate = new BasicDBObject("$lte",new Date());
		// queryObject.put("serviceExpireOn", predicate);
		// DBCursor cur = serviceCollection.find(queryObject);
		DBObject o = (DBObject) JSON.parse(query);
		// System.out.println(o);
		// skip and limit
		DBCursor cur = serviceCollection.find(o).skip(skip).limit(limit);
		if (logger.isDebugEnabled()) {
			logger.debug("result size: " + cur.size());
		}
		JSONArray arr = new JSONArray();

		try {
			while (cur.hasNext()) {
				arr.put(JSON.serialize(cur.next()));
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	@Override
	public JSONArray queryJSON(String query, Integer skip)
			throws QueryException, PersistentStoreFailureException {

		DBObject o = (DBObject) JSON.parse(query);
		DBCursor cur = serviceCollection.find(o).skip(skip);

		if (logger.isDebugEnabled()) {
			logger.debug("result size: " + cur.size());
		}
		JSONArray arr = new JSONArray();
		try {
			while (cur.hasNext()) {
				arr.put(JSON.serialize(cur.next()));
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#queryJSONWithLimit(java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public JSONArray queryJSONWithLimit(String s, Integer limit) {
		DBObject o = (DBObject) JSON.parse(s);
		DBCursor cur = serviceCollection.find(o).limit(limit);

		if (logger.isDebugEnabled()) {
			logger.debug("result size: " + cur.size());
		}
		JSONArray arr = new JSONArray();
		try {
			while (cur.hasNext()) {
				arr.put(JSON.serialize(cur.next()));
			}
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#queryDistinctJSON(java.lang.String)
	 */
	@Override
	public JSONArray queryDistinctJSON(String attributeName) {
		List<DBObject> lst = serviceCollection.distinct(attributeName);
		JSONArray arr = new JSONArray(lst);
		return arr;
	}

	@Override
	public JSONArray paginatedQuery(String query, Integer pageSize, String id) {
		DBObject queryObj = (DBObject) JSON.parse(query);
		DBCursor cur = null;
		BasicDBObject idOrderBy = new BasicDBObject("_id", 1);
		if (id == null) {
			cur = serviceCollection.find(queryObj).sort(idOrderBy)
					.limit(pageSize);
		} else {
			// { "_id" : { "$gt" : { "$oid" :
			// "4e1da24b7b1a26e6dc6455b5"}},"serviceType":"jms"}
			StringBuffer b = new StringBuffer();
			b.append("{").append("\"_id\"").append(":").append("{")
					.append("\"$gt\":").append("{").append("\"$oid\"")
					.append(":\"").append(id).append("\"}").append("}}");
			DBObject db = (DBObject) JSON.parse(b.toString());

			if (queryObj.keySet().size() > 0) {
				db.putAll(queryObj);
			}
			cur = serviceCollection.find(db).sort(idOrderBy).limit(pageSize);

		}

		List<DBObject> lst = cur.toArray();
		cur.close();
		JSONArray arr = new JSONArray(lst);
		return arr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#deleteAll()
	 */
	@Override
	public void deleteAll() {
		if (logger.isDebugEnabled()) {
			logger.debug("deleting all the contents from the db collection");
		}
		BasicDBObject o = new BasicDBObject(0);
		serviceCollection.remove(o);

	}

	/**
	 * @throws JSONException
	 * 
	 */
	public List<ServiceObject> findAll() throws JSONException {
		List<ServiceObject> lst = new CopyOnWriteArrayList<ServiceObject>();
		DBCursor c = serviceCollection.find();
		while (c.hasNext()) {
			DBObject type = (DBObject) c.next();
			ServiceObject s = new ServiceObject(type.toString());
			lst.add(s);
		}
		c.close();
		return Collections.unmodifiableList(lst);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#findAndDelete(java.lang.String)
	 */
	@Override
	public void findAndDelete(String query) {
		DBObject db = (DBObject) JSON.parse(query);
		if (logger.isTraceEnabled()) {
			logger.debug("delete by query: " + db.toString());
		}

		serviceCollection.remove(db);
	}

}
