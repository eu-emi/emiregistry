/**
 * 
 */
package eu.emi.dsr;


import org.restlet.Context;
import org.restlet.ext.jaxrs.JaxRsApplication;

/**
 * @author a.memon
 *
 */
public class DSRJaxRsApplication extends JaxRsApplication{
	public DSRJaxRsApplication(Context context) {
        super(context);
        this.add(new DSRApplication());
        //this.setGuard(); // if needed
        //this.setRoleChecker(...); // if needed
    }
	
	
	
}
