/**
 * 
 */
package eu.emi.dsr.core;



import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.junit.Test;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionsT;
import org.ogf.schemas.glue._2009._03.spec_2.ObjectFactory;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;
import static org.junit.Assert.*;

/**
 * @author a.memon
 *
 */
public class TestJAXB {
	@Test
	public void test(){
		ObjectFactory o = new ObjectFactory();
		
		ServiceT st = o.createServiceT();
		st.setType("jms");
		st.setName("job management service");
		
		EndpointT ent = o.createEndpointT();
		ent.setURL("http://1");
		st.getEndpoint().add(ent);
		
		ExtensionT et = o.createExtensionT();
		et.setKey("updateSince");
		et.setValue(new Date().toString());
		ExtensionsT ets = new ExtensionsT();
		ets.getExtension().add(et);
		
		ExtensionT et1 = o.createExtensionT();
		et1.setKey("expireOn");
		et1.setValue(new Date().toString());
		ets.getExtension().add(et1);
		st.setExtensions(ets);
		JAXBElement<ServiceT> jst = o.createService(st);
		StringWriter sw = new StringWriter();
		JAXB.marshal(jst,sw);
		System.out.println(sw);
		assertTrue(jst.getValue().getEndpoint().get(0).getURL()=="http://1");
	}
	
	
	@Test
	public void testBF() throws JSONException, XMLStreamException{
		JSONObject obj = new JSONObject("{\"alice\":\"bob\"}}");
        AbstractXMLStreamReader reader = new MappedXMLStreamReader(obj);

        // BadgerFish
        // JSONObject obj = new JSONObject("{ \"alice\": { \"$\" : \"bob\" } }");
        // AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());


        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("bob", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
	}
}
