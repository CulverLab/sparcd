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

public class ImmediateExecutor extends BaseSanimalExecutor
{
	private final ObservableList<Worker<?>> activeTasks = FXCollections.observableArrayList(task -> new Observable[] { task.progressProperty(), task.messageProperty() });
	// The number of tasks currently running
	private final ReadOnlyIntegerWrapper tasksRunning = new ReadOnlyIntegerWrapper();

	public ImmediateExecutor()
	{
		super(Executors.newCachedThreadPool());
	}

	@Override
	protected void onSucceeded(Worker<?> worker)
	{
		this.tasksRunning.add(-1);
		this.activeTasks.remove(worker);
	}

	@Override
	protected void onRunning(Worker<?> worker)
	{
		this.tasksRunning.add(1);
		this.activeTasks.add(worker);
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

	public ObservableList<Worker<?>> getActiveTasks()
	{
		return this.activeTasks;
	}
}
