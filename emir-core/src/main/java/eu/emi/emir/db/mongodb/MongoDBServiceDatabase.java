package eu.emi.emir.db.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.QueryException;
import eu.emi.emir.db.ServiceDatabase;
import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventTypes;

import com.mongodb.*;
import com.mongodb.MongoException.DuplicateKey;
import com.mongodb.util.JSON;

/**
 * Mongodb integration class to proxy database operations 
 * 
 * @author martoni
 * @author a.memon
 * @author g.szigeti
 * 
 */
public class MongoDBServiceDatabase implements ServiceDatabase {
	private static Logger logger = Log.getLogger(Log.EMIR_DB,
			MongoDBServiceDatabase.class);
	private static Mongo connection;
	private DB database;
	private DBCollection serviceCollection;

	public MongoDBServiceDatabase() {
		if (EMIRServer.getServerProperties() == null) {
			new EMIRServer();
		}
		String hostname = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_HOSTNAME);
		Integer port = EMIRServer.getServerProperties().getIntValue(ServerProperties.PROP_MONGODB_PORT);
		
		String dbName = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_DB_NAME);
		String colName = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_COLLECTION_NAME);

		String dbUserName = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_USERNAME);
		String dbPassword = EMIRServer.getServerProperties().getValue(ServerProperties.PROP_MONGODB_PASSWORD);

		try {
			// connection = MongoConnection.get(hostname,
			// Integer.valueOf(port));
			if (connection == null) {

				MongoOptions mo = new MongoOptions();
				mo.autoConnectRetry = true;
				mo.connectTimeout = 100;
				mo.maxWaitTime = 100;
				mo.socketKeepAlive = true;
				mo.connectionsPerHost = 255;

				ServerAddress sa = new ServerAddress(hostname,
						Integer.valueOf(port));
				connection = new Mongo(sa, mo);
			}

			database = connection.getDB(dbName);

			if (dbUserName != null) {
				if ((!dbUserName.equalsIgnoreCase(""))
						&& (!dbPassword.equalsIgnoreCase(""))) {
					if (!database
							.authenticate(dbUserName, dbPassword.toCharArray())) {
						
						Log.logException("Cannot authenticate the user: " + dbUserName + "\nProvide the correct MongoDB database username and password in configuration file and restart the EMIR server again", new RuntimeException("MongoDB Authentication Failed"));
						
						System.out
								.printf("%s:%s.%s.%s",
										"Error occurred while starting the EMIR server",
										"Cannot authenticate the database User: "
												+ dbUserName,
										" Provide the correct MongoDB database username and password in configuration file and restart the EMIR server again",
										" Stoppoing the EMIR server.");
						System.exit(1);
					}

				}	
			}
			

			serviceCollection = database.getCollection(colName);

			// setting index and unique constraint on "service endpoint id"
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(),
					"1");

			serviceCollection.ensureIndex(obj,
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), true);
		} catch (MongoException e) {
			Log.logException("", e);
			logger.warn(e.getCause());
		} catch (Exception e) {
			logger.error("Error in connecting the MongoDB database", e);
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
			
			if (connection == null) {
				connection = new Mongo(hostname, port);
			}

			database = connection.getDB(dbName);

			serviceCollection = database.getCollection(colName);

			// setting index and uniquesness on service url
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(),
					"1");
			
//			obj.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(),
//					"1");
//			
//			obj.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),"1");
//					
//			obj.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(),"1");

			serviceCollection.ensureIndex(obj,
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), true);
			if (logger.isDebugEnabled()) {
				logger.debug("Unique index created: " + obj);
			}
		} catch (Exception e) {
			logger.error("Error in connecting the MongoDB database", e);

		}
	}

	@Override
	public void insert(ServiceObject item) throws ExistingResourceException,
			PersistentStoreFailureException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("inserting: " + item.toDBObject());
			}
			database.requestStart();
			database.requestEnsureConnection();
			DBObject db = item.toDBObject();
			// db.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
			// .getAttributeName(), new Date());

			serviceCollection.insert(db, WriteConcern.SAFE);
			
			database.requestDone();
			
			logger.info("inserted Service Endpoint record with ID: " + item.getEndpointID());
			// EventDispatcher.notifyRecievers(new Event(EventTypes.SERVICE_ADD,
			// item
			// .toJSON()));
		} catch (MongoException e) {
			if (e instanceof DuplicateKey) {
				throw new ExistingResourceException("Endpoint record with ID: "
						+ item.getEndpointID() + " - already exists", e);
			} else {
				throw new PersistentStoreFailureException(e);
			}

		}

	}

	public void insert(DBObject item) throws ExistingResourceException,
			PersistentStoreFailureException {
		@SuppressWarnings("unused")
		List<String> lstError = new CopyOnWriteArrayList<String>();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("inserting: " + item);
			}
			DBObject db = item;
			
			db.put(ServiceBasicAttributeNames.SERVICE_CREATED_ON
					.getAttributeName(), new Date());
			database.requestStart();
			database.requestEnsureConnection();
			serviceCollection.insert(db, WriteConcern.SAFE);
			database.requestDone();
			// EventManager.notifyRecievers(new Event(EventTypes.SERVICE_ADD,obj.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(),
