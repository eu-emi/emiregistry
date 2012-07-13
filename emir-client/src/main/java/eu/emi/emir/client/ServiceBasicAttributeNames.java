/**
 * 
 */
package eu.emi.emir.client;

import java.util.Date;
import org.codehaus.jettison.json.JSONArray;

/**
 * @author a.memon
 * 
 */
public enum ServiceBasicAttributeNames {
	        SERVICE_ID("Service_ID",null,"GLUE 2.0 Service ID",true),
	        SERVICE_NAME("Service_Name", null, "Service Name",true), 
	        SERVICE_TYPE("Service_Type", null, "Service Type",true), 
	        SERVICE_ENDPOINT_URL("Service_Endpoint_URL", null, "Service Unique URL (Mandatory Attribute)",true), 
	        SERVICE_CREATED_ON("Service_CreationTime", Date.class), 
	        SERVICE_CAPABILITY("Service_Capability"), 
	        SERVICE_QUALITYLEVEL("Service_QualityLevel"), 
	        SERVICE_ENDPOINT_QUALITYLEVEL("Service_Endpoint_QualityLevel"), 
	        SERVICE_COMPLEXITY("Service_Complexity"), 
	        SERVICE_VALIDITY("Service_Validity",Long.class), 
	        SERVICE_EXPIRE_ON("Service_ExpireOn", Date.class), 
	        SERVICE_UPDATE_SINCE("updateSince", Date.class), 
	        SERVICE_OWNER_DN("serviceOwner"), 
	        SERVICE_DN("Service_DN"),
	        SERVICE_EXTENSIONS("Service_Extensions", JSONArray.class),
	        SERVICE_ENDPOINT_ID("Service_Endpoint_ID",null,"GLUE 2.0 Service Endpoint ID",true),
			SERVICE_ENDPOINT_CAPABILITY("Service_Endpoint_Capability",JSONArray.class,"Service Endpoint Capabilities",true), 
			SERVICE_ENDPOINT_TECHNOLOGY("Service_Endpoint_Technology",null,"Technology used by the Endpoint",true), 
			SERVICE_ENDPOINT_IFACENAME("Service_Endpoint_InterfaceName",null,"Service Endpoint Interface Name",true), 
			SERVICE_ENDPOINT_IFACE_VER("Service_Endpoint_InterfaceVersion",null,"Service Endpoint Interface Version",true),
			SERVICE_ENDPOINT_IFACE_EXT("Service_Endpoint_InterfaceExtension"), 
			SERVICE_ENDPOINT_WSDL("Service_Endpoint_WSDL"), 
			SERVICE_ENDPOINT_SUPPORTED_PROFILE("Service_Endpoint_SupportedProfile", JSONArray.class), 
			SERVICE_ENDPOINT_SEMANTICS("Service_Endpoint_Semantics", JSONArray.class), 
			SERVICE_ENDPOINT_HEALTH_STATE("Service_Endpoint_HealthState"), 
			SERVICE_ENDPOINT_HEALTH_STATEINFO("Service_Endpoint_HealthStateInfo"), 
			SERVICE_ENDPOINT_SERVING_STATE("Service_Endpoint_ServingState"), 
			SERVICE_ENDPOINT_STARTTIME("Service_Endpoint_StartTime", Date.class), 
			SERVICE_ENDPOINT_ISSUERCA("Service_Endpoint_IssuerCA"), 
			SERVICE_ENDPOINT_TRUSTEDCA("Service_Endpoint_TrustedCA", JSONArray.class), 
			SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE("Service_Endpoint_DowntimeAnnounce", Date.class), 
			SERVICE_ENDPOINT_DOWNTIME_START("Service_Endpoint_DowntimeStart", Date.class), 
			SERVICE_ENDPOINT_DOWNTIME_END("Service_Endpoint_DowntimeEnd", Date.class), 
			SERVICE_ENDPOINT_DOWNTIME_INFO("Service_DowntimeInfo"), 
			SERVICE_ENDPOINT_IMPL_NAME("Service_Endpoint_ImplementationName"), 
			SERVICE_ENDPOINT_IMPL_VERSION("Service_Endpoint_ImplementationVersion"), 
			SERVICE_ENDPOINT_IMPLEMENTOR("Service_Endpoint_Implementor"), 
			SERVICE_DB_ID("_id",null,"MongoDB specific identifier"),
			SERVICE_LOCATION_ADDRESS("Service_Location_Address", null, "Street Address"), 
			SERVICE_LOCATION_PLACE("Service_Location_Place",null,"City Name"),
			SERVICE_LOCATION_COUNTRY("Service_Location_Country",null,"Country Name"),
			SERVICE_LOCATION_POSTCODE("Service_Location_PostCode",null,"Postal Code"),
			SERVICE_LOCATION_LATITUDE("Service_Location_Latitude",null,"Geo Location Latitude"),
			SERVICE_LOCATION_LONGITUDE("Service_Location_Longitude",null,"Geo Location Longitude"),
			SERVICE_CONTACT("Service_Contact",JSONArray.class,"GLUE 2.0 Service Contact information as Array"),
			SERVICE_CONTACT_DETAIL("Detail",null,"GLUE 2.0 Service Contact Detail"),
			SERVICE_CONTACT_TYPE("ContactType",null,"GLUE 2.0 Service Contact Type"),
			SERVICE_ENDPOINT_ACCESSPOLICY_RULE("Service_Endpoint_AccessPolicy_Rule",JSONArray.class,"VO Name");

	private String attributeName;
	private Class<?> attributeType;
	private String attributeDesc;
	private boolean mandatory = false;

	/**
	 * 
	 */
	private ServiceBasicAttributeNames(String attr) {
		this.attributeName = attr;
	}

	private ServiceBasicAttributeNames(String attr, Class<?> type) {
		this.attributeName = attr;
		this.attributeType = type;
	}

	private ServiceBasicAttributeNames(String attr, Class<?> type,
			String description) {
		this.attributeName = attr;
		this.attributeType = type;
		this.attributeDesc = description;
	}
	
	private ServiceBasicAttributeNames(String attr, Class<?> type,
			String description, boolean mandatory) {
		this.attributeName = attr;
		this.attributeType = type;
		this.attributeDesc = description;
		this.mandatory = mandatory;
	}

	/**
	 * 
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * 
	 * @return the attributeName
	 */
	public Class<?> getAttributeType() {
		if (attributeType == null) {
			return String.class;
		}
		return attributeType;
	}

	/**
	 * @return
	 */
	public String getAttributeDesc() {
		if (attributeDesc == null) {
			return "Description Undefined";
		}
		return attributeDesc;
	}
	
	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	
	/* (non-Javadoc)
			 * @see java.lang.Enum#toString()
			 */
			@Override
	public String toString() {
		return getAttributeName();
	}

}
