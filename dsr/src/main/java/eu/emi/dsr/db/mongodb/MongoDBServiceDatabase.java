package eu.emi.dsr.db.mongodb;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import eu.emi.dsr.util.Log;

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
					ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					1);
			logger.info(obj);
			serviceCollection.createIndex(obj);
			serviceCollection.ensureIndex(obj, "serviceUrl", true);
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
			connection = MongoConnection.get(hostname, port);
			database = connection.getDB(dbName);
			serviceCollection = database.getCollection(colName);
			// setting index and uniquesness on "serviceurl"
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
					1);
			logger.info(obj);
			serviceCollection.createIndex(obj);
			serviceCollection.ensureIndex(obj, "serviceUrl", true);
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
			logger.info("inserting: "+item.toDBObject());
			DBObject db = item.toDBObject();
			db.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON.getAttributeName(), new Date());
			WriteResult wr = serviceCollection.insert(db, WriteConcern.SAFE);
		} catch (MongoException e) {
			if (e instanceof DuplicateKey) {
				throw new ExistingResourceException("Service with URL: "+item.getUrl()+" - already exists",e);
			} else {
				throw new PersistentStoreFailureException(e);
			}
		}

	
	}

	@Override
	public ServiceObject getServiceByUrl(String identifier)
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		Map<String, String> map = new HashMap<String, String>();
		map.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				identifier);
		ServiceObject so = null;
		try {

			DBCursor cur = serviceCollection.find(new BasicDBObject(
					"serviceUrl", identifier));
			while (cur.hasNext()) {
				so = new ServiceObject(cur.next().toString());
			}

		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return so;
	}

	@Override
	public void deleteByUrl(String url) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		BasicDBObject query = new BasicDBObject();
		query.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				url);
		serviceCollection.findAndRemove(query);
	}

	// @Override
	// public boolean contains(Object identifier)
	// throws MultipleResourceException, PersistentStoreFailureException {
	// BasicDBObject query = new BasicDBObject();
	// query.put("id", identifier);
	// DBCursor cur = serviceCollection.find(query);
	// if (cur == null) {
	// throw new PersistentStoreFailureException();
	// }
	// if (!cur.hasNext()) {
	// return false;
	// }
	// cur.next();
	// if (cur.hasNext()) {
	// throw new MultipleResourceException();
	// }
	// return true;
	// }

	@Override
	public void update(ServiceObject sObj) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		DBObject dbObj = sObj.toDBObject();
		//change the update date
		dbObj.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE.getAttributeName(), new Date());
		BasicDBObject query = new BasicDBObject();		
		query.put(ServiceBasicAttributeNames.SERVICE_URL.getAttributeName(),
				sObj.getUrl());
		
		serviceCollection.update(query, dbObj);
	}

	@Override
	public List<ServiceObject> query(String query) throws QueryException,
			PersistentStoreFailureException {
		
		
//		BasicDBObject queryObject = new BasicDBObject();
//		BasicDBObject predicate = new BasicDBObject("$lte",new Date());
//		queryObject.put("serviceExpireOn", predicate);
//		DBCursor cur = serviceCollection.find(queryObject);
		DBObject o = (DBObject) JSON.parse(query);
//		System.out.println(o);
		DBCursor cur = serviceCollection.find(o);
		List<ServiceObject> resultCollection = new CopyOnWriteArrayList<ServiceObject>();
		
		try {
			while (cur.hasNext()) {
				
				ServiceObject s = new ServiceObject(cur.next().toString());
				resultCollection.add(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableList(resultCollection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#deleteAll()
	 */
	@Override
	public void deleteAll() {
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
		return Collections.unmodifiableList(lst);
	}

	/* (non-Javadoc)
	 * @see eu.emi.dsr.db.ServiceDatabase#findAndDelete(java.lang.String)
	 */
	@Override
	public void findAndDelete(String query) {
		DBObject db = (DBObject) JSON.parse(query);
		logger.debug("delete by query: "+db.toString());
		serviceCollection.remove(db);
	}

}
