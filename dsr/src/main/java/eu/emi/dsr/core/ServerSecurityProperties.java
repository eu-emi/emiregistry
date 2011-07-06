/**
 * 
 */
package eu.emi.dsr.core;

/**
 * helper class to fetch all the server security related properties
 * 
 * @author a.memon
 *
 */
public class ServerSecurityProperties extends SecurityProperties implements Cloneable{
	private static final long serialVersionUID = -3688956711635418420L;
	
	private Boolean clientAuthn;
	private String attributeSource;
	private String attributeLocation;
	
	public Boolean getClientAuthn() {
		return clientAuthn;
	}



	public void setClientAuthn(Boolean clientAuthn) {
		this.clientAuthn = clientAuthn;
	}



	public String getAttributeSource() {
		return attributeSource;
	}



	public void setAttributeSource(String attributeSource) {
		this.attributeSource = attributeSource;
	}



	public String getAttributeLocation() {
		return attributeLocation;
	}



	public void setAttributeLocation(String attributeLocation) {
		this.attributeLocation = attributeLocation;
	}



	public Boolean getAccessControl() {
		return accessControl;
	}



	public void setAccessControl(Boolean accessControl) {
		this.accessControl = accessControl;
	}



	private Boolean accessControl;
	
	
	
	/* (non-Javadoc)
	 * @see eu.emi.dsr.core.SecurityProperties#getType()
	 */
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "server";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ServerSecurityProperties clone() throws CloneNotSupportedException {
		return (ServerSecurityProperties) super.clone();
	}
		
	
}
