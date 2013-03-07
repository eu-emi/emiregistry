/**
 * 
 */
package eu.emi.emir.client.util;

import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author a.memon
 *
 */
public interface ExtentendedMultiValuedMap<K, V> extends MultivaluedMap<K, V>{
	/***
	 * key / single-value pairs to initialise the multivalued map
	 * 
	 * @param map 
	 */
	public void putAllMap(Map<K, V> map);
	
	/***
	 * 
	 * 
	 * @return key / single-value pairs  
	 */
	public Map<K,V> getMap();
}
