/**
 * 
 */
package eu.emi.dsr.validator;

/**
 * @author a.memon
 *
 */
public class ValidatorFactory {
	private static InfoValidator v;
	public static InfoValidator getRegistrationValidator(){
		if (v == null) {
			v = new RegistrationValidator();
		}
		return v;
	}
	
}
