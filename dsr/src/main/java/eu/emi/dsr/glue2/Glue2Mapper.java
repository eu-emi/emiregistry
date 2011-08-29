/**
 * 
 */
package eu.emi.dsr.glue2;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointHealthStateT;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionsT;
import org.ogf.schemas.glue._2009._03.spec_2.ObjectFactory;
import org.ogf.schemas.glue._2009._03.spec_2.QualityLevelT;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;
import org.ogf.schemas.glue._2009._03.spec_2.ServingStateT;

import eu.emi.dsr.core.ServiceBasicAttributeNames;
import eu.emi.dsr.util.ServiceUtil;
import eu.eu_emi.emiregistry.QueryResult;

/**
 * @author a.memon
 * 
 */
public class Glue2Mapper {
	private static SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	@SuppressWarnings("unused")
	private static List<String> lstNames;
	private final ObjectFactory of;
	/**
	 * 
	 */
	
	public Glue2Mapper() {
		of = new ObjectFactory();
		createAttributelist();
	}
	public QueryResult toQueryResult(JSONArray jo) throws JSONException,
			DatatypeConfigurationException, ParseException {
		eu.eu_emi.emiregistry.ObjectFactory o = new eu.eu_emi.emiregistry.ObjectFactory();
		QueryResult qr = o.createQueryResult();
		List<ServiceT> l = qr.getService();
		JAXBElement<ServiceT>[] j = toGlue2Service(jo);
		
		for (int i = 0; i < j.length; i++) {
			l.add(j[i].getValue());
		}
		qr.setCount(new BigInteger("" + jo.length()));

		return qr;
	}

	public JAXBElement<ServiceT>[] toGlue2Service(JSONArray jo)
			throws JSONException, DatatypeConfigurationException,
			ParseException {
		@SuppressWarnings("unchecked")
		JAXBElement<ServiceT>[] e = new JAXBElement[jo.length()];
		List<JAXBElement<ServiceT>> lst = new ArrayList<JAXBElement<ServiceT>>();
						
		for (int i = 0; i < jo.length(); i++) {
			JAXBElement<ServiceT> jt = toGlue2XML(jo.getJSONObject(i));
			e[i] = jt;
			lst.add(jt);
		}
		
		
		return e;
	}
	
	private void createAttributelist(){
		List<String> lst = new ArrayList<String>();
		for(ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()){
			lst.add(s.getAttributeName());
		}
		lstNames = lst;
	}

