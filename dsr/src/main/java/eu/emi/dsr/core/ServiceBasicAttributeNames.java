/**
 * 
 */
package eu.emi.dsr.core;

/**
 * @author a.memon
 * 
 */
public enum ServiceBasicAttributeNames {
	SERVICE_TYPE("serviceType"), SERVICE_URL("serviceUrl"), SERVICE_EXPIRE_ON(
			"serviceExpireOn"), SERVICE_CREATED_ON("createdOn"), SERVICE_UPDATE_SINCE("updateSince"), SERVICE_OWNER("serviceOwner");
	private final String attributeName;

	/**
	 * 
	 */
	private ServiceBasicAttributeNames(String attr) {
		this.attributeName = attr;
	}

	/**
	 * 
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
}
