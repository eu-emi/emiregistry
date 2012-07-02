/**
 * 
 */
package eu.emi.emir.event;

import org.junit.Test;

import eu.emi.emir.event.Event;
import eu.emi.emir.event.EventDispatcher;
import eu.emi.emir.event.EventListener;
import eu.emi.emir.event.EventTypes;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestEventManager {
	

	@Test
	public void test() {
		EventDispatcher.add(new EventListener() {

			@Override
			public void recieve(Event event) {
				if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
					assertEquals("service_being_added", event.getData());
				}

			}
		});
		EventDispatcher.notifyRecievers(new Event(EventTypes.SERVICE_ADD,
				"service_being_added"));
		
		EventDispatcher.removeAll();
		
		
		

	}
}
