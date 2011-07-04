package eu.emi.dsr.db.mongodb;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.ServiceDatabase;
import com.mongodb.*;
import com.mongodb.util.JSON;

/**
 * @author martoni
 * 
 */
public class MongoDBServiceDatabase implements ServiceDatabase {
	private Mongo connection;
	private DB database;
	private DBCollection serviceCollection;
	
	public MongoDBServiceDatabase() {
		// TODO: do it production ready
		try {
			connection = new Mongo("localhost", 27017);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: do it production ready
		database = connection.getDB("mydb");
		serviceCollection = database.getCollection("serviceRegistry");
	}
	@Override
	public Object insert(Object item) throws ExistingResourceException,
			PersistentStoreFailureException {
		// TODO: do it production ready
		BasicDBObject inputObject = (BasicDBObject) JSON.parse((String) item);
		try {
			if (contains(inputObject.get("id"))) {
				throw new ExistingResourceException();
			}
		} catch (MultipleResourceException e) {
			throw new ExistingResourceException();
		}
		serviceCollection.insert(inputObject);
		return inputObject;
	}

	@Override
	public Object get(Object identifier) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		BasicDBObject query = new BasicDBObject();
		query.put("id", identifier);
		DBCursor cur = serviceCollection.find(query);
		if (!contains(identifier)) { 
			throw new NonExistingResourceException();
		}
		return cur.next();
	}

	@Override
	public void delete(Object identifier) throws MultipleResourceException,
			NonExistingResourceException, PersistentStoreFailureException {
		if (!contains(identifier)) { 
			throw new NonExistingResourceException();
		}
		BasicDBObject query = new BasicDBObject();
		query.put("id", identifier);
		serviceCollection.findAndRemove(query);
	}

	@Override
	public boolean contains(Object identifier)
			throws MultipleResourceException, PersistentStoreFailureException {
		BasicDBObject query = new BasicDBObject();
		query.put("id", identifier);
		DBCursor cur = serviceCollection.find(query);
		if (cur == null) {
			throw new PersistentStoreFailureException();
		}
		if (!cur.hasNext()) {
			return false;
		}
		cur.next();
		if (cur.hasNext()) {
			throw new MultipleResourceException();
		}
		return true;
	}

	@Override
	public void update(Object identifier, Object newItem)
			throws MultipleResourceException, NonExistingResourceException,
			PersistentStoreFailureException {
		if (!contains(identifier)) { 
			throw new NonExistingResourceException();
		}
		BasicDBObject updateObject = (BasicDBObject) JSON.parse((String) newItem);
		BasicDBObject query = new BasicDBObject();
		query.put("id", identifier);
		serviceCollection.findAndModify(query, updateObject);
	}

	@Override
	public Collection<Object> query(Object query) throws QueryException,
			PersistentStoreFailureException {
		BasicDBObject queryObject = (BasicDBObject) JSON.parse((String) query);
		DBCursor cur = serviceCollection.find(queryObject);
		Collection<Object> resultCollection = Collections.emptyList();
		while (cur.hasNext()) {
			resultCollection.add(cur.next());
		}
		return resultCollection;
	}

}
