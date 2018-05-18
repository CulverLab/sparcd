package model.cyverse;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.image.*;
import model.location.Location;
import model.query.CyVerseQuery;
import model.species.Species;
import model.util.RoundingUtils;
import model.util.SettingsData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.irods.jargon.core.connection.*;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.*;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class used to wrap the CyVerse Jargon FTP library
 */
public class CyVerseConnectionManager
{
	// The string containing the host address that we connect to
	private static final String CYVERSE_HOST = "data.cyverse.org"; // diana.cyverse.org
	// The directory that each user has as their home directory
	private static final String HOME_DIRECTORY = "/iplant/home/";
	// The directory that collections are stored in
	private static final String COLLECTIONS_DIRECTORY = "/iplant/home/dslovikosky/Sanimal/Collections";
	// Each user is part of the iPlant zone
	private static final String ZONE = "iplant";
	// The type used to serialize a list of locations through Gson
	private static final Type LOCATION_LIST_TYPE = new TypeToken<ArrayList<Location>>()
	{
	}.getType();
	// The type used to serialize a list of species through Gson
	private static final Type SPECIES_LIST_TYPE = new TypeToken<ArrayList<Species>>()
	{
	}.getType();
	// The type used to serialize a list of permissions through Gson
	private static final Type PERMISSION_LIST_TYPE = new TypeToken<ArrayList<Permission>>()
	{
	}.getType();
	private static final SimpleDateFormat FOLDER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

	private IRODSAccount authenticatedAccount;
	private CyVerseSessionManager sessionManager;

	/**
	 * Given a username and password, this method logs a cyverse user in
	 *
	 * @param username The username of the CyVerse account
	 * @param password The password of the CyVerse account
	 * @return True if the login was successful, false otherwise
	 */
	public Boolean login(String username, String password)
	{
		try
		{
			// Create a new CyVerse account given the host address, port, username, password, homedirectory, and one field I have no idea what it does..., however leaving it as empty string makes file creation work!
			IRODSAccount account = IRODSAccount.instance(CYVERSE_HOST, 1247, username, password, HOME_DIRECTORY + username, ZONE, "", AuthScheme.STANDARD);
			// Create a new session
			IRODSSession session = IRODSSession.instance(IRODSSimpleProtocolManager.instance());
			// Create an irodsAO
			IRODSAccessObjectFactory irodsAO = IRODSAccessObjectFactoryImpl.instance(session);
			// Perform the authentication and get a response
			AuthResponse authResponse = irodsAO.authenticateIRODSAccount(account);
			// If the authentication worked, return true and set the username and logged in fields
			if (authResponse.isSuccessful())
			{
				// Cache the authenticated IRODS account
				this.authenticatedAccount = authResponse.getAuthenticatedIRODSAccount();

				// Store a session manager
				this.sessionManager = new CyVerseSessionManager(this.authenticatedAccount);

				// We're good, return true
				return true;
			}
			else
			{
				// If the authentication failed, print a message, and logout in case the login partially completed
				SanimalData.getInstance().getErrorDisplay().printError("Authentication failed. Response was: " + authResponse.getAuthMessage());
			}
			session.closeSession(account);
		}
		// If the authentication failed, print a message, and logout in case the login partially completed
		catch (InvalidUserException | AuthenticationException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Authentication failed!");
		}
		// If the authentication failed due to a jargon exception, print a message, and logout in case the login partially completed
		// Not really sure how this happens, probably if the server incorrectly responds or is down
		catch (JargonException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Authentication failed",
					"Could not authenticate the user!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
		// Default, just return false
		return false;
	}

	/**
	 * This method initializes the remove sanimal directory stored on the users account.
	 */
	public void initSanimalRemoteDirectory()
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				IRODSFileFactory fileFactory = sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);

				// If the main Sanimal directory does not exist yet, create it
				IRODSFile sanimalDirectory = fileFactory.instanceIRODSFile("./Sanimal");
				if (!sanimalDirectory.exists())
					sanimalDirectory.mkdir();

				// Create a subfolder containing all settings that the sanimal program stores
				IRODSFile sanimalSettings = fileFactory.instanceIRODSFile("./Sanimal/Settings");
				if (!sanimalSettings.exists())
					sanimalSettings.mkdir();