	public JAXBElement<ServiceT> toGlue2XML(JSONObject jo)
			throws JSONException, DatatypeConfigurationException,
			ParseException {
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		ServiceT st =  of.createServiceT();
		EndpointT et =of.createEndpointT();
		ExtensionsT ets = of.createExtensionsT();
		int i = 1;
		if (jo.length() > 0) {
			for (Iterator<?> iterator = jo.keys(); iterator.hasNext();) {
				String type = (String) iterator.next();
				
				

				st.setName(jo.has(ServiceBasicAttributeNames.SERVICE_NAME
						.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_NAME
								.getAttributeName()) : null);
				st.setBaseType("Entity");

				st.setQualityLevel(jo
						.has(ServiceBasicAttributeNames.SERVICE_QUALITYLEVEL
								.getAttributeName()) ? QualityLevelT.fromValue(jo
						.getString(ServiceBasicAttributeNames.SERVICE_QUALITYLEVEL
								.getAttributeName()))
						: null);

				st.setType(jo.has(ServiceBasicAttributeNames.SERVICE_TYPE
						.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_TYPE
								.getAttributeName()) : null);

				st.setValidity(jo
						.has(ServiceBasicAttributeNames.SERVICE_VALIDITY
								.getAttributeName()) ? new BigInteger(jo
						.getString(ServiceBasicAttributeNames.SERVICE_VALIDITY
								.getAttributeName())) : null);

				st.setCreationTime(jo
						.has(ServiceBasicAttributeNames.SERVICE_CREATED_ON
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo.getJSONObject(
								ServiceBasicAttributeNames.SERVICE_CREATED_ON
										.getAttributeName()).getString("$date")))
						: null);

				if (jo.has("_id")) {
					st.setID(jo.getJSONObject("_id").getString("$oid"));
					et.setID(jo.getJSONObject("_id").getString("$oid"));
				}
				
				et.setName(jo.has(ServiceBasicAttributeNames.SERVICE_NAME
						.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_NAME
								.getAttributeName()) : null);
				et.setBaseType("Entity");
				et.setCreationTime(jo
						.has(ServiceBasicAttributeNames.SERVICE_CREATED_ON
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo.getJSONObject(
								ServiceBasicAttributeNames.SERVICE_CREATED_ON
										.getAttributeName()).getString("$date")))
						: null);
				et.setDowntimeAnnounce(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo
								.getJSONObject(
										ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE
												.getAttributeName()).getString(
										"$date")))
						: null);
				et.setDowntimeEnd(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_END
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo
								.getJSONObject(
										ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_END
												.getAttributeName()).getString(
										"$date")))
						: null);
				et.setDowntimeStart(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo
								.getJSONObject(
										ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START
												.getAttributeName()).getString(
										"$date")))
						: null);
				et.setDowntimeInfo(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_INFO
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_INFO
								.getAttributeName())
						: null);

				et.setHealthState(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
								.getAttributeName()) ? EndpointHealthStateT.fromValue(jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
								.getAttributeName()))
						: null);

				et.setHealthStateInfo(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
								.getAttributeName())
						: null);
				et.setImplementationName(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_NAME
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_NAME
								.getAttributeName())
						: null);
				et.setImplementationName(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_VERSION
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_VERSION
								.getAttributeName())
						: null);
				et.setImplementor(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPLEMENTOR
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPLEMENTOR
								.getAttributeName())
						: null);
				et.setInterfaceName(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
								.getAttributeName())
						: null);
				et.setIssuerCA(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ISSUERCA
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ISSUERCA
								.getAttributeName())
						: null);
				et.setQualityLevel(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_QUALITYLEVEL
								.getAttributeName()) ? QualityLevelT.fromValue(jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_QUALITYLEVEL
								.getAttributeName()))
						: null);
				et.setServingState(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SERVING_STATE
								.getAttributeName()) ? ServingStateT.fromValue(jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SERVING_STATE
								.getAttributeName()))
						: null);
				et.setStartTime(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_STARTTIME
								.getAttributeName()) ? toXmlGregorian(formatter
						.parse(jo
								.getJSONObject(
										ServiceBasicAttributeNames.SERVICE_ENDPOINT_STARTTIME
												.getAttributeName()).getString(
										"$date")))
						: null);
				et.setTechnology(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
								.getAttributeName())
						: null);
				et.setURL(jo
						.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName()) ? jo
						.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
								.getAttributeName())
						: null);
				
				//arrays
				if ((et.getCapability().size() <= 0)
						&& (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
								.getAttributeName()))) {
					et.getCapability()
							.addAll(toStringCollection(jo

									.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
											.getAttributeName())));
				}
				if ((et.getTrustedCA().size() <= 0)
						&& (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TRUSTEDCA
								.getAttributeName()))) {
					et.getTrustedCA()
							.addAll(toStringCollection(jo

									.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TRUSTEDCA
											.getAttributeName())));
				}
				if ((et.getInterfaceVersion().size() <= 0)
						&& (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
								.getAttributeName()))) {
					et.getInterfaceVersion()
							.addAll(toStringCollection(jo
									.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
											.getAttributeName())));
				}
				if ((et.getInterfaceExtension().size() <= 0)
						&& (jo.has(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_EXT
								.getAttributeName()))) {
					et.getInterfaceExtension()
							.addAll(toStringCollection(jo
									.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_EXT
											.getAttributeName())));
				}
				
				if (!ServiceUtil.getAttributeNames().contains(type)) {
					ExtensionT extT = of.createExtensionT();
					extT.setLocalID(""+i);
					extT.setKey(type);
					extT.setValue(jo.getString(type));
					ets.getExtension().add(extT);
					
				}
				

			}
			st.getEndpoint().add(et);			
			st.setExtensions(ets);
			
		} else {
			return null;
		}
		
		
				
		return of.createService(st);

	}

	

	private List<String> toStringCollection(JSONArray arr) throws JSONException {
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < arr.length(); i++) {
			lst.add(arr.getString(i));
		}
		return lst;
	}

	private static XMLGregorianCalendar toXmlGregorian(Date d)
			throws DatatypeConfigurationException {
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(d);
		XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(gcal);
		return xgcal;
	}
}