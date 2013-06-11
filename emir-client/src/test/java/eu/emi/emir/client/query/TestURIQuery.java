package eu.emi.emir.client.query;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 * 
 */

/**
 * @author a.memon
 *
 */
public class TestURIQuery {
	@Test
	public void testSimpleQuery(){
		assertEquals("a-param",URIQuery.builder().addParam("a", "a-param").addParam("b", "b-param").build().getMultiValuedMap().getFirst("a"));
	}
	
	@Test
	public void testMergedQuery(){
		URIQuery eq = EndpointQuery.endpointBuilder().setPageSize(100).build();
		URIQuery mergedQuery = URIQuery.builder().setRef("asdasdsd").mergeURIQuery(eq).build();
		assertTrue((mergedQuery.getMultiValuedMap().containsKey("ref")));
	}
	
	
}
