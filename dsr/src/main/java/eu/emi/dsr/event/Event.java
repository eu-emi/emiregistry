/**
 * 
 */
package eu.emi.dsr.event;

/**
 * @author a.memon
 *
 */
public class Event {
	private String type;
	private Object data;
	/**
	 * 
	 */
	public Event(String type, Object data) {
		this.type = type;
		this.data = data;
	}
	public String getType(){
		return type;
	}
	public Object getData(){
		return data;	
	}
}
