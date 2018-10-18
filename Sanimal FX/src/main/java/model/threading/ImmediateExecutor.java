package model.threading;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class that takes a task and IMMEDIATELY begins exeuction without queuing
 */
public class ImmediateExecutor extends BaseSanimalExecutor
{
	// A list of active tasks being processed
	private final ObservableList<Task<?>> activeTasks = FXCollections.observableArrayList(task -> new Observable[] { task.progressProperty(), task.messageProperty() });
	// The number of tasks currently running
	private final ReadOnlyIntegerWrapper tasksRunning = new ReadOnlyIntegerWrapper();

	/**
	 * Constructor specifies a cached thread pool which can grow infinitely
	 */
	public ImmediateExecutor()
	{
		super(Executors.newFixedThreadPool(50));
	}

	/**
	 * When a task finishes, just remove it from the list of running tasks
	 *
	 * @param worker The worker that finished
	 */
	@Override
	protected void onSucceeded(Worker<?> worker)
	{
		this.tasksRunning.add(-1);
		if (worker instanceof Task<?>)
			this.activeTasks.remove(worker);
	}

	/**
	 * When a task starts, just add it from the list of running tasks
	 *
	 * @param worker The worker that began
	 */
	@Override
	protected void onRunning(Worker<?> worker)
	{
		this.tasksRunning.add(1);
		if (worker instanceof Task<?>)
			this.activeTasks.add((Task<?>) worker);
	}

	///
	/// Getters, but use read only
	///

	public Integer getTaskRunning()
	{
		return this.tasksRunning.getValue();
	}

	public ReadOnlyIntegerProperty taskRunningProperty()
	{
		return this.tasksRunning.getReadOnlyProperty();
	}

	public ObservableList<Task<?>> getActiveTasks()
	{
		return this.activeTasks;
	}
}
