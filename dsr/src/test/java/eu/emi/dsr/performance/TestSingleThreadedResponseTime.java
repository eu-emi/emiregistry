/**
 * 
 */
package eu.emi.dsr.performance;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.emi.client.DSRClient;
import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.db.mongodb.MongoDBServiceDatabase;

/**
 * This test class measures the response time for the registrations and query - for graph
 * purpose
 * 
 * @author a.memon
 * 
 */
public class TestSingleThreadedResponseTime {
	DSRClient c = new DSRClient("http://zam1161v02.zam.kfa-juelich.de:54321");

	// variables to hold the readings
	Map<String, Object> regReadings = new HashMap<String, Object>();
	Map<String, Object> queryReadings = new HashMap<String, Object>();

	private static final String Reg10 = "1";
	private static final String Reg100 = "2";
	private static final String Reg1000 = "3";
	private static final String Reg10000 = "4";
	private static final String Reg100000 = "5";
	private static final String Reg1000000 = "6";

	private MongoDBServiceDatabase mdb;
	
	@Before
	public void setUp() {
		mdb = new MongoDBServiceDatabase("zam1161v02.zam.kfa-juelich.de", 27017, "emiregistry-loaddb", "services-loadcol");
	}

	public JSONArray getDummyRegistration(int i) throws JSONException {
		JSONArray ja = new JSONArray();
		JSONObject j = new JSONObject();
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), i);
		j.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), "abc");
		j.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"eu.emi.es");
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), "ok");
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
				.getAttributeName(), "ResourceInfoService");
		ja.put(j);
		return ja;

	}
	
	public JSONArray getDummyRegistration(long i) throws JSONException {
		JSONArray ja = new JSONArray();
		JSONObject j = new JSONObject();
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
				.getAttributeName(), i);
		j.put(ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(), "abc");
		j.put(ServiceBasicAttributeNames.SERVICE_TYPE.getAttributeName(),
				"eu.emi.es");
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
				.getAttributeName(), "ok");
		j.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
				.getAttributeName(), "ResourceInfoService");
		ja.put(j);
		return ja;

	}
	
	public void query(){
		       // use three attributes to query
				MultivaluedMap<String, String> map = new MultivaluedMapImpl();
				map.putSingle(
						ServiceBasicAttributeNames.SERVICE_NAME.getAttributeName(),
						"abc");
				map.putSingle(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
						.getAttributeName(), "ok");
				c.query(map);
	}
	
	public void register(int times) throws JSONException{
		for (int i = 0; i < times; i++) {
			c.register(getDummyRegistration(i));
		}
}

	public void register(Long times) throws JSONException{
		for (int i = 0; i < times; i++) {
			c.register(getDummyRegistration(i));
		}
}
	
	
	@Test
	public void test10() throws Exception {
		Long startTime = System.currentTimeMillis();
		register(10);
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg10, m+" s");
		} else {
			regReadings.put(Reg10, m+ " ms");
		}
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg10, m+" s");
		} else {
			queryReadings.put(Reg10, m+ " ms");
		}
		
		generateReport("10");
	}
	
	

	@Test
	public void test100() throws Exception {
		Long startTime = System.currentTimeMillis();
		register(100);
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg100, m+" s");
		} else {
			regReadings.put(Reg100, m+ " ms");
		}
		
		
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();
		
		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg100, m+" s");
		} else {
			queryReadings.put(Reg100, m+ " ms");
		}
		
		generateReport("100");
	}

	@Test
	public void test1000() throws Exception {
		Long startTime = System.currentTimeMillis();
		register(1000);
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg1000, m+" s");
		} else {
			regReadings.put(Reg1000, m+ " ms");
		}
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();
		
		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg1000, m+" s");
		} else {
			queryReadings.put(Reg1000, m+ " ms");
		}
		
		generateReport("1000");
	}

	@Test
	public void test10000() throws Exception {
		Long startTime = System.currentTimeMillis();
		register(10000);
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg10000, m+" s");
		} else {
			regReadings.put(Reg10000, m+ " ms");
		}
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg10000, m+" s");
		} else {
			queryReadings.put(Reg10000, m+ " ms");
		}
		
		
		generateReport("10000");
	}

	@Test
	public void test100000() throws Exception {
		Long startTime = System.currentTimeMillis();
		register(100000);
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg100000, m+" s");
		} else {
			regReadings.put(Reg100000, m+ " ms");
		}
		
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();

		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg100000, m+" s");
		} else {
			queryReadings.put(Reg100000, m+ " ms");
		}
		
		
		generateReport("100000");
	}
	
	@Test
	public void test1000000() throws Exception {
		Long startTime = System.currentTimeMillis();
		for (Long i = 0L; i < 1000000; i++) {
			c.register(getDummyRegistration(i));
		}
		Long endTime = System.currentTimeMillis();
		Long mean = endTime-startTime;
		
		Double m = mean.doubleValue();
		if ((m/100) >= 1) {
			m = m/1000;
			regReadings.put(Reg1000000, m+" s");
		} else {
			regReadings.put(Reg1000000, m+ " ms");
		}
		
		
		
		startTime = System.currentTimeMillis();
		query();
		endTime = System.currentTimeMillis();
		mean = endTime-startTime;
		
		m = mean.doubleValue();

		if ((m/100) >= 1) {
			m = m/1000;
			queryReadings.put(Reg1000000, m+" s");
		} else {
			queryReadings.put(Reg1000000, m+ " ms");
		}
		
		
		generateReport("1000000");
	}

	

	@After
	public void tearDown() {
		//clean-up the database in every call
		mdb.dropCollection();
		mdb.dropDB();
		
	}

	/**
	 * @param string 
	 * 
	 */
	private void generateReport(String string) {
		System.out.println("==== Registration of "+string+" records ====");
		System.out.println(regReadings);
		
		System.out.println("==== Querying of "+string+" records ====");
		System.out.println(queryReadings);
	}

}
