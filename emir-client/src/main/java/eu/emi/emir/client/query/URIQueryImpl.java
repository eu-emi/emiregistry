/**
 * 
 */
package eu.emi.emir.client.query;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author a.memon
 *
 */
public class URIQueryImpl extends URIQuery{
	private final MultivaluedMap<String, String> queryMap;
	
	/**
	 * 
	 */
	protected URIQueryImpl(MultivaluedMap<String, String> queryMap) {
		this.queryMap = queryMap;
	}
	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery#getQueryString()
	 */
	@Override
	public String getQueryString() {
		//TODO generate proper http request query string
		return queryMap.toString();
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery#getMultiValuedMap()
	 */
	@Override
	public MultivaluedMap<String, String> getMultiValuedMap() {
		return queryMap;
	}

}
