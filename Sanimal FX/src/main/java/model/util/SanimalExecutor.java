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

public class SanimalExecutor
{
	// Use a thread pool executor to perform tasks that take a while
	private ThreadPoolExecutor taskPerformer = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	private final ReadOnlyStringWrapper messageProperty = new ReadOnlyStringWrapper();
	private final ReadOnlyDoubleWrapper progressProperty = new ReadOnlyDoubleWrapper();
	private final ReadOnlyBooleanWrapper taskRunningProperty = new ReadOnlyBooleanWrapper();
	private final List<Service> services = new ArrayList<>();

	public SanimalExecutor()
	{

	}

	public <V> void registerService(Service<V> service)
	{
		services.add(service);
		service.setExecutor(taskPerformer);
		EventHandler<WorkerStateEvent> onSucceeded = service.getOnSucceeded();
		service.setOnSucceeded(serviceEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(serviceEvent);
			this.unbindCurrentTask();
		});
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
		EventHandler<WorkerStateEvent> onSucceeded = task.getOnSucceeded();
		task.setOnSucceeded(taskEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(taskEvent);
			this.unbindCurrentTask();
		});
		EventHandler<WorkerStateEvent> onRunning = task.getOnRunning();
		task.setOnRunning(taskEvent ->
		{
			if (onRunning != null)
				onRunning.handle(taskEvent);
			this.messageProperty.bind(task.messageProperty());
			this.progressProperty.bind(task.progressProperty());
			this.taskRunningProperty.setValue(true);
		});
		return this.taskPerformer.submit(task);
	}

	private void unbindCurrentTask()
	{
		this.messageProperty.unbind();
		this.progressProperty.unbind();
		this.taskRunningProperty.setValue(false);
	}

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
