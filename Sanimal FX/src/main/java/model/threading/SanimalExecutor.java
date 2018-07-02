package model.threading;

/**
 * Class used to keep track of threads to run in the background
 */
public class SanimalExecutor
{
	// Queued executor is used to perform tasks one by one
	private QueuedExecutor queuedExecutor = new QueuedExecutor();
	// Immediate executor is used to do tasks at once
	private ImmediateExecutor immediateExecutor = new ImmediateExecutor();

	/**
	 * Returns the queued executor, use this if you want to perform tasks one by one
	 *
	 * @return The queued executor
	 */
	public QueuedExecutor getQueuedExecutor()
	{
		return this.queuedExecutor;
	}

	/**
	 * Returns the immediate executor, use this if you want to perform tasks at the same time immediately
	 *
	 * @return The immediate executor
	 */
	public ImmediateExecutor getImmediateExecutor()
	{
		return this.immediateExecutor;
	}

	/**
	 * Returns true if any of the executors are performing tasks, false otherwise
	 *
	 * @return True if any task is running, false otherwise
	 */
	public Boolean anyTaskRunning()
	{
		return this.queuedExecutor.getTaskRunning() || this.immediateExecutor.getActiveTasks().size() > 0;
	}
}
