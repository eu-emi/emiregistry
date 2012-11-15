/**
 * 
 */
package eu.emi.emir.resource;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXB;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.emi.emir.TestRegistryBase;
import eu.emi.emir.client.EMIRClient;
import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.TestValueConstants;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.db.ExistingResourceException;
import eu.emi.emir.db.PersistentStoreFailureException;
import eu.emi.emir.db.mongodb.MongoDBServiceDatabase;
import eu.emi.emir.db.mongodb.ServiceObject;
import eu.eu_emi.emiregistry.QueryResult;
import eu.unicore.bugsreporter.annotation.FunctionalTest;

/**
 * Integration test
 * 
 * @author a.memon
 * 
 */
public class TestServiceCollectionResource extends TestRegistryBase {

	public static MongoDBServiceDatabase db;

	// @Before
	public void _setUp() throws JSONException, ExistingResourceException,
			PersistentStoreFailureException {
		@SuppressWarnings("unused")
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 12);
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services");
		JSONObject date = new JSONObject();
		date.put("$date", DateUtil.toUTCFormat(c.getTime()));
		for (int i = 0; i < 50; i++) {
			JSONObject entry1 = new JSONObject();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://" + UUID.randomUUID());
			entry1.put(
					ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"jms");
			entry1.put(ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
					.getAttributeName(), date);
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "critical");
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
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "ok");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);
		}
	}

	@Before
	public void setUp() throws JSONException, ExistingResourceException,
			PersistentStoreFailureException {
		JSONObject jo = TestValueConstants.getJSONWithMandatoryAttributes();
		db = new MongoDBServiceDatabase("localhost", 27017, "emiregistry",
				"services");
		db.deleteAll();
		for (int i = 0; i < 50; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://" + UUID.randomUUID());
			jo.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"jms");
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "critical");
			ServiceObject so = new ServiceObject(jo);
			db.insert(so);
		}
		for (int i = 0; i < 50; i++) {
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
					.getAttributeName(), "http://" + UUID.randomUUID());
			jo.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
					"sms");
			jo.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
					.getAttributeName(), "ok");
			ServiceObject so = new ServiceObject(jo);
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
			EMIRClient cr = new EMIRClient(BaseURI);
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			java.util.List<String> lst = new ArrayList<String>();
			lst.add("jms");
			map.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), lst);
			JSONArray o = cr.queryByQueryParams(map);
			System.out.println(o.toString(2));
			assertEquals(50, o.length());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testFindNone() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI);
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			java.util.List<String> lst = new ArrayList<String>();
			lst.add("blah");
			map.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), lst);
			JSONArray o = cr.queryByQueryParams(map);
			System.out.println(o);
			assertTrue(o.length() == 0);
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testLimit20() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI);
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			java.util.List<String> lst = new ArrayList<String>();
			lst.add("jms");
			java.util.List<String> lstLimit = new ArrayList<String>();
			lstLimit.add("20");
			map.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), lst);
			map.put("limit", lstLimit);
			JSONArray o = cr.queryByQueryParams(map);
			assertTrue(o.length() == 20);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testFindLast30() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI);
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			java.util.List<String> lst = new ArrayList<String>();
			lst.add("jms");
			java.util.List<String> lstSkip = new ArrayList<String>();
			lstSkip.add("20");
			map.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), lst);
			map.put("skip", lstSkip);
			JSONArray o = cr.queryByQueryParams(map);
			System.out.println(o.length());
			System.out.println(o.toString(2));
			assertTrue(o.length() == 30);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void testFindLastHalfOf40() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI);
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			java.util.List<String> lst = new ArrayList<String>();
			lst.add("jms");
			java.util.List<String> lstLimit = new ArrayList<String>();
			lstLimit.add("20");
			java.util.List<String> lstSkip = new ArrayList<String>();
			lstSkip.add("20");
			map.put(ServiceBasicAttributeNames.SERVICE_TYPE.toString(), lst);
			map.put("skip", lstSkip);
			map.put("limit", lstLimit);
			JSONArray o = cr.queryByQueryParams(map);
			System.out.println(o.length());
			assertEquals(20, o.length());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGetSupportedTypes() throws JSONException {
		EMIRClient cr = new EMIRClient(BaseURI + "/services/types");
		JSONArray o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);
		System.out.println(o);
		assertTrue(o.get(0).equals("jms") || o.get(0).equals("sms"));

	}

	@Test
	@FunctionalTest(id = "ServiceEndpointRecordsQueryForJSONTest", description = "Test queries defined in JSON format for the service endpoint records in JSON format")
	public void testRichQueryForJSON() throws JSONException {
		EMIRClient cr = new EMIRClient(BaseURI);

		// { $or: [ { Service_Type: jms }, { Service_Endpoint_HealthState: ok }
		// ] }
		JSONObject orQueryDocument = new JSONObject();
		JSONObject p1 = new JSONObject();
		p1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		JSONObject p2 = new JSONObject();
		p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), "ok");
		JSONArray j = new JSONArray();
		j.put(p1);
		j.put(p2);
		orQueryDocument.put("$or", j);
		assertEquals(100, cr.richQueryForJSON(orQueryDocument).length()-1);

		// { $and: [ { Service_Type: jms }, { Service_Endpoint_HealthState:
		// critical } ] }
		// p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(),
		// "critical");
		// JSONObject andQueryDocument = new JSONObject();
		// JSONArray j1 = new JSONArray();
		// j1.put(p1);
		// j1.put(p2);
		// andQueryDocument.put("$and", j1);
		// System.out.println(andQueryDocument);
		// System.out.println(cr.queryJSON(andQueryDocument).length());

		// { Service_Endpoint_HealthState : { $ne : ok } } }
		JSONObject jo = new JSONObject();
		jo.put("$ne", "ok");
		JSONObject ne = new JSONObject();
		ne.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), jo);
		assertEquals(50, cr.richQueryForJSON(ne).length());

	}
	
	@Test
	@FunctionalTest(id = "ServiceEndpointRecordsQueryForXMLTest", description = "Test queries defined in JSON format for the service endpoint records in XML format")
	public void testRichQueryForXML() throws JSONException {
		EMIRClient cr = new EMIRClient(BaseURI);

		// { $or: [ { Service_Type: jms }, { Service_Endpoint_HealthState: ok }
		// ] }
		JSONObject orQueryDocument = new JSONObject();
		JSONObject p1 = new JSONObject();
		p1.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"jms");
		JSONObject p2 = new JSONObject();
		p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), "ok");
		JSONArray j = new JSONArray();
		j.put(p1);
		j.put(p2);
		orQueryDocument.put("$or", j);
		assertEquals(100, cr.richQueryForXML(orQueryDocument).getService().size());

		// { $and: [ { Service_Type: jms }, { Service_Endpoint_HealthState:
		// critical } ] }
		// p2.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE.getAttributeName(),
		// "critical");
		// JSONObject andQueryDocument = new JSONObject();
		// JSONArray j1 = new JSONArray();
		// j1.put(p1);
		// j1.put(p2);
		// andQueryDocument.put("$and", j1);
		// System.out.println(andQueryDocument);
		// System.out.println(cr.queryJSON(andQueryDocument).length());

		// { Service_Endpoint_HealthState : { $ne : ok } } }
		JSONObject jo = new JSONObject();
		jo.put("$ne", "ok");
		JSONObject ne = new JSONObject();
		ne.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), jo);
		assertEquals(50, cr.richQueryForXML(ne).getService().size());
	}

	@Test
	public void testSimplePagedQueryWithJSON() throws JSONException {
		// starting page
		EMIRClient cr = new EMIRClient(BaseURI
				+ "/services?Service_Type=jms&pageSize=10");
		JSONArray o = cr.getClientResource()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(JSONArray.class);

		System.out.println(o.toString(2));

		assertEquals(10, o.length() - 1);

		System.out.println(o.length());

		System.out.println(BaseURI
				+ "/services?Service_Type=jms&pageSize=10&ref="
				+ o.getJSONObject(o.length() - 1).getString("ref"));

		cr = new EMIRClient(BaseURI
				+ "/services?Service_Type=jms&pageSize=10&ref="
				+ o.getJSONObject(o.length() - 1).getString("ref"));
		o = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONArray.class);
		assertEquals(10, o.length() - 1);

		System.out.println(BaseURI
				+ "/services?Service_Type=jms&pageSize=10&ref="
				+ o.getJSONObject(o.length() - 1).getString("ref"));

		cr = new EMIRClient(BaseURI
				+ "/services?Service_Type=jms&pageSize=10&ref="
				+ o.getJSONObject(o.length() - 1).getString("ref"));
		o = cr.getClientResource().accept(MediaType.APPLICATION_JSON_TYPE)
				.get(JSONArray.class);
		assertEquals(10, o.length() - 1);

	}

	@Test
	public void testSimplePagedQueryWithXML() throws JSONException {

		// starting page
		EMIRClient cr = new EMIRClient(BaseURI
				+ "/services?Service_Type=jms&pageSize=10");
		QueryResult o = cr.getClientResource()
				.accept(MediaType.APPLICATION_XML_TYPE).get(QueryResult.class);

		JAXB.marshal(o, System.out);

		System.out.println(o.getService().size());

		assertEquals(10, o.getService().size());

		// test for next 20 records
		cr = new EMIRClient(BaseURI
				+ "/services?Service_Type=jms&pageSize=20&ref=" + o.getRef());

		o = cr.getClientResource().accept(MediaType.APPLICATION_XML_TYPE)
				.get(QueryResult.class);

		JAXB.marshal(o, System.out);

		System.out.println(o.getService().size());

		assertEquals(20, o.getService().size());
	}

	
	@Deprecated
	@Test
	@FunctionalTest(id = "ServiceQueryGLUE2Test", description = "Test querying the service records in GLUE 2.0 format")
	public void testGlue2QueryCollection() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI
					+ "/services/query.xml?Service_Type=jms");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("50")));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGlue2QueryCollectionWithMIME() {
		try {
			EMIRClient cr = new EMIRClient(BaseURI
					+ "/services?Service_Type=jms");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			System.out.println(o.getService().size());
			assertTrue(o.getCount().equals(new BigInteger("50")));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testGlue2QueryCollectionWithParamMIMEType() {
		try {
			JSONObject entry1 = TestValueConstants
					.getJSONWithMandatoryAttributes();
			entry1.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
					.getAttributeName(), "http://3");
			ServiceObject so = new ServiceObject(entry1);
			db.insert(so);

			EMIRClient cr = new EMIRClient(BaseURI
					+ "/services?Service_Endpoint_URL=http://3");
			QueryResult o = cr.getClientResource()
					.accept(MediaType.APPLICATION_XML_TYPE)
					.get(QueryResult.class);
			JAXB.marshal(o, System.out);
			assertTrue(o.getCount().equals(new BigInteger("1")));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
