package model.threading;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Class used as a base for all thread executors
 */
public abstract class BaseSanimalExecutor
{
	// The task performer that does the thread execution
	private ExecutorService taskPerformer;

	/**
	 * Constructor initializes fields
	 *
	 * @param taskPerformer The task performer to do the threading
	 */
	public BaseSanimalExecutor(ExecutorService taskPerformer)
	{
		this.taskPerformer = taskPerformer;
	}

	/**
	 * This is used to register a service into the executor. This should be called on all services before using the executor
	 *
	 * @param service The service to register
	 * @param <V> Ignored, can be anything
	 */
	public <V> void registerService(Service<V> service)
	{
		// Ensure that the executor is properly set
		service.setExecutor(taskPerformer);
		// When the task succeeds, unbind the currently running task
		EventHandler<WorkerStateEvent> onSucceeded = service.getOnSucceeded();
		service.setOnSucceeded(taskEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(taskEvent);
			onSucceeded(service);
		});
		// When the service begins running, bind the message/progress and task running properties
		EventHandler<WorkerStateEvent> onRunning = service.getOnRunning();
		service.setOnRunning(taskEvent ->
		{
			if (onRunning != null)
				onRunning.handle(taskEvent);
			onRunning(service);
		});
	}

	/**
	 * Add a task to the queue to be done in the background. All tasks submitted this way are done in order
	 *
	 * @param task The task to be performed
	 * @return A future task that will be completed some time
	 */
	public <T> Future<?> addTask(Task<T> task)
	{
		// When the task succeeds, unbind the currently running task
		EventHandler<WorkerStateEvent> onSucceeded = task.getOnSucceeded();
		task.setOnSucceeded(taskEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(taskEvent);
			onSucceeded(task);
		});
		// When the service begins running, bind the message/progress and task running properties
		EventHandler<WorkerStateEvent> onRunning = task.getOnRunning();
		task.setOnRunning(taskEvent->
		{
			if (onRunning != null)
				onRunning.handle(taskEvent);
			onRunning(task);
		});
		// Add the task to be performed
		return this.taskPerformer.submit(task);
	}

	/**
	 * Called when a task finishes
	 *
	 * @param worker The worker that finished
	 */
	protected abstract void onSucceeded(Worker<?> worker);

	/**
	 * Called when a task begins
	 *
	 * @param worker The worker that began
	 */
	protected abstract void onRunning(Worker<?> worker);
}