				// If we don't have a default species.json file, put a default one onto the storage location
				IRODSFile sanimalSpeciesFile = fileFactory.instanceIRODSFile("./Sanimal/Settings/species.json");
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
						SanimalData.getInstance().getErrorDisplay().showPopup(
								Alert.AlertType.ERROR,
								null,
								"Error",
								"JSON error",
								"Could not read the local species.json file!\n" + ExceptionUtils.getStackTrace(e),
								false);
					}
				}

				// If we don't have a default locations.json file, put a default one onto the storage location
				IRODSFile sanimalLocationsFile = fileFactory.instanceIRODSFile("./Sanimal/Settings/locations.json");
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
						SanimalData.getInstance().getErrorDisplay().showPopup(
								Alert.AlertType.ERROR,
								null,
								"Error",
								"JSON error",
								"Could not read the local locations.json file!\n" + ExceptionUtils.getStackTrace(e),
								false);
					}
				}

				// If we don't have a default settings.json file, put a default one onto the storage location
				IRODSFile sanimalSettingsFile = fileFactory.instanceIRODSFile("./Sanimal/Settings/settings.json");
				if (!sanimalSettingsFile.exists())
				{
					// Pull the default settings.json file
					try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/settings.json"));
						 BufferedReader fileReader = new BufferedReader(inputStreamReader))
					{
						// Read the Json file
						String json = fileReader.lines().collect(Collectors.joining("\n"));
						// Write it to the directory
						this.writeRemoteFile("./Sanimal/Settings/settings.json", json);
					}
					catch (IOException e)
					{
						SanimalData.getInstance().getErrorDisplay().showPopup(
								Alert.AlertType.ERROR,
								null,
								"Error",
								"JSON error",
								"Could not read the local settings.json file!\n" + ExceptionUtils.getStackTrace(e),
								false);
					}
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Initialization error",
						"Could not initialize the CyVerse directories!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			sessionManager.closeSession();
		}
	}

	/**
	 * Connects to CyVerse and uploads the given settings into the settings.json file
	 *
	 * @param settingsData The new settings to upload
	 */
	public void pushLocalSettings(SettingsData settingsData)
	{
		if (this.sessionManager.openSession())
		{
			// Convert the settings to JSON format
			String json = SanimalData.getInstance().getGson().toJson(settingsData);
			// Write the settings.json file to the server
			this.writeRemoteFile("./Sanimal/Settings/settings.json", json);
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Connects to CyVerse and downloads the user's settings
	 *
	 * @return User settings stored on the CyVerse system
	 */
	public SettingsData pullRemoteSettings()
	{
		if (this.sessionManager.openSession())
		{
			// Path to the file on the CyVerse server should be named settings.json
			String fileName = "./Sanimal/Settings/settings.json";
			// Read the contents of the file into a string
			String fileContents = this.readRemoteFile(fileName);
			// Ensure that we in fact got data back
			if (fileContents != null)
			{
				// Try to parse the JSON string into a settings data
				try
				{
					this.sessionManager.closeSession();
					// Get the GSON object to parse the JSON. Return the list of new locations
					return SanimalData.getInstance().getGson().fromJson(fileContents, SettingsData.class);
				}
				catch (JsonSyntaxException e)
				{
					// If the JSON file is incorrectly formatted, throw an error and return null
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"JSON error",
							"Could not pull the settings from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
							false);
				}
			}
			this.sessionManager.closeSession();
		}

		return null;
	}

	/**
	 * Connects to CyVerse and downloads the list of the user's locations
	 *
	 * @return A list of locations stored on the CyVerse system
	 */
	public List<Location> pullRemoteLocations()
	{
		if (this.sessionManager.openSession())
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
					this.sessionManager.closeSession();
					// Get the GSON object to parse the JSON. Return the list of new locations
					return SanimalData.getInstance().getGson().fromJson(fileContents, LOCATION_LIST_TYPE);
				}
				catch (JsonSyntaxException e)
				{
					// If the JSON file is incorrectly formatted, throw an error and return an empty list
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"JSON error",
							"Could not pull the location list from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
							false);
				}
			}
			this.sessionManager.closeSession();
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
		if (this.sessionManager.openSession())
		{
			// Convert the location list to JSON format
			String json = SanimalData.getInstance().getGson().toJson(newLocations);
			// Write the locations.json file to the server
			this.writeRemoteFile("./Sanimal/Settings/locations.json", json);
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Connects to CyVerse and downloads the list of the user's species list
	 *
	 * @return A list of species stored on the CyVerse system
	 */
	public List<Species> pullRemoteSpecies()
	{
		if (this.sessionManager.openSession())
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
					this.sessionManager.closeSession();
					// Get the GSON object to parse the JSON. Return the list of new locations
					return SanimalData.getInstance().getGson().fromJson(fileContents, SPECIES_LIST_TYPE);
				}
				catch (JsonSyntaxException e)
				{
					// If the JSON file is incorrectly formatted, throw an error and return an empty list
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"JSON error",
							"Could not pull the species list from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
							false);
				}
			}
			this.sessionManager.closeSession();
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
		if (this.sessionManager.openSession())
		{
			// Convert the species list to JSON format
			String json = SanimalData.getInstance().getGson().toJson(newSpecies);
			// Write the species.json file to the server
			this.writeRemoteFile("./Sanimal/Settings/species.json", json);
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Connects to CyVerse and downloads the list of the user's collections
	 *
	 * @return A list of collections stored on the CyVerse system
	 */
	public List<ImageCollection> pullRemoteCollections()
	{
		// Create a list of collections
		List<ImageCollection> imageCollections = new ArrayList<>();
		if (this.sessionManager.openSession())
		{
			try
			{
				// Grab the collections folder and make sure it exists
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile collectionsFolder = fileFactory.instanceIRODSFile(COLLECTIONS_DIRECTORY);
				if (collectionsFolder.exists())
				{
					// Grab a list of files in the collections directory
					File[] files = collectionsFolder.listFiles();
					if (files instanceof IRODSFile[])
					{
						// List of collection folders
						IRODSFile[] collections = (IRODSFile[]) files;
						// Iterate over all collections
						for (IRODSFile collectionDir : collections)
						{
							// Make sure we can read the collections directory
							if (collectionDir.isDirectory())
							{
								// Read the collection JSON file to get the collection properties
								String collectionJSONFile = collectionDir.getAbsolutePath() + "/collection.json";
								String collectionJSON = this.readRemoteFile(collectionJSONFile);
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

											String permissionsJSONFile = collectionDir.getAbsolutePath() + "/permissions.json";
											String permissionsJSON = this.readRemoteFile(permissionsJSONFile);

											// This will be null if we can't see the upload directory
											if (permissionsJSON != null)
											{
												// Get the GSON object to parse the JSON.
												List<Permission> permissions = SanimalData.getInstance().getGson().fromJson(permissionsJSON, PERMISSION_LIST_TYPE);
												if (permissions != null)
												{
													// We need to initialize the internal listeners because the deserialization process causes the fields to get wiped and reset
													permissions.forEach(Permission::initListeners);
													imageCollection.getPermissions().addAll(permissions);
												}
											}
											else
											{
												// Grab the uploads directory
												IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDir.getAbsolutePath() + "/Uploads");
												// If we got a null permissions JSON, we check if we can see the uploads folder. If so, we have upload permissions!
												if (collectionDirUploads.exists())
												{
													// Add a permission for my own permissions
													Permission myPermission = new Permission();
													myPermission.setOwner(false);
													myPermission.setUsername(SanimalData.getInstance().getUsername());
													myPermission.setUpload(collectionDirUploads.canWrite());
													myPermission.setRead(collectionDirUploads.canRead());
													imageCollection.getPermissions().add(myPermission);
												}
											}
										}
									}
									catch (JsonSyntaxException e)
									{
										// If the JSON file is incorrectly formatted, throw an error and return an empty list
										SanimalData.getInstance().getErrorDisplay().showPopup(
												Alert.AlertType.ERROR,
												null,
												"Error",
												"JSON collection error",
												"Could not read the collection " + collectionJSONFile + "!\n" + ExceptionUtils.getStackTrace(e),
												false);
									}
								}
							}
						}
					}
				}
				else
				{
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"Collection error",
							"Collections folder not found on CyVerse!\n",
							false);
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"JSON collection download error",
						"Could not pull the collection list from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}

		return imageCollections;
	}

	/**
	 * Connects to CyVerse and uploads the given collection to CyVerse's data store
	 *
	 * @param collection The list of new species to upload
	 */
	public void pushLocalCollection(ImageCollection collection, StringProperty messageCallback)
	{
		if (this.sessionManager.openSession())
		{
			// Check if we are the owner of the collection
			String ownerUsername = collection.getOwner();
			if (ownerUsername != null && ownerUsername.equals(SanimalData.getInstance().getUsername()))
			{
				try
				{
					IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);

					// The name of the collection directory is the UUID of the collection
					String collectionDirName = COLLECTIONS_DIRECTORY + "/" + collection.getID().toString();

					// Create the directory, and set the permissions appropriately
					IRODSFile collectionDir = fileFactory.instanceIRODSFile(collectionDirName);
					if (!collectionDir.exists())
						collectionDir.mkdir();
					this.setFilePermissions(collectionDirName, collection.getPermissions(), false, false);

					if (messageCallback != null)
						messageCallback.setValue("Writing collection JSON file...");

					// Create a collections JSON file to hold the settings
					String collectionJSONFile = collectionDirName + "/collection.json";
					String json = SanimalData.getInstance().getGson().toJson(collection);
					this.writeRemoteFile(collectionJSONFile, json);
					// Set the file's permissions. We force read only so that even users with write permissions cannot change this file
					this.setFilePermissions(collectionJSONFile, collection.getPermissions(), true, false);

					if (messageCallback != null)
						messageCallback.setValue("Writing permissions JSON file...");

					// Create a permissions JSON file to hold the permissions
					String collectionPermissionFile = collectionDirName + "/permissions.json";
					json = SanimalData.getInstance().getGson().toJson(collection.getPermissions());
					this.writeRemoteFile(collectionPermissionFile, json);

					if (messageCallback != null)
						messageCallback.setValue("Writing collection Uploads directory...");

					// Create the folder containing uploads, and set its permissions
					IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDirName + "/Uploads");
					if (!collectionDirUploads.exists())
						collectionDirUploads.mkdir();
					this.setFilePermissions(collectionDirUploads.getAbsolutePath(), collection.getPermissions(), false, true);
				}
				catch (JargonException e)
				{
					e.printStackTrace();
				}
			}

			this.sessionManager.closeSession();
		}
	}

	/**
	 * Removes a collection from CyVerse's system
	 *
	 * @param collection The collection to delete from CyVerse
	 */
	public void removeCollection(ImageCollection collection)
	{
		if (this.sessionManager.openSession())
		{
			// The name of the collection to remove
			String collectionsDirName = COLLECTIONS_DIRECTORY + "/" + collection.getID().toString();
			try
			{
				// If it exists, delete it
				IRODSFile collectionDir = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount).instanceIRODSFile(collectionsDirName);
				if (collectionDir.exists())
					collectionDir.delete();
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Deletion error",
						"Could not delete the collection from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Sets the file permission for a file on the CyVerse system
	 *
	 * @param fileName The name of the file to update permissions of
	 * @param permissions The list of permissions to set
	 * @param forceReadOnly If the highest level of permission should be READ not WRITE
	 * @param recursive If the permissions are to be recursive
	 * @throws JargonException Thrown if something goes wrong in the Jargon library
	 */
	private void setFilePermissions(String fileName, ObservableList<Permission> permissions, boolean forceReadOnly, boolean recursive) throws JargonException
	{
		// Create the file, and remove all permissions from it
		IRODSFile file = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount).instanceIRODSFile(fileName);
		this.removeAllFilePermissions(file);
		// If the file is a directory, set the directory permissions
		if (file.isDirectory())
		{
			// Go through each non-owner permission
			CollectionAO collectionAO = this.sessionManager.getCurrentAO().getCollectionAO(this.authenticatedAccount);
			permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
				try
				{
					// If the user can upload, and we're not forcing read only, set the permission to write
					if (permission.canUpload() && !forceReadOnly)
						collectionAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername(), recursive);
					// If the user can read set the permission to write
					else if (permission.canRead())
						collectionAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername(), recursive);
				}
				catch (JargonException e)
				{
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"Permission error",
							"Error setting permissions for user!\n" + ExceptionUtils.getStackTrace(e),
							false);
				}
			});
		}
		// File permissions are done differently, so do that here
		else if (file.isFile())
		{
			DataObjectAO dataObjectAO = this.sessionManager.getCurrentAO().getDataObjectAO(this.authenticatedAccount);
			// Go through each permission and set the file permissions
			permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
				try
				{
					// If the user can upload, and we're not forcing read only, set the permission to write
					if (permission.canUpload() && !forceReadOnly)
						dataObjectAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername());
						// If the user can read set the permission to write
					else if (permission.canRead())
						dataObjectAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername());
				}
				catch (JargonException e)
				{
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"Permission error",
							"Error setting permissions for user!\n" + ExceptionUtils.getStackTrace(e),
							false);
				}
			});
		}
	}

	/**
	 * Removes all file permissions except the owner
	 *
	 * @param file The file to remove permission from
	 * @throws JargonException Thrown if something goes wrong in the Jargon library
	 */
	private void removeAllFilePermissions(IRODSFile file) throws JargonException
	{
		// Directories are done differently than files, so test this first
		if (file.isDirectory())
		{
			// If it's a collection, we list all permission for the folder
			CollectionAndDataObjectListingEntry collectionPermissions = this.sessionManager.getCurrentAO().getCollectionAndDataObjectListAndSearchAO(this.authenticatedAccount).getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(file.getAbsolutePath());
			CollectionAO collectionAO = this.sessionManager.getCurrentAO().getCollectionAO(this.authenticatedAccount);
			// We go through each permission, and remove all access permissions from that user
			collectionPermissions.getUserFilePermission().forEach(userFilePermission -> {
				if (userFilePermission.getFilePermissionEnum() != FilePermissionEnum.OWN)
					try
					{
						collectionAO.removeAccessPermissionForUser(ZONE, file.getAbsolutePath(), userFilePermission.getUserName(), true);
					}
					catch (JargonException e)
					{
						SanimalData.getInstance().getErrorDisplay().showPopup(
								Alert.AlertType.ERROR,
								null,
								"Error",
								"Permission error",
								"Error removing permissions from user!\n" + ExceptionUtils.getStackTrace(e),
								false);
					}
			});
		}
		else if (file.isFile())
		{
			// If it's a file, we list all permission for the file
			DataObjectAO dataObjectAO = this.sessionManager.getCurrentAO().getDataObjectAO(this.authenticatedAccount);
			// We go through each permission, and remove all access permissions from that user
			dataObjectAO.listPermissionsForDataObject(file.getAbsolutePath()).forEach(userFilePermission -> {
				if (userFilePermission.getFilePermissionEnum() != FilePermissionEnum.OWN)
					try
					{
						dataObjectAO.removeAccessPermissionsForUser(ZONE, file.getAbsolutePath(), userFilePermission.getUserName());
					}
					catch (JargonException e)
					{
						SanimalData.getInstance().getErrorDisplay().showPopup(
								Alert.AlertType.ERROR,
								null,
								"Error",
								"Permission error",
								"Error removing permissions from user!\n" + ExceptionUtils.getStackTrace(e),
								false);
					}
			});
		}
	}

	/**
	 * Test to see if the given username is valid on the CyVerse system
	 *
	 * @param username The username to test
	 * @return True if the username exists on CyVerse, false otherwise
	 */
	public Boolean isValidUsername(String username)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				User byName = this.sessionManager.getCurrentAO().getUserAO(this.authenticatedAccount).findByName(username);
				// Grab the user object for a given name, if it's null, it doesn't exist!
				this.sessionManager.closeSession();
				return byName != null;
			}
			catch (JargonException ignored)
			{
			}
			this.sessionManager.closeSession();
		}
		return false;
	}

	/**
	 * Uploads a set of images to CyVerse
	 *
	 * @param collection The collection to upload to
	 * @param directoryToWrite The directory to write
	 * @param transferCallback The callback that will receive callbacks if the transfer is in progress
	 * @param messageCallback Optional message callback that will show what is currently going on
	 */
	public void uploadImages(ImageCollection collection, ImageDirectory directoryToWrite, TransferStatusCallbackListener transferCallback, StringProperty messageCallback)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Grab the uploads folder for a given collection
				String collectionUploadDirStr = COLLECTIONS_DIRECTORY + "/" + collection.getID().toString() + "/Uploads";
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile collectionUploadDir = fileFactory.instanceIRODSFile(collectionUploadDirStr);
				// If the uploads directory exists and we can write to it, upload
				if (collectionUploadDir.exists() && collectionUploadDir.canWrite())
				{
					if (messageCallback != null)
						messageCallback.setValue("Creating upload folder on CyVerse...");

					// Create a new folder for the upload, we will use the current date as the name plus our username
					String uploadFolderName = FOLDER_FORMAT.format(new Date(this.sessionManager.getCurrentAO().getEnvironmentalInfoAO(this.authenticatedAccount).getIRODSServerCurrentTime())) + " " + SanimalData.getInstance().getUsername();
					String uploadDirName = collectionUploadDirStr + "/" + uploadFolderName;

					if (messageCallback != null)
						messageCallback.setValue("Creating TAR file out of the directory before uploading...");

					// Create the JSON file representing the upload
					Integer imageCount = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).count());
					Integer imagesWithSpecies = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry && !((ImageEntry) imageContainer).getSpeciesPresent().isEmpty()).count());
					CloudUploadEntry uploadEntry = new CloudUploadEntry(SanimalData.getInstance().getUsername(), LocalDateTime.now(), imagesWithSpecies, imageCount, uploadDirName);
					// Convert the upload entry to JSON format
					String json = SanimalData.getInstance().getGson().toJson(uploadEntry);
					// Create the UploadMeta.json
					File directoryMetaJSON = SanimalData.getInstance().getTempDirectoryManager().createTempFile("UploadMeta.json");
					directoryMetaJSON.createNewFile();
					try (PrintWriter out = new PrintWriter(directoryMetaJSON))
					{
						out.println(json);
					}

					// Create the meta.csv representing the metadata for all images in the tar file
					String localDirAbsolutePath = directoryToWrite.getFile().getAbsolutePath();
					String localDirName = directoryToWrite.getFile().getName();
					AvuData collectionIDTag = new AvuData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");

					// Make a set of tar files from the image files. Don't use a single tar file because we may have > 1000 images in each
					File[] tarsToWrite = DirectoryManager.directoryToTars(directoryToWrite, directoryMetaJSON, imageEntry ->
					{
						try
						{
							// Compute the image's "cyverse" path
							String fileRelativePath = localDirName + StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), localDirAbsolutePath);
							fileRelativePath = fileRelativePath.replace('\\', '/');
							List<AvuData> imageMetadata = imageEntry.convertToAVUMetadata();
							imageMetadata.add(collectionIDTag);
							return fileRelativePath + "," + imageMetadata.stream().map(avuData -> avuData.getAttribute() + "," + avuData.getValue() + "," + avuData.getUnit()).collect(Collectors.joining(",")) + "\n";
						}
						catch (JargonException e)
						{
							SanimalData.getInstance().getErrorDisplay().printError("Could not add metadata to image: " + imageEntry.getFile().getAbsolutePath() + ", error was: ");
							e.printStackTrace();
						}
						return "";
					}, 900);

					// For each tar part, upload
					for (Integer tarPart = 0; tarPart < tarsToWrite.length; tarPart++)
					{
						if (messageCallback != null)
							messageCallback.setValue("Uploading TAR file part (" + (tarPart + 1) + " / " + tarsToWrite.length + ") to CyVerse...");

						File toWrite = tarsToWrite[tarPart];
						File localToUpload = new File(FilenameUtils.getFullPath(toWrite.getAbsolutePath()) + uploadFolderName + "-" + tarPart.toString() + "." + FilenameUtils.getExtension(toWrite.getAbsolutePath()));
						toWrite.renameTo(localToUpload);
						// Upload the tar
						this.sessionManager.getCurrentAO().getDataTransferOperations(this.authenticatedAccount).putOperation(localToUpload, collectionUploadDir, transferCallback, null);

						localToUpload.delete();
					}
					// Let rules do the rest!
				}
			}
			catch (JargonException | IOException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Upload error",
						"Could not upload the images to CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Save the set of images that were downloaded to CyVerse
	 *
	 * @param collection The collection to upload to
	 * @param uploadEntryToSave The directory to write
	 * @param messageCallback Message callback that will show what is currently going on
	 */
	public void saveImages(ImageCollection collection, CloudUploadEntry uploadEntryToSave, StringProperty messageCallback)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Grab the save folder for a given collection
				String collectionSaveDirStr = COLLECTIONS_DIRECTORY + "/" + collection.getID().toString() + "/Uploads";
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile collectionSaveDir = fileFactory.instanceIRODSFile(collectionSaveDirStr);
				// If the save directory exists and we can write to it, save
				if (collectionSaveDir.exists() && collectionSaveDir.canWrite())
				{
					// Grab the image directory to save
					ImageDirectory imageDirectory = uploadEntryToSave.getCloudImageDirectory();
					// Grab the list of images to upload
					List<CloudImageEntry> toUpload = imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof CloudImageEntry).map(imageContainer -> (CloudImageEntry) imageContainer).collect(Collectors.toList());
					Platform.runLater(() -> imageDirectory.setUploadProgress(0.0));

					messageCallback.setValue("Saving " + toUpload.size() + " images to CyVerse...");

					Double numberOfImagesToUpload = (double) toUpload.size();
					Integer numberOfDetaggedImages = 0;
					Integer numberOfRetaggedImages = 0;
					// Begin saving
					for (int i = 0; i < toUpload.size(); i++)
					{
						// Grab the cloud image entry to upload
						CloudImageEntry cloudImageEntry = toUpload.get(i);
						// If it has been pulled save it
						if (cloudImageEntry.hasBeenPulledFromCloud() && cloudImageEntry.isCloudDirty())
						{
							if (cloudImageEntry.getSpeciesPresent().isEmpty() && cloudImageEntry.wasTaggedWithSpecies())
								numberOfDetaggedImages++;
							else if (!cloudImageEntry.getSpeciesPresent().isEmpty() && !cloudImageEntry.wasTaggedWithSpecies())
								numberOfRetaggedImages++;

							// Save that specific cloud image
							this.sessionManager.getCurrentAO().getDataTransferOperations(this.authenticatedAccount).putOperation(cloudImageEntry.getFile(), cloudImageEntry.getCyverseFile(), new TransferStatusCallbackListener()
							{
								@Override
								public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) { return FileStatusCallbackResponse.CONTINUE; }
								@Override
								public void overallStatusCallback(TransferStatus transferStatus) {}
								@Override
								public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection) { return CallbackResponse.YES_FOR_ALL; }
							}, null);

							// Get the absolute path of the uploaded file
							String fileAbsoluteCyVersePath = cloudImageEntry.getCyverseFile().getAbsolutePath();
							// Update the collection tag
							AvuData collectionIDTag = new AvuData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");
							// Write image metadata to the file
							List<AvuData> imageMetadata = cloudImageEntry.convertToAVUMetadata();
							imageMetadata.add(collectionIDTag);
							imageMetadata.forEach(avuData ->
							{
								try
								{
									// Set the file AVU data
									this.sessionManager.getCurrentAO().getDataObjectAO(this.authenticatedAccount).setAVUMetadata(fileAbsoluteCyVersePath, avuData);
								}
								catch (JargonException e)
								{
									SanimalData.getInstance().getErrorDisplay().printError("Could not add metadata to image: " + cloudImageEntry.getCyverseFile().getAbsolutePath() + ", error was: ");
									e.printStackTrace();
								}
							});

							// Update the progress every 20 uploads
							if (i % 20 == 0)
							{
								int finalI = i;
								Platform.runLater(() -> imageDirectory.setUploadProgress(finalI / numberOfImagesToUpload));
							}
						}
					}

					// Add an edit comment so users know the file was edited
					uploadEntryToSave.getEditComments().add("Edited by " + SanimalData.getInstance().getUsername() + " on " + FOLDER_FORMAT.format(Calendar.getInstance().getTime()));
					Integer imagesWithSpecies = uploadEntryToSave.getImagesWithSpecies() - numberOfDetaggedImages + numberOfRetaggedImages;
					uploadEntryToSave.setImagesWithSpecies(imagesWithSpecies);
					// Convert the upload entry to JSON format
					String json = SanimalData.getInstance().getGson().toJson(uploadEntryToSave);
					// Write the UploadMeta.json file to the server
					this.writeRemoteFile(uploadEntryToSave.getUploadIRODSPath() + "/UploadMeta.json", json);
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Saving error",
						"Could not save the image list to the collection on CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Used to retrieve a list of uploads to a collection and any uploads are automatically inserted into the collection
	 *
	 * @param collection The image collection to retrieve uploads from
	 * @param progressProperty How far we are
	 */
	public void retrieveAndInsertUploadList(ImageCollection collection, DoubleProperty progressProperty)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Clear the current collection uploads
				Platform.runLater(() -> collection.getUploads().clear());
				// Grab the uploads folder for a given collection
				String collectionUploadDirStr = COLLECTIONS_DIRECTORY + "/" + collection.getID().toString() + "/Uploads";
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile collectionUploadDir = fileFactory.instanceIRODSFile(collectionUploadDirStr);
				// If the uploads directory exists and we can read it, read
				if (collectionUploadDir.exists() && collectionUploadDir.canRead())
				{
					File[] files = collectionUploadDir.listFiles(File::isDirectory);
					double totalFiles = files.length;
					int numDone = 0;
					for (File file : files)
					{
						progressProperty.setValue(++numDone / totalFiles);
						// We recognize uploads by their UploadMeta.json file
						String contents = this.readRemoteFile(file.getAbsolutePath() + "/UploadMeta.json");
						if (contents != null)
						{
							try
							{
								// Download the cloud upload entry
								CloudUploadEntry uploadEntry = SanimalData.getInstance().getGson().fromJson(contents, CloudUploadEntry.class);
								if (uploadEntry != null)
								{
									uploadEntry.initFromJSON();
									Platform.runLater(() -> collection.getUploads().add(uploadEntry));
								}
							}
							catch (JsonSyntaxException e)
							{
								// If the JSON file is incorrectly formatted, throw an error
								SanimalData.getInstance().getErrorDisplay().showPopup(
										Alert.AlertType.ERROR,
										null,
										"Error",
										"JSON upload error",
										"Could not read the upload metadata for the upload " + file.getName() + "!\n" + ExceptionUtils.getStackTrace(e),
										false);
							}
						}
					}
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Upload retrieval error",
						"Could not download the list of uploads to the collection from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Given a collection and an upload to that collection this method returns the local cloud image directory
	 *
	 * @param uploadEntry The upload in the collection to download
	 * @return A local version of the uploadEntry
	 */
	public CloudImageDirectory downloadUploadDirectory(CloudUploadEntry uploadEntry)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Grab the uploads folder for a given collection
				String cloudDirectoryStr = uploadEntry.getUploadIRODSPath();
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile cloudDirectory = fileFactory.instanceIRODSFile(cloudDirectoryStr);
				CloudImageDirectory cloudImageDirectory = new CloudImageDirectory(cloudDirectory);
				this.createDirectoryAndImageTree(cloudImageDirectory);
				// We need to make sure we remove the UploadMeta.json "image entry"
				cloudImageDirectory.getChildren().removeIf(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).getCyverseFile().getAbsolutePath().contains("UploadMeta.json"));
				this.sessionManager.closeSession();
				return cloudImageDirectory;
			}
			catch (JargonException e)
			{
				e.printStackTrace();
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Download failed",
						"Downloading uploaded collection failed!",
						false);
			}
			this.sessionManager.closeSession();
		}

		return null;
	}

	/**
	 * Recursively create the directory structure
	 *
	 * @param current
	 *            The current directory to work on
	 */
	private void createDirectoryAndImageTree(CloudImageDirectory current)
	{
		IRODSFile[] subFiles = (IRODSFile []) current.getCyverseDirectory().listFiles((dir, name) -> true);

		if (subFiles != null)
		{
			// Get all files in the directory
			for (IRODSFile file : subFiles)
			{
				// Add all image files to the directory
				if (!file.isDirectory())
				{
					current.addImage(new CloudImageEntry(file));
				}
				// Add all subdirectories to the directory
				else
				{
					CloudImageDirectory subDirectory = new CloudImageDirectory(file);
					current.addChild(subDirectory);
					this.createDirectoryAndImageTree(subDirectory);
				}
			}
		}
	}

	/**
	 * Performs a query given a cyverseQuery object and returns a list of image paths that correspond with the query
	 *
	 * @param queryBuilder The query builder with all specified options
	 * @return A list of image CyVerse paths instead of local paths
	 */
	public List<String> performQuery(CyVerseQuery queryBuilder)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Convert the query builder to a query generator
				IRODSGenQueryFromBuilder query = queryBuilder.build().exportIRODSQueryFromBuilder(this.sessionManager.getCurrentAO().getJargonProperties().getMaxFilesAndDirsQueryMax());
				// Perform the query, and get a set of results
				IRODSGenQueryExecutor irodsGenQueryExecutor = this.sessionManager.getCurrentAO().getIRODSGenQueryExecutor(this.authenticatedAccount);
				IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);

				List<String> matchingFilePaths = new ArrayList<>();

				// Iterate while more results exist
				do
				{
					// Grab each row
					for (IRODSQueryResultRow resultRow : resultSet.getResults())
					{
						// Get the path to the image and the image name, create an absolute path with the info
						String pathToImage = resultRow.getColumn(0);
						String imageName = resultRow.getColumn(1);
						matchingFilePaths.add(pathToImage + "/" + imageName);
					}

					// Need this test to avoid NoMoreResultsException
					if (resultSet.isHasMoreRecords())
					{
						// Move the result set on if there's more records
						IRODSQueryResultSet nextResultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
						// Close the current result set
						irodsGenQueryExecutor.closeResults(resultSet);
						// Advance the "pointer" to the next result set
						resultSet = nextResultSet;
					}
				} while (resultSet.isHasMoreRecords());
				this.sessionManager.closeSession();
				return matchingFilePaths;
			}
			catch (JargonQueryException | JargonException | NumberFormatException | GenQueryBuilderException e)
			{
				e.printStackTrace();
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Query failed",
						"Query caused an exception!",
						false);
			}
			this.sessionManager.closeSession();
		}

		return Collections.emptyList();
	}

	/**
	 * Given a list of CyVerse absolute paths, this fetches the metadata for each image and returns it as an image entry
	 *
	 * @param absoluteIRODSPaths The list of absolute iRODS paths on CyVerse
	 * @return A list of images with metadata on CyVerse
	 */
	public List<ImageEntry> fetchMetadataFor(List<String> absoluteIRODSPaths)
	{
		List<ImageEntry> toReturn = new ArrayList<>();

		if (this.sessionManager.openSession())
		{
			// A unique list of species and locations is used to ensure images with identical locations don't create two locations
			List<Location> uniqueLocations = new LinkedList<>();
			List<Species> uniqueSpecies = new LinkedList<>();
			try
			{
				// We will fill in these various fields from the image metadata
				LocalDateTime localDateTime;
				String locationName;
				String locationID;
				Double locationLatitude;
				Double locationLongitude;
				Double locationElevation;
				// Map species IDs to metadata entries
				Map<Integer, String> speciesIDToCommonName = new HashMap<>();
				Map<Integer, String> speciesIDToScientificName = new HashMap<>();
				Map<Integer, Integer> speciesIDToCount = new HashMap<>();
				UUID collectionID = null;

				for (String irodsAbsolutePath : absoluteIRODSPaths)
				{
					localDateTime = LocalDateTime.MIN;
					locationName = "";
					locationID = "";
					locationLatitude = 0D;
					locationLongitude = 0D;
					locationElevation = 0D;
					speciesIDToCommonName.clear();
					speciesIDToScientificName.clear();
					speciesIDToCount.clear();

					// Perform a second query that returns ALL metadata from a given image
					for (MetaDataAndDomainData fileDataField : this.sessionManager.getCurrentAO().getDataObjectAO(this.authenticatedAccount).findMetadataValuesForDataObject(irodsAbsolutePath))
					{
						// Test what type of attribute we got, if it's important store the result for later
						switch (fileDataField.getAvuAttribute())
						{
							case SanimalMetadataFields.A_DATE_TIME_TAKEN:
								Long timeTaken = Long.parseLong(fileDataField.getAvuValue());
								localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeTaken), ZoneId.systemDefault());
								break;
							case SanimalMetadataFields.A_LOCATION_NAME:
								locationName = fileDataField.getAvuValue();
								break;
							case SanimalMetadataFields.A_LOCATION_ID:
								locationID = fileDataField.getAvuValue();
								break;
							case SanimalMetadataFields.A_LOCATION_LATITUDE:
								locationLatitude = Double.parseDouble(fileDataField.getAvuValue());
								break;
							case SanimalMetadataFields.A_LOCATION_LONGITUDE:
								locationLongitude = Double.parseDouble(fileDataField.getAvuValue());
								break;
							case SanimalMetadataFields.A_LOCATION_ELEVATION:
								locationElevation = Double.parseDouble(fileDataField.getAvuValue());
								break;
							case SanimalMetadataFields.A_SPECIES_COMMON_NAME:
								speciesIDToCommonName.put(Integer.parseInt(fileDataField.getAvuUnit()), fileDataField.getAvuValue());
								break;
							case SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME:
								speciesIDToScientificName.put(Integer.parseInt(fileDataField.getAvuUnit()), fileDataField.getAvuValue());
								break;
							case SanimalMetadataFields.A_SPECIES_COUNT:
								speciesIDToCount.put(Integer.parseInt(fileDataField.getAvuUnit()), Integer.parseInt(fileDataField.getAvuValue()));
								break;
							case SanimalMetadataFields.A_COLLECTION_ID:
								collectionID = UUID.fromString(fileDataField.getAvuValue());
								break;
							default:
								break;
						}
					}

					// Grab the collection that this image is a part of
					UUID finalCollectionID = collectionID;
					Optional<ImageCollection> correctCollection = SanimalData.getInstance().getCollectionList().stream().filter(imageCollection -> imageCollection.getID().equals(finalCollectionID)).findFirst();
					if (correctCollection.isPresent())
					{
						// Grab the collection if it's present (it should never not be present)
						ImageCollection imageCollection = correctCollection.get();
						// Get the permission for my own account to this collection
						Optional<Permission> myPermissions = imageCollection.getPermissions().stream().filter(permission -> permission.getUsername().equals(SanimalData.getInstance().getUsername())).findFirst();
						if (myPermissions.isPresent())
						{
							// If I can't upload I must only be able to read, so round the query results as asked for by Sue, may need to change this in the future
							Permission permission = myPermissions.get();
							if (!permission.canUpload())
							{
								locationLatitude = RoundingUtils.round(locationLatitude, 2);
								locationLongitude = RoundingUtils.round(locationLongitude, 2);
							}
						}
					}

					// Compute a new location if we need to
					String finalLocationID = locationID;
					Boolean locationForImagePresent = uniqueLocations.stream().anyMatch(location -> location.getId().equals(finalLocationID));
					// Do we have the location?
					if (!locationForImagePresent)
						uniqueLocations.add(new Location(locationName, locationID, locationLatitude, locationLongitude, locationElevation));
					// Compute a new species (s) if we need to
					for (Integer key : speciesIDToScientificName.keySet())
					{
						// Grab the scientific name of the species
						String speciesScientificName = speciesIDToScientificName.get(key);
						// Grab the common name of the species
						String speciesName = speciesIDToCommonName.get(key);
						// Test if the species is present, if not add it
						Boolean speciesForImagePresent = uniqueSpecies.stream().anyMatch(species -> species.getScientificName().equalsIgnoreCase(speciesScientificName));
						if (!speciesForImagePresent)
							uniqueSpecies.add(new Species(speciesName, speciesScientificName, Species.DEFAULT_ICON));
					}

					// Grab the correct location for the image entry
					Location correctLocation = uniqueLocations.stream().filter(location -> location.getId().equals(finalLocationID)).findFirst().get();
					// Create the image entry
					ImageEntry entry = new ImageEntry(new File(irodsAbsolutePath));
					// Set the location and date taken
					entry.setLocationTaken(correctLocation);
					entry.setDateTaken(localDateTime);
					// Add the species to the image entries
					for (Integer key : speciesIDToScientificName.keySet())
					{
						String speciesScientificName = speciesIDToScientificName.get(key);
						Integer speciesCount = speciesIDToCount.get(key);
						// Grab the species based on ID
						Species correctSpecies = uniqueSpecies.stream().filter(species -> species.getScientificName().equals(speciesScientificName)).findFirst().get();
						entry.addSpecies(correctSpecies, speciesCount);
					}
					toReturn.add(entry);
				}
			}
			catch (JargonException | NumberFormatException e)
			{
				e.printStackTrace();
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Query failed",
						"Query caused an exception!",
						false);
			}
			this.sessionManager.closeSession();
		}

		return toReturn;
	}

	/**
	 * Downloads a CyVerse file to a local file
	 *
	 * @param cyverseFile The file in CyVerse to download
	 * @return The local file
	 */
	public File remoteToLocalImageFile(IRODSFile cyverseFile)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				// Grab the name of the CyVerse file
				String fileName = cyverseFile.getName();
				// Create a temporary file to write to with the same name
				File localImageFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile(fileName);

				// Download the file locally
				this.sessionManager.getCurrentAO().getDataTransferOperations(this.authenticatedAccount).getOperation(cyverseFile, localImageFile, new TransferStatusCallbackListener()
				{
					@Override
					public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) { return FileStatusCallbackResponse.CONTINUE; }
					@Override
					public void overallStatusCallback(TransferStatus transferStatus) {}
					@Override
					public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection) { return CallbackResponse.YES_FOR_ALL; }
				}, null);

				this.sessionManager.closeSession();
				return localImageFile;
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"JSON error",
						"Could not pull the remote file (" + cyverseFile.getName() + ")!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
			this.sessionManager.closeSession();
		}

		return null;
	}

	/**
	 * Reads a file from CyVerse assuming a user is already logged in
	 *
	 * @param file The path to the file to read
	 * @return The contents of the file on CyVerse's system as a string
	 */
	private String readRemoteFile(String file)
	{
		try
		{
			IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
			// Create a temporary file to write to
			File localFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile("sanimalTemp." + FilenameUtils.getExtension(file));
			// Delete the temporary file before copying so that we don't need to specify overwriting
			localFile.delete();
			// Create the remote file instance
			IRODSFile remoteFile = fileFactory.instanceIRODSFile(file);
			// Ensure it exists
			if (remoteFile.exists())
			{
				// Ensure it can be read
				if (remoteFile.canRead())
				{
					this.sessionManager.getCurrentAO().getDataTransferOperations(this.authenticatedAccount).getOperation(remoteFile, localFile, null, null);
					// Ensure that the file exists and transfered
					if (localFile.exists())
					{
						// Read the contents of the file and return them
						return new String(Files.readAllBytes(localFile.toPath()));
					}
				}
				else
				{
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.ERROR,
							null,
							"Error",
							"Permission error",
							"Could not read the remote file!",
							false);
				}
			}
		}
		catch (IOException | JargonException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"CyVerse error",
					"Could not pull the remote file!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}

		// If anything fails return null
		return null;
	}

	/**
	 * Write a value to a file on the CyVerse server
	 *
	 * @param file  The file to write to
	 * @param value The string value to write to the file
	 */
	private void writeRemoteFile(String file, String value)
	{
		// Create a temporary file to write each location to before uploading
		try
		{
			IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
			// Create a local file to write to
			File localFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile("sanimalTemp." + FilenameUtils.getExtension(file));
			localFile.createNewFile();
			// Ensure the file we made exists
			if (localFile.exists())
			{
				// Create the irods file to write to
				IRODSFile remoteLocationFile = fileFactory.instanceIRODSFile(file);

				// Create a file writer which writes a string to a file. Write the value to the local file
				try (PrintWriter fileWriter = new PrintWriter(localFile))
				{
					fileWriter.write(value);
				}
				// Perform a put operation to write the local file to the CyVerse server
				this.sessionManager.getCurrentAO().getDataTransferOperations(this.authenticatedAccount).putOperation(localFile, remoteLocationFile, new TransferStatusCallbackListener()
				{
					@Override
					public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) { return FileStatusCallbackResponse.CONTINUE; }
					@Override
					public void overallStatusCallback(TransferStatus transferStatus) {}
					@Override
					public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection) { return CallbackResponse.YES_FOR_ALL; }
				}, null);
			}
			else
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"File error",
						"Error creating a temporary file to write to!",
						false);
			}
		}
		catch (IOException | JargonException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Permission error",
					"Error pushing remote file (" + file + ")!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}
}
