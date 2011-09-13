/**
 * 
 */
package eu.emi.dsr.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import eu.emi.dsr.core.RegistryThreadPool;

/**
 * @author a.memon
 *
 */
public class EventDispatcher {
	private static List<EventListener> lstEvent = new CopyOnWriteArrayList<EventListener>();
	public static void add(EventListener e){
		lstEvent.add(e);
	}
	public static void remove(EventListener e){
		lstEvent.remove(e);
	}
	
	public static void notifyRecievers(final Event e){
		RegistryThreadPool.getExecutorService().execute(new Runnable() {			
			@Override
			public void run() {
				for (Iterator<EventListener> iterator = lstEvent.iterator(); iterator.hasNext();) {
					EventListener eventReciever = (EventListener) iterator
							.next();
					eventReciever.recieve(e);
					
				}			
			}
		});
		
	}
	
	public static void removeAll(){
		lstEvent.clear();
	}
	
	public List<EventListener> getRecieverList(){
		return Collections.unmodifiableList(lstEvent);
	}
}
