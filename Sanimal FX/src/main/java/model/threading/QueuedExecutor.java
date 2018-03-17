package model.threading;

import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueuedExecutor extends BaseSanimalExecutor
{
	// The current message to be displayed
	private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper();
	// The progress of the current task
	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
	// If a task is currently running
	private final ReadOnlyBooleanWrapper taskRunning = new ReadOnlyBooleanWrapper();

	public QueuedExecutor()
	{
		super(Executors.newSingleThreadExecutor());
	}

	@Override
	protected void onSucceeded(Worker<?> worker)
	{
		this.message.unbind();
		this.progress.unbind();
		this.taskRunning.setValue(false);
	}

	@Override
	protected void onRunning(Worker<?> worker)
	{
		this.message.bind(worker.messageProperty());
		this.progress.bind(worker.progressProperty());
		this.taskRunning.setValue(true);
	}

	///
	/// Getters, but use read only
	///

	public String getMessage()
	{
		return this.message.getValue();
	}

	public ReadOnlyStringProperty messageProperty()
	{
		return message.getReadOnlyProperty();
	}

	public Double getProgress()
	{
		return this.progress.getValue();
	}

	public ReadOnlyDoubleProperty progressProperty()
	{
		return progress.getReadOnlyProperty();
	}

	public Boolean getTaskRunning()
	{
		return this.taskRunning.getValue();
	}

	public ReadOnlyBooleanProperty taskRunningProperty()
	{
		return this.taskRunning.getReadOnlyProperty();
	}
}
