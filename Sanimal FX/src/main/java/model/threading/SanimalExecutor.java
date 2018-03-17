package model.threading;

import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class used to keep track of threads to run in the background
 */
public class SanimalExecutor
{
	private QueuedExecutor queuedExecutor = new QueuedExecutor();
	private ImmediateExecutor immediateExecutor = new ImmediateExecutor();

	public QueuedExecutor getQueuedExecutor()
	{
		return this.queuedExecutor;
	}

	public ImmediateExecutor getImmediateExecutor()
	{
		return this.immediateExecutor;
	}

	public Boolean anyTaskRunning()
	{
		return this.queuedExecutor.getTaskRunning();
	}
}
