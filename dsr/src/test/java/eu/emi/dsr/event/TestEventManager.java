/**
 * 
 */
package eu.emi.dsr.event;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import eu.emi.dsr.core.RegistryThreadPool;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestEventManager {
	@Before
	public void setup() {
		Properties p = new Properties();
		DSRServer d = new DSRServer(new Configuration(p));
	}

	@Test
	public void test() {
		EventManager.add(new EventReciever() {

			@Override
			public void recieve(Event event) {
				if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
					assertEquals("service_being_added", event.getData());
				}

			}
		});
		EventManager.notifyRecievers(new Event(EventTypes.SERVICE_ADD,
				"service_being_added"));
		
		EventManager.removeAll();
		
		// for asynchronous eventing
		EventManager.add(new EventReciever() {

			@Override
			public void recieve(Event event) {
				if (event.getType().equalsIgnoreCase(EventTypes.SERVICE_ADD)) {
					assertEquals("service_being_added_async", event.getData());
				}

			}
		});

		for (int i = 0; i < 100; i++) {
			EventManager.notifyRecievers(new Event(EventTypes.SERVICE_ADD,
			"service_being_added_async"));	
		}
		

	}
}
