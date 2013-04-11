package eu.emi.emir.db.mongodb;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.util.VersionUtil;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.FacetKeyType;
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
import eu.emi.emir.validator.InvalidServiceDescriptionException;
import eu.emi.emir.validator.RegistrationValidator;
import eu.unicore.util.configuration.ConfigurationException;

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
	private static MongoClient connection;
	private DB database;
	private DBCollection serviceCollection;
	private String hostName;
	private Integer port;
	private String colName;
	private String dbName;
	private String userName;
	private String password;
	private static String dbVersion;
	
	
	
	public MongoDBServiceDatabase() {
		if (EMIRServer.getServerProperties() == null) {
			new EMIRServer();
		}
		hostName = EMIRServer.getServerProperties().getValue(
				ServerProperties.PROP_MONGODB_HOSTNAME);
		port = EMIRServer.getServerProperties().getIntValue(
				ServerProperties.PROP_MONGODB_PORT);

		dbName = EMIRServer.getServerProperties().getValue(
				ServerProperties.PROP_MONGODB_DB_NAME);
		colName = EMIRServer.getServerProperties().getValue(
				ServerProperties.PROP_MONGODB_COLLECTION_NAME);

		userName = EMIRServer.getServerProperties().getValue(
				ServerProperties.PROP_MONGODB_USERNAME);
		password = EMIRServer.getServerProperties().getValue(
				ServerProperties.PROP_MONGODB_PASSWORD);

		try {
			if (connection == null) {

				MongoClientOptions mo = MongoClientOptions.builder().autoConnectRetry(true).connectTimeout(5000).maxAutoConnectRetryTime(10000).maxWaitTime(5000).socketKeepAlive(true).socketTimeout(0).connectionsPerHost(255).build();
				
				ServerAddress sa = new ServerAddress(hostName,
						Integer.valueOf(port));
				connection = new MongoClient(sa, mo);
			}

			database = connection.getDB(dbName);

			if (userName != null) {
				if ((!userName.equalsIgnoreCase(""))
						&& (!password.equalsIgnoreCase(""))) {
					if (!database.authenticate(userName,
							password.toCharArray())) {

						Log.logException(
								"Cannot authenticate the user: "
										+ userName
										+ "\nProvide the correct MongoDB database username and password in configuration file and restart the EMIR server again",
								new RuntimeException(
										"MongoDB Authentication Failed"));

						System.out
								.printf("%s:%s.%s.%s",
										"Error occurred while starting the EMIR server",
										"Cannot authenticate the database User: "
												+ userName,
										" Provide the correct MongoDB database username and password in configuration file and restart the EMIR server again",
										" Stoppoing the EMIR server.");
						System.exit(1);
					}

				}
			}

			serviceCollection = database.getCollection(colName);

			// setting index and unique constraint on "service endpoint id"
			BasicDBObject obj = new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(),
							1);
			serviceCollection.ensureIndex(obj,
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), true);
			
			
            BasicDBObject nonUniqueIndexKeys = new BasicDBObject();
			nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), 1);
			nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), 1);
			nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(),1);
			serviceCollection.ensureIndex(nonUniqueIndexKeys, "secondary",false);
		} catch (MongoException e) {
			Log.logException("", e, logger);
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
			this.hostName = hostname;
			this.dbName = dbName;
			this.colName = colName;
			this.port = port;
			init();
		} catch (Exception e) {
			logger.error("Error in connecting the MongoDB database", e);

		}
	}
	
	
	private void init(){
		try {

			if (connection == null) {
				connection = new MongoClient(hostName, port);
			}

			database = connection.getDB(dbName);

			serviceCollection = database.getCollection(colName);

			// setting index and uniquesness on service url
			
			BasicDBObject obj = getUniqueIndexes();
					
					
			serviceCollection.ensureIndex(getUniqueIndexes(),
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), true);
			serviceCollection.ensureIndex(getNonUniqueIndexes());
			if (logger.isDebugEnabled()) {
				logger.debug("Unique index created: " + obj);
			}
		} catch (Exception e) {
			logger.error("Error in connecting the MongoDB database", e);

		}
	}
	
	private BasicDBObject getUniqueIndexes(){
		BasicDBObject obj = new BasicDBObject(
				ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
						.getAttributeName(),
				1);
		return obj;
	}
	
	private BasicDBObject getNonUniqueIndexes(){
		BasicDBObject nonUniqueIndexKeys = new BasicDBObject();
		nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), 1);
		nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), 1);
		nonUniqueIndexKeys.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(),1);
		return nonUniqueIndexKeys;
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

			logger.info("inserted Service Endpoint record with ID: "
					+ item.getEndpointID());
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
	public void deleteByEndpointID(String endpointID)
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		database.requestStart();
		database.requestEnsureConnection();
		BasicDBObject query = new BasicDBObject();
		query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
				.getAttributeName(), endpointID);
		DBObject d = serviceCollection.findAndRemove(query);

		database.requestDone();
		if (d == null) {
			if (logger.isDebugEnabled()) {
				String msg = "No service description with the Endpoint ID:"
						+ endpointID + " exists";
				logger.debug(msg);
			}
			throw new NonExistingResourceException(
					"No service description with the Endpoint ID:" + endpointID
							+ " exists");
		}
		logger.info("deleted: " + endpointID);
		// sending delete event to the receivers
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
			String id = sObj.getEndpointID();
			logger.debug("updating service description: " + sObj);

			database.requestStart();
			database.requestEnsureConnection();
			DBObject dbObj = sObj.toDBObject();
			// change the update date
			// dbObj.put(ServiceBasicAttributeNames.SERVICE_UPDATE_SINCE
			// .getAttributeName(), new Date());
			BasicDBObject query = new BasicDBObject();
			query.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), sObj.getEndpointID());

			// Check the entry is exist or not. Validation need if it is not
			// exist before.
			DBObject db = serviceCollection.findOne(new BasicDBObject(
					ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
							.getAttributeName(), sObj.getEndpointID()));
			if (db == null) {
				try {
					new RegistrationValidator().validateInfo(sObj.toJSON());
				} catch (ConfigurationException e) {
					Log.logException(
							"Error during the update message validation. (ID:"
									+ id + ")", e, logger);
					return;
				} catch (InvalidServiceDescriptionException e) {
					Log.logException(
							"Error during the update message validation. (ID:"
									+ id + ")", e, logger);
					return;
				} catch (JSONException e) {
					Log.logException(
							"Error during the update message validation. (ID:"
									+ id + ")", e, logger);
					return;
				} catch (ParseException e) {
					Log.logException(
							"Error during the update message validation. (ID:"
									+ id + ")", e, logger);
					return;
				}
			}

			serviceCollection.update(query, dbObj, true, false); // upsert=true
																	// and
																	// multi=false
			database.requestDone();
			logger.info("UPDATED Service Endpoint Record with ID: " + id);
			// sending update event to the receivers
			// EventDispatcher.notifyRecievers(new
			// Event(EventTypes.SERVICE_UPDATE,
			// sObj.toJSON()));
		} catch (MongoException e) {
			Log.logException("Error updating the Service Record in MongoDB: "
					+ sObj, e, logger);
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
		return paginatedQuery(query, pageSize, id, "_id");
	}

	@Override
	public JSONArray paginatedQuery(String query, Integer pageSize, String id,
			String orderBy) {
		DBObject queryObj = (DBObject) JSON.parse(query);
		DBCursor cur = null;
		if (orderBy == null || orderBy.isEmpty()) {
			orderBy = "_id";
		}
		BasicDBObject OrderBy = new BasicDBObject(orderBy, 1);
		if (id == null) {
			cur = serviceCollection.find(queryObj).sort(OrderBy)
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
			cur = serviceCollection.find(db).sort(OrderBy).limit(pageSize);

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
	
	public void dropDB(String dbName) {
		connection.dropDatabase(dbName);
	}
	
	public DB createOrGetDB(String name){
		
		return connection.getDB(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.dsr.db.ServiceDatabase#findAndDelete(java.lang.String)
	 */
	@Override
	public String getDBVersion() {
		if (dbVersion == null) {
			CommandResult result = database.command("serverStatus");
			dbVersion = result.getString("version");
		}
		
		return dbVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.emir.db.ServiceDatabase#size()
	 */
	@Override
	public Long size() {
		if (serviceCollection == null) {
			return 0L;
		}
		Long size = serviceCollection.count();
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.emir.db.ServiceDatabase#facetedQuery()
	 */
	@Deprecated
	public JSONArray facetedQuery(Set<String> j) throws JSONException {
		JSONArray ja = new JSONArray();

		for (String str : j) {

			// create match
			BasicDBObject match = new BasicDBObject();
			match.put("$match", new BasicDBObject());

			// build the $projection operation
			DBObject fields = new BasicDBObject();

			// fields.put("count", 1);
			DBObject project = new BasicDBObject();

			// Now the $group operation
			DBObject groupFields = new BasicDBObject();

			// build the $projection operation
			fields.put(str, 1);
			project.put("$project", fields);

			// Now the $group operation
			groupFields.put("_id", "$" + str);

			// performing sum and storing it in the count attribute
			groupFields.put("count", new BasicDBObject("$sum", 1));

			DBObject group = new BasicDBObject("$group", groupFields);
			AggregationOutput output = serviceCollection.aggregate(match,
					project, group);

			Iterable<DBObject> it = output.results();

			JSONArray terms = new JSONArray();

			for (DBObject dbObject : it) {
				JSONObject term = new JSONObject(JSON.serialize(dbObject));
				System.out.println(term);
				terms.put(term);
			}

			System.out.println("\n\n");

			JSONObject attrName = new JSONObject();

			attrName.put(str, terms);
			ja.put(attrName);
		}

		return ja;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.emir.db.ServiceDatabase#facetedQuery(java.util.Map)
	 */
	@Override
	public JSONArray facetedQuery(Map<String, String> map) throws Exception {
		Version v =new Version(2, 4, 1, null);
		logger.info(VersionUtil.parseVersion(getDBVersion()).compareTo(v));
		if (VersionUtil.parseVersion(getDBVersion()).compareTo(v) < 0) {
			throw new UnsupportedOperationException("The MongoDB server version should be equal to or greater than: "+v.toString()+", current version is: "+getDBVersion());
		};
		logger.info(getDBVersion());
		JSONArray ja = new JSONArray();
		Set<String> j = map.keySet();
		for (String key : j) {

			// create match
			BasicDBObject match = new BasicDBObject();
			match.put("$match", new BasicDBObject());

			// build the $projection operation
			DBObject fields = new BasicDBObject();

			// fields.put("count", 1);
			DBObject project = new BasicDBObject();

			// Now the $group operation
			DBObject groupFields = new BasicDBObject();

			DBObject unwindFields = new BasicDBObject();

			

			// build the $projection operation
			fields.put(key, 1);
			project.put("$project", fields);

			// Now the $group operation
			groupFields.put("_id", "$" + key);

			// performing sum and storing it in the count attribute
			groupFields.put("count", new BasicDBObject("$sum", 1));

			DBObject group = new BasicDBObject("$group", groupFields);
			
			AggregationOutput output = null;
			
			if (map.get(key).equalsIgnoreCase(FacetKeyType.ARRAY)) {
				unwindFields
				.put("$unwind",
						"$"
								+ key);
				
				output = serviceCollection.aggregate(match,
						unwindFields, project, group);	
			} else if (map.get(key).equalsIgnoreCase(FacetKeyType.SIMPLE)) {
				output = serviceCollection.aggregate(match, project, group);
			}
			

			Iterable<DBObject> it = output.results();

			JSONArray terms = new JSONArray();

			for (DBObject dbObject : it) {
				JSONObject term = new JSONObject(JSON.serialize(dbObject));
				terms.put(term);
			}

			JSONObject attrName = new JSONObject();

			attrName.put(key, terms);
			ja.put(attrName);
		}

		return ja;
	}

	

	

}