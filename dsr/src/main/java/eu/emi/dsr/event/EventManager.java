/**
 * 
 */
package eu.emi.dsr.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import eu.emi.dsr.core.RegistryThreadPool;

/**
 * @author a.memon
 *
 */
public class EventManager {
	private static List<EventReciever> lstEvent = new CopyOnWriteArrayList<EventReciever>();
	public static void add(EventReciever e){
		lstEvent.add(e);
	}
	public static void remove(EventReciever e){
		lstEvent.remove(e);
	}
	
	public static void notifyRecievers(final Event e){
		RegistryThreadPool.getExecutorService().execute(new Runnable() {			
			@Override
			public void run() {
				for (Iterator iterator = lstEvent.iterator(); iterator.hasNext();) {
					EventReciever eventReciever = (EventReciever) iterator
							.next();
					eventReciever.recieve(e);
					
				}			
			}
		});
		
	}
	
	public static void removeAll(){
		lstEvent.clear();
	}
	
	public List<EventReciever> getRecieverList(){
		return Collections.unmodifiableList(lstEvent);
	}
}
