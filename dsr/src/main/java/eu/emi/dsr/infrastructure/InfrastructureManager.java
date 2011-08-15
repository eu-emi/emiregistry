/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import com.sun.jersey.api.client.ClientResponse.Status;

import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.ServerConstants;
import eu.emi.dsr.db.MultipleResourceException;
import eu.emi.dsr.db.NonExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.db.mongodb.ServiceObject;
import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 *
 */
public class InfrastructureManager implements ServiceInfrastructure {
	private static Logger logger = Log.getLogger(Log.DSR,
			InfrastructureManager.class);
	private Configuration conf;
	private static Connection conn;
	private static Statement stat;
	private String dbname = "emiregistry";
	
	private List<String> parentsRoute;
	private List<String> childServices;

	public InfrastructureManager(Configuration config) {
		conf = config;
		parentsRoute = new ArrayList<String>();
		childServices = new ArrayList<String>();
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
        	String h2db = conf.getProperty(ServerConstants.H2_DBFILE_PATH);
        	if (h2db.isEmpty()){
        		h2db = "./Emiregistry";
        	}
        	//conn = DriverManager.getConnection("jdbc:h2:./Emiregistry", "sa", "");
        	conn = DriverManager.getConnection("jdbc:h2:"+h2db,
        			conf.getProperty(ServerConstants.H2_USERNAME),
        			conf.getProperty(ServerConstants.H2_PASSWORD));
	        stat = conn.createStatement();
	        stat.execute("create table " + dbname + "(id varchar(255) primary key, new int, del int)");
		} catch (SQLException e) {
			if ( e.toString().substring(30, 60).equals("Database may be already in use") ){
				logger.error("DB locked! " + e);
			} else if ( e.toString().substring(30, 64).equals("Table \"EMIREGISTRY\" already exists") )  {
				logger.debug("DB exist. " + e);
			} else {
				logger.error("Other SQL exception: " + e);
			}
		}
	}
	
	protected void finalize () throws SQLException {
		stat.close();
		conn.close();
	}
	
	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#setParentsRoute(java.util.List)
	 */
	@Override
	public void setParentsRoute(List<String> identifiers) throws EmptyIdentifierFailureException, NullPointerFailureException{
		if (identifiers == null) throw new NullPointerFailureException();
		if (identifiers.isEmpty()) throw new EmptyIdentifierFailureException();
		
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
		return childServices;
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#addChildDSR(java.util.String)
	 */
	@Override
	public void addChildDSR(String identifier) throws AlreadyExistFailureException, EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		if (childServices.contains(identifier)) throw new AlreadyExistFailureException();
		childServices.add(identifier);
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#setParent(java.util.String)
	 */
	@Override
	public void setParent(String identifier)
			throws EmptyIdentifierFailureException, NullPointerFailureException {
		if (identifier == null) throw new NullPointerFailureException();
		if (identifier.isEmpty()) throw new EmptyIdentifierFailureException();
		
		parentsRoute.clear();
		parentsRoute.add(identifier);
		
	}

	/* 
	 * @see eu.emi.dsr.infrastructure.ServiceInfrastructure#getParent()
	 */
	@Override
	public String getParent() {
		if (parentsRoute.isEmpty()){
			return "";
		}
		return parentsRoute.get(0);
	}
	
	/**
	 * Handle the unsended registration message.
	 * @param service identifier
	 * @return None
	 */
	public void handleRegistration(String identifier) {
		logger.debug("handleRegistration called with this ID: " + identifier);
		try {
		    ResultSet rs;
		    rs = stat.executeQuery("select * from " + dbname + " where id = '" + identifier + "'");
		    if (!rs.first()) {
		    	logger.debug( identifier + " is not in the list! Insert new record...");
		    	stat.execute("insert into " + dbname + " values('"+identifier+"', 1, 0)");
		    }
		    else {
		    	logger.debug( "The list contains this '" + identifier + "' ID! Update comming...");
		    	stat.execute("update " + dbname + " set del=0 where id='"+ identifier+"'");
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle the unsended update message.
	 * @param service identifier
	 * @return None
	 */
	public void handleUpdate(String identifier) {
		logger.debug("handleUpdate called with this ID: " + identifier);
		try {
		    ResultSet rs;
		    rs = stat.executeQuery("select * from " + dbname + " where id = '" + identifier + "'");
		    if (rs.wasNull()) {
		    	logger.debug( identifier + " is not in the list! Insert new record...");
		    	stat.execute("insert into " + dbname + " values('"+identifier+"', 0, 0)");
		    }
		    else {
		    	logger.debug( "The list contains this '" + identifier + "' ID! Everything correct.");
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle the unsended delete message.
	 * @param service identifier
	 * @return None
	 */
	public void handleDelete(String identifier) {
		logger.debug("handleDelete called with this ID: " + identifier);
		try {
		    ResultSet rs;
		    rs = stat.executeQuery("select * from " + dbname + " where id = '" + identifier + "'");
		    if (rs.wasNull()) {
		    	logger.debug( identifier + " is not in the list! Insert new record...");
		    	stat.execute("insert into " + dbname + " values('"+identifier+"', 0, 1)");
		    }
		    else {
		    	logger.debug( "The list contains this '" + identifier + "' ID!");
		    	rs.next();
		    	if ( rs.getString("new") == "1"){
		    		logger.debug( "Remove this '" + identifier + "' ID from the list!");
		    		stat.execute("delete from " + dbname + " where id='"+ identifier+"'");				
		    	}
		    	else {
		    		logger.debug( "Update comming...");
		    		stat.execute("update " + dbname + " set del=1 where id='"+ identifier+"'");
		    	}
		    }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Database synchronization with the parent.
	 * @return True or False. It is depends from DB synchronization.
	 */
	public boolean dbSynchronization(String id, Method method, int responsestatus) {
		logger.debug("DB synchronization started.");
		switch (method){
		case REGISTER:
			logger.debug("REGISTRATION comming..., ID: " + id +", response status: " + responsestatus);
			if ( responsestatus == Status.OK.getStatusCode() ){
	    		//try {
	    			logger.debug("register, delete");
					//stat.execute("delete from " + dbname + " where id='"+ id+"'");
				//} catch (SQLException e) {}				
			}
			else if ( responsestatus == Status.NOT_MODIFIED.getStatusCode() ){
	    		try {
	    			logger.debug("register, update");
					stat.execute("update " + dbname + " set new=0, del=0 where id='"+ id+"'");
				} catch (SQLException e) {}
			}
			break;
		case UPDATE:
			logger.debug("UPDATE comming..., ID: " + id +", response status: " + responsestatus);
			if ( responsestatus == Status.OK.getStatusCode() ){
	    		try {
					stat.execute("delete from " + dbname + " where id='"+ id+"'");
				} catch (SQLException e) {}				
			}
			else if ( responsestatus == Status.NOT_MODIFIED.getStatusCode() ){
	    		try {
					stat.execute("update " + dbname + " set new=1, del=0 where id='"+ id+"'");
				} catch (SQLException e) {}
			}
			break;
		case DELETE:
			logger.debug("DELETE comming..., ID: " + id +", response status: " + responsestatus);
			if ( responsestatus == Status.OK.getStatusCode() ){
	    		try {
					stat.execute("delete from " + dbname + " where id='"+ id+"'");
				} catch (SQLException e) {}				
			}
			break;
		default:
			logger.debug("Bad method type, ID: " + id);
			return false;
		}
		JSONObject jo;
		//Registration messages
		jo = search(1,0);
		//Update messages
		jo = search(0,0);
		//Remove messages
		jo = search(0,1);
		return true;
	}
	
	private JSONObject search( int ne, int del){
		JSONObject jo = new JSONObject();
		List<String> ids = new ArrayList<String>();
	    ResultSet rs;
	    logger.debug("search new = "+ne+" del= "+ del);
	    // IDs search
	    try {
			rs = stat.executeQuery("select id from " + dbname + " where new = " + ne + " and del = " + del + "");
			while ( rs.next() ){
				ids.add(rs.getString("id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    MongoDBServiceDatabase mongoDB = new MongoDBServiceDatabase(
	    		conf.getProperty(ServerConstants.MONGODB_HOSTNAME),
	    		Integer.valueOf(conf.getProperty(ServerConstants.MONGODB_PORT)),
	    		conf.getProperty(ServerConstants.MONGODB_DB_NAME),
	    		conf.getProperty(ServerConstants.MONGODB_COLLECTION_NAME));
	    
		ServiceObject so = null;
		try {
			for (int i=0; i < ids.size(); i++){
				logger.debug("listában:: "+ ids.get(i));
				so = mongoDB.getServiceByUrl(ids.get(i));
				if ( so != null ) {
					//append to the JSONObject
					System.out.println("adatbazis elem: " + so.toJSON().toString());
					//jo.
				}
			}
		} catch (MultipleResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (NonExistingResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jo;
	}
}
