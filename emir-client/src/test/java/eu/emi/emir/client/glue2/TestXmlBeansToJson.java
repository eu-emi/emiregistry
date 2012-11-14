/**
 * 
 */
package eu.emi.emir.client.glue2;

import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.ogf.schemas.glue.x2009.x03.spec20R1.EndpointT;
import org.ogf.schemas.glue.x2009.x03.spec20R1.ServiceT;
import static org.junit.Assert.*;
import eu.emi.emir.client.glue2.XmlbeansToJson;


/**
 * @author a.memon
 * 
 */
public class TestXmlBeansToJson {
	ServiceT service;

	@Before
	public void setUp() {
		service = ServiceT.Factory.newInstance();
		service.setID("id");
		service.setName("name");
		service.setType("some-type");

		EndpointT et = service.addNewEndpoint();
		et.addInterfaceVersion("1.0.0-1");
		et.setID("1");
		et.setURL("http://1");
		et.setInterfaceName("emi-es");
		et.addCapability("compute");
		et.addCapability("storage");
		et.setTechnology("webservice");

		EndpointT et1 = service.addNewEndpoint();
		et1.addInterfaceVersion("2.0.0-1");
		et1.setID("2");
		et1.setURL("http://2");
		et1.setInterfaceName("emi-es");
		et1.addCapability("discovery");
		et1.addCapability("brokering");
		et1.setTechnology("webservice");
	}

	@Test
	public void test() throws JSONException {
		System.out.println(XmlbeansToJson.convert(service).toString(2));
		assertTrue(XmlbeansToJson.convert(service).length() > 0);
	}
	
	@Test
	public void testString() throws JSONException, XmlException {
		assertNotNull(XmlbeansToJson.convert(service.toString()));
	}

}
