/**
 * 
 */
package eu.emi.emir.core;

import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * It lowers the overhead of extraordinary thread creation within a container
 * pre-configured size of thread pool. Most of the code is taken from
 * {@link http
 * ://unicore.svn.sourceforge.net/viewvc/unicore/wsrflite/tags/use-2.0
 * .1/core/src
 * /main/java/de/fzj/unicore/wsrflite/ResourcePool.java?revision=10715
 * &view=markup}
 * 
 * @author a.memon
 * 
 */
public class RegistryThreadPool {

	/**
	 * property key for setting the core thread pool size for the scheduled
	 * execution service
	 */
	public static final String CORE_POOL_SIZE = "registry.scheduled.size";

	/**
	 * property key for setting the timeout in millis for removing idle threads
	 */
	public static final String POOL_TIMEOUT = "registry.scheduled.idletime";

	/**
	 * property key for setting the minimum thread pool size for the scheduled
	 * execution service
	 */
	public static final String EXEC_CORE_POOL_SIZE = "registry.executor.minsize";

	/**
	 * property key for setting the maximum thread pool size for the scheduled
	 * execution service
	 */
	public static final String EXEC_MAX_POOL_SIZE = "registry.executor.maxsize";

	/**
	 * property key for setting the timeout in millis for removing idle threads
	 */
	public static final String EXEC_POOL_TIMEOUT = "registry.executor.idletime";

	private RegistryThreadPool() {
	}

	private static boolean isConfigured = false;

	private static ScheduledThreadPoolExecutor scheduler;

	private static ThreadPoolExecutor executor;

	/**
	 * get a {@link ScheduledExecutorService} for executing tasks at a given
	 * schedule
	 * 
	 * @return
	 */
	public static synchronized ScheduledExecutorService getScheduledExecutorService() {
		if (!isConfigured)
			configure();
		return scheduler;
	}

	/**
	 * get a {@link ExecutorService} for executing tasks
	 * 
	 * @return ExecutorService
	 */
	public static synchronized ExecutorService getExecutorService() {
		if (!isConfigured)
			configure();
		return executor;
	}

	/**
	 * get an {@link CompletionService} using the Exector service
	 * 
	 * @param <V>
	 * @return
	 */
	public static synchronized <V> CompletionService<V> getCompletionService() {
		return new ExecutorCompletionService<V>(getExecutorService());
	}

	/**
	 * Configure the pool. Properties are read from the {@link Kernel}
	 * properties.
	 */
	protected static void configure() {
		configureScheduler();
		configureExecutor();
		isConfigured = true;
	}

	protected static void configureScheduler() {
		int core = 4;
		scheduler = new ScheduledThreadPoolExecutor(core);
		int idle = 50;
		scheduler.setKeepAliveTime(idle, TimeUnit.MILLISECONDS);
		scheduler.setThreadFactory(new ThreadFactory() {
			final AtomicInteger threadNumber = new AtomicInteger(1);

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("emir-sched-" + threadNumber.getAndIncrement());
				return t;
			}
		});
	}

	protected static void configureExecutor() {
		int min = 10;
		int max = 32;
		int idle = 50;

		executor = new ThreadPoolExecutor(min, max, idle,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					final AtomicInteger threadNumber = new AtomicInteger(1);

					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("registry-executor-"
								+ threadNumber.getAndIncrement());
						return t;
					}
				});

	}

	// getters for interesting data

	/**
	 * get the current minimum pool size of the scheduler pool
	 */
	public static int getScheduledExecutorCorePoolSize() {
		return scheduler.getCorePoolSize();
	}

	/**
	 * get the current maximum pool size of the scheduler pool
	 */
	public static int getScheduledExecutorMaxPoolSize() {
		return scheduler.getMaximumPoolSize();
	}

	/**
	 * get the number of currently active threads in the scheduler pool
	 */
	public static int getScheduledExecutorActiveThreadCount() {
		return scheduler.getActiveCount();
	}

	public static int getExecutorCorePoolSize() {
		return executor.getCorePoolSize();
	}

	public static int getExecutorMaxPoolSize() {
		return executor.getMaximumPoolSize();
	}

	public static int getExecutorActiveThreadCount() {
		return executor.getActiveCount();
	}

}
