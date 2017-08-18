package model.cyverse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.*;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.*;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class used to wrap the CyVerse Jargon FTP library
 */
public class CyVerseConnectionManager
{
	// The string containing the host address that we connect to
	private static final String CYVERSE_HOST = "data.cyverse.org";
	// The directory that each user has as their home directory
	private static final String HOME_DIRECTORY = "/iplant/home/";
	// Each user is part of the iPlant zone
	private static final String ZONE = "iplant";
	// The type used to serialize a list of locations through Gson
	private static final Type LOCATION_LIST_TYPE = new TypeToken<ArrayList<Location>>(){}.getType();
	// The type used to serialize a list of species through Gson
	private static final Type SPECIES_LIST_TYPE = new TypeToken<ArrayList<Species>>(){}.getType();

	// The currently logged in account
	private IRODSAccount account;
	// The current user session
	private IRODSSession session;
	// The irodsAO used to authenticate the CyVerse account
	private IRODSAccessObjectFactory irodsAO;

	// Start all the Access Objects we use to accesss the user's account
	private IRODSFileSystemAO irodsFileSystemAO;
	private IRODSFileFactory irodsFileFactory;
	private UserAO irodsUserAO;
	private DataTransferOperations irodsDataTransfer;
	// End all the Access Objects we use to accesss the user's account

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
				// Create a new CyVerse account given the host address, port, username, password, homedirectory, and one field I have no idea what it does..., however leaving it as empty string makes file creation work!
				this.account = IRODSAccount.instance(CYVERSE_HOST, 1247, username, password,HOME_DIRECTORY + username, ZONE, "", AuthScheme.STANDARD);
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

