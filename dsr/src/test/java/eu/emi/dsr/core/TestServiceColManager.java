/**
 * 
 */
package eu.emi.dsr.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.QueryException;
import eu.emi.dsr.db.mongodb.MongoDBTestBase;
/**
 * @author a.memon
 * 
 */
public class TestServiceColManager extends MongoDBTestBase{
	ServiceColManager mgr = null;
	@Before
	public void setup(){
		mgr = new ServiceColManager();
	}
	@Test
	public void testGetServiceByRefernces() {
		try {
			JSONArray o = mgr.getServiceReferences();
			assertNotNull(o);
			System.out.println(o);
		} catch (JSONException e) {
			fail();
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (PersistentStoreFailureException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	

}
