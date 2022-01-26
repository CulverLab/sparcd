package model.s3;

import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.util.Pair;
import model.SanimalData;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Class that maintains connections to S3
 */
public class S3SessionManager
{
	// A map of thread -> session objects, used to keep 1 session per thread
	private Map<Thread, String> sessions = Collections.synchronizedMap(new HashMap<>());

	// A reference to the authenticated irods account
	private AmazonS3 authenticatedAccount;

	/**
	 * Constructor just needs the authenticated irods account
	 *
	 * @param authenticatedAccount The account that has been authenticated
	 */
	public S3SessionManager(AmazonS3 authenticatedAccount)
	{
		this.authenticatedAccount = authenticatedAccount;
	}

	/**
	 * Either returns false if a session is already open in the current thread or a session fails to open, returns true otherwise
	 *
	 * @return True if the session was opened successfully
	 */
	public boolean openSession()
	{
		// Grab the current thread
		Thread current = Thread.currentThread();
		// Test if this thread already has a session object
		if (this.sessions.containsKey(current))
			return false;
		else
		{
			// Create a session
			try
			{
				// Create the session and store it
				this.sessions.put(current, current.toString());
				return true;
			}
			// Print an error and return false
			catch (Exception e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Session error",
						"Error creating a session!\n" + ExceptionUtils.getStackTrace(e),
						false);
				return false;
			}
		}
	}

	/**
	 * Closes the session for the current thread if there is one open at the moment
	 */
	public void closeSession()
	{
		// Grab the current thread, and see if a session is associated with the thread
		Thread current = Thread.currentThread();
		if (this.sessions.containsKey(current))
		{
			// If so, grab the session and close it
			try
			{
				// Close the session
			}
			// An error occured, ignore it
			catch (Exception e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Session error",
						"Error closing a session!\n" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessions.remove(current);
		}
	}

	/**
	 * Getter for the current session this thread is operating on
	 *
	 * @return A session object or null if no session object is present for this thread
	 */
	public String getCurrentSession()
	{
		String session = this.sessions.get(Thread.currentThread());
		if (session == null)
			return null;
		return session;
	}
}
