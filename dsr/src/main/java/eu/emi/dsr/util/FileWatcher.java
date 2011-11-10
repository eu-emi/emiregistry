package eu.emi.dsr.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * helper to watch a set of files and invoke a specific action if one of them was modified.
 * This should be executed periodically, for example using the scheduled executor
 * service from the {@link ResourcePool}
 * 
 * @author schuller
 * @author a.memon
 */
public class FileWatcher implements Runnable{

	private final Set<File> targets=new HashSet<File>();
	
	private final Runnable action;
	
	private long lastAccessed=System.currentTimeMillis();
		
	public FileWatcher(File target, Runnable action)throws FileNotFoundException{
		if(!target.exists() || !target.canRead()){
			throw new FileNotFoundException("File "+target.getAbsolutePath()+
					" does not exist or is not readable.");
		}
		this.action=action;
		addTarget(target);
	}
	
	public void schedule(int delay, TimeUnit timeunit){
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, delay, delay, timeunit);
	}
	
	/**
	 * add a file to be watched<br/>
	 * 
	 * Note: files are stored in a Set, so it is no problem if the same file is added
	 * repeatedly
	 * 
	 * @param target - the file to watch
	 */
	public void addTarget(File target){
		targets.add(target);
	}

	public boolean removeTarget(File target){
		return targets.remove(target);
	}

	/**
	 * check if one of the target files has been touched and invoke 
	 * the action if it has
	 */
	public void run(){
		for(File target: targets){
			if(target.exists() && target.lastModified()>=lastAccessed){
				lastAccessed=System.currentTimeMillis();
				action.run();
				break;
			}
		}
		
	}

}
