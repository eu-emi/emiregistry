/**
 * 
 */
package eu.emi.emir.client.glue2;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXB;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointT;
import org.ogf.schemas.glue._2009._03.spec_2.ObjectFactory;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;
import static org.junit.Assert.*;

/**
 * @author a.memon
 *
 */
public class TestJaxbToJson {
	@Test
	public void convertSingleXmlDoc() throws JSONException{
		ObjectFactory of = new ObjectFactory();
		ServiceT s = of.createServiceT();
		s.setID("id_1");
		s.setName("name_1");
		s.setType("type_1");
		List<EndpointT> lstEp = s.getEndpoint();
		EndpointT et = of.createEndpointT();
		
		et.setID("epr_1");
		et.setInterfaceName("iname_1");
		et.getInterfaceVersion().add("1.0.0");
		et.setTechnology("webservices");
		et.setURL("http://1");
		et.setImplementationName("emi-es");
		et.setImplementationVersion("1.x.x");
		et.getCapability().add("jobmgmt");
		et.getCapability().add("workflow");
		et.getCapability().add("cloud");
		et.setValidity(new BigInteger("360000"));
		lstEp.add(et);
		
		StringWriter sw = new StringWriter();
		JAXB.marshal(s, sw);
		System.out.println(sw);
		
		JSONArray ja = JaxbToJson.convert(sw.toString());
		
		System.out.println(ja.toString(2));
		
		assertTrue(ja.length() == 1);
		
		
	}
	
	@Test
	public void convertTwoXmlDocs() throws JSONException{
		ObjectFactory of = new ObjectFactory();
		ServiceT s = of.createServiceT();
		s.setID("id_1");
		s.setName("name_1");
		s.setType("type_1");
		List<EndpointT> lstEp = s.getEndpoint();
		
		EndpointT et = of.createEndpointT();
		et.setID("epr_1");
		et.setInterfaceName("iname_1");
		et.getInterfaceVersion().add("1.0.0");
		et.setTechnology("webservices");
		et.setURL("http://1");
		et.setImplementationName("emi-es");
		et.setImplementationVersion("1.x.x");
		et.getCapability().add("jobmgmt");
		et.getCapability().add("workflow");
		et.getCapability().add("cloud");
		
		EndpointT et1 = of.createEndpointT();
		et1.setID("epr_2");
		et1.setInterfaceName("iname_2");
		et1.getInterfaceVersion().add("1.0.0");
		et1.setTechnology("rmi");
		et1.setURL("http://2");
		et1.setImplementationName("bes");
		et1.setImplementationVersion("2.x.x");
		et1.getCapability().add("virtualisation");
		et1.getCapability().add("workflow");
		et1.getCapability().add("cloud");
				
		lstEp.add(et);
		lstEp.add(et1);
		
		StringWriter sw = new StringWriter();
		JAXB.marshal(s, sw);
		System.out.println(sw);
		
		JSONArray ja = JaxbToJson.convert(sw.toString());
	}
}
