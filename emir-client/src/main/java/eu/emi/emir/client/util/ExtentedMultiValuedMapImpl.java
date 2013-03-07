/**
 * 
 */
package eu.emi.emir.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author a.memon
 * 
 */
public class ExtentedMultiValuedMapImpl extends MultivaluedMapImpl implements
		ExtentendedMultiValuedMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.emir.core.ExtentendedMultiValuedMap#putAllMap(java.util.Map)
	 */
	@Override
	public void putAllMap(Map<String, String> map) {
		Set<String> keys = map.keySet();

		for (String k : keys) {
			putSingle(k, map.get(k));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.emi.emir.client.util.ExtentendedMultiValuedMap#getMap()
	 */
	@Override
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<String, String>();
		Set<String> keys = keySet();
		for (String k : keys) {
			map.put(k, getFirst(k));			
		}
		return map;
	}

}
