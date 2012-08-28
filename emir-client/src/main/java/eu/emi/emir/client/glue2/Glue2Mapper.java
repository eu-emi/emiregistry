/**
 * 
 */
package eu.emi.emir.client.glue2;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ogf.schemas.glue._2009._03.spec_2.AccessPolicyT;
import org.ogf.schemas.glue._2009._03.spec_2.ContactT;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointHealthStateT;
import org.ogf.schemas.glue._2009._03.spec_2.EndpointT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionT;
import org.ogf.schemas.glue._2009._03.spec_2.ExtensionsT;
import org.ogf.schemas.glue._2009._03.spec_2.LocationT;
import org.ogf.schemas.glue._2009._03.spec_2.ObjectFactory;
import org.ogf.schemas.glue._2009._03.spec_2.QualityLevelT;
import org.ogf.schemas.glue._2009._03.spec_2.ServiceT;
import org.ogf.schemas.glue._2009._03.spec_2.ServingStateT;

import eu.emi.emir.client.ServiceBasicAttributeNames;
import eu.emi.emir.client.util.DateUtil;
import eu.emi.emir.client.util.Log;
import eu.eu_emi.emiregistry.QueryResult;
//import eu.emi.emir.util.ServiceUtil;

/**
 * Converts JSON to xml
 * 
 * @author a.memon
 * 
 */
public class Glue2Mapper {
	private static Logger logger = Log.getLogger(Log.EMIR_CORE,
			Glue2Mapper.class);
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

