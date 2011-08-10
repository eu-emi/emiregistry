package eu.emi.dsr.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import eu.emi.dsr.core.RegistryThreadPool;


/**
 * runs a task with an external timeout
 *  
 * @param <V> - the type of result
 * 
 * @author schuller
 */
public class TimeoutRunner<V> implements Callable<V> {

	private static final Logger logger=Log.getLogger(Log.DSR,TimeoutRunner.class);

	private final Callable<V> task;
	
	private V result;
	
	private final int timeout;
	
	private final TimeUnit unit;
	
	/**
	 * @param timeout - milliseconds before timeout
	 * @param task - the task to execute
	 */
	public TimeoutRunner(Callable<V> task, int timeout, TimeUnit unit){
		this.task=task;
		this.timeout=timeout;
		this.unit=unit;
	}
	
	public V call() throws RejectedExecutionException, InterruptedException{
		logger.debug("Starting task with timeout of "+timeout+ " "+unit);
		try{
			Future<V> res=RegistryThreadPool.getExecutorService().submit(task);
			result=res.get(timeout, unit);
		}
		catch(TimeoutException ignored){
			logger.debug("Timeout reached!");
		}
		catch(Exception ex){
			Log.logException("Error waiting for task to complete", ex, logger);
		}
		
		return result;
	}
	
	/**
	 * helper for computing a result using a TimeoutRunner
	 * 
	 * @param <Result>
	 * @param task
	 * @param timeout - time out in milliseconds
	 * @return a Result or <code>null</code> if the timeout is reached, or an exception occurs
	 */
	public static <Result> Result compute(Callable<Result> task, int timeout){
		TimeoutRunner<Result> runner=new TimeoutRunner<Result>(task, timeout, TimeUnit.MILLISECONDS);
		try{
			return runner.call();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * helper for computing a result using a TimeoutRunner
	 * 
	 * @param <Result>
	 * @param task
	 * @param timeout
	 * @param units the {@link TimeUnit} to use
	 * @return a Result or <code>null</code> if the timeout is reached
	 */
	public static <Result> Result compute(Callable<Result> task, int timeout, TimeUnit units){
		TimeoutRunner<Result> runner=new TimeoutRunner<Result>(task, timeout, units);
		try{
			return runner.call();
		}catch(Exception e){
			return null;
		}
	}
	
}
