package model.threading;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import model.SanimalData;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Wrapper task that is aware of errors
 *
 * @param <V> Decides the task return value
 */
public abstract class ErrorTask<V> extends Task<V>
{
	/**
	 * Constructor adds a failed listener
	 */
	public ErrorTask()
	{
		super();
		// If the task fails, print an error
		EventHandler<WorkerStateEvent> handler = event ->
		{
			SanimalData.getInstance().getErrorDisplay().printError("Task failed! Error was: ");
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
