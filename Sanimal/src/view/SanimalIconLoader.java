package view;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used in threading off runnable tasks
 * 
 * @author David Slovikosky
 */
public class SanimalIconLoader
{
	// Using singleton design pattern
	private static SanimalIconLoader instance = new SanimalIconLoader();
	// Use a thread pool executor to perform the tasks
	private final ThreadPoolExecutor iconLoader = new ScheduledThreadPoolExecutor(5);

	/**
	 * Constructor is private due to singleton design pattern
	 */
	private SanimalIconLoader()
	{
	}

	/**
	 * @return Returns the one icon loader instance
	 */
	public static SanimalIconLoader getInstance()
	{
		return instance;
	}

	/**
	 * Schedules a task to run on one of the threads
	 * 
	 * @param runnable
	 *            The task to execute
	 * 
	 * @return The task submitted
	 */
	public Future<?> scheduleTask(Runnable runnable)
	{
		return this.iconLoader.submit(runnable);
	}
}
