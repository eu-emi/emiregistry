/**
 * 
 */
package eu.emi.emir.client.query;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author a.memon
 *
 */
public class EndpointQueryImpl extends URIQueryImpl{

	/**
	 * @param queryMap
	 */
	protected EndpointQueryImpl(MultivaluedMap<String, String> queryMap) {
		super(queryMap);
	}
	
}
