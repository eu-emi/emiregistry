/**
 * 
 */
package eu.emi.dsr.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import org.apache.log4j.Logger;
import eu.emi.dsr.util.Log;

/**
 * @author g.szigeti
 *
 */
public class InfrastructureManager implements ServiceInfrastructure {
	private static Logger logger = Log.getLogger(Log.DSR,
			InfrastructureManager.class);
	private static Connection conn;
	private static Statement stat;
	private String dbname = "emiregistry";
	
	private List<String> parentsRoute;
	private List<String> childServices;

	public InfrastructureManager() {
		parentsRoute = new ArrayList<String>();
		childServices = new ArrayList<String>();
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
        	conn = DriverManager.getConnection("jdbc:h2:./Emiregistry", "sa", "");
	        stat = conn.createStatement();
	        stat.execute("create table " + dbname + "(id varchar(255) primary key, new int, del int)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("DB exist. " + e);
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
	 * @return 
	 * @return None
	 */
	public void handleRegistration(String identifier) {
		logger.debug("handleRegistration called with this ID: " + identifier);
		try {
		    ResultSet rs;
		    rs = stat.executeQuery("select * from " + dbname + " where id = '" + identifier + "'");
		    if (rs.wasNull()) {
				logger.debug( identifier + " is not in the list! Insert new record...");
		    	stat.execute("insert into " + dbname + " values('"+identifier+"', 1, 0)");
		    }
		    else {
				logger.debug( "The list contains this '" + identifier + "' ID! Update comming...");
				rs.next();
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
	 * @return 
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
	 * @return 
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
}
