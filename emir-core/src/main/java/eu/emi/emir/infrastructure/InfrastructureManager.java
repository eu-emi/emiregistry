/**
 * 
 */
package eu.emi.emir.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.ServerProperties;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.Log;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;
import eu.emi.emir.security.ClientSecurityProperties;

/**
 * @author g.szigeti
 * 
 */
public class InfrastructureManager implements ServiceInfrastructure {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			InfrastructureManager.class);
	private static Connection conn;
	private static Statement stat;
	private String dbname = "emiregistry";

	private List<String> parentsRoute;

	/**
	 * Constructor Create connection to the H2 database.
	 * 
	 * @param configuration
	 */
	public InfrastructureManager() {
		parentsRoute = new ArrayList<String>();
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			Log.logException("", e);
		}
		try {
			String h2db = EMIRServer.getServerProperties().getValue(
					ServerProperties.PROP_H2_DBFILE_PATH);
			if (h2db == null || h2db.isEmpty()) {
				h2db = "/var/lib/emi/emir/data/Emiregistry";
			}
			conn = DriverManager.getConnection("jdbc:h2:" + h2db, "sa", "");
			stat = conn.createStatement();
			stat.execute("create table " + dbname
					+ "(id varchar(255) primary key, new int, del int)");
		} catch (SQLException e) {
			if (e.toString().substring(30, 60)
					.equals("Database may be already in use")) {
				logger.error("DB locked! " + e);
			} else if (e.toString().substring(30, 64)
					.equals("Table \"EMIREGISTRY\" already exists")) {
				if (logger.isDebugEnabled()) {
					logger.debug("DB exist. " + e);
				}
			} else {
				logger.error("Other SQL exception: " + e);
			}
		}
	}

	/**
	 * Destructor Stop every open connection.
	 * 
	 * @param None
	 * @throws SQLException
	 */
	protected void finalize() throws SQLException {
		stat.close();
		conn.close();
	}

	/*
	 * @see
	 * eu.emi.dsr.infrastructure.ServiceInfrastructure#setParentsRoute(java.
	 * util.List)
	 */
	@Override
	public void setParentsRoute(List<String> identifiers)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifiers == null)
			throw new NullPointerFailureException();
		if (identifiers.isEmpty())
			throw new EmptyIdentifierFailureException();

		parentsRoute.clear();
		parentsRoute.addAll(identifiers);
	}

	/*
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getParentsRoute()
	 */
	@Override
	public List<String> getParentsRoute() {
		return parentsRoute;
	}

	/*
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getChildDSRs()
	 */
	@Override
	public List<String> getChildDSRs() {
		List<String> result = new ArrayList<String>();
		logger.debug("Not implemented yet.");
		return result;
	}

	/*
	 * @see
	 * eu.emi.dsr.infrastructure.ServiceInfrastructure#addChildDSR(java.util
	 * .String)
	 */
	@Override
	public void addChildDSR(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null)
			throw new NullPointerFailureException();
		if (identifier.isEmpty())
			throw new EmptyIdentifierFailureException();

		logger.debug("Not implemented yet.");
	}

	/*
	 * @see
	 * eu.emi.dsr.infrastructure.ServiceInfrastructure#setParent(java.util.String
	 * )
	 */
	@Override
	public void setParent(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null)
			throw new NullPointerFailureException();
		if (identifier.isEmpty())
			throw new EmptyIdentifierFailureException();

		parentsRoute.clear();
		parentsRoute.add(identifier);

	}

	/*
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getParent()
	 */
	@Override
	public String getParent() {
		if (parentsRoute.isEmpty()) {
			return "";
		}
		return parentsRoute.get(0);
	}

	/**
	 * Handle the unsent registration message.
	 * 
	 * @param list
	 *            of service identifier
	 * @return None
	 */
	public void handleRegistration(List<String> identifiers) {
		logger.debug("handleRegistration called...");
		for (int i = 0; i < identifiers.size(); i++) {
			String identifier = identifiers.get(i);
			logger.debug("next ID by the Registration: " + identifier);
			try {
				ResultSet rs;
				rs = stat.executeQuery("select * from " + dbname
						+ " where id = '" + identifier + "'");
				if (!rs.first()) {
					logger.debug(identifier
							+ " is not in the list! Insert new record...");
					stat.execute("insert into " + dbname + " values('"
							+ identifier + "', 1, 0)");
				} else {
					logger.debug("The list contains this '" + identifier
							+ "' ID! Update comming...");
					stat.execute("update " + dbname + " set del=0 where id='"
							+ identifier + "'");
				}
			} catch (SQLException e) {
				Log.logException("", e);
			}
		}
	}

	/**
	 * Handle the unsent update message.
	 * 
	 * @param list
	 *            of service identifier
	 * @return None
	 */
	public void handleUpdate(List<String> identifiers) {
		logger.debug("handleUpdate called...");
		for (int i = 0; i < identifiers.size(); i++) {
			String identifier = identifiers.get(i);
			logger.debug("next ID by the Update: " + identifier);
			try {
				ResultSet rs;
				rs = stat.executeQuery("select * from " + dbname
						+ " where id = '" + identifier + "'");
				if (!rs.first()) {
					logger.debug(identifier
							+ " is not in the list! Insert new record...");
					stat.execute("insert into " + dbname + " values('"
							+ identifier + "', 0, 0)");
				} else {
					logger.debug("The list contains this '" + identifier
							+ "' ID! Everything correct.");
				}
			} catch (SQLException e) {
				Log.logException("", e);
			}
		}
	}

	/**
	 * Handle the unsent delete message.
	 * 
	 * @param service
	 *            identifier
	 * @return None
	 */
	public void handleDelete(String identifier) {
		logger.debug("handleDelete called with this ID: " + identifier);
		try {
			ResultSet rs;
			rs = stat.executeQuery("select * from " + dbname + " where id = '"
					+ identifier + "'");
			if (!rs.first()) {
				logger.debug(identifier
						+ " is not in the list! Insert new record...");
				stat.execute("insert into " + dbname + " values('" + identifier
						+ "', 0, 1)");
			} else {
				logger.debug("The list contains this '" + identifier + "' ID!");
				if (rs.getString(2).equals("1")) {
					logger.debug("Remove this '" + identifier
							+ "' ID from the list!");
					stat.execute("delete from " + dbname + " where id='"
							+ identifier + "'");
				} else {
					logger.debug("Update comming...");
					stat.execute("update " + dbname + " set del=1 where id='"
							+ identifier + "'");
				}
			}
		} catch (SQLException e) {
			Log.logException("", e);
		}

	}

	/**
	 * Database synchronization with the parent.
	 * 
	 * @return True or False. It is depends from DB synchronization.
	 */
	public boolean dbSynchronization(List<String> ids, Method method,
			int responsestatus) {
		logger.debug("DB synchronization started.");
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			switch (method) {
			case REGISTER:
				logger.debug("REGISTRATION comming..., ID: " + id
						+ ", response status: " + responsestatus);
				if (responsestatus == Status.OK.getStatusCode()) {
					try {
						logger.debug("register, delete");
						stat.execute("delete from " + dbname + " where id='"
								+ id + "'");
					} catch (SQLException e) {
					}
				} else if (responsestatus == Status.CONFLICT.getStatusCode()) {
					try {
						logger.debug("register, update");
						stat.execute("update " + dbname
								+ " set new=0, del=0 where id='" + id + "'");
					} catch (SQLException e) {
					}
				}
				break;
			case UPDATE:
				logger.debug("UPDATE comming..., ID: " + id
						+ ", response status: " + responsestatus);
				if (responsestatus == Status.OK.getStatusCode()) {
					try {
						stat.execute("delete from " + dbname + " where id='"
								+ id + "'");
					} catch (SQLException e) {
					}
				} else if (responsestatus == Status.CONFLICT.getStatusCode()) {
					try {
						stat.execute("update " + dbname
								+ " set new=1, del=0 where id='" + id + "'");
					} catch (SQLException e) {
					}
				}
				break;
			case DELETE:
				logger.debug("DELETE comming..., ID: " + id
						+ ", response status: " + responsestatus);
				if (responsestatus == Status.OK.getStatusCode()) {
					try {
						stat.execute("delete from " + dbname + " where id='"
								+ id + "'");
					} catch (SQLException e) {
					}
				}
				break;
			default:
				logger.debug("Bad method type, ID: " + id);
				return false;
			}
		}
		// Synchronization messages are sending
		int maxMessageSize = EMIRServer.getServerProperties().getIntValue(
				ServerProperties.PROP_RECORD_MAXIMUM);
		JSONArray jos;
		// Registration messages
		boolean register = true;
		jos = search(1, 0);
		if (jos.length() > 0) {
			int i = 0;
			while (i < jos.length()) {
				// Create message part from the original
				JSONArray sended = new JSONArray();
				for (int index = i; (index < (i + maxMessageSize))
						&& (index < jos.length()); index++) {
					try {
						sended.put(jos.getJSONObject(index));
					} catch (JSONException e) {
						Log.logException("", e);
					}
				}
				// Send
				register = register && send(sended, Method.REGISTER);
				// Next start index calculation
				i += maxMessageSize;
			}
		}
		// Update messages
		boolean update = true;
		jos = search(0, 0);
		if (jos.length() > 0) {
			int i = 0;
			while (i < jos.length()) {
				// Create message part from the original
				JSONArray sended = new JSONArray();
				for (int index = i; (index < (i + maxMessageSize))
						&& (index < jos.length()); index++) {
					try {
						sended.put(jos.getJSONObject(index));
					} catch (JSONException e) {
						Log.logException("", e);
					}
				}
				// Send
				update = update && send(sended, Method.UPDATE);
				// Next start index calculation
				i += maxMessageSize;
			}
		}
		// Remove messages
		boolean delete = true;
		jos = search(0, 1);
		if (jos.length() > 0) {
			delete = send(jos, Method.DELETE);
		}

		return (register && update && delete);
	}

	/**
	 * Search in the H2 database.
	 * 
	 * @param int, it is a new value in the filter
	 * @param int, it is a del value in the filter
	 * @return JSONArray, every JSONs from the database, that fit to the filter.
	 */
	private JSONArray search(int ne, int del) {
		JSONArray jo = new JSONArray();
		List<String> ids = new ArrayList<String>();
		ResultSet rs;
		logger.debug("search new = " + ne + " del= " + del);
		// IDs search
		try {
			rs = stat.executeQuery("select id from " + dbname + " where new = "
					+ ne + " and del = " + del + "");
			while (rs.next()) {
				ids.add(rs.getString("id"));
				if (ne == 0 && del == 1) {
					jo.put(rs.getString("id"));
				}
			}
		} catch (SQLException e) {
			Log.logException("", e);
		}
		if (ne == 0 && del == 1) {
			return jo;
		}

		ServerProperties sp = EMIRServer.getServerProperties();

		MongoDBServiceDatabase mongoDB = new MongoDBServiceDatabase(
				sp.getValue(ServerProperties.PROP_MONGODB_HOSTNAME),
				sp.getIntValue(ServerProperties.PROP_MONGODB_PORT),
				sp.getValue(ServerProperties.PROP_MONGODB_DB_NAME),
				sp.getValue(ServerProperties.PROP_MONGODB_COLLECTION_NAME));

		ServiceObject so = null;
		try {
			for (int i = 0; i < ids.size(); i++) {
				so = mongoDB.getServiceByUrl(ids.get(i));
				if (so != null) {
					// append to the JSONArray
					System.out
							.println("Stored JSON: " + so.toJSON().toString());
					jo.put(so.toJSON());
				}
			}
		} catch (MultipleResourceException e) {
			Log.logException("", e);
		} catch (PersistentStoreFailureException e) {
			Log.logException("", e);
		} catch (NonExistingResourceException e) {
			Log.logException("", e);
		}

		return jo;
	}

	/**
	 * Send message to the parent.
	 * 
	 * @param JSONArray
	 *            , collections of the JSONs
	 * @param method
	 *            , type of the message
	 * @return boolean
	 */
	private boolean send(JSONArray jos, Method method) {
		ServerProperties sp = EMIRServer.getServerProperties();

		String parentUrl = sp.getValue(ServerProperties.PROP_PARENT_ADDRESS);
		EMIRClient c = null;
		if (parentUrl.startsWith("https")) {
			ClientSecurityProperties csp = null;
			try {
				csp = EMIRServer
						.getClientSecurityProperties();
			} catch (Exception e) {
				Log.logException("Error reading the security properties", e);
			}
			c = new EMIRClient(parentUrl + "/serviceadmin", csp);
		} else {
			c = new EMIRClient(parentUrl + "/serviceadmin");
		}

		WebResource client = c.getClientResource();
		ClientResponse res = null;
		boolean retval = true;
		int cycleNr = 1;
		if (method == Method.DELETE) {
			cycleNr = jos.length();
		}
		for (int i = 0; i < cycleNr; i++) {
			try {
				switch (method) {
				case REGISTER:
					logger.debug("send register");
					res = client.accept(MediaType.APPLICATION_JSON_TYPE).post(
							ClientResponse.class, jos);
					break;
				case UPDATE:
					logger.debug("send update");
					res = client.accept(MediaType.APPLICATION_JSON_TYPE).put(
							ClientResponse.class, jos);
					break;
				case DELETE:
					logger.debug("send delete");
					res = client
							.queryParam(
									ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
											.getAttributeName(),
									jos.getString(i)).delete(
									ClientResponse.class);
					break;
				default:
					break;
				}
			} catch (ClientHandlerException e) {
				retval = false;
			} catch (UniformInterfaceException e) {
				Log.logException("", e);
				retval = false;
			} catch (JSONException e) {
				Log.logException("", e);
				retval = false;
			}

			if (res.getStatus() == Status.OK.getStatusCode()) {
				// delete entry(s) from the list
				try {
					if (method == Method.DELETE) {
						deleteentry(jos.getString(i));
					} else if (method == Method.REGISTER) {
						databaseclean(1, 0);
					} else if (method == Method.UPDATE) {
						databaseclean(0, 0);
					}
				} catch (JSONException e) {
					Log.logException("", e);
				}
			}

			if (res.getStatus() == Status.CONFLICT.getStatusCode()) {
				int failedIndex = 0;
				String failedURL = "";
				JSONArray failedJSONs = res.getEntity(JSONArray.class);
				try {
					failedURL = failedJSONs
							.getJSONObject(failedIndex)
							.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
									.getAttributeName()).toString();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				String currentURL = "";
				int j = 0;
				while (j != jos.length()) {
					try {
						currentURL = jos
								.getJSONObject(j)
								.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
										.getAttributeName()).toString();
					} catch (JSONException e) {
						Log.logException("", e);
						break;
					}
					if (!currentURL.equals(failedURL)) {
						// The 'currentURL' was successfully synchronized
						deleteentry(currentURL);
					} else {
						// Error happened in the synchronization
						failedIndex++;
						if (failedIndex < failedJSONs.length()) {
							try {
								// next failed URL calculation
								failedURL = failedJSONs
										.getJSONObject(failedIndex)
										.get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
												.getAttributeName()).toString();
							} catch (JSONException e) {
								Log.logException("", e);
							}
						}
					}
					j++;
				}
				logger.debug("Error during the " + method.name() + " method.");
				retval = false;
			}
		}
		return retval;
	}

	/**
	 * Delete all entries from the H2 database, that fit to the filter.
	 * 
	 * @param int, new filter value
	 * @param int, del filter value
	 * @return None
	 */
	private void databaseclean(int ne, int del) {
		logger.debug("Database cleaning! new=" + ne + " del=" + del);
		try {
			stat.execute("delete from " + dbname + " where new=" + ne
					+ " and del=" + del);
		} catch (SQLException e) {
		}
		return;
	}

	/**
	 * Delete one entry from the H2 database, that fit to the filter.
	 * 
	 * @param String
	 *            , ID filter value
	 * @return None
	 */
	private void deleteentry(String id) {
		logger.debug("Delete entry! id=" + id);
		try {
			stat.execute("delete from " + dbname + " where id='" + id + "'");
		} catch (SQLException e) {
			Log.logException("", e);
		}
		return;
	}
}
