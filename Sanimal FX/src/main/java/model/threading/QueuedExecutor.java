package model.threading;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.util.concurrent.Executors;

/**
 * Executor that queues all tasks and performs them one by one
 */
public class QueuedExecutor extends BaseSanimalExecutor
{
	// The current message to be displayed
	private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper();
	// The progress of the current task
	private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
	// If a task is currently running
	private final ReadOnlyBooleanWrapper taskRunning = new ReadOnlyBooleanWrapper(false);
	// The task that is currently running
	private final ObservableList<Task<?>> tasks = FXCollections.observableArrayList();

	/**
	 * Constructor specifies a single thread which can do one task at a time
	 */
	public QueuedExecutor()
	{
		super(Executors.newSingleThreadExecutor());
	}

	/**
	 * When a task finishes, remove it from the list of running tasks and update the various string properties
	 *
	 * @param worker The worker that finished
	 */
	@Override
	protected void onSucceeded(Worker<?> worker)
	{
		this.message.unbind();
		this.progress.unbind();
		if (worker instanceof Task<?>)
			this.tasks.remove(worker);
		this.taskRunning.setValue(false);
	}

	/**
	 * When a task begins, add it from the list of running tasks and update the various string properties
	 *
	 * @param worker The worker that started
	 */
	@Override
	protected void onRunning(Worker<?> worker)
	{
		this.message.bind(worker.messageProperty());
		this.progress.bind(worker.progressProperty());
		if (worker instanceof Task<?>)
			this.tasks.add((Task<?>) worker);
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

	public ObservableList<Task<?>> getTasks()
	{
		return this.tasks;
	}
}
