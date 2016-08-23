package model;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used in threading off runnable tasks
 * 
 * @author David Slovikosky
 */
public class SanimalWorker
{
	// Using singleton design pattern
	private static SanimalWorker instance = new SanimalWorker();
	// Use a thread pool executor to perform the tasks
	private final ThreadPoolExecutor iconLoader = new ScheduledThreadPoolExecutor(5);

	/**
	 * Constrcutor is private due to singleton design pattern
	 */
	private SanimalWorker()
	{
	}

	/**
	 * @return Returns the one sanimal worker instance
	 */
	public static SanimalWorker getInstance()
	{
		return instance;
	}

	/**
	 * Schedules a task to run on one of the threads
	 * 
	 * @param runnable
	 *            The task to execute
	 */
	public void scheduleTask(Runnable runnable)
	{
		this.iconLoader.submit(runnable);
	}
}
