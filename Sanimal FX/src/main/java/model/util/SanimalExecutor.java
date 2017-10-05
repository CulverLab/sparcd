package model.util;

import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class used to keep track of threads to run in the background
 */
public class SanimalExecutor
{
	// Use a thread pool executor to perform tasks that take a while
	private ThreadPoolExecutor taskPerformer = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	// The current message to be displayed
	private final ReadOnlyStringWrapper messageProperty = new ReadOnlyStringWrapper();
	// The progress of the current task
	private final ReadOnlyDoubleWrapper progressProperty = new ReadOnlyDoubleWrapper();
	// The number of tasks currently running
	private final ReadOnlyBooleanWrapper taskRunningProperty = new ReadOnlyBooleanWrapper();
	private final List<Service> services = new ArrayList<>();

	public SanimalExecutor()
	{

	}

	/**
	 * This is used to register a service into the executor. This should be called on all services before using the executor
	 *
	 * @param service The service to register
	 * @param <V> Ignored, can be anything
	 */
	public <V> void registerService(Service<V> service)
	{
		// Add the service to a list
		services.add(service);
		// Ensure that the executor is properly set
		service.setExecutor(taskPerformer);
		// When the task succeeds, unbind the currently running task
		EventHandler<WorkerStateEvent> onSucceeded = service.getOnSucceeded();
		service.setOnSucceeded(serviceEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(serviceEvent);
			this.unbindCurrentTask();
		});
		// When the service begins running, bind the message/progress and task running properties
		EventHandler<WorkerStateEvent> onRunning = service.getOnRunning();
		service.setOnRunning(serviceEvent ->
		{
			if (onRunning != null)
				onRunning.handle(serviceEvent);
			this.messageProperty.bind(service.messageProperty());
			this.progressProperty.bind(service.progressProperty());
			this.taskRunningProperty.setValue(true);
		});
	}

	/**
	 * Add a task to the queue to be done in the background
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
			this.unbindCurrentTask();
		});
		// When the service begins running, bind the message/progress and task running properties
		EventHandler<WorkerStateEvent> onRunning = task.getOnRunning();
		task.setOnRunning(taskEvent ->
		{
			if (onRunning != null)
				onRunning.handle(taskEvent);
			this.messageProperty.bind(task.messageProperty());
			this.progressProperty.bind(task.progressProperty());
			this.taskRunningProperty.setValue(true);
		});
		// Add the task to be performed
		return this.taskPerformer.submit(task);
	}

	/**
	 * Unbinds the currently running or finished task
	 */
	private void unbindCurrentTask()
	{
		this.messageProperty.unbind();
		this.progressProperty.unbind();
		this.taskRunningProperty.setValue(false);
	}

	///
	/// Getters/Setters, but use read only
	///

	public String getMessage()
	{
		return this.messageProperty.getValue();
	}

	public ReadOnlyStringProperty messageProperty()
	{
		return messageProperty.getReadOnlyProperty();
	}

	public Double getProgress()
	{
		return this.progressProperty.getValue();
	}

	public ReadOnlyDoubleProperty progressProperty()
	{
		return progressProperty.getReadOnlyProperty();
	}

	public Boolean getTaskRunning()
	{
		return this.taskRunningProperty.getValue();
	}

	public ReadOnlyBooleanProperty taskRunningProperty()
	{
		return this.taskRunningProperty.getReadOnlyProperty();
	}
}
