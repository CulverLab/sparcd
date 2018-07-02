package model.cyverse;

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

/**
 * Class that maintains connections to cyverse
 */
public class CyVerseSessionManager
{
	// A map of thread -> session objects, used to keep 1 session per thread
	private Map<Thread, Pair<IRODSSession, IRODSAccessObjectFactory>> sessions = Collections.synchronizedMap(new HashMap<>());

	// A reference to the authenticated irods account
	private IRODSAccount authenticatedAccount;

	/**
	 * Constructor just needs the authenticated irods account
	 *
	 * @param authenticatedAccount The account that has been authenticated
	 */
	public CyVerseSessionManager(IRODSAccount authenticatedAccount)
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
				IRODSSession newSession = IRODSSession.instance(IRODSSimpleProtocolManager.instance());
				IRODSAccessObjectFactory newAccessFactory = IRODSAccessObjectFactoryImpl.instance(newSession);
				this.sessions.put(current, new Pair<>(newSession, newAccessFactory));
				return true;
			}
			// Print an error and return false
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().notify("Error creating a session!\n" + ExceptionUtils.getStackTrace(e));
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
			Pair<IRODSSession, IRODSAccessObjectFactory> session = this.sessions.get(current);
			try
			{
				// Close the session
				session.getKey().closeSession(this.authenticatedAccount);
			}
			// An error occured, ignore it
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().notify("Error closing a session!\n" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			}
			this.sessions.remove(current);
		}
	}

	/**
	 * Getter for the current session this thread is operating on
	 *
	 * @return A session object or null if no session object is present for this thread
	 */
	public IRODSSession getCurrentSession()
	{
		Pair<IRODSSession, IRODSAccessObjectFactory> sessionPair = this.sessions.get(Thread.currentThread());
		if (sessionPair == null)
			return null;
		return sessionPair.getKey();
	}

	/**
	 * Getter for the current access object this thread is operating on
	 *
	 * @return An access object or null if no access object object is present for this thread
	 */
	public IRODSAccessObjectFactory getCurrentAO()
	{
		Pair<IRODSSession, IRODSAccessObjectFactory> sessionPair = this.sessions.get(Thread.currentThread());
		if (sessionPair == null)
			return null;
		return sessionPair.getValue();
	}
}
