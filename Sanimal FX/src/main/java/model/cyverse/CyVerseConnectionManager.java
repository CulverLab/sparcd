package model.cyverse;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import model.SanimalData;
import model.image.DirectoryManager;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.*;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private User userUser;
	private User publicUser;

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

				// Grab the two public users (the "user" user and the "public" user)
				this.userUser = this.accessObjects.getUserAO().findByName("users");
				this.publicUser = this.accessObjects.getUserAO().findByName("public");

				// We're good, return true
				return true;
			}
			else
			{
				// If the authentication failed, print a message, and logout in case the login partially completed
				System.out.println("Authentication failed. Response was: " + authResponse.getAuthMessage());
			}
		}
		// If the authentication failed, print a message, and logout in case the login partially completed
		catch (InvalidUserException | AuthenticationException e)
		{
			System.out.println("Authentication failed!");
		}
		// If the authentication failed due to a jargon exception, print a message, and logout in case the login partially completed
		// Not really sure how this happens, probably if the server incorrectly responds or is down
		catch (JargonException e)
		{
			System.err.println("Unknown Jargon Exception. Error was:\n");
			e.printStackTrace();
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
					System.err.println("Could not read species.json. The file might be incorrectly formatted...");
					e.printStackTrace();
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
					System.err.println("Could not read locations.json. The file might be incorrectly formatted...");
					e.printStackTrace();
				}
			}
		}
		catch (JargonException e)
		{
			System.err.println("Error initializing Sanimal directory. Error was:\n");
			e.printStackTrace();
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
											IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDir.getAbsolutePath() + "/Uploads");
											if (collectionDirUploads.exists())
											{
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
									System.out.println("Error reading the Json file " + collectionJSONFile + ", Error was:\n");
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
		}
		catch (JargonException e)
		{
			e.printStackTrace();
		}

		return imageCollections;
	}

	/**
	 * Connects to CyVerse and uploads the given collection to CyVerse's data store
	 *
	 * @param collection The list of new species to upload
	 */
	public void pushLocalCollection(ImageCollection collection)
	{
		String collectionsDir = "/iplant/home/dslovikosky/Sanimal/Collections";
		IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
		// Check if we are the owner of the collection
		String ownerUsername = collection.getOwner();
		if (ownerUsername != null && ownerUsername.equals(SanimalData.getInstance().getUsername()))
		{
			try
			{
				String collectionDirName = collectionsDir + "/" + collection.getID().toString();

				IRODSFile collectionDir = fileFactory.instanceIRODSFile(collectionDirName);
				if (!collectionDir.exists())
					collectionDir.mkdir();
				this.setFilePermissions(collectionDirName, collection.getPermissions(), false);

				String collectionJSONFile = collectionDirName + "/collection.json";
				String json = SanimalData.getInstance().getGson().toJson(collection);
				this.writeRemoteFile(collectionJSONFile, json);
				this.setFilePermissions(collectionJSONFile, collection.getPermissions(), true);

				String collectionPermissionFile = collectionDirName + "/permissions.json";
				json = SanimalData.getInstance().getGson().toJson(collection.getPermissions());
				this.writeRemoteFile(collectionPermissionFile, json);

				IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDirName + "/Uploads");
				if (!collectionDirUploads.exists())
					collectionDirUploads.mkdir();
				this.setFilePermissions(collectionDirUploads.getAbsolutePath(), collection.getPermissions(), false);
			}
			catch (JargonException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void removeCollection(ImageCollection collection)
	{
		String collectionsDirName = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString();
		try
		{
			IRODSFile collectionDir = this.accessObjects.getFileFactory().instanceIRODSFile(collectionsDirName);
			if (collectionDir.exists())
				collectionDir.delete();
		}
		catch (JargonException e)
		{
			e.printStackTrace();
			System.out.println("Error deleting collection: " + collection.getName());
		}
	}

	private void setFilePermissions(String fileName, ObservableList<Permission> permissions, boolean forceReadOnly) throws JargonException
	{
		IRODSFile file = this.accessObjects.getFileFactory().instanceIRODSFile(fileName);
		this.removeAllFilePermissions(file);
		if (file.isDirectory())
		{
			CollectionAO collectionAO = this.accessObjects.getCollectionAO();
			permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
				try
				{
					if (permission.canUpload() && !forceReadOnly)
						collectionAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername(), false);
					else if (permission.canRead())
						collectionAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername(), false);
				}
				catch (JargonException e)
				{
					System.err.println("Can't set user permissions???");
					e.printStackTrace();
				}
			});
		}
		else if (file.isFile())
		{
			DataObjectAO dataObjectAO = this.accessObjects.getDataObjectAO();
			permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
				try
				{
					if (permission.canUpload() && !forceReadOnly)
						dataObjectAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername());
					else if (permission.canRead())
						dataObjectAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername());
				}
				catch (JargonException e)
				{
					System.err.println("Can't set user permissions???");
					e.printStackTrace();
				}
			});
		}
	}

	private void removeAllFilePermissions(IRODSFile file) throws JargonException
	{
		if (file.isDirectory())
		{
			CollectionAndDataObjectListingEntry collectionPermissions = this.accessObjects.getCollectionAndDataObjectListAndSearchAO().getCollectionAndDataObjectListingEntryAtGivenAbsolutePath(file.getAbsolutePath());
			CollectionAO collectionAO = this.accessObjects.getCollectionAO();
			collectionPermissions.getUserFilePermission().forEach(userFilePermission -> {
				if (userFilePermission.getFilePermissionEnum() != FilePermissionEnum.OWN)
					try
					{
						collectionAO.removeAccessPermissionForUser(ZONE, file.getAbsolutePath(), userFilePermission.getUserName(), true);
					}
					catch (JargonException e)
					{
						System.err.println("Error removing permissions from user.");
						e.printStackTrace();
					}
			});
		}
		else if (file.isFile())
		{
			DataObjectAO dataObjectAO = this.accessObjects.getDataObjectAO();
			dataObjectAO.listPermissionsForDataObject(file.getAbsolutePath()).forEach(userFilePermission -> {
				if (userFilePermission.getFilePermissionEnum() != FilePermissionEnum.OWN)
					try
					{
						dataObjectAO.removeAccessPermissionsForUser(ZONE, file.getAbsolutePath(), userFilePermission.getUserName());
					}
					catch (JargonException e)
					{
						System.err.println("Error removing permissions from user.");
						e.printStackTrace();
					}
			});
		}
	}

	public Boolean isValidUsername(String username)
	{
		try
		{
			return this.accessObjects.getUserAO().findByName(username) != null;
		}
		catch (JargonException ignored)
		{
		}
		return false;
	}

	public void uploadImages(ImageCollection collection, ImageDirectory directoryToWrite, TransferStatusCallbackListener transferCallback)
	{
		try
		{
			String collectionUploadDirStr = "/iplant/home/dslovikosky/Sanimal/Collections/" + collection.getID().toString() + "/Uploads";
			IRODSFileFactory fileFactory = this.accessObjects.getFileFactory();
			IRODSFile collectionUploadDir = fileFactory.instanceIRODSFile(collectionUploadDirStr);
			if (collectionUploadDir.exists() && collectionUploadDir.canWrite())
			{
				String uploadFolderName = FOLDER_FORMAT.format(new Date(this.accessObjects.getEnvironmentalInfoAO().getIRODSServerCurrentTime())) + " " + SanimalData.getInstance().getUsername();
				String uploadDirName = collectionUploadDirStr + "/" + uploadFolderName;

				IRODSFile uploadDir = fileFactory.instanceIRODSFile(uploadDirName);
				uploadDir.mkdir();

				// Make a tar file from the image files
				File toWrite = DirectoryManager.directoryToTar(directoryToWrite);

				if (toWrite != null)
				{
					this.accessObjects.getDataTransferOperations().putOperation(toWrite, uploadDir, transferCallback, null);
					this.accessObjects.getBulkFileOperationsAO().extractABundleIntoAnIrodsCollection(uploadDirName + "/" + toWrite.getName(), uploadDirName, "");
					IRODSFile uploadedFile = this.accessObjects.getFileFactory().instanceIRODSFile(uploadDirName + "/" + toWrite.getName());
					if (uploadedFile.exists())
						uploadedFile.delete();
				}
			}
		}
		catch (JargonException e)
		{
			e.printStackTrace();
			System.out.println("Upload failed!");
		}
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
			Path localPath = Files.createTempFile("sanimalTemp", "." + FilenameUtils.getExtension(file));
			File localFile = localPath.toFile();
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
					// Delete the local file after exiting
					localFile.deleteOnExit();
					// Ensure that the file exists and transfered
					if (localFile.exists())
					{
						// Read the contents of the file and return them
						return new String(Files.readAllBytes(localFile.toPath()));
					}
				} else
				{
					System.err.println("Remote file cannot be read.\n");
				}
			} else
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
			Path localPath = Files.createTempFile("sanimalTemp", "." + FilenameUtils.getExtension(file));
			// Grab the file from the path
			File localFile = localPath.toFile();
			// Delete the file once the program exits
			localFile.deleteOnExit();
			// Ensure the file we made exists
			if (localFile.exists())
			{
				// Create the irods file to write to
				IRODSFile remoteLocationFile = fileFactory.instanceIRODSFile(file);
				// If it exists already, delete it
				if (remoteLocationFile.exists())
					remoteLocationFile.delete();

				// Create a file writer which writes a string to a file. Write the value to the local file
				try (PrintWriter fileWriter = new PrintWriter(localFile))
				{
					fileWriter.write(value);
				}
				// Perform a put operation to write the local file to the CyVerse server
				this.accessObjects.getDataTransferOperations().putOperation(localFile, remoteLocationFile, null, null);
			} else
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
}
