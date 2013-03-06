/**
 * 
 */
package eu.emi.emir.client.query;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder;

/**
 * @author a.memon
 *
 */
public class EndpointQueryBuilderImpl extends EndpointQueryBuilder{

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceType(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceType(String type) {
		addParam(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), type);	
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceName(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceName(String name) {
		addParam(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), name);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setEndpointId(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setEndpointId(String endpointId) {
		addParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), endpointId);
		return this;
		
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceId(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceId(String serviceId) {
		addParam(ServiceBasicAttributeNames.SERVICE_ID.getAttributeName(), serviceId);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceCapability(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceEndpointCapability(String capability) {
		addParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY.getAttributeName(), capability);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceEndpointTechnology(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceEndpointTechnology(String technology) {
		addParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY.getAttributeName(), technology);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceEndpointIFaceName(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceEndpointIFaceName(String interfaceName) {
		addParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName(), interfaceName);
		return this;
		
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.EndpointQuery.EndpointQueryBuilder#setServiceEndpointIFaceImplName(java.lang.String)
	 */
	@Override
	public EndpointQueryBuilder setServiceEndpointImplName(String implName) {
		addParam(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_NAME.getAttributeName(), implName);
		return this;
		
	}
	
	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQueryBuilderImpl#build()
	 */
	@Override
	public URIQuery build() {
		final EndpointQueryImpl impl = new EndpointQueryImpl(map);
		return impl;
	}

}
