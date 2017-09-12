package model.cyverse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.*;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

				// If we don't have a default species.json file, put a default one onto the storage location
				IRODSFile sanimalSpeciesFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/species.json");
				if (!sanimalSpeciesFile.exists())
				{
					// Pull the default species.json file
					try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/species.json"));
						 BufferedReader fileReader = new BufferedReader(inputStreamReader))
					{
						// Read the Json file
						String json = fileReader.lines().collect(Collectors.joining("\n"));
						// Write it to the directory
						this.writeRemoteFile("./Sanimal/Settings/species.json", json);
					}
					catch (IOException e)
					{
						System.err.println("Could not read species.json. The file might be incorrectly formatted...");
						e.printStackTrace();
					}
				}

				// If we don't have a default locations.json file, put a default one onto the storage location
				IRODSFile sanimalLocationsFile = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Settings/locations.json");
				if (!sanimalLocationsFile.exists())
				{
					// Pull the default locations.json file
					try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/locations.json"));
						 BufferedReader fileReader = new BufferedReader(inputStreamReader))
					{
						// Read the Json file
						String json = fileReader.lines().collect(Collectors.joining("\n"));
						// Write it to the directory
						this.writeRemoteFile("./Sanimal/Settings/locations.json", json);
					}
					catch (IOException e)
					{
						System.err.println("Could not read locations.json. The file might be incorrectly formatted...");
						e.printStackTrace();
					}
				}

				// Create a subfolder containing all images uploaded with Sanimal. This is temporary
				IRODSFile sanimalUploads = this.irodsFileFactory.instanceIRODSFile("./Sanimal/Collections");
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

	/**
	 * Connects to CyVerse and downloads the list of the user's locations
	 *
	 * @return A list of locations stored on the CyVerse system
	 */
	public List<Location> pullRemoteLocations()
	{
		// Path to the file on the CyVerse server should be named locations.json
		String fileName = "./Sanimal/Settings/locations.json";
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(fileName);
		// Ensure that we in fact got data back
		if (fileContents != null)
		{
			// Try to parse the JSON string into a list of locations
			try
			{
				// Get the GSON object to parse the JSON. Return the list of new locations
				return SanimalData.getInstance().getGson().fromJson(fileContents, LOCATION_LIST_TYPE);
			}
			catch (JsonSyntaxException e)
			{
				// If the JSON file is incorrectly formatted, throw an error and return an empty list
				System.out.println("Error reading the Json file " + fileName + ", Error was:\n");
				e.printStackTrace();
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Connects to CyVerse and uploads the given list of lcations into the locations.json file
	 *
	 * @param newLocations The list of new locations to upload
	 */
	public void pushLocalLocations(List<Location> newLocations)
	{
		// Convert the location list to JSON format
		String json = SanimalData.getInstance().getGson().toJson(newLocations);
		// Write the locations.json file to the server
		this.writeRemoteFile("./Sanimal/Settings/locations.json", json);
	}

	/**
	 * Connects to CyVerse and downloads the list of the user's species list
	 *
	 * @return A list of species stored on the CyVerse system
	 */
	public List<Species> pullRemoteSpecies()
	{
		// Path to the file on the CyVerse server should be named species.json
		String fileName = "./Sanimal/Settings/species.json";
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(fileName);
		// Ensure that we in fact got data back
		if (fileContents != null)
		{
			// Try to parse the JSON string into a list of species
			try
			{
				// Get the GSON object to parse the JSON. Return the list of new locations
				return SanimalData.getInstance().getGson().fromJson(fileContents, SPECIES_LIST_TYPE);
			}
			catch (JsonSyntaxException e)
			{
				// If the JSON file is incorrectly formatted, throw an error and return an empty list
				System.out.println("Error reading the Json file " + fileName + ", Error was:\n");
				e.printStackTrace();
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Connects to CyVerse and uploads the given list of species into the species.json file
	 *
	 * @param newSpecies The list of new species to upload
	 */
	public void pushLocalSpecies(List<Species> newSpecies)
	{
		// Convert the species list to JSON format
		String json = SanimalData.getInstance().getGson().toJson(newSpecies);
		// Write the species.json file to the server
		this.writeRemoteFile("./Sanimal/Settings/species.json", json);
	}

	/**
	 * Connects to CyVerse and downloads the list of the user's collections
	 *
	 * @return A list of collections stored on the CyVerse system
	 */
	public List<ImageCollection> pullRemoteCollections()
	{
		String collectionsFolderName = "./Sanimal/Collections";
		List<ImageCollection> imageCollections = new ArrayList<>();
		try
		{
			IRODSFile collectionsFolder = this.irodsFileFactory.instanceIRODSFile(collectionsFolderName);
			if (collectionsFolder.exists())
			{
				File[] files = collectionsFolder.listFiles();
				if (files instanceof IRODSFile[])
				{
					IRODSFile[] collections = (IRODSFile[]) files;
					for (IRODSFile collectionDir : collections)
					{
						if (collectionDir.canRead() && collectionDir.isDirectory())
						{
							String jsonFile = collectionDir.getAbsolutePath() + "/collection.json";
							String collectionJSON = this.readRemoteFile(jsonFile);
							if (collectionJSON != null)
							{
								// Try to parse the JSON string into collection
								try
								{
									// Get the GSON object to parse the JSON.
									ImageCollection imageCollection = SanimalData.getInstance().getGson().fromJson(collectionJSON, ImageCollection.class);
									if (imageCollection != null)
									{
										imageCollections.add(imageCollection);
									}
								}
								catch (JsonSyntaxException e)
								{
									// If the JSON file is incorrectly formatted, throw an error and return an empty list
									System.out.println("Error reading the Json file " + jsonFile + ", Error was:\n");
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
			else
			{
				System.out.println("Collections folder not found!");
			}
			/*
			// Read the contents of the file into a string
			String fileContents = this.readRemoteFile(fileName);
			// Ensure that we in fact got data back
			if (fileContents != null)
			{
				// Try to parse the JSON string into a list of locations
				try
				{
					// Get the GSON object to parse the JSON. Return the list of new locations
					return SanimalData.getInstance().getGson().fromJson(fileContents, LOCATION_LIST_TYPE);
				}
				catch (JsonSyntaxException e)
				{
					// If the JSON file is incorrectly formatted, throw an error and return an empty list
					System.out.println("Error reading the Json file " + fileName + ", Error was:\n");
					e.printStackTrace();
				}
			}
			*/
		}
		catch (JargonException e)
		{
			e.printStackTrace();
		}

		return imageCollections;
	}

	/**
	 * Connects to CyVerse and uploads the given list of collections to CyVerse's data store
	 *
	 * @param collections The list of new species to upload
	 */
	public void pushLocalCollections(List<ImageCollection> collections)
	{
		String collectionsDir = "./Sanimal/Collections";
		collections.forEach(collection -> {
			List<Permission> currentUserPermissions = collection.getPermissions().filtered(permission -> permission.getUsername().equals(this.account.getUserName()));
			try
			{
				if (currentUserPermissions.size() == 1)
				{
					Permission currentUser = currentUserPermissions.get(0);
					if (currentUser.getOwner())
					{
						String collectionDirName = collectionsDir + "/" + collection.getID().toString();

						IRODSFile collectionDir = this.irodsFileFactory.instanceIRODSFile(collectionDirName);
						if (collectionDir.canRead())
						{
							if (!collectionDir.exists())
							{
								collectionDir.mkdir();
							}

							String jsonFile = collectionDirName + "/collection.json";
							String json = SanimalData.getInstance().getGson().toJson(collection);
							this.writeRemoteFile(jsonFile, json);

							IRODSFile collectionDirUploads = this.irodsFileFactory.instanceIRODSFile(collectionsDir + "/Uploads");
							if (!collectionDirUploads.exists())
								collectionDirUploads.mkdir();
						}
					}

				}
			}
			catch (JargonException e)
			{
				e.printStackTrace();
			}
		});
	}

	/**
	 * Reads a file from CyVerse assuming a user is already logged in
	 *
	 * @param file The path to the file to read
	 *
	 * @return The contents of the file on CyVerse's system as a string
	 */
	private String readRemoteFile(String file)
	{
		// Ensure we're logged in
		if (this.loggedInProperty.getValue())
		{
			try
			{
				// Create a temporary file to write to
				Path localPath = Files.createTempFile("sanimalTemp", "." + FilenameUtils.getExtension(file));
				File localFile = localPath.toFile();
				// Delete the temporary file before copying so that we don't need to specify overwriting
				localFile.delete();
				// Create the remote file instance
				IRODSFile remoteFile = this.irodsFileFactory.instanceIRODSFile(file);
				// Ensure it exists
				if (remoteFile.exists())
				{
					// Ensure it can be read
					if (remoteFile.canRead())
					{
						this.irodsDataTransfer.getOperation(remoteFile, localFile, null, null);
						// Delete the local file after exiting
						localFile.deleteOnExit();
						// Ensure that the file exists and transfered
						if (localFile.exists())
						{
							// Read the contents of the file and return them
							return new String(Files.readAllBytes(localFile.toPath()));
						}
					}
					else
					{
						System.err.println("Remote file cannot be read.\n");
					}
				}
				else
				{
					System.err.println("Remote file does not exist.\n");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pulling remote file (" + file + "). Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to read the sanimal remote directory!");
		}

		// If anything fails return null
		return null;
	}

	/**
	 * Write a value to a file on the CyVerse server
	 *
	 * @param file The file to write to
	 * @param value The string value to write to the file
	 */
	private void writeRemoteFile(String file, String value)
	{
		// Ensure we're logged in properly
		if (this.loggedInProperty.getValue())
		{
			// Create a temporary file to write each location to before uploading
			try
			{
				// Create a local file to write to
				Path localPath = Files.createTempFile("sanimalTemp", "." + FilenameUtils.getExtension(file));
				// Grab the file from the path
				File localFile = localPath.toFile();
				// Delete the file once the program exits
				localFile.deleteOnExit();
				// Ensure the file we made exists
				if (localFile.exists())
				{
					// Create the irods file to write to
					IRODSFile remoteLocationFile = this.irodsFileFactory.instanceIRODSFile(file);
					// If it exists already, delete it
					if (remoteLocationFile.exists())
						remoteLocationFile.delete();

					// Create a file writer which writes a string to a file. Write the value to the local file
					try (PrintWriter fileWriter = new PrintWriter(localFile))
					{
						fileWriter.write(value);
					}
					// Perform a put operation to write the local file to the CyVerse server
					this.irodsDataTransfer.putOperation(localFile, remoteLocationFile, null, null);
				}
				else
				{
					System.err.println("Error creating a temporary file to write to.");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (JargonException e)
			{
				System.err.println("Error pushing remote file (" + file + "). Error was:\n");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Must be logged in to push a file to the Sanimal directory!");
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
