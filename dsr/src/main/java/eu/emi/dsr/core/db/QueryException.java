package eu.emi.dsr.core.db;

public class QueryException extends Exception {

	/**
	 * The serial version UID expected at serializable classes
	 */
	private static final long serialVersionUID = 7506634169927409715L;
	private Object query;
	
	public QueryException(Object query) {
		super();
		this.setQuery(query);
	}

	public QueryException(Object query, String message) {
		super(message);
		this.setQuery(query);
	}

	/**
	 * @return the query
	 */
	public Object getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	private void setQuery(Object query) {
		this.query = query;
	}

}