//			"1");
//	
//	obj.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),"1");
//			
//	obj.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(),"1");
			// item
			// .toJSON()));
		} catch (MongoException e) {
			if (e instanceof DuplicateKey) {
				throw new ExistingResourceException(e);
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
			Log.logException("", e);
		} catch (JSONException e) {
			throw new NonExistingResourceException(e);
		}

		return so;
	}
	
	@Override
	public ServiceObject getServiceByEndpointID(String identifier)
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		ServiceObject so = null;
		try {
			System.out.println(findAll());
			DBObject db = serviceCollection.findOne(new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), identifier));
			if (db == null) {
				return null;
			}
			so = new ServiceObject(db);

		} catch (MongoException e) {
			Log.logException("", e);
		} catch (JSONException e) {
			throw new NonExistingResourceException(e);
		}

		return so;
	}

	@Override
	public void deleteByEndpointID(String endpointID) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		database.requestStart();
		database.requestEnsureConnection();
		BasicDBObject query = new BasicDBObject();
		query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), endpointID);
		DBObject d = serviceCollection.findAndRemove(query);

		database.requestDone();
		if (d == null) {
			if (logger.isDebugEnabled()) {
				String msg = "No service description with the Endpoint ID:" + endpointID
						+ " exists";
				logger.debug(msg);
			}
			throw new NonExistingResourceException(
					"No service description with the Endpoint ID:" + endpointID + " exists");
		}
		logger.info("deleted: " + endpointID);
		// sending update event to the receivers
		try {
			JSONObject deletedEntry = new JSONObject(d.toString());
			EventDispatcher.notifyRecievers(new Event(
					EventTypes.SERVICE_DELETE, deletedEntry));

		} catch (JSONException e) {
			logger.warn(e.getCause());
		}
	}

	@Override
	public void update(ServiceObject sObj) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		try {
			String  id = sObj.getServiceID();
			if (logger.isDebugEnabled()) {
				logger.debug("updating service description: " + sObj);
			}
			database.requestStart();
			database.requestEnsureConnection();
			DBObject dbObj = sObj.toDBObject();
			// change the update date
			// dbObj.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
			// .getAttributeName(), new Date());
			BasicDBObject query = new BasicDBObject();
			query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), sObj.getEndpointID());

			serviceCollection.update(query, dbObj);
			database.requestDone();
			logger.info("updated Service Endpoint Record with ID: " + id);
			// sending update event to the receivers
			// EventDispatcher.notifyRecievers(new
			// Event(EventTypes.SERVICE_UPDATE,
			// sObj.toJSON()));
		} catch (MongoException e) {
			Log.logException("Error updating the Service Record in MongoDB: "+sObj, e, logger);
		}

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
			Log.logException("", e);
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
			Log.logException("", e);
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
		try {
			while (cur.hasNext()) {
				ServiceObject s = new ServiceObject(cur.next().toString());
				resultCollection.add(s);
			}
			cur.close();
		} catch (Exception e) {
			Log.logException("", e);
		}
		return Collections.unmodifiableList(resultCollection);
	}

	@Override
	public JSONArray queryJSON(String query) throws QueryException,
			PersistentStoreFailureException, MongoException, JSONException {
		DBObject o = (DBObject) JSON.parse(query);
		DBCursor cur = serviceCollection.find(o);
		JSONArray arr = new JSONArray();
		while (cur.hasNext()) {
			arr.put(new JSONObject(JSON.serialize(cur.next())));
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
				arr.put(new JSONObject(JSON.serialize(cur.next())));
			}
			cur.close();
		} catch (Exception e) {
			Log.logException("", e);
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
				arr.put(new JSONObject(JSON.serialize(cur.next())));
			}
			cur.close();
		} catch (Exception e) {
			Log.logException("", e);
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
				arr.put(new JSONObject(JSON.serialize(cur.next())));
			}
			cur.close();
		} catch (Exception e) {
			Log.logException("", e);
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
		String s = "{ "
				+ ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
						.getAttributeName() + " : { $exists : true } }";
		DBObject query = (DBObject) JSON.parse(s);
		@SuppressWarnings("unchecked")
		List<DBObject> lst = serviceCollection.distinct(attributeName, query);
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
		database.requestStart();
		serviceCollection.remove(o);
		database.requestDone();
		logger.info("deleted all the contents from the db collection");

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
		database.requestStart();
		database.requestEnsureConnection();
		serviceCollection.remove(db);
		
		database.requestDone();

	}

	public void dropCollection() {
		if (logger.isDebugEnabled()) {
			logger.debug("dropping collection: " + serviceCollection.getName());
		}
		serviceCollection.drop();
	}

	public void dropDB() {
		database.dropDatabase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#findAndDelete(java.lang.String)
	 */
	@Override
	public String getDBVersion() {
		CommandResult result = database.command("serverStatus");
		return result.getString("version");
	}
}