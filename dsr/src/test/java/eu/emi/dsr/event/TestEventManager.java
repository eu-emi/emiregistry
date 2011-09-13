/**
 * 
 */
package eu.emi.dsr.event;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import eu.emi.dsr.DSRServer;
import eu.emi.dsr.core.Configuration;
import static org.junit.Assert.*;

/**
 * @author a.memon
 * 
 */
public class TestEventManager {
	@Before
	public void setup() {
		Properties p = new Properties();
		new DSRServer(new Configuration(p));
	}

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
