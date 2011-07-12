/**
 * 
 */
package eu.emi.dsr.event;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 * @author a.memon
 *
 */
public class TestEventManager {
	@Test
	public void test(){
		EventManager.add(new EventReciever() {
			
			@Override
			public void recieve(Event event) {
				if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
					assertEquals("service_being_added", event.getData());
				}
				
			}
		});
		
		EventManager.notifyRecievers(new Event(EventTypes.SERVICE_ADD, "service_being_added"));
	}
}
