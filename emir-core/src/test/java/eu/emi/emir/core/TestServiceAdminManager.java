/**
 * 
 */
package eu.emi.emir.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.emir.EMIRServer;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.MultipleResourceException;
import eu.emi.emir.db.NonExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.MongoDBTestBase;
import eu.emi.emir.exception.UnknownServiceException;
/**
 * @author a.memon
 * 
 */

public class TestServiceAdminManager extends MongoDBTestBase{
	static ServiceAdminManager adminMgr;

	@Before
	public void setup() {
		Properties p = new Properties();
		p.setProperty("emir.address", "http://localhost:54321");
		@SuppressWarnings("unused")
		EMIRServer s = new EMIRServer(p);		
		adminMgr = new ServiceAdminManager(new MongoDBServiceDatabase("localhost",27017, "emiregistry", "services"));
		adminMgr.removeAll();
		
	}

	@Test
	public void addServiceDescription() throws Exception {		
		assertNotNull(adminMgr.addService(TestValueConstants.getJSONWithMandatoryAttributes()));
		System.out.println("service added");
		
	}
	
	@Test(expected = ExistingResourceException.class)
	public void failDuplicateInsertion() throws Exception {		
		adminMgr.addService(TestValueConstants.getJSONWithMandatoryAttributes());
		adminMgr.addService(TestValueConstants.getJSONWithMandatoryAttributes());				
	}

	

	@Test
	public void updateService() throws Exception {
		String id = "emi-es-id";
		
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), id);
		adminMgr.addService(jo);
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName(), "emi-es");
		assertNotNull(adminMgr.updateService(jo));
		System.out.println("service endpoint record updated");
		assertEquals("emi-es", adminMgr.findServiceByEndpointID(id).get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME.getAttributeName()));
	}

	@Test
	public void findServiceByEndpointID() throws Exception {
		String id = "emi-es-id";
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID.getAttributeName(), id);
		adminMgr.addService(jo);
		System.out.println(adminMgr.findServiceByEndpointID(id).get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
		assertEquals("http://1",adminMgr.findServiceByEndpointID(id).get(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL.getAttributeName()));
	}
	
	
	
	@After
	public void cleanUp() throws UnknownServiceException, MultipleResourceException, NonExistingResourceException, PersistentStoreFailureException{
		adminMgr.removeAll();
	}
}
