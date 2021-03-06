/**
 * 
 */
package eu.emi.emir.client.glue2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ogf.schemas.glue._2009._03.spec_2.AccessPolicyT;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointT;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;

/**
 * Converts XML to json array
 * 
 * @author a.memon
 * 
 */
public class JaxbToJson {
	private static Logger logger = Log.getLogger(Log.EMIR_CLIENT,
			JaxbToJson.class);
	
	
	/***
	 * 
	 * @param glueXml valid glue2.0 xml document containing service and endpoint description
	 * @return JSONArray object for registration or update
	 * @throws JSONException
	 * @throws DatatypeConfigurationException
	 */
	public synchronized static JSONArray convert(String glueXml)
			throws JSONException, DatatypeConfigurationException {
		if (glueXml == null || glueXml.isEmpty()) {
			logger.warn("Invalid or NULL GLUE 2.0 XML document");
			throw new IllegalArgumentException(
					"Invalid or NULL GLUE 2.0 XML document");
		}
		InputStream is = new ByteArrayInputStream(glueXml.getBytes());
		ServiceT s = JAXB.unmarshal(is, ServiceT.class);
		return convert(s);
	}

	public synchronized static JSONArray convert(ServiceT service)
			throws JSONException, DatatypeConfigurationException {
		if (service == null || service.getEndpoint().isEmpty()) {
			logger.warn("Invalid or NULL GLUE 2.0 XML document");
			throw new IllegalArgumentException(
					"Invalid or NULL GLUE 2.0 XML document");
		}

		JSONArray emirJsonArr = null;

		emirJsonArr = new JSONArray();

		List<EndpointT> lstEt = service.getEndpoint();

		List<String> lstErr = new ArrayList<String>();

		for (EndpointT ep : lstEt) {
			JSONObject emirJson = new JSONObject();
			// setting service mandatory fields
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

			if (ep.getInterfaceVersion().size() > 0) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
								.getAttributeName(), ep.getInterfaceVersion()
								.get(0));
			} else {
				lstErr.add("Mandatory 'Endpoint Interface Version' attribute missing");
			}

			if (ep.getCapability().size() > 0) {
				JSONArray capArr = new JSONArray();
				for (String capability : ep.getCapability()) {
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

			// Optional attributes
			// mind the limitation of integer here (max must not be greater than
			// 2^31-1)
			if (ep.getValidity() != null && ep.getValidity().intValue() > 0) {

				BigInteger sec = ep.getValidity();

				Calendar c = Calendar.getInstance();

				c.add(Calendar.SECOND, sec.intValue());

				DateUtil.addDate(emirJson,
						ServiceBasicAttributeNames.SERVICE_EXPIRE_ON
								.getAttributeName(), c.getTime());

			}

			if (ep.getAccessPolicy().size() > 0) {
				AccessPolicyT at = ep.getAccessPolicy().get(0);

				if (at.getRule().size() > 0) {
					emirJson.put(
							ServiceBasicAttributeNames.SERVICE_ENDPOINT_ACCESSPOLICY_RULE
									.getAttributeDesc(), at.getRule().get(0));
				}
			}

			if (ep.getDowntimeAnnounce() != null) {
				DateUtil.addDate(
						emirJson,
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE
								.getAttributeName(), DateUtil
								.fromXmlGregorian(ep.getDowntimeAnnounce()));
			}

			if (ep.getDowntimeEnd() != null) {
				DateUtil.addDate(
						emirJson,
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_END
								.getAttributeName(), DateUtil
								.fromXmlGregorian(ep.getDowntimeEnd()));
			}

			if (ep.getDowntimeInfo() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_INFO
								.getAttributeName(), ep.getDowntimeInfo());
			}

			if (ep.getDowntimeStart() != null) {
				DateUtil.addDate(
						emirJson,
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_END
								.getAttributeName(), DateUtil
								.fromXmlGregorian(ep.getDowntimeStart()));
			}

			if (ep.getHealthState() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
								.getAttributeName(), ep.getHealthState()
								.toString());
			}

			if (ep.getHealthStateInfo() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
								.getAttributeName(), ep.getHealthStateInfo()
								.toString());
			}

			if (ep.getImplementationName() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_NAME
								.getAttributeName(), ep.getImplementationName());
			}

			if (ep.getImplementationVersion() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_VERSION
								.getAttributeName(), ep.getImplementationVersion());
			}

			if (ep.getImplementor() != null) {
				emirJson.put(
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPLEMENTOR
								.getAttributeName(), ep.getImplementor());
			}

			if (ep.getOtherInfo() != null && ep.getOtherInfo().size() > 0) {
				JSONArray otherInfo = new JSONArray();
				for(String info : ep.getOtherInfo()){
					otherInfo.put(info);
				}
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_OTHER_INFO.getAttributeName(), otherInfo);
			}

			if (ep.getQualityLevel() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_QUALITYLEVEL.getAttributeName(), ep.getQualityLevel().toString());
			}

			if (ep.getSemantics() != null && ep.getSemantics().size() > 0) {
				JSONArray semantics = new JSONArray();
				for(String info : ep.getSemantics()){
					semantics.put(info);
				}
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SEMANTICS.getAttributeName(), semantics);
			}

			if (ep.getServingState() != null) {
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SERVING_STATE.getAttributeName(), ep.getServingState().toString());
			}

			if (ep.getStartTime() != null) {
				DateUtil.addDate(
						emirJson,
						ServiceBasicAttributeNames.SERVICE_ENDPOINT_STARTTIME
								.getAttributeName(), DateUtil
								.fromXmlGregorian(ep.getStartTime()));
			}

			if (ep.getSupportedProfile() != null
					&& ep.getSupportedProfile().size() > 0) {
				JSONArray profile = new JSONArray();
				for(String info : ep.getSupportedProfile()){
					profile.put(info);
				}
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SUPPORTED_PROFILE.getAttributeName(), profile);

			}

			if (ep.getWSDL() != null && ep.getWSDL().size() > 0) {
				JSONArray wsdl = new JSONArray();
				for(String info : ep.getWSDL()){
					wsdl.put(info);
				}
				emirJson.put(ServiceBasicAttributeNames.SERVICE_ENDPOINT_WSDL.getAttributeName(), wsdl);
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
