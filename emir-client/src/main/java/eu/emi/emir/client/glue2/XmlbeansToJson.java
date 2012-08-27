/**
 * 
 */
package eu.emi.emir.client.glue2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ogf.schemas.glue.x2009.x03.spec20R1.EndpointT;
import org.ogf.schemas.glue.x2009.x03.spec20R1.ServiceT;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;

/**
 * Converts GLUE 2.0 XMLBeans XML to EMIR's JSON
 * 
 * @author a.memon
 * 
 */
public class XmlbeansToJson {
	private static Logger logger = Log.getLogger(Log.EMIR_CLIENT,
			XmlbeansToJson.class);

	public synchronized static JSONArray convert(String serviceXML)
			throws XmlException, JSONException {
		ServiceT service = ServiceT.Factory.parse(serviceXML);
		return convert(service);
	}

	public synchronized static JSONArray convert(final ServiceT service)
			throws JSONException {
		if (service == null) {
			logger.warn("Invalid or NULL GLUE 2.0 XML document");
			throw new IllegalArgumentException(
					"Invalid or NULL GLUE 2.0 XML document");
		}

		if (service.getEndpointArray().length == 0) {
			logger.warn("No Endpoints found in the XML document");
			throw new IllegalArgumentException(
					"No Endpoints found in the XML document");
		}

		JSONArray emirJsonArr = null;

		emirJsonArr = new JSONArray();

		EndpointT[] epArr = service.getEndpointArray();

		for (EndpointT ep : epArr) {

			List<String> lstErr = new ArrayList<String>();

			JSONObject emirJson = new JSONObject();

			// setting service mandatory attributes
			if (service.getID() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ID
						.getAttributeName(), service.getID());
			} else {
				lstErr.add("Mandatory 'Service ID' attribute missing");
			}

			if (service.getName() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ID
						.getAttributeName(), service.getName());
			} else {
				lstErr.add("Mandatory 'Service Name' attribute missing");
			}

			if (service.getType() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_TYPE
						.getAttributeName(), service.getType());
			} else {
				lstErr.add("Mandatory 'Service Type' attribute missing");
			}

			// setting endpoint mandatory attributes
			if (ep.getID() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
						.getAttributeName(), ep.getID());
			} else {
				lstErr.add("Mandatory 'Endpoint ID' attribute missing");
			}

			if (ep.getURL() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
						.getAttributeName(), ep.getURL());
			} else {
				lstErr.add("Mandatory 'Endpoint URL' attribute missing");
			}

			if (ep.getInterfaceName() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
								.getAttributeName(), ep.getInterfaceName());
			} else {
				lstErr.add("Mandatory 'Endpoint Interface Name' attribute missing");
			}

			if (ep.getInterfaceVersionArray(0).length() > 0) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
								.getAttributeName(), ep
								.getInterfaceVersionArray(0));
			} else {
				lstErr.add("Mandatory 'Endpoint Interface Version' attribute missing");
			}

			if (ep.getCapabilityArray().length > 0) {
				JSONArray capArr = new JSONArray();
				for (String capability : ep.getCapabilityArray()) {
					capArr.put(capability);
				}
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
								.getAttributeName(), capArr);
			} else {
				lstErr.add("Mandatory 'Endpoint Capability' attribute missing");
			}

			if (ep.getTechnology() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
								.getAttributeName(), ep.getTechnology());

			} else {
				lstErr.add("Mandatory 'Endpoint Technology' attribute missing");
			}
			
			
			//mind the limitation of integer here
			if (ep.getValidity() != null) {

				BigInteger sec = ep.getValidity();

				Calendar c = Calendar.getInstance();

				c.add(Calendar.SECOND, sec.intValue());

				DateUtil.addDate(emirJson,
						ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
								.getAttributeName(), c.getTime());

			}

			if (lstErr.size() > 0) {
				StringBuilder b = new StringBuilder();
				b.append("Incomplete Endpoint description:");
				for (String err : lstErr) {
					b.append("\n-" + err);
				}
				b.append("\nfrom: " + service);
				logger.error(b.toString());
				continue;
			}

			emirJsonArr.put(emirJson);

		}

		if (logger.isTraceEnabled()) {
			logger.trace("Valid JSON created from GLUE 2.0 XML: "
					+ emirJsonArr.toString(2));
		}
		return emirJsonArr;
	}

}
