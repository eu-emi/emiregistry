/**
 * 
 */
package eu.emi.emir.glue2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;

import eu.emi.emir.glue2.Glue2Mapper;
import eu.emi.emir.glue2.JSONToGlue2MappingException;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon
 * 
 */
public class TestGlue2Mapper {
	JSONObject jo;

	@Before
	public void setup() throws IOException, JSONException {
		String str = FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json"));
		System.out.println(str);
		jo = new JSONObject(FileUtils.readFileToString(new File("src/test/resources/json/serviceinfo.json")));
	}

	@Test
	public void testToQueryResultSingleXML() throws Exception {
		Glue2Mapper gm = new Glue2Mapper();
		JSONArray ja = new JSONArray();
		ja.put(jo);
		JAXBElement<ServiceT>[] sa = gm.toGlue2Service(ja);
		assertNotNull(sa[0].getValue().getName());

		StringWriter sw = new StringWriter();
		QueryResult qr = gm.toQueryResult(ja);

		JAXB.marshal(qr, sw);
		System.out.println(sw);

	}

	@Test
	public void testToQueryResultMultipleXML() throws Exception {
		Glue2Mapper gm = new Glue2Mapper();
		JSONArray ja = new JSONArray();
		ja.put(jo);
		ja.put(jo);
		QueryResult qr = gm.toQueryResult(ja);
		assertNotNull(qr.getService().get(0).getName());
		assertEquals(new BigInteger("" + 2), qr.getCount());
		StringWriter sw = new StringWriter();
		JAXB.marshal(qr, sw);
		System.out.println(sw);

	}

	@Test
	public void testExtensions() throws JSONException, DatatypeConfigurationException, ParseException, JSONToGlue2MappingException {
		Glue2Mapper gm = new Glue2Mapper();
		JSONArray ja = new JSONArray();
		jo.put("myextension1", "value1");
		jo.put("myextension2", "value2");
		ja.put(jo);
		
		QueryResult qr = gm.toQueryResult(ja);
		
		
		assertNotNull(qr.getService().get(0).getName());
		assertEquals("myextension1", qr.getService().get(0).getExtensions().getExtension().get(0).getKey());
		StringWriter sw = new StringWriter();
		JAXB.marshal(qr, sw);
		System.out.println(sw);
	}
}