					// Setup all access objects to the account (Represented by the AO at the end of the class name)
					this.irodsFileSystemAO = this.irodsAO.getIRODSFileSystemAO(this.account);
					this.irodsFileFactory = this.irodsAO.getIRODSFileFactory(this.account);
					this.irodsUserAO = this.irodsAO.getUserAO(this.account);
					this.irodsDataTransfer = this.irodsAO.getDataTransferOperations(this.account);

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
			this.irodsFileFactory = null;
			this.irodsFileSystemAO = null;
			this.irodsUserAO = null;
			this.irodsDataTransfer = null;
		}
	}

	/**
	 * This method initializes the remove sanimal directory stored on the users account.
	 */
	public void initSanimalRemoteDirectory()
	{
		if (this.loggedInProperty.getValue())
		{
			try
			{
				// If the main Sanimal directory does not exist yet, create it
				IRODSFile sanimalDirectory = this.irodsFileFactory.instanceIRODSFile("./Sanimal");
				if (!sanimalDirectory.exists())
					sanimalDirectory.mkdir();

				// Create a subfolder containing all settings that the sanimal program stores
				IRODSFile sanimalSettings = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings");
				if (!sanimalSettings.exists())
					sanimalSettings.mkdir();

				// Create a subfolder containing all images uploaded with Sanimal
				IRODSFile sanimalUploads = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Uploads");
				if (!sanimalUploads.exists())
					sanimalUploads.mkdir();
			}
			catch (JargonException e)
			{
				System.err.println("Error initializing Sanimal directory. Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to initialize the sanimal remote directory!");
		}
	}

	public List<Location> pullRemoteLocations()
	{
		List<Location> remoteLocations = new ArrayList<>();
		if (this.loggedInProperty.getValue())
		{
			// Read the contents of the locations folder and see if there's any pre-saved locations
			try
			{
				Path localLocationPath = Files.createTempFile("locations", ".json");
				File localLocationFile = localLocationPath.toFile();
				localLocationFile.delete();
				IRODSFile remoteLocationFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/locations.json");
				if (remoteLocationFile.exists())
				{
					if (remoteLocationFile.canRead())
					{
						this.irodsDataTransfer.getOperation(remoteLocationFile, localLocationFile, null, null);
						localLocationFile.deleteOnExit();
						if (localLocationFile.exists())
						{
							try
							{
								String fileContents = new String(Files.readAllBytes(localLocationFile.toPath()));
								List<Location> locations = SanimalData.getInstance().getGson().fromJson(fileContents, LOCATION_LIST_TYPE);
								remoteLocations.addAll(locations);
							}
							catch (JsonSyntaxException e)
							{
								System.out.println("Error reading the Json file " + localLocationFile.getAbsolutePath() + ", Error was:\n");
								e.printStackTrace();
							}
						}
					}
					else
					{
						System.err.println("Error reading location json file.\n");
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pulling remote locations. Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to initialize the sanimal remote directory!");
		}
		return remoteLocations;
	}

	public void pushLocalLocations(List<Location> newLocations)
	{
		if (this.loggedInProperty.getValue())
		{
			// Create a temporary file to write each location to before uploading
			try
			{
				Path localLocationFilePath = Files.createTempFile("locations", ".json");
				File localLocationFile = localLocationFilePath.toFile();
				localLocationFile.deleteOnExit();
				if (localLocationFile.exists())
				{
					IRODSFile remoteLocationFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/locations.json");
					if (remoteLocationFile.exists())
						remoteLocationFile.delete();

					String json = SanimalData.getInstance().getGson().toJson(newLocations);
					try (PrintWriter fileWriter = new PrintWriter(localLocationFile))
					{
						fileWriter.write(json);
					}
					this.irodsDataTransfer.putOperation(localLocationFile, remoteLocationFile, null, null);
				}
				else
				{
					System.err.println("Error creating a temporary file to write locations to.\n");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pulling remote locations. Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to push the sanimal local locations!");
		}
	}

	public List<Species> pullRemoteSpecies()
	{
		List<Species> remoteSpecies = new ArrayList<>();
		if (this.loggedInProperty.getValue())
		{
			// Read the contents of the species folder and see if there's any pre-saved species
			try
			{
				Path localSpeciesPath = Files.createTempFile("species", ".json");
				File localSpeciesFile = localSpeciesPath.toFile();
				localSpeciesFile.delete();
				IRODSFile remoteSpeciesFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/species.json");
				if (remoteSpeciesFile.exists())
				{
					if (remoteSpeciesFile.canRead())
					{
						this.irodsDataTransfer.getOperation(remoteSpeciesFile, localSpeciesFile, null, null);
						localSpeciesFile.deleteOnExit();
						if (localSpeciesFile.exists())
						{
							try
							{
								String fileContents = new String(Files.readAllBytes(localSpeciesFile.toPath()));
								List<Species> species = SanimalData.getInstance().getGson().fromJson(fileContents, SPECIES_LIST_TYPE);
								remoteSpecies.addAll(species);
							}
							catch (JsonSyntaxException e)
							{
								System.out.println("Error reading the Json file " + localSpeciesFile.getAbsolutePath() + ", Error was:\n");
								e.printStackTrace();
							}
						}
					}
					else
					{
						System.err.println("Error reading species json file.\n");
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pulling remote species. Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to initialize the sanimal remote directory!");
		}
		return remoteSpecies;
	}

	public void pushLocalSpecies(List<Species> newSpecies)
	{
		if (this.loggedInProperty.getValue())
		{
			// Create a temporary file to write each species to before uploading
			try
			{
				Path localSpeciesFilePath = Files.createTempFile("species", ".json");
				File localSpeciesFile = localSpeciesFilePath.toFile();
				localSpeciesFile.deleteOnExit();
				if (localSpeciesFile.exists())
				{
					IRODSFile remoteSpeciesFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/species.json");
					if (remoteSpeciesFile.exists())
						remoteSpeciesFile.delete();

					String json = SanimalData.getInstance().getGson().toJson(newSpecies);
					try (PrintWriter fileWriter = new PrintWriter(localSpeciesFile))
					{
						fileWriter.write(json);
					}
					this.irodsDataTransfer.putOperation(localSpeciesFile, remoteSpeciesFile, null, null);
				}
				else
				{
					System.err.println("Error creating a temporary file to write species to.\n");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pulling remote species. Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to push the sanimal local species!");
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
