package model.util;

import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;


/**
 * Wrapper service that is aware of errors
 *
 * @param <V> Decides the service return value
 */
public abstract class ErrorService<V> extends Service<V>
{
	public ErrorService()
	{
		super();
		EventHandler<WorkerStateEvent> handler = event ->
		{
			System.out.println("Service failed! Error was: ");
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