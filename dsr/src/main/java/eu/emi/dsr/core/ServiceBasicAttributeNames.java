/**
 * 
 */
package eu.emi.dsr.core;

import java.sql.Date;

import org.codehaus.jettison.json.JSONArray;

/**
 * @author a.memon
 * 
 */
public enum ServiceBasicAttributeNames {
	SERVICE_NAME("Service_Name"), SERVICE_TYPE("Service_Type"), SERVICE_ENDPOINT_URL(
			"Service_Endpoint_URL"), SERVICE_CREATED_ON("Service_CreationTime",
			Date.class), SERVICE_CAPABILITY("Service_Capability"), SERVICE_QUALITYLEVEL(
			"Service_QualityLevel"), SERVICE_COMPLEXITY("Service_Complexity"), SERVICE_VALIDITY(
			"Service_Validity", Long.class), SERVICE_EXPIRE_ON(
			"serviceExpireOn", Date.class), SERVICE_UPDATE_SINCE("updateSince",
			Date.class), SERVICE_OWNER("serviceOwner"), SERVICE_EXTENSIONS(
			"Service_Extensions", JSONArray.class), SERVICE_ENDPOINT_CAPABILITY(
			"Service_Endpoint_Capability"), SERVICE_ENDPOINT_TECHNOLOGY(
			"Service_Endpoint_Technology"), SERVICE_ENDPOINT_IFACENAME(
			"Service_Endpoint_InterfaceName"), SERVICE_ENDPOINT_IFACE_VER(
			"Service_Endpoint_InterfaceVersion"), SERVICE_ENDPOINT_IFACE_EXT(
			"Service_Endpoint_InterfaceExtension"), SERVICE_ENDPOINT_WSDL(
			"Service_Endpoint_WSDL"), SERVICE_ENDPOINT_SUPPORTED_PROFILE(
			"Service_Endpoint_SupportedProfile", JSONArray.class), SERVICE_ENDPOINT_SEMANTICS(
			"Service_Endpoint_Semantics", JSONArray.class), SERVICE_ENDPOINT_HEALTH_STATE(
			"Service_Endpoint_HealthState"), SERVICE_ENDPOINT_HEALTH_STATEINFO(
			"Service_Endpoint_HealthStateInfo"), SERVICE_ENDPOINT_SERVING_STATE(
			"Service_Endpoint_ServingState"), SERVICE_ENDPOINT_STARTTIME(
			"Service_Endpoint_StartTime", Date.class), SERVICE_ENDPOINT_ISSUERCA(
			"Service_Endpoint_IssuerCA"), SERVICE_ENDPOINT_TRUSTEDCA(
			"Service_Endpoint_TrustedCA", JSONArray.class), SERVICE_ENDPOINT_DOWNTIME_ANNOUNCE(
			"Service_Endpoint_DowntimeAnnounce", Date.class), SERVICE_ENDPOINT_DOWNTIME_START(
			"Service_Endpoint_DowntimeStart", Date.class), SERVICE_ENDPOINT_DOWNTIME_END(
			"Service_Endpoint_DowntimeEnd", Date.class);

	private String attributeName;

	private Class<?> attributeType;

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
}