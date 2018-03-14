package model.cyverse;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.SanimalData;
import model.image.*;
import model.location.Location;
import model.species.Species;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
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

	// Start all the Access Objects we use to accesss the user's account
	private CyVerseAOs accessObjects;

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
				account = authResponse.getAuthenticatedIRODSAccount();

				// Setup all access objects to the account (Represented by the AO at the end of the class name)
				this.accessObjects = new CyVerseAOs(irodsAO, account);

				// We're good, return true
				return true;
			}
			else
			{
				// If the authentication failed, print a message, and logout in case the login partially completed
				SanimalData.getInstance().getErrorDisplay().printError("Authentication failed. Response was: " + authResponse.getAuthMessage());
			}
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
		try
		{
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();

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
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"JSON error",
						"Could not pull the location list from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
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
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"JSON error",
						"Could not pull the species list from CyVerse!\n" + ExceptionUtils.getStackTrace(e),
						false);
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
		String collectionsFolderName = "/iplant/home/dslovikosky/Sanimal/Collections";
		List<ImageCollection> imageCollections = new ArrayList<>();
		try
		{
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile collectionsFolder = fileFactory.instanceIRODSFile(collectionsFolderName);
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

		return imageCollections;
	}

	/**
	 * Connects to CyVerse and uploads the given collection to CyVerse's data store
	 *
	 * @param collection The list of new species to upload
	 */
	public void pushLocalCollection(ImageCollection collection, StringProperty messageCallback)
	{
		String collectionsDir = "/iplant/home/dslovikosky/Sanimal/Collections";
		IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
		// Check if we are the owner of the collection
		String ownerUsername = collection.getOwner();
		if (ownerUsername != null && ownerUsername.equals(SanimalData.getInstance().getUsername()))
		{
			try
			{
				// The name of the collection directory is the UUID of the collection
				String collectionDirName = collectionsDir + "/" + collection.getID().toString();

				// Create the directory, and set the permissions appropriately
				IRODSFile collectionDir = fileFactory.instanceIRODSFile(collectionDirName);
				if (!collectionDir.exists())
					collectionDir.mkdir();
				this.setFilePermissions(collectionDirName, collection.getPermissions(), false);

				if (messageCallback != null)
					messageCallback.setValue("Writing collection JSON file...");

				// Create a collections JSON file to hold the settings
				String collectionJSONFile = collectionDirName + "/collection.json";
				String json = SanimalData.getInstance().getGson().toJson(collection);
				this.writeRemoteFile(collectionJSONFile, json);
				// Set the file's permissions. We force read only so that even users with write permissions cannot change this file
				this.setFilePermissions(collectionJSONFile, collection.getPermissions(), true);

				if (messageCallback != null)
					messageCallback.setValue("Writing permissions JSON file...");

				// Create a permissions JSON file to hold the permissions
				String collectionPermissionFile = collectionDirName + "/permissions.json";
				json = SanimalData.getInstance().getGson().toJson(collection.getPermissions());
				this.writeRemoteFile(collectionPermissionFile, json);

				if (messageCallback != null)
					messageCallback.setValue("Creating collection Uploads directory...");

				// Create the folder containing uploads, and set its permissions
				IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDirName + "/Uploads");
				if (!collectionDirUploads.exists())
					collectionDirUploads.mkdir();
				this.setFilePermissions(collectionDirUploads.getAbsolutePath(), collection.getPermissions(), false);
				this.accessObjects.getCollectionAO().setAccessPermissionInherit(ZONE, collectionDirUploads.getAbsolutePath(), true);
			}
			catch (JargonException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes a collection from CyVerse's system
	 * @param collection
	 */
	public void removeCollection(ImageCollection collection)
	{
		// The name of the collection to remove
		String collectionsDirName = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString();
		try
		{
			// If it exists, delete it
			IRODSFile collectionDir = this.accessObjects.getFileFactory().instanceIRODSFile(collectionsDirName);
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
	}

	/**
	 * Sets the file permission for a file on the CyVerse system
	 *
	 * @param fileName The name of the file to update permissions of
	 * @param permissions The list of permissions to set
	 * @param forceReadOnly If the highest level of permission should be READ not WRITE
	 * @throws JargonException Thrown if something goes wrong in the Jargon library
	 */
	private void setFilePermissions(String fileName, ObservableList<Permission> permissions, boolean forceReadOnly) throws JargonException
	{
		// Create the file, and remove all permissions from it
		IRODSFile file = this.accessObjects.getFileFactory().instanceIRODSFile(fileName);
		this.removeAllFilePermissions(file);
		// If the file is a directory, set the directory permissions
		if (file.isDirectory())
		{
			// Go through each non-owner permission
			CollectionAO collectionAO = this.accessObjects.getCollectionAO();
			permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
				try
				{
					// If the user can upload, and we're not forcing read only, set the permission to write
					if (permission.canUpload() && !forceReadOnly)
						collectionAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername(), false);
					// If the user can read set the permission to write
					else if (permission.canRead())
						collectionAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername(), false);
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
			DataObjectAO dataObjectAO = this.accessObjects.getDataObjectAO();
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
			CollectionAndDataObjectListingEntry collectionPermissions = this.accessObjects.getCollectionAndDataObjectListAndSearchAO().getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(file.getAbsolutePath());
			CollectionAO collectionAO = this.accessObjects.getCollectionAO();
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
			DataObjectAO dataObjectAO = this.accessObjects.getDataObjectAO();
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
		try
		{
			// Grab the user object for a given name, if it's null, it doesn't exist!
			return this.accessObjects.getUserAO().findByName(username) != null;
		}
		catch (JargonException ignored)
		{
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
		try
		{
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString() + "/Uploads";
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile collectionUploadDir = fileFactory.instanceIRODSFile(collectionUploadDirStr);
			// If the uploads directory exists and we can write to it, upload
			if (collectionUploadDir.exists() && collectionUploadDir.canWrite())
			{
				if (messageCallback != null)
					messageCallback.setValue("Creating upload folder on CyVerse...");

				// Create a new folder for the upload, we will use the current date as the name plus our username
				String uploadFolderName = FOLDER_FORMAT.format(new Date(this.accessObjects.getEnvironmentalInfoAO().getIRODSServerCurrentTime())) + " " + SanimalData.getInstance().getUsername();
				String uploadDirName = collectionUploadDirStr + "/" + uploadFolderName;

				// Make the directory to upload to
				IRODSFile uploadDir = fileFactory.instanceIRODSFile(uploadDirName);
				uploadDir.mkdir();

				if (messageCallback != null)
					messageCallback.setValue("Creating TAR file out of the directory before uploading...");

				// Make a tar file from the image files
				File toWrite = DirectoryManager.directoryToTar(directoryToWrite);

				// If the tar was created, upload it
				if (toWrite != null)
				{
					if (messageCallback != null)
						messageCallback.setValue("Uploading TAR file to CyVerse...");
					// Uplaod the tar
					this.accessObjects.getDataTransferOperations().putOperation(toWrite, uploadDir, transferCallback, null);
					if (messageCallback != null)
						messageCallback.setValue("Extracting TAR file on CyVerse into a directory...");
					// Extract the tar
					this.accessObjects.getBulkFileOperationsAO().extractABundleIntoAnIrodsCollectionWithForceOption(uploadDirName + "/" + toWrite.getName(), uploadDirName, "");
					if (messageCallback != null)
						messageCallback.setValue("Removing temporary TAR file...");
					// Remove the tar, since it was extracted by now
					IRODSFile uploadedFile = this.accessObjects.getFileFactory().instanceIRODSFile(uploadDirName + "/" + toWrite.getName());
					if (uploadedFile.exists())
						uploadedFile.delete();
					// Upload the JSON file representing the upload
					CloudUploadEntry uploadEntry = new CloudUploadEntry(SanimalData.getInstance().getUsername(), LocalDateTime.now(), true, uploadDirName);
					// Convert the upload entry to JSON format
					String json = SanimalData.getInstance().getGson().toJson(uploadEntry);
					// Write the UploadMeta.json file to the server
					this.writeRemoteFile(uploadDirName + "/UploadMeta.json", json);
				}
			}
		}
		catch (JargonException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Upload error",
					"Could not upload the images to CyVerse!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	/**
	 * Save the set of images that were downloaded to CyVerse
	 * @param collection The collection to upload to
	 * @param uploadEntryToSave The directory to write
	 * @param messageCallback Message callback that will show what is currently going on
	 */
	public void saveImages(ImageCollection collection, CloudUploadEntry uploadEntryToSave, StringProperty messageCallback)
	{
		try
		{
			// Grab the save folder for a given collection
			String collectionSaveDirStr = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString() + "/Uploads";
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile collectionSaveDir = fileFactory.instanceIRODSFile(collectionSaveDirStr);
			// If the save directory exists and we can write to it, save
			if (collectionSaveDir.exists() && collectionSaveDir.canWrite())
			{
				ImageDirectory imageDirectory = uploadEntryToSave.getCloudImageDirectory();
				List<CloudImageEntry> toUpload = imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof CloudImageEntry).map(imageContainer -> (CloudImageEntry) imageContainer).collect(Collectors.toList());
				imageDirectory.setUploadProgress(0.0);

				messageCallback.setValue("Saving " + toUpload.size() + " images to CyVerse...");

				uploadEntryToSave.getEditComments().add("Edited by " + SanimalData.getInstance().getUsername() + " on " + FOLDER_FORMAT.format(Calendar.getInstance().getTime()));
				// Convert the upload entry to JSON format
				String json = SanimalData.getInstance().getGson().toJson(uploadEntryToSave);
				// Write the UploadMeta.json file to the server
				this.writeRemoteFile(uploadEntryToSave.getUploadIRODSPath() + "/UploadMeta.json", json);

				Double numberOfImagesToUpload = (double) toUpload.size();
				// Begin saving
				for (int i = 0; i < toUpload.size(); i++)
				{
					CloudImageEntry cloudImageEntry = toUpload.get(i);
					if (cloudImageEntry.hasBeenPulledFromCloud())
					{
						// Save that specific cloud image
						this.accessObjects.getDataTransferOperations().putOperation(cloudImageEntry.getFile(), cloudImageEntry.getCyverseFile(), new TransferStatusCallbackListener()
						{
							@Override
							public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) { return FileStatusCallbackResponse.CONTINUE; }
							@Override
							public void overallStatusCallback(TransferStatus transferStatus) {}
							@Override
							public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection) { return CallbackResponse.YES_FOR_ALL; }
						}, null);

						if (i % 20 == 0)
						{
							int finalI = i;
							Platform.runLater(() -> imageDirectory.setUploadProgress(finalI / numberOfImagesToUpload));
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
					"Saving error",
					"Could not save the image list to the collection on CyVerse!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void retrieveAndInsertUploadList(ImageCollection collection)
	{
		try
		{
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString() + "/Uploads";
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile collectionUploadDir = fileFactory.instanceIRODSFile(collectionUploadDirStr);
			// If the uploads directory exists and we can read it, read
			if (collectionUploadDir.exists() && collectionUploadDir.canRead())
			{
				File[] files = collectionUploadDir.listFiles(File::isDirectory);
				for (File file : files)
				{
					// We recognize uploads by their UploadMeta.json file
					String contents = this.readRemoteFile(file.getAbsolutePath() + "/UploadMeta.json");
					if (contents != null)
					{
						try
						{
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
	}

	public CloudImageDirectory downloadUploadDirectory(ImageCollection collection, CloudUploadEntry uploadEntry)
	{
		try
		{
			// Grab the uploads folder for a given collection
			String cloudDirectoryStr = uploadEntry.getUploadIRODSPath();
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile cloudDirectory = fileFactory.instanceIRODSFile(cloudDirectoryStr);
			CloudImageDirectory cloudImageDirectory = new CloudImageDirectory(cloudDirectory);
			// The head directory gets the parent collection assigned to it
			cloudImageDirectory.setParentCollection(collection);
			this.createDirectoryAndImageTree(cloudImageDirectory);
			// We need to make sure we remove the UploadMeta.json "image entry"
			cloudImageDirectory.getChildren().removeIf(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).getCyverseFile().getAbsolutePath().contains("UploadMeta.json"));
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

	public File remoteToLocalImageFile(IRODSFile cyverseFile)
	{
		try
		{
			String fileName = cyverseFile.getName();
			File localImageFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile(fileName);

			this.accessObjects.getDataTransferOperations().getOperation(cyverseFile, localImageFile, new TransferStatusCallbackListener()
			{
				@Override
				public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) throws JargonException
				{
					return FileStatusCallbackResponse.CONTINUE;
				}

				@Override
				public void overallStatusCallback(TransferStatus transferStatus) throws JargonException
				{
				}

				@Override
				public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection)
				{
					return CallbackResponse.YES_FOR_ALL;
				}
			}, null);

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
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
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
					this.accessObjects.getDataTransferOperations().getOperation(remoteFile, localFile, null, null);
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
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
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
				this.accessObjects.getDataTransferOperations().putOperation(localFile, remoteLocationFile, new TransferStatusCallbackListener()
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