	public QueryResult toQueryResult(JSONArray jo)
			throws JSONToGlue2MappingException {
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
			throws JSONToGlue2MappingException {
		@SuppressWarnings("unchecked")
		JAXBElement<ServiceT>[] e = new JAXBElement[jo.length()];
		List<JAXBElement<ServiceT>> lst = new ArrayList<JAXBElement<ServiceT>>();
		try {
			for (int i = 0; i < jo.length(); i++) {

				JAXBElement<ServiceT> jt = toGlue2XML(jo.getJSONObject(i));
				e[i] = jt;
				lst.add(jt);

			}
		} catch (Exception e2) {
			logger.warn("Error in transforming JSON to GLUE 2.0 XML", e2);
			throw new JSONToGlue2MappingException(e2);
		}

		return e;
	}

	private void createAttributelist() {
		List<String> lst = new ArrayList<String>();
		for (ServiceBasicAttributeNames s : ServiceBasicAttributeNames.values()) {
			lst.add(s.getAttributeName());
		}
		lstNames = lst;
	}

	public JAXBElement<ServiceT> toGlue2XML(JSONObject jo)
			throws JsonMappingException, JSONException {
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		ServiceT st = of.createServiceT();
		EndpointT et = of.createEndpointT();
		LocationT lt = of.createLocationT();
		ExtensionsT ets = of.createExtensionsT();

		if (jo.length() > 0) {
			for (Iterator<?> iterator = jo.keys(); iterator.hasNext();) {
				String keyName = (String) iterator.next();
				ServiceBasicAttributeNames s = ServiceBasicAttributeNames
						.fromString(keyName);
				if (s != null) {
					// the json key exists in the ServiceBasicAttributeNames
					// class
					st.setBaseType("Entity");
					switch (s) {
					case SERVICE_ENDPOINT_ID:
						try {
							et.setID(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ID
											.getAttributeName()));
						} catch (JSONException e1) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e1);
						}
						break;
					case SERVICE_ENDPOINT_ACCESSPOLICY_RULE:
						try {
							AccessPolicyT accessT = of.createAccessPolicyT();
							accessT.getRule()
									.addAll(toStringCollection(jo
											.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ACCESSPOLICY_RULE
													.getAttributeName())));
							et.getAccessPolicy().add(0, accessT);
						} catch (JSONException e1) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e1);
						}
						break;
					case SERVICE_ENDPOINT_CAPABILITY:
						try {
							et.getCapability()
									.addAll(toStringCollection(jo
											.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_CAPABILITY
													.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;

					case SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE:
						try {
							et.setDowntimeAnnounce(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE
											.getAttributeName()))));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_DOWNTIME_END:
						try {
							et.setDowntimeEnd(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_END
											.getAttributeName()))));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_DOWNTIME_INFO:
						try {
							et.setDowntimeInfo(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_INFO
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_DOWNTIME_START:
						try {
							et.setDowntimeStart(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_ENDPOINT_DOWNTIME_START
											.getAttributeName()))));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_HEALTH_STATE:
						try {
							et.setHealthState(EndpointHealthStateT.fromValue(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATE
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_HEALTH_STATEINFO:
						try {
							et.setHealthStateInfo(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_HEALTH_STATEINFO
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IFACE_EXT:
						try {
							et.getInterfaceExtension()
									.addAll(toStringCollection(jo
											.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_EXT
													.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IFACE_VER:
						try {
							et.getInterfaceVersion()
									.add(0,
											jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACE_VER
													.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IFACENAME:
						try {
							et.setInterfaceName(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IFACENAME
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IMPL_NAME:
						try {
							et.setImplementationName(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_NAME
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IMPL_VERSION:
						try {
							et.setImplementationVersion(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPL_VERSION
											.getAttributeName()));

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_IMPLEMENTOR:
						try {
							et.setImplementor(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_IMPLEMENTOR
											.getAttributeName()));

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_ISSUERCA:
						try {
							et.setIssuerCA(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_ISSUERCA
											.getAttributeName()));

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_QUALITYLEVEL:
						try {
							et.setQualityLevel(QualityLevelT.fromValue(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_QUALITYLEVEL
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_SEMANTICS:
						try {
							try {
								et.getSemantics()
										.addAll(toStringCollection(jo
												.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SEMANTICS
														.getAttributeName())));
							} catch (Exception e) {
								logger.warn(
										"error mapping " + s.getAttributeName()
												+ " to GLUE 2.0 XML", e);
								;
							}
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_SERVING_STATE:
						try {
							et.setServingState(ServingStateT.fromValue(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SERVING_STATE
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_STARTTIME:
						try {
							et.setStartTime(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_ENDPOINT_STARTTIME
											.getAttributeName()))));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_SUPPORTED_PROFILE:
						try {
							et.getSupportedProfile()
									.addAll(toStringCollection(jo
											.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_SUPPORTED_PROFILE
													.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_TECHNOLOGY:
						try {
							et.setTechnology(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TECHNOLOGY
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_TRUSTEDCA:
						try {
							et.getTrustedCA()
									.addAll(toStringCollection(jo

											.getJSONArray(ServiceBasicAttributeNames.SERVICE_ENDPOINT_TRUSTEDCA
													.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_URL:
						try {
							et.setURL(jo
									.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_URL
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ENDPOINT_WSDL:
						try {
							et.getWSDL()
									.add(0,
											jo.getString(ServiceBasicAttributeNames.SERVICE_ENDPOINT_WSDL
													.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_CAPABILITY:
						break;
					// do nothing
					case SERVICE_COMPLEXITY:
						try {
							st.setComplexity(jo
									.getString(ServiceBasicAttributeNames.SERVICE_COMPLEXITY
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_CONTACT:
						try {
							JSONArray ja = jo
									.getJSONArray(ServiceBasicAttributeNames.SERVICE_CONTACT
											.getAttributeName());
							for (int j = 0; j < ja.length(); j++) {
								JSONObject cj = ja.getJSONObject(j);
								ContactT ct = of.createContactT();
								ct.setDetail(cj
										.has(ServiceBasicAttributeNames.SERVICE_CONTACT_DETAIL
												.getAttributeName()) ? cj
										.getString(ServiceBasicAttributeNames.SERVICE_CONTACT_DETAIL
												.getAttributeName())
										: null);
								ct.setType(cj
										.has(ServiceBasicAttributeNames.SERVICE_CONTACT_TYPE
												.getAttributeName()) ? cj
										.getString(ServiceBasicAttributeNames.SERVICE_CONTACT_TYPE
												.getAttributeName())
										: null);
								st.getContact().add(ct);
							}
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_CREATED_ON:
						try {
							st.setCreationTime(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_CREATED_ON
											.getAttributeName()))));
							et.setCreationTime(DateUtil.toXmlGregorian(DateUtil.getDate(jo
									.getJSONObject(ServiceBasicAttributeNames.SERVICE_CREATED_ON
											.getAttributeName()))));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_DN:
						try {

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_EXPIRE_ON:
						try {

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_EXTENSIONS:
						try {

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_ID:
						try {
							// service id
							if (jo.has(ServiceBasicAttributeNames.SERVICE_ID
									.getAttributeName())) {
								st.setID(jo
										.getString(ServiceBasicAttributeNames.SERVICE_ID
												.getAttributeName()));
							} else if (jo
									.has(ServiceBasicAttributeNames.SERVICE_DB_ID
											.getAttributeName())) {
								st.setID(jo
										.getJSONObject(
												ServiceBasicAttributeNames.SERVICE_DB_ID
														.getAttributeName())
										.getString("$oid"));
							} else {
								st.setID("null-id");
							}
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}

						break;
					case SERVICE_LOCATION_ADDRESS:
						try {
							lt.setAddress(jo
									.getString(ServiceBasicAttributeNames.SERVICE_LOCATION_ADDRESS
											.getAttributeName()));

							lt.setID(jo
									.has(ServiceBasicAttributeNames.SERVICE_DB_ID
											.getAttributeName()) ? jo
									.getJSONObject(
											ServiceBasicAttributeNames.SERVICE_DB_ID
													.getAttributeName())
									.getString("$oid")
									: null);

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_LOCATION_COUNTRY:
						try {
							lt.setCountry(jo
									.getString(ServiceBasicAttributeNames.SERVICE_LOCATION_COUNTRY
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_LOCATION_LATITUDE:
						try {
							lt.setLatitude(new Float(
									jo.getDouble(ServiceBasicAttributeNames.SERVICE_LOCATION_LATITUDE
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
							;
						}
						break;
					case SERVICE_LOCATION_LONGITUDE:
						try {
							lt.setLongitude(new Float(
									jo.getDouble(ServiceBasicAttributeNames.SERVICE_LOCATION_LONGITUDE
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_LOCATION_PLACE:
						try {
							lt.setPlace(jo
									.getString(ServiceBasicAttributeNames.SERVICE_LOCATION_PLACE
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_LOCATION_POSTCODE:
						try {
							lt.setPostCode(jo
									.getString(ServiceBasicAttributeNames.SERVICE_LOCATION_POSTCODE
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_NAME:
						try {
							st.setName(jo
									.getString(ServiceBasicAttributeNames.SERVICE_NAME
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_OWNER_DN:
						break;
					case SERVICE_QUALITYLEVEL:
						try {
							st.setQualityLevel(QualityLevelT.fromValue(jo
									.getString(ServiceBasicAttributeNames.SERVICE_QUALITYLEVEL
											.getAttributeName())));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_TYPE:
						try {
							st.setType(jo
									.getString(ServiceBasicAttributeNames.SERVICE_TYPE
											.getAttributeName()));
						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					case SERVICE_UPDATE_SINCE:
						break;
					case SERVICE_VALIDITY:
						try {

						} catch (Exception e) {
							logger.warn("error mapping " + s.getAttributeName()
									+ " to GLUE 2.0 XML", e);
						}
						break;
					default:
						break;
					}
				} else {
					ExtensionT extT = of.createExtensionT();
					extT.setLocalID(UUID.randomUUID().toString());
					extT.setKey(keyName);
					extT.setValue(jo.getString(keyName));
					ets.getExtension().add(extT);
				}
			}
			st.getEndpoint().add(et);
			st.setLocation(lt);
			st.setExtensions(ets);
		} else {
			return null;
		}

		return of.createService(st);

	}

	private List<String> toStringCollection(JSONArray arr) throws JSONException {
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < arr.length(); i++) {
			try {
				lst.add(arr.getString(i));
			} catch (Exception e) {
				logger.warn("The value is not JSON array: " + arr);
				if (logger.isTraceEnabled()) {
					logger.trace("The value is not JSON array: " + arr, e);
				}
				continue;
			}

		}
		return lst;
	}	
}
