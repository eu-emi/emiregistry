/**
 * 
 */
package eu.emi.dsr.db.mongodb;

import java.net.UnknownHostException;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * @author a.memon
 *
 */
public class MongoConnection {
	private static Mongo mongo = null;
	public static Mongo get(String hostname, Integer port) throws UnknownHostException, MongoException{
		if (mongo == null) {
			mongo = new Mongo(hostname, port);
		}
		return mongo;
	}
	public static Mongo getCachedInstance(){
		return mongo;
	}
}
