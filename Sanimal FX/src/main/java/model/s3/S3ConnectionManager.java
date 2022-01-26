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
import model.cyverse.RetryTransferStatusCallbackListener;
import model.image.*;
import model.location.Location;
import model.query.CyVerseQuery;
import model.species.Species;
import model.util.RoundingUtils;
import model.util.SettingsData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A class used to wrap the S3 library
 */
public class S3ConnectionManager
{
	// String of our base folder
	private static final String ROOT_FOLDER = "Sanimal";
	// The name of the collections folder
	private static final String COLLECTIONS_FOLDER_NAME = "Collections";
	// The name of the Uploads folder
	private static final String UPLOADS_FOLDER_NAME = "Uploads";
	// The string of the settings folder path
	private static final String ROOT_SETTINGS_FOLDER = String.join("/", ROOT_FOLDER, "Settings");
	// The string of the collections folder path
	private static final String COLLECTIONS_FOLDER = String.join("/", ROOT_FOLDER, COLLECTIONS_FOLDER_NAME);
	// The name of the species file
	private static final String SPECIES_FILE = "species.json";
	// The name of the locations file
	private static final String LOCATION_FILE = "locations.json";
	// The name of the settings file
	private static final String SETTINGS_FILE = "settings.json";
	// The name of the collections JSON file
	private static final String COLLECTIONS_JSON_FILE = "collection.json";
	// Name of the collections permissions file
	private static final String COLLECTIONS_PERMISSIONS_FILE = "permissions.json";
	// Name of the Upload JSON file
	private static final String UPLOAD_JSON_FILE = "UploadMeta.json";

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
	private static final String FOLDER_TIMESTAMP_FORMAT = "uuuu.MM.dd.HH.mm.ss";

	private AmazonS3 s3Client; //authenticatedAccount;
	private S3SessionManager sessionManager;

	// Retry waiting variables
	private int retryWaitIndex = 0;
	private int[] retryWaitSeconds = {5, 30, 70, 180, 300};

