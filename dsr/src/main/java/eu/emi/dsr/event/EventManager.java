/**
 * 
 */
package eu.emi.dsr.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	
	public static void notifyRecievers(Event e){
		for (Iterator<EventReciever> iterator = lstEvent.iterator(); iterator.hasNext();) {
			EventReciever rcv = (EventReciever) iterator.next();
			rcv.recieve(e);
		}
	}
	
	public List<EventReciever> getRecieverList(){
		return Collections.unmodifiableList(lstEvent);
	}
}
