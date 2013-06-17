/**
 * 
 */
package eu.emi.emir.client.query;


import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.emi.emir.client.query.URIQuery.URIQueryBuilder;

/**
 * @author a.memon
 *
 */
public class URIQueryBuilderImpl extends URIQuery.URIQueryBuilder{
	MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		
	/**
	 * 
	 */
	public URIQueryBuilderImpl() {
		
	}	
	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery.URIQueryBuilder#addParam(java.lang.String, java.lang.String)
	 */
	@Override
	public URIQueryBuilder addParam(String name, String value) {
		map.putSingle(name, value);	
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery.URIQueryBuilder#addParam(java.lang.String, java.util.List)
	 */
	@Override
	public URIQueryBuilder addParam(String name, List<String> value) {
		map.put(name, value);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery.URIQueryBuilder#setRef(java.lang.String)
	 */
	@Override
	public URIQueryBuilder setRef(String reference) {
		map.putSingle("ref", reference);
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery.URIQueryBuilder#setPageSize(java.lang.Integer)
	 */
	@Override
	public URIQueryBuilder setPageSize(Integer pageSize) {
		map.putSingle("pageSize", Integer.toString(pageSize));
		return this;
	}
	/* (non-Javadoc)
	 * @see eu.emi.emir.client.query.URIQuery.URIQueryBuilder#build()
	 */
	@Override
	public URIQuery build() {
		final URIQueryImpl impl = new URIQueryImpl(map);
		return impl;
	}
	@Override
	public URIQueryBuilder setSkip(Integer skip) {
		map.putSingle("skip", Integer.toString(skip));
		return this;
	}
	@Override
	public URIQueryBuilder setResultLimit(Integer limit) {
		map.putSingle("skip", Integer.toString(limit));
		return this;
	}
	@Override
	public URIQueryBuilder mergeURIQuery(URIQuery uriQuery) {
		map.putAll(uriQuery.getMultiValuedMap());
		return this;
	}
	
}
