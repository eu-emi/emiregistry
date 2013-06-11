/**
 * 
 */
package eu.emi.emir.client.query;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author a.memon
 *
 */
public abstract class URIQuery{
	/***
	 * use one of the static methods to obtain Builder and then obtain Query from that
	 */
	protected URIQuery(){
		
	}
	
	public static URIQueryBuilder builder(){
		return new URIQueryBuilderImpl();
	}
	
	public static URIQueryBuilder endpointBuilder(){
		return new EndpointQueryBuilderImpl();
	}
	
	
	/**
	 * 
	 */
	public abstract String getQueryString();
	
	public abstract MultivaluedMap<String, String> getMultiValuedMap();
	
	public static abstract class URIQueryBuilder{
		/**
		 * Use one of the URIQuery's static methods to instantiate the builder
		 */
		protected URIQueryBuilder() {
			
		}
		
		public abstract URIQueryBuilder addParam(String name, String value);
		
		public abstract URIQueryBuilder addParam(String name, List<String> value);
		
		/**
		 * Set ref to the next page
		 * */
		public abstract URIQueryBuilder setRef(String reference);
		
		public abstract URIQueryBuilder setPageSize(Integer pageSize);
		
		public abstract URIQueryBuilder setSkip(Integer skip);
		
		public abstract URIQueryBuilder setResultLimit(Integer limit);
		
		/**
		 * Merge from another URIQuery
		 * */		
		public abstract URIQueryBuilder mergeURIQuery(URIQuery uriQuery);
		
		public abstract <E extends URIQuery> E build();		
	}
}