	/**
	 * Given a URL, username and password, this method logs a S3 user in
	 *
	 * @param url The url endpoint to access
	 * @param username The username of the S3 account
	 * @param password The password of the S3 account
	 * @return True if the login was successful, false otherwise
	 */
	public Boolean login(String url, String username, String password)
	{
		Boolean success = true;		// Assume success

		try
		{
			AWSCredentials credentials = new BasicAWSCredentials(username, password);
			ClientConfiguration clientConfiguration = new ClientConfiguration();
			clientConfiguration.setSignerOverride("AWSS3V4SignerType");

			// Create a new S3 client instance
			this.s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

			// Store in a session manager
			this.sessionManager = new S3SessionManager(s3Client);

		}
		// If the authentication failed, print a message, and logout in case the login partially completed
		// Not really sure how this happens, probably if the server incorrectly responds or is down
		catch (AmazonServiceException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Authentication failed",
					"Could not authenticate the user!\n" + ExceptionUtils.getStackTrace(e),
					false);

			success = false;
		}
		// Return how successful we were
		return success;
	}

	/**
	 * This method initializes the remove sanimal directory stored on the users account.
	 */
	public void initSanimalRemoteDirectory()
	{
		try
		{
			// If the main Sanimal directory does not exist yet, create it
			if (!this.s3Client.doesBucketExistV2(ROOT_FOLDER)) {
				root_bucket = this.s3Client.createBucket(ROOT_FOLDER);

			// Create a subfolder containing all settings that the sanimal program stores
			if (!this.s3Client.doesBucketExistV2(ROOT_SETTINGS_FOLDER)) {
				root_bucket = this.s3Client.createBucket(ROOT_SETTINGS_FOLDER);

			// If we don't have a default species.json file, put a default one onto the storage location
			if (this.objectExists(ROOT_SETTINGS_FOLDER, SPECIES_FILE)) {
			{
				// Pull the default species.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/species.json"));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_SETTINGS_FOLDER, SPECIES_FILE, json);
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
			if (this.objectExists(ROOT_SETTINGS_FOLDER, LOCATIONS_FILE)) {
			{
				// Pull the default locations.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/locations.json"));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_SETTINGS_FOLDER, LOCATIONS_FILE, json);
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
			if (this.objectExists(ROOT_SETTINGS_FOLDER, SETTINGS_FILE)) {
			{
				// Pull the default settings.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/settings.json"));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_SETTINGS_FOLDER, SETTINGS_FILE, json);
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
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Initialization error",
					"Could not initialize the S3 directories!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	/**
	 * Connects to S3 and uploads the given settings into the settings.json file
	 *
	 * @param settingsData The new settings to upload
	 */
	public void pushLocalSettings(SettingsData settingsData)
	{
		// Convert the settings to JSON format
		String json = SanimalData.getInstance().getGson().toJson(settingsData);

		// Write the settings.json file to the server
		this.writeRemoteFile(ROOT_SETTINGS_FOLDER, SETTINGS_FILE, json);
	}

	/**
	 * Connects to S3 and downloads the user's settings
	 *
	 * @return User settings stored on the S3 system
	 */
	public SettingsData pullRemoteSettings()
	{
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(ROOT_SETTINGS_FOLDER, SETTINGS_FILE,);

		// Ensure that we in fact got data back
		if (fileContents != null)
		{
			// Try to parse the JSON string into a settings data
			try
			{
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
						"Could not pull the settings from S3!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
		}

		return null;
	}

	/**
	 * Connects to S3 and uploads the given list of lcations into the locations.json file
	 *
	 * @param newLocations The list of new locations to upload
	 */
	public void pushLocalLocations(List<Location> newLocations)
	{
		// Convert the location list to JSON format
		String json = SanimalData.getInstance().getGson().toJson(newLocations);

		// Write the locations.json file to the server
		this.writeRemoteFile(ROOT_SETTINGS_FOLDER, LOCATIONS_FILE, json);
	}

	/**
	 * Connects to S3 and downloads the list of the user's locations
	 *
	 * @return A list of locations stored on the S3 system
	 */
	public List<Location> pullRemoteLocations()
	{
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(ROOT_SETTINGS_FOLDER, LOCATIONS_FILE);

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
						"Could not pull the location list from S3!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Connects to S3 and uploads the given list of species into the species.json file
	 *
	 * @param newSpecies The list of new species to upload
	 */
	public void pushLocalSpecies(List<Species> newSpecies)
	{
		// Convert the species list to JSON format
		String json = SanimalData.getInstance().getGson().toJson(newSpecies);

		// Write the species.json file to the server
		this.writeRemoteFile(ROOT_SETTINGS_FOLDER, SPECIES_FILE, json);
	}

	/**
	 * Connects to S3 and downloads the list of the user's species list
	 *
	 * @return A list of species stored on the S3 system
	 */
	public List<Species> pullRemoteSpecies()
	{
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(ROOT_SETTINGS_FOLDER, SPECIES_FILE);

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
						"Could not pull the species list from S3!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
		}

		return Collections.emptyList();
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
		try
		{
			// Grab the collections folder and make sure it exists
			if (this.s3Client.doesBucketExistV2(COLLECTIONS_FOLDER)) {
			{
				// Grab a list of files in the collections directory
				List<String> files = this.listObjects(ROOT_FOLDER, COLLECTIONS_FOLDER_NAME);
				if (files.size() > 0)
				{
					// Iterate over all collections
					for (String collectionDir : files)
					{
						// Create the path to the collections JSON
						String collectionJSONFile = String.join("/", COLLECTIONS_FOLDER_NAME, collectionDir, COLLECTIONS_JSON_FILE)
						// If we have a collections JSON file, we parse the file
						if (this.objectExists(ROOT_FOLDER, collectionJSONFile)) {
						{
							// Read the collection JSON file to get the collection properties
							String collectionJSON = this.readRemoteFile(ROOT_FOLDER, collectionJSONFile);
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

										String permissionsJSONFile = String.join("/", COLLECTIONS_FOLDER_NAME, collectionDir, COLLECTIONS_PERMISSIONS_FILE);
										String permissionsJSON = this.readRemoteFile(ROOT_FOLDER, permissionsJSONFile);

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
											String uploadsFolder = String.join("/", COLLECTIONS_FOLDER_NAME, collectionDir, UPLOADS_FOLDER_NAME)
											// If we got a null permissions JSON, we check if we can see the uploads folder. If so, we have upload permissions!
											if (this.objectExists(ROOT_FOLDER, uploadsFolder)) {
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
						"Collections folder not found on S3!\n",
						false);
			}
		}
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"JSON collection download error",
					"Could not pull the collection list from S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}

		return imageCollections;
	}

	/**
	 * Connects to S3 and uploads the given collection
	 *
	 * @param collection The list of new species to upload
	 */
	public void pushLocalCollection(ImageCollection collection, StringProperty messageCallback)
	{
		// Check if we are the owner of the collection
		String ownerUsername = collection.getOwner();
		if (ownerUsername != null && ownerUsername.equals(SanimalData.getInstance().getUsername()))
		{
			try
			{
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);

				// The name of the collection directory is the UUID of the collection
				String collectionDirName = String.join("/", COLLECTIONS_FOLDER, collection.getID().toString());

				// Create the directory, and set the permissions appropriately
				if (!this.objectExists(ROOT_FOLDER, collectionDirName))
					this.createFolder(ROOT_FOLDER, collectionDirName);
				this.setFilePermissions(ROOT_FOLDER, collectionDirName, collection.getPermissions(), false, false);

				if (messageCallback != null)
					messageCallback.setValue("Writing collection JSON file...");

				// Create a collections JSON file to hold the settings
				String collectionJSONFile = String.join("/", collectionDirName, COLLECTIONS_JSON_FILE);
				String json = SanimalData.getInstance().getGson().toJson(collection);
				this.writeRemoteFile(ROOT_FOLDER, collectionJSONFile, json);
				// Set the file's permissions. We force read only so that even users with write permissions cannot change this file
				this.setFilePermissions(collectionJSONFile, collection.getPermissions(), true, false);

				if (messageCallback != null)
					messageCallback.setValue("Writing permissions JSON file...");

				// Create a permissions JSON file to hold the permissions
				String collectionPermissionFile = String.join("/", collectionDirName, COLLECTIONS_PERMISSIONS_FILE);
				json = SanimalData.getInstance().getGson().toJson(collection.getPermissions());
				this.writeRemoteFile(ROOT_FOLDER, collectionPermissionFile, json);

				if (messageCallback != null)
					messageCallback.setValue("Writing collection Uploads directory...");

				// Create the folder containing uploads, and set its permissions
				String collectionDirUpload = String.join("/", collectionDirName, "Uploads");
				if (!this.objectExists(ROOT_FOLDER, collectionDirUpload))
					this.createFolder(ROOT_FOLDER, collectionDirUpload);
				this.setFilePermissions(collectionDirUploads.getAbsolutePath(), collection.getPermissions(), false, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes a collection from S3's system
	 *
	 * @param collection The collection to delete from S3
	 */
	public void removeCollection(ImageCollection collection)
	{
		String delimiter = "/";

		// The name of the collection to remove
		String collectionsDirName = String.join(delimiter, COLLECTIONS_FOLDER, collection.getID().toString()) + delimiter;
		try
		{
			// If it exists, delete it
			if (this.objectExists(ROOT_FOLDER, collectionsDirName))
				this.deleteFolder(ROOT_FOLDER, collectionsDirName);
		}
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Deletion error",
					"Could not delete the collection from S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	/**
	 * Sets the file permission for a file on the S3 system
	 *
	 * @param bucket The bucket to access
	 * @param fileName The name of the file to update permissions of
	 * @param permissions The list of permissions to set
	 * @param forceReadOnly If the highest level of permission should be READ not WRITE
	 * @param recursive If the permissions are to be recursive
	 * @throws Exception Thrown if something goes wrong
	 */
	private void setFilePermissions(String bucket, String fileName, ObservableList<Permission> permissions, boolean forceReadOnly, boolean recursive) throws Exception
	{
		List<String> objectList;

		// Remove all permissions from it
		this.removeAllFilePermissions(bucket, fileName);

		// If the file is a directory, set the directory permissions
		if (this.folderExists(bucket, objectName))
		{
			objectList = this.listFolderObjects(ROOT_FOLDER, file.getUploadIRODSPath());
		}
		else if (this.objectExists(objectName))
		{
			objectList.add(file.getUploadIRODSPath());
		}

		// Set the permissions for all the objects
		for (String oneObject: objectList)
		{
			AccessControlList acl;

			try
			{
				permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
					// If the user can upload, and we're not forcing read only, set the permission to write
					if (permission.canUpload() && !forceReadOnly) {
						acl.grantPermission(EmailAddressGrantee(permission.getUsername), WRITE);
						
					}
					// Set the read permission
					if (permission.canRead()) {
						acl.grantPermission(EmailAddressGrantee(permission.getUsername), READ);
					}
				});

				if (acl.length() > 0)
				{
					this.s3Client.setObjectAcl(bucket, oneObject, acl);
				}
			}
			catch (Exception e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Permission error",
						"Error setting permissions for user!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
		}
	}

	/**
	 * Removes all file permissions except the owner
	 *
	 * @param bucket The bucket the object is in
	 * @param objectName The object to remove permission from
	 * @throws Exception Thrown if something goes wrong
	 */
	private void removeAllFilePermissions(String bucket, String objectName) throws Exception
	{
		List<String> objectList;

		// Directories are done differently than files, so test this first
		if (this.folderExists(bucket, objectName))
		{
			objectList = this.listFolderObjects(ROOT_FOLDER, file.getUploadIRODSPath());
		}
		else if (this.objectExists(objectName))
		{
			objectList.add(file.getUploadIRODSPath());
		}

		// Change the ACL for all objects we have
		for (String oneObject: objectList)
		{
			try
			{
				// Current set of ACLs
				AccessControlList acl = this.s3Client.getObjectAcl(ROOT_FOLDER, oneObject);

				Owner owner = acl.getOwner();

				// Remove everyone but the owner
				boolean removedAcl = false;
				for (Grant oneGrant: acl.getGrantsAsList())
				{
					if (oneGrant.getGrantee().getIdentifier() != owner.displayName())
					{
						acl.revokeAllPermissions(oneGrant.getGrantee());
						removedAcl = true;
					}
				}

				// Update the Object if we changed ACLs
				if (removedAcl == true)
				{
					this.s3Client.setObjectAcl(ROOT_FOLDER, oenObject, acl);
				}
			}
			catch (Exception e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Permission error",
						"Error removing permissions from user!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
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
//		try
//		{
//			User byName = this.sessionManager.getCurrentAO().getUserAO(this.authenticatedAccount).findByName(username);
//			// Grab the user object for a given name, if it's null, it doesn't exist!
//			this.sessionManager.closeSession();
//			return byName != null;
//		}
//		catch (Exception ignored)
//		{
//		}
//		return false;
		return true;
	}

	/**
	 * Uploads a set of images to CyVerse
	 *
	 * @param collection The collection to upload to
	 * @param directoryToWrite The directory to write
	 * @param description The description of the upload
	 * @param transferCallback The callback that will receive callbacks if the transfer is in progress
	 * @param messageCallback Optional message callback that will show what is currently going on
	 */
	public void uploadImages(ImageCollection collection, ImageDirectory directoryToWrite, String description, TransferStatusCallbackListener transferCallback, StringProperty messageCallback)
	{
		this.retryDelayReset();
		try
		{
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);

			// If the uploads directory exists and we can write to it, upload
			if (this.objectExists(ROOT_FOLDER, collectionUploadDirStr))
			{
				if (messageCallback != null)
					messageCallback.setValue("Creating upload folder on S3...");

				// Create a new folder for the upload, we will use the current date as the name plus our username
				String uploadFolderName = this.formatNowTimestamp(FOLDER_TIMESTAMP_FORMAT) + "_" + SanimalData.getInstance().getUsername();
				String uploadDirName = String.join("/", collectionUploadDirStr, uploadFolderName);

				if (messageCallback != null)
					messageCallback.setValue("Creating internal files before uploading...");

				// Create the JSON file representing the upload
				Integer imageCount = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).count());
				Integer imagesWithSpecies = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry && !((ImageEntry) imageContainer).getSpeciesPresent().isEmpty()).count());
				CloudUploadEntry uploadEntry = new CloudUploadEntry(SanimalData.getInstance().getUsername(), LocalDateTime.now(), imagesWithSpecies, imageCount, uploadDirName, description);
				// Convert the upload entry to JSON format
				String json = SanimalData.getInstance().getGson().toJson(uploadEntry);
				// Create the UploadMeta json file
				File directoryMetaJSON = SanimalData.getInstance().getTempDirectoryManager().createTempFile(UPLOAD_JSON_FILE);
				directoryMetaJSON.createNewFile();
				try (PrintWriter out = new PrintWriter(directoryMetaJSON))
				{
					out.println(json);
				}

				// Create the meta.csv representing the metadata for all images in the tar file
				String localDirAbsolutePath = directoryToWrite.getFile().getAbsolutePath();
				String localDirName = directoryToWrite.getFile().getName();
				AvuData collectionIDTag = new AvuData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");

				// List of images to be uploaded
				List<ImageEntry> imageEntries = directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList());

				// Create the meta data file to upload
				File metaCSV = SanimalData.getInstance().getTempDirectoryManager().createTempFile("meta.csv");
				metaCSV.createNewFile();
				this.createImageMetaFile(metaCSV, imageEntries, imageEntry ->
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
					catch (Exception e)
					{
						SanimalData.getInstance().getErrorDisplay().printError("Could not add metadata to image: " + imageEntry.getFile().getAbsolutePath() + ", error was: ");
						e.printStackTrace();
					}
					return "";
				});

				// Get initial list of files to upload
				File[] transferFiles = new File[imageEntries.size() + 2];
				Integer fileIndex = 0;
				for (; fileIndex < newTarNames.size(); fileIndex++)
				{
					transferFiles[fileIndex] = imageEntries[fileIndex];
				}
				transferFiles[fileIndex++] = directoryMetaJSON;
				transferFiles[fileIndex++] = metaCSV;

				// Transfer the files with retry attempts
				RetryTransferStatusCallbackListener retryListener = new RetryTransferStatusCallbackListener(transferCallback);
				boolean keepRetrying = true;
				do
				{
					// Make sure we clear failed file tracking
					retryListener.resetFailedFiles();

					// Loop through the list of files to upload
					for (Integer filePart = 0; filePart < transferFiles.length; filePart++)
					{
						if (messageCallback != null && filePart % 50 == 0)
							messageCallback.setValue("Uploading files (" + (filePart + 1) + " / " + transferFiles.length + ") to S3...");

						File toWrite = transferFiles[filePart];

						// Upload the file
						try
						{
							this.s3Client.putObject(new PutObjectRequest(ROOT_FOLDER, toWrite.getAbsolutePath(), toWrite));
						}
						catch (Exception e)
						{
							// Check if we're still trying to resend files or giving up because we've tried enough times
							if (!keepRetrying)
							{
								// Give up
								SanimalData.getInstance().getErrorDisplay().printError("Giving up on UploadImage retries");
								throw e;
							}

							retryListener.addFailedFile(toWrite.getAbsolutePath());
							messageCallback.setValue("Failed to upload TAR file part "+ (filePart + 1) + " to CyVerse.");

							// Add remaining files to failed list and break the loop
							for (Integer rem_part = filePart + 1; rem_part < transferFiles.length; rem_part++)
							{
								retryListener.addFailedFile(transferFiles[rem_part].getAbsolutePath());
							}
							break;
						}
					}

					// Get list of upload failures so we can handle them
					List<String> failedTransfers = retryListener.getFailedFiles();

					// If we have failed transfers, retry them
					if (failedTransfers.size() > 0)
					{
						List<File> failedFiles =  new ArrayList<File>();

						if (messageCallback != null)
							messageCallback.setValue("Retrying " + (failedTransfers.size()) + " files that failed to upload");

						// Find the failed files to make a new transfer list
						for (Integer filePart = 0; filePart < transferFiles.length; filePart++)
						{
							File toWrite = transferFiles[filePart];
							String localPath = toWrite.getAbsolutePath();
							if (failedTransfers.indexOf(localPath) >= 0)
							{
								// We have a match, store for trying again
								failedFiles.add(toWrite);
							}
						}

						// If we have failures, try again after a backoff period
						if (failedFiles.size() > 0)
						{
							// Provide the set of files to retry
							transferFiles = new File[failedFiles.size()];
							for (Integer filePart = 0; filePart < failedFiles.size(); filePart++)
							{
								transferFiles[filePart] = failedFiles.get(filePart);
							}

							// Sleep for the retry period
							keepRetrying = this.retryDelayWait();
						}
					}

				} while (retryListener.hasFailedFiles());
				// Let rules do the rest!

				// Remove local files
				directoryMetaJSON.delete();
				metaCSV.delete();
			}
		}
		catch (JargonException | IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Upload error",
					"Could not upload the images to S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
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
		try
		{
			// Grab the save folder for a given collection
			String collectionSaveDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);

			// If the save directory exists and we can write to it, save
			if (this.objecExists(ROOT_FOLDER, collectionsSaveDirStr))
			{
				// Grab the image directory to save
				ImageDirectory imageDirectory = uploadEntryToSave.getCloudImageDirectory();
				// Grab the list of images to upload
				List<CloudImageEntry> toUpload = imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof CloudImageEntry).map(imageContainer -> (CloudImageEntry) imageContainer).collect(Collectors.toList());
				Platform.runLater(() -> imageDirectory.setUploadProgress(0.0));

				messageCallback.setValue("Saving " + toUpload.size() + " images to S3...");

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
						this.s3Client.putObject(new PutObjectRequest(ROOT_FOLDER, cloudImageEntry.getCyverseFile().toString(), cloudImageEntry.getFile()));

						// Get the absolute path of the uploaded file
						String fileAbsoluteCyVersePath = cloudImageEntry.getCyverseFile().getAbsolutePath();
						// Update the collection tag
						AvuData collectionIDTag = new AvuData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");
						// Write image metadata to the file
/* Convert this to JSON and upload updated meta data
						List<AvuData> imageMetadata = cloudImageEntry.convertToAVUMetadata();
						imageMetadata.add(collectionIDTag);
						imageMetadata.forEach(avuData ->
						{
							try
							{
								// Set the file AVU data
								this.sessionManager.getCurrentAO().getDataObjectAO(this.authenticatedAccount).setAVUMetadata(fileAbsoluteCyVersePath, avuData);
							}
							catch (Exception e)
							{
								SanimalData.getInstance().getErrorDisplay().printError("Could not add metadata to image: " + cloudImageEntry.getCyverseFile().getAbsolutePath() + ", error was: ");
								e.printStackTrace();
							}
						});
*/
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
				// Write the UploadMeta json file to the server
				this.writeRemoteFile(String.join("/", uploadEntryToSave.getUploadIRODSPath(), UPLOAD_JSON_FILE), json);
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

	/**
	 * Used to retrieve a list of uploads to a collection and any uploads are automatically inserted into the collection
	 *
	 * @param collection The image collection to retrieve uploads from
	 * @param progressProperty How far we are
	 */
	public void retrieveAndInsertUploadList(ImageCollection collection, DoubleProperty progressProperty)
	{
		try
		{
			// Clear the current collection uploads
			Platform.runLater(() -> collection.getUploads().clear());
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);
			// If the uploads directory exists and we can read it, read
			if (this.folderExists(ROOT_FOLDER, collectionUploadDirStr))
			{
				List<String> files = this.listFolderObjects(ROOT_FOLDER, collectionUploadDirStr);
				double totalFiles = files.length;
				int numDone = 0;
				for (File file : files)
				{
					progressProperty.setValue(++numDone / totalFiles);
					// We recognize uploads by their UploadMeta json file
					String contents = this.readRemoteFile(ROOT_FOLDER, String.join("/", file, UPLOAD_JSON_FILE);
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
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Upload retrieval error",
					"Could not download the list of uploads to the collection from S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
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
		try
		{
			// Grab the uploads folder for a given collection
			String cloudDirectoryStr = uploadEntry.getUploadPath();
			CloudImageDirectory cloudImageDirectory = new CloudImageDirectory(cloudDirectoryStr);
			this.createDirectoryAndImageTree(cloudImageDirectory);

			// We need to make sure we remove the UploadMeta json "image entry"
			cloudImageDirectory.getChildren().removeIf(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).getCloudFile().getAbsolutePath().contains(UPLOAD_JSON_FILE));
			return cloudImageDirectory;
		}
		catch (Exception e)
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
		List<String> subFiles = this.listObject(ROOT_FOLDER, current.getCloudDirectory());

		if (subFiles.length() > 0)
		{
			// Get all files in the directory
			for (String file : subFiles)
			{
				// Add all image files to the directory
				if (!this.folderExists(file))
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
// Make this local to the JSON file
/*		try
		{
			// Convert the query builder to a query generator
			IRODSGenQueryFromBuilder query = queryBuilder.build().exportIRODSQueryFromBuilder(this.sessionManager.getCurrentAO().getJargonProperties().getMaxFilesAndDirsQueryMax());
			// Perform the query, and get a set of results
			IRODSGenQueryExecutor irodsGenQueryExecutor = this.sessionManager.getCurrentAO().getIRODSGenQueryExecutor(this.authenticatedAccount);
			IRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQuery(query, 0);
			IRODSQueryResultSet nextResultSet = null;

			List<String> matchingFilePaths = new ArrayList<>();
			
			// Don't bother looping unless we have something
			if (resultSet != null) {
				// Initialize for first pass through loop
				nextResultSet = resultSet;

				// Iterate while more results exist
				do
				{
					// Advance the "pointer" to the next result set
					resultSet = nextResultSet;
					
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
						nextResultSet = irodsGenQueryExecutor.getMoreResults(resultSet);
					}
				} while (resultSet.isHasMoreRecords());

				// Close the result set
				irodsGenQueryExecutor.closeResults(resultSet);
			}
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
*/
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
// Read from JSON file
		List<ImageEntry> toReturn = new ArrayList<>();
/*
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
					if (speciesCount == null) {
						  System.out.println("NULL COUNT: fetchMetadataFor: " + speciesScientificName + "  Count: " + speciesCount);
					  continue;
					}
					
					// Grab the species based on ID
					Species correctSpecies = uniqueSpecies.stream().filter(species -> species.getScientificName().equals(speciesScientificName)).findFirst().get();
					entry.addSpecies(correctSpecies, speciesCount);
				}
				toReturn.add(entry);
			}
		}
		catch (Exception | NumberFormatException e)
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
*/
		return toReturn;
	}

	/**
	 * Function used to download a list of images into a directory specified. Also takes a progress callback as an argument that that can be updated to
	 * show task progress
	 *
	 * @param absoluteImagePaths A list of absolute paths to download
	 * @param dirToSaveTo The directory to download into
	 * @param progressCallback A callback that can be updated to show download progress
	 */
	public void downloadImages(List<String> absoluteImagePaths, File dirToSaveTo, DoubleProperty progressCallback)
	{
		List<String> absoluteLocalFilePaths = absoluteImagePaths.stream().map(absoluteImagePath -> dirToSaveTo.getAbsolutePath() + File.separator + FilenameUtils.getName(absoluteImagePath)).collect(Collectors.toList());
		for (int i = 0; i < absoluteImagePaths.size(); i++)
		{
			String absoluteImagePath = absoluteImagePaths.get(i);
			String absoluteLocalFilePath = absoluteLocalFilePaths.get(i);
			File localFile = new File(absoluteLocalFilePath);

			// While the file exists, we update the path to have a new file name, and then re-create the local file
			while (localFile.exists())
			{
				// Use a random alphabetic character at the end of the file name to make sure the file name is unique
				absoluteLocalFilePath = absoluteLocalFilePath.replace(".", RandomStringUtils.randomAlphabetic(1) + ".");
				localFile = new File(absoluteLocalFilePath);
			}
			try
			{
				this.saveRemoteFile(ROOT_FOLDER, absoluteImagePath, localFile);
			}
			catch (Exception e)
			{
				System.out.println("There was an error downloading the image file, error was:\n" + ExceptionUtils.getStackTrace(e));
			}

			if (i % 10 == 0)
				progressCallback.setValue((double) i / absoluteImagePaths.size());
		}
	}

	/**
	 * Downloads an S3 file to a local file
	 *
	 * @param cloudFile The file in S3 to download
	 * @return The local file
	 */
	public File remoteToLocalImageFile(String cloudFile)
	{
		try
		{
			// Create a temporary file to write to with the same name
			File localImageFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile(cloudFile);

			// Download the file locally
			this.saveRemoteFile(ROOT_FOLDER, cyverseFile, localImageFile);

			return localImageFile;
		}
		catch (Exception e)
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
	 * Checks if a folder-like object exists in a bucket
	 * 
	 * @param bucket The path of the bucket to check
	 * @param folderPath The path of the folder-like object to look for
	 * @return Returns true if the object exists, and false if not
	 */
	private boolean folderExists(String bucket, String folderPath)
	{
		String prefix = "/";
		if (!folderPath.endswith(prefix))
		{
			folderPath += prefix;
		}

		return this.objectExists(bucket, folderPath);
    }

	/**
	 * Checks if an object exists in a bucket
	 * 
	 * @param bucket The path of the bucket to check
	 * @param objectName The name of the object to look for
	 * @return Returns true if the object exists, and false if not
	 */
	private boolean objectExists(String bucket, String objectName)
	{
		try
		{
			HeadObjectResponse sanimalSpeciesFile = this.s3Client.headObject(
				HeadObjectRequest.builder().bucket(bucket).key(objectName).build());
			return true;
		}
		catch (NoSuchKeyException e)
		{
        	return false;
    	}
    }

	/**
	 * Reads a file from S3 assuming a user is already logged in
	 *
	 * @param bucket The bucket to load the object from
	 * @param objectName The name of the Object to read
	 * @return The contents of the file on S3's system as a string
	 */
	private String readRemoteFile(String bucket, String objectName)
	{
		try
		{
			// Ensure it exists
			if (this.objectExists(bucket, objectName))
			{
	            GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucket, objectName);
	            S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

				// Read the contents of the file and return them
				return new String(objectPortion.getObjectContent());
			}
		}
		catch (AmazonServiceException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Permission error",
					"Could not read the remote file!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
		catch (AmazonClientException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"S3 error",
					"Could not pull the remote file!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}

		// If anything fails return null
		return null;
	}

	/**
	 * Reads a file from S3 assuming a user is already logged in
	 *
	 * @param bucket The bucket to load the object from
	 * @param objectName The name of the Object to read
	 * @param saveFile The file to save the download to
	 */
	private void saveRemoteFile(String bucket, String objectName, File saveFile)
	{
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucket, objectName);
        S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

		// Write the contents of the file
		FileOutputStream outputStream = new FileOutputStream(saveFile);
		outputStream.write(objectPortion.getObjectContent());
	}

	/**
	 * Write a value to a object on the S3 server
	 *
	 * @param bucket The path to the object to write
	 * @param objectName The name of the object to write
	 * @param value The string value to write to the file
	 */
	private void writeRemoteFile(String bucket, String objectName, String value)
	{
		// Create a temporary file to write each location to before uploading
		try
		{
			// Create a local file to write to
			File localFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile(
																	"sanimalTemp." + FilenameUtils.getExtension(file));
			localFile.createNewFile();

			// Ensure the file we made exists
			if (localFile.exists())
			{
				// Create a file writer which writes a string to a file. Write the value to the local file
				try (PrintWriter fileWriter = new PrintWriter(localFile))
				{
					fileWriter.write(value);
				}

	            // Upload file
	            this.s3Client.putObject(new PutObjectRequest(bucket, objectName, localFile));
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
		catch (IOException e)
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

	/**
	 * Returns a list of the objects in the prefix path of the bucket
	 * 
	 * @param bucket The path to the bucket to search
	 * @param prefix Additional path information for the search
	 * @return returns the list of found Objects 
	 */
	private List<String> listObjects(String bucket, String prefix)
	{
	    String delimiter = "/";
	    if (!prefix.endsWith(delimiter)) {
	        prefix += delimiter;
	    }

	    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
	            .withBucketName(bucket).withPrefix(prefix)
	            .withDelimiter(delimiter);
	    ObjectListing objects = this.s3Client.listObjects(listObjectsRequest);
	    return objects.getCommonPrefixes();
	}

	/**
	 * Returns a list of folder-type objects in the prefix path of the bucket
	 * 
	 * @param bucket The path to the bucket to search
	 * @param prefix Additional path information for the search
	 * @return returns the list of found folder-like Objects 
	 */
	private List<String> listFolderObjects(String bucket, String prefix)
	{
	    String delimiter = "/";
	    if (!prefix.endsWith(delimiter)) {
	        prefix += delimiter;
	    }

		List<String> allObjects = this.listObjects(buckets, prefix);

		List<String> folderObjects;
		for (String oneObject: allObjects)
		{
			if (oneObject.lastIndexOf(delimiter) > prefix.length())
			{
				folderObjects.add(oneObject);
			}
		}

		return folderObjects;
	}

	/**
	 * Creates an Object using the folder path
	 * 
	 * @param bucket The path to the bucket to create the folder path in
	 * @param folderPath The path of the folder to create
	 */
	private void createFolder(String bucket, String folderPath)
	{
	    String delimiter = "/";

        // Create metadata with content 0 
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0L);
        
        // Empty content
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        
        // Creates a PutObjectRequest by passing the folder name with the suffix
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folderName.toString() + delimiter, inputStream, metadata);
        
        //Send the request to s3 to create the folder
        this.s3Client.putObject(putObjectRequest);            
    }

	/**
	 * Removes all Objects using the folder path
	 * 
	 * @param bucket The path to the bucket to delete the folder path from
	 * @param folderPath The path of the folder to delete
	 */
	private void deleteFolder(String bucket, String folderPath)
	{
	    String delimiter = "/";

	    // Ensure a delimiter so we don't remove similarly named objects (starting the same: eg. 'boo' and 'booth')
	    if (!folderPath.endswith(delimeter))
	    	folderPath += delimiter;

	    // Get the list of objects starting with this path
	    ObjectListing objectList = this.s3Client.listObjects(bucket, folderPath);

	    // Prepare the key names for removal. Include the folder as well
	    List<S3ObjectSummary> objectSummaryList = objectList.getObjectSummaries();
	    String[] keysList = new String[objectSummaryList.size() + 1];	// Plus one, for the folder
	    int count = 0;
	    for (S3ObjectSummary summary : objectSummaryList)
	        keysList[count++] = summary.getKey();
	    keysList[count++] = folderPath.substring(0, folderPath.length() - 1);	// Remove trailing delimiter
	    
	    // Set up the delete request
	    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keysList);

	    // Delete the objects
	    this.s3Client.deleteObjects(deleteObjectsRequest);
	}

	/**
	 * Returns a formatted timestamp of the current time (now)
	 * 
	 * @param format The format string for the timestamp
	 * @return The formatted timestamp string
	 */
	private String formatNowTimestamp(String format)
	{
		return ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(FOLDER_TIMESTAMP_FORMAT)).toString();
	}

	/**
	 * Resets the retry wait variables
	 */
	private void retryDelayReset()
	{
		this.retryWaitIndex = 0;
	}

	/**
	 * Sleeps for the numbers of seconds specified by the retry index
	 *
	 * @return will return false if there are no more timeouts available (probably should stop trying) otherwise true is returned after sleep finishes
	 */
	private boolean retryDelayWait()
	{
		if (this.retryWaitIndex >= this.retryWaitSeconds.length)
		{
			return false;
		}

		int waitSeconds = this.retryWaitSeconds[this.retryWaitIndex];
		this.retryWaitIndex++;

		long start_timestamp = 0;
		long cur_timestamp = 0;
		do
		{
			start_timestamp = System.currentTimeMillis();
			try
			{
				TimeUnit.SECONDS.sleep(waitSeconds);
			}
			catch (InterruptedException ex)
			{
				// We're ignoring this exception since we handle interruptions by default (by retrying)
			}

			cur_timestamp = System.currentTimeMillis();

			waitSeconds -= (cur_timestamp - start_timestamp) / 1000;

		} while (waitSeconds > 0);

		return true;
	}

	/**
	 * Writes the metadata for images to a file
	 * 
	 * @param outFile The file to write to
	 * @param imageEntries The image entries to write
	 * @param imageToMetadata The CSV file representing each image's metadata
	 */
	private void createImageMetaFile(File outFile, List<ImageEntry> imageEntries, Function<ImageEntry, String> imageToMetadata)
	{
		PrintWriter metaOut = new PrintWriter(outFile);
		for (ImageEntry imageEntry: imageEntries)
		{
			// Write a metadata entry into our meta file
			metaOut.write(imageToMetadata.apply(imageEntry));
		}
		// Close the writer to the metadata file
		metaOut.close();
	}

}
