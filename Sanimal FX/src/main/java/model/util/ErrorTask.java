package model.util;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.util.function.Consumer;

/**
 * Wrapper task that is aware of errors
 *
 * @param <V> Decides the task return value
 */
public abstract class ErrorTask<V> extends Task<V>
{
	public ErrorTask()
	{
		super();
		EventHandler<WorkerStateEvent> handler = event ->
		{
			System.out.println("Task failed! Error was: ");
			Worker source = event.getSource();
			if (source != null)
			{
				System.out.println("Error Message: " + source.getMessage());
				System.out.println("Stack trace: ");
				Throwable exception = source.getException();
				if (exception != null)
				{
					exception.printStackTrace();
				}
			}
		};
		// When the task fails print out the failure
		this.setOnFailed(handler);
		this.setOnCancelled(handler);
	}
}
