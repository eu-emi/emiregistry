/**
 * 
 */
package eu.emi.emir.client.query;

/**
 * @author a.memon
 *
 */
public abstract class EndpointQuery extends URIQuery{
	/**
	 * 
	 */
	protected EndpointQuery() {
		
	}
	
	public static abstract class EndpointQueryBuilder extends URIQueryBuilderImpl{
		/**
		 * 
		 */
		protected EndpointQueryBuilder() {
			// TODO Auto-generated constructor stub
		}
		
		public abstract EndpointQueryBuilder setServiceType(String type);
		public abstract EndpointQueryBuilder setServiceName(String name);
		public abstract EndpointQueryBuilder setEndpointId(String endpointId);
		public abstract EndpointQueryBuilder setServiceId(String serviceId);
		public abstract EndpointQueryBuilder setServiceEndpointCapability(String capability);
		public abstract EndpointQueryBuilder setServiceEndpointTechnology(String technology);
		public abstract EndpointQueryBuilder setServiceEndpointIFaceName(String interfaceName);
		public abstract EndpointQueryBuilder setServiceEndpointImplName(String interfaceImplName);		
	}
	
}
