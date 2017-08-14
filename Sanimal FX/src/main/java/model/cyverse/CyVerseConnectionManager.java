package model.cyverse;

import javafx.application.Platform;
import javafx.beans.property.*;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

/**
 * A class used to wrap the CyVerse Jargon FTP library
 */
public class CyVerseConnectionManager
{
	// The string containing the host address that we connect to
	private static final String CYVERSE_HOST = "data.iplantcollaborative.org";
	// The directory that each user has as their home directory
	private static final String HOME_DIRECTORY = "/iplant/home/";

	// The currently logged in account
	private IRODSAccount account;
	// The current user session
	private IRODSSession session;
	// The irodsAO used to authenticate the CyVerse account
	private IRODSAccessObjectFactory irodsAO;
	// The file system on the IRODS server
	private IRODSFileSystem fileSystem;

	// A username property which we can bind to in the rest of the program
	private ReadOnlyStringWrapper usernameProperty = new ReadOnlyStringWrapper("");
	// A logged in property which we can bind to in the rest of the program, is set to true when a user is logged in
	private ReadOnlyBooleanWrapper loggedInProperty = new ReadOnlyBooleanWrapper(false);

	/**
	 * Given a username and password, this method logs a cyverse user in
	 *
	 * @param username The username of the CyVerse account
	 * @param password The password of the CyVerse account
	 *
	 * @return True if the login was successful, false otherwise
	 */
	public Boolean login(String username, String password)
	{
		// Ensure we're not logged in yet
		if (!this.loggedInProperty.getValue())
		{
			try
			{
				// Create a new CyVerse account given the host address, port, username, password, homedirectory, and two fields I have no idea what they do...
				// For whatever reason, username, and username causes the authentication to work, so let's use it...
				this.account = IRODSAccount.instance(CYVERSE_HOST, 1247, username, password,HOME_DIRECTORY + username, username, "./", AuthScheme.STANDARD);
				// Create a new session
				this.session = IRODSSession.instance(IRODSSimpleProtocolManager.instance());
				// Create an irodsAO
				this.irodsAO = IRODSAccessObjectFactoryImpl.instance(this.session);
				// Perform the authentication and get a response
				AuthResponse authResponse = this.irodsAO.authenticateIRODSAccount(this.account);
				// If the authentication worked, return true and set the username and logged in fields
				if (authResponse.isSuccessful())
				{
					// Do this on the FX thread
					Platform.runLater(() -> {
						// Set the username property to the new user
						this.usernameProperty.setValue(username);
						// Set the logged in value to true
						this.loggedInProperty.setValue(true);
					});
					// Cache the authenticated IRODS account
					this.account = authResponse.getAuthenticatedIRODSAccount();
					this.fileSystem = IRODSFileSystem.instance();
					// We're good, return true
					return true;
				}
				else
				{
					// If the authentication failed, print a message, and logout in case the login partially completed
					System.out.println("Authentication failed. Response was: " + authResponse.getAuthMessage());
					this.logout();
					return false;
				}
			}
			// If the authentication failed, print a message, and logout in case the login partially completed
			catch (InvalidUserException | AuthenticationException e)
			{
				System.out.println("Authentication failed!");
				this.logout();
				return false;
			}
			// If the authentication failed due to a jargon exception, print a message, and logout in case the login partially completed
			// Not really sure how this happens, probably if the server incorrectly responds or is down
			catch (JargonException e)
			{
				System.err.println("Unknown Jargon Exception. Error was:\n");
				e.printStackTrace();
				this.logout();
				return false;
			}
		}
		// Default, just return false
		return false;
	}

	/**
	 * Logs the user out of their account
	 */
	public void logout()
	{
		// Ensure that we have a currently logged in account
		if (this.loggedInProperty.getValue())
		{
			// Also closes the session and account
			this.irodsAO.closeSessionAndEatExceptions(this.account);
			// Set logged in to false, and the username to the empty string
			Platform.runLater(() -> {
				this.loggedInProperty.setValue(false);
				this.usernameProperty.setValue("");
			});
			// Reset the irodsAO, account, an session to null
			this.irodsAO = null;
			this.account = null;
			this.session = null;
		}
	}

	public void test()
	{
		try
		{
			DataTransferOperations dataTransferOperations = this.irodsAO.getDataTransferOperations(this.account);
		}
		catch (JargonException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return Gets the username of the logged in user, read only though
	 */
	public ReadOnlyStringProperty usernameProperty()
	{
		return usernameProperty.getReadOnlyProperty();
	}

	/**
	 * @return True if a user is logged in, false otherwise, read only though
	 */
	public ReadOnlyBooleanProperty loggedInProperty()
	{
		return loggedInProperty.getReadOnlyProperty();
	}
}
