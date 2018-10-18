package model.threading;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import model.SanimalData;
import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * Wrapper service that is aware of errors
 *
 * @param <V> Decides the service return value
 */
public abstract class ErrorService<V> extends Service<V>
{
	/**
	 * Constructor adds a failed listener
	 */
	public ErrorService()
	{
		super();
		// If the service's task fails, print an error
		EventHandler<WorkerStateEvent> handler = event ->
		{
			SanimalData.getInstance().getErrorDisplay().printError("Service failed! Error was: ");
			Worker source = event.getSource();
			if (source != null)
			{
				SanimalData.getInstance().getErrorDisplay().printError("Error Message: " + source.getMessage());
				Throwable exception = source.getException();
				if (exception != null)
					SanimalData.getInstance().getErrorDisplay().printError("Stack trace: " + ExceptionUtils.getStackTrace(exception));
			}
		};
		// When the task fails print out the failure
		this.setOnFailed(handler);
	}
}