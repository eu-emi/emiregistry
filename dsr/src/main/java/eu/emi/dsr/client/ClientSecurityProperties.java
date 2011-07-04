/**
 * 
 */
package eu.emi.dsr.client;


import eu.emi.dsr.core.SecurityProperties;

/**
 * @author a.memon
 *
 */
public class ClientSecurityProperties extends SecurityProperties{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5598417926299125938L;

	public String getType(){
		return "client";
	}
}
