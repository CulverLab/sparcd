package model.util;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Class used to wrap a task and allows it to have an on finished event
 *
 * @param <V> Decides the task return value
 */
public abstract class FinishableTask<V> extends Task<V>
{
	/**
	 * The onFinished event handler is called whenever the Task state
	 * transitions to the SUCCEEDED, FAILED, or CANCELED state.
	 *
	 * @param value the event handler, can be null to clear it
	 */
	public final void setOnFinished(EventHandler<WorkerStateEvent> value)
	{
		this.setOnSucceeded(value);
		this.setOnFailed(value);
		this.setOnCancelled(value);
	}
}
