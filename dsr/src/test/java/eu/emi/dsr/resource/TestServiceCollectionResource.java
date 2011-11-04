/**
 * 
 */
package eu.emi.dsr.resource;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.TestRegistryBase;
import eu.emi.client.DSRClient;
import eu.emi.client.ServiceBasicAttributeNames;
import eu.emi.dsr.db.ExistingResourceException;
import eu.emi.dsr.db.PersistentStoreFailureException;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;
import eu.emi.dsr.db.mongodb.ServiceObject;
import eu.emi.dsr.util.ServiceUtil;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * Integration test
 * 
 * @author a.memon
 * 
 */
public class TestServiceCollectionResource extends TestRegistryBase {

	public static MongoDBServiceDatabase db;

	@Before
	public void setUp() throws JSONException, ExistingResourceException,
			PersistentStoreFailureException {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 12);
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services-test");
		JSONObject date = new JSONObject();
		date.put("$date", ServiceUtil.toUTCFormat(c.getTime()));
		for (int i = 0; i < 50; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"jms");
			entry1.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(), "critical");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
		for (int i = 0; i < 50; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"sms");
			entry1.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(), "ok");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
	}

	@After
	public void cleanUp() throws JSONException {
		db.deleteAll();
		assertTrue(db.findAll().size() == 0);
	}

	/**
	 * This test may fail sometimes because of the timing
	 */
	@Test
	public void testFindByType() {
		try {
			DSRClient cr = new DSRClient(BaseURI + "/services?Service_Type=jms");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			assertTrue(o.length() == 50);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testFindNone() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services?Service_Type=blah");
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);

			assertTrue(o.length() == 0);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testLimit20() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services?Service_Type=jms&limit=20");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			System.out.println(o.length());
			assertTrue(o.length() == 20);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testFindLast30() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services?Service_Type=jms&skip=20");
			// JSONObject o =
			// cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);
			System.out.println(o.length());
			assertTrue(o.length() == 30);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testFindLastHalfOf40() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services?Service_Type=jms&skip=20&limit=20");
			JSONArray o = cr.getClientResource()
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(JSONArray.class);

			assertEquals(20, o.length());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetSupportedTypes() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI + "/services/types");
		JSONArray o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		System.out.println(o);
		assertTrue(o.get(0).equals("jms") || o.get(0).equals("sms"));

	}

	@Test
	public void testQueryJSON() throws JSONException {
		DSRClient cr = new DSRClient(BaseURI);
		
		
		//{ $or: [ { Service_Type: jms }, { Service_Endpoint_HealthState: ok } ] }
		JSONObject orQueryDocument = new JSONObject();
		JSONObject p1 = new JSONObject();
		p1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(), "jms");
		JSONObject p2 = new JSONObject();
		p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(), "ok");
		JSONArray j = new JSONArray();
		j.put(p1);
		j.put(p2);
		orQueryDocument.put("$or", j);
		assertTrue(cr.queryJSON(orQueryDocument).length()==100);
		
		//{ $and: [ { Service_Type: jms }, { Service_Endpoint_HealthState: critical } ] }
//		p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(), "critical");
//		JSONObject andQueryDocument = new JSONObject();
//		JSONArray j1 = new JSONArray();
//		j1.put(p1);
//		j1.put(p2);
//		andQueryDocument.put("$and", j1);
//		System.out.println(andQueryDocument);
//		System.out.println(cr.queryJSON(andQueryDocument).length());
		
		//{ Service_Endpoint_HealthState : { $ne : ok } } }
		JSONObject jo = new JSONObject();
		jo.put("$ne", "ok");
		JSONObject ne = new JSONObject();
		ne.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(), jo);
		assertTrue(cr.queryJSON(ne).length()==50);
		
	}

	@Test
	public void testPagedQuery() throws JSONException {
		// starting page
		DSRClient cr = new DSRClient(BaseURI
				+ "/services/pagedquery?Service_Type=jms&pageSize=10");
		JSONObject o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());

		System.out.println(BaseURI
				+ "/services/pagedquery?Service_Type=jms&pageSize=10&ref="
				+ o.get("ref"));
		cr = new DSRClient(BaseURI
				+ "/services/pagedquery?Service_Type=jms&pageSize=10&ref="
				+ o.get("ref"));
		o = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());

		System.out.println(BaseURI
				+ "/services/pagedquery?Service_Type=jms&pageSize=10&ref="
				+ o.get("ref"));
		cr = new DSRClient(BaseURI
				+ "/services/pagedquery?Service_Type=jms&pageSize=10&ref="
				+ o.get("ref"));
		o = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONObject.class);
		assertEquals(10, o.getJSONArray("result").length());

	}

	@Test
	public void testGlue2QueryCollection() {
		try {
			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query.xml?Service_Type=jms");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("50")));
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testGlue2QueryCollectionWithMIME() {
		try {
			DSRClient cr = new DSRClient(BaseURI + "/services?Service_Type=jms");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("50")));
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testGlue2QueryCollection1() {
		try {
			JSONObject entry1 = new JSONObject();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://1");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);

			DSRClient cr = new DSRClient(BaseURI
					+ "/services/query.xml?Service_Endpoint_URL=http://1");
			QueryResult o = cr.getClientResource().get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("1")));
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testGlue2QueryCollectionWithParamMIMEType() {
		try {
			JSONObject entry1 = new JSONObject();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://1");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);

			DSRClient cr = new DSRClient(BaseURI
					+ "/services?Service_Endpoint_URL=http://1");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("1")));
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	

}
