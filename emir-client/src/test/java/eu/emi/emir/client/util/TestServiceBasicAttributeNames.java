/**
 * 
 */
package eu.emi.emir.client.util;

import org.junit.Test;

import eu.emi.emir.client.ServiceBasicAttributeNames;

import static  org.junit.Assert.*;

/**
 * @author a.memon
 *
 */
public class TestServiceBasicAttributeNames {
	@Test
	public void testFromString(){
			ServiceBasicAttributeNames s = ServiceBasicAttributeNames.fromString("Service_Endpoint_ID");
			assertTrue(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName() == s.getAttributeName());
			System.out.println(s.getAttributeName());
			ServiceBasicAttributeNames s1 = ServiceBasicAttributeNames.fromString("Service_Endpoint_ID_1");
			//it should return NULL if it does not reside in the enums
			assertNull(s1);	
		
	}
}
