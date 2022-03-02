package model.s3;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.s3.RetryTransferStatusCallbackListener;
import model.image.*;
import model.location.Location;
import model.query.S3MetaDataAndDomainData;
import model.query.S3Query;
import model.query.S3QueryExecute;
import model.query.S3QueryResultRow;
import model.query.S3QueryResultSet;
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
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.security.InvalidParameterException;

/**
 * A class used to wrap the S3 library
 */
public class S3ConnectionManager
{
	// String of our base folder
	private static final String ROOT_BUCKET = "sanimal";
	// The name of the collections folder
	private static final String COLLECTIONS_FOLDER_NAME = "Collections";
	// The name of the Uploads folder
	private static final String UPLOADS_FOLDER_NAME = "Uploads";
	// The string of the settings folder path
	private static final String SETTINGS_FOLDER = "Settings";
	// The name of the species file
	private static final String SPECIES_FILE = "species.json";
	// The path of the species file
	private static final String SPECIES_FILE_PATH = String.join("/", SETTINGS_FOLDER, SPECIES_FILE);
	// The name of the locations file
	private static final String LOCATION_FILE = "locations.json";
	// The path of the locations file
	private static final String LOCATION_FILE_PATH = String.join("/", SETTINGS_FOLDER, LOCATION_FILE);
	// The name of the settings file
	private static final String SETTINGS_FILE = "settings.json";
	// The path of the settings file
	private static final String SETTINGS_FILE_PATH = String.join("/", SETTINGS_FOLDER, SETTINGS_FILE);
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
	private static final Type PERMISSION_LIST_TYPE = new TypeToken<ArrayList<model.s3.Permission>>()
	{
	}.getType();

	// Folder name formatting string
	private static final String FOLDER_TIMESTAMP_FORMAT = "uuuu.MM.dd.HH.mm.ss";

	private AmazonS3 s3Client; //authenticatedAccount;

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
			if (this.s3Client.doesBucketExistV2(ROOT_BUCKET) == false)
			{
				Bucket root_bucket = this.s3Client.createBucket(ROOT_BUCKET);
			}

			// Create a subfolder containing all settings that the sanimal program stores
			if (!this.folderExists(ROOT_BUCKET, SETTINGS_FOLDER))
			{
				this.createFolder(ROOT_BUCKET, SETTINGS_FOLDER);
			}

			// Create a subfolder to contain all the collections
			if (!this.folderExists(ROOT_BUCKET, COLLECTIONS_FOLDER_NAME))
			{
				this.createFolder(ROOT_BUCKET, COLLECTIONS_FOLDER_NAME);
			}

			// If we don't have a default species.json file, put a default one onto the storage location
			if (!this.objectExists(ROOT_BUCKET, SPECIES_FILE_PATH))
			{
				// Pull the default species.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/" + SPECIES_FILE));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_BUCKET, SPECIES_FILE_PATH, json);
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
			if (!this.objectExists(ROOT_BUCKET, LOCATION_FILE_PATH))
			{
				// Pull the default locations.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/" + LOCATION_FILE));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_BUCKET, LOCATION_FILE_PATH, json);
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
			if (!this.objectExists(ROOT_BUCKET, SETTINGS_FILE_PATH))
			{
				// Pull the default settings.json file
				try (InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getResourceAsStream("/" + SETTINGS_FILE));
					 BufferedReader fileReader = new BufferedReader(inputStreamReader))
				{
					// Read the Json file
					String json = fileReader.lines().collect(Collectors.joining("\n"));
					// Write it to the directory
					this.writeRemoteFile(ROOT_BUCKET, SETTINGS_FILE_PATH, json);
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
		this.writeRemoteFile(ROOT_BUCKET, SETTINGS_FILE_PATH, json);
	}

	/**
	 * Connects to S3 and downloads the user's settings
	 *
	 * @return User settings stored on the S3 system
	 */
	public SettingsData pullRemoteSettings()
	{
		// Read the contents of the file into a string
		System.out.println("pullRemoteSettings: " + ROOT_BUCKET + " -> " + SETTINGS_FILE_PATH);
		String fileContents = this.readRemoteFile(ROOT_BUCKET, SETTINGS_FILE_PATH);

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

		System.out.println("pullRemoteSettings: DONE");
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
		System.out.println("pushLocalLocations: '" + LOCATION_FILE_PATH + "'");
		this.writeRemoteFile(ROOT_BUCKET, LOCATION_FILE_PATH, json);
	}

	/**
	 * Connects to S3 and downloads the list of the user's locations
	 *
	 * @return A list of locations stored on the S3 system
	 */
	public List<Location> pullRemoteLocations()
	{
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(ROOT_BUCKET, LOCATION_FILE_PATH);

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
		this.writeRemoteFile(ROOT_BUCKET, SPECIES_FILE_PATH, json);
	}

	/**
	 * Connects to S3 and downloads the list of the user's species list
	 *
	 * @return A list of species stored on the S3 system
	 */
	public List<Species> pullRemoteSpecies()
	{
		// Read the contents of the file into a string
		String fileContents = this.readRemoteFile(ROOT_BUCKET, SPECIES_FILE_PATH);

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
	 * Connects to the cloud and downloads the list of the user's collections
	 *
	 * @return A list of collections stored on the cloud system
	 */
	public List<ImageCollection> pullRemoteCollections()
	{
		System.out.println("pullRemoteCollections(): BEGIN");
		// Create a list of collections
		List<ImageCollection> imageCollections = new ArrayList<>();
		try
		{
			// Grab the collections folder and make sure it exists
			if (this.folderExists(ROOT_BUCKET, COLLECTIONS_FOLDER_NAME))
			{
				// Grab a list of files in the collections directory
				List<String> files = this.listFolders(ROOT_BUCKET, COLLECTIONS_FOLDER_NAME);
				if (files.size() > 0)
				{
					// Iterate over all collections
					for (String collectionDir : files)
					{
						// Create the path to the collections JSON
						String collectionJSONFile = String.join("/", collectionDir, COLLECTIONS_JSON_FILE);
						// If we have a collections JSON file, we parse the file
						if (this.objectExists(ROOT_BUCKET, collectionJSONFile))
						{
							// Read the collection JSON file to get the collection properties
							String collectionJSON = this.readRemoteFile(ROOT_BUCKET, collectionJSONFile);
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

										String permissionsJSONFile = String.join("/", collectionDir, COLLECTIONS_PERMISSIONS_FILE);
										String permissionsJSON = this.readRemoteFile(ROOT_BUCKET, permissionsJSONFile);

										// This will be null if we can't see the upload directory
										if (permissionsJSON != null)
										{
											// Get the GSON object to parse the JSON.
											List<model.s3.Permission> permissions = SanimalData.getInstance().getGson().fromJson(permissionsJSON, PERMISSION_LIST_TYPE);
											if (permissions != null)
											{
												// We need to initialize the internal listeners because the deserialization process causes the fields to get wiped and reset
												permissions.forEach(model.s3.Permission::initListeners);
												imageCollection.getPermissions().addAll(permissions);
											}
										}
										else
										{
											// Grab the uploads directory
											String uploadsFolder = String.join("/", collectionDir, UPLOADS_FOLDER_NAME);
											System.out.println("    Checking uploads folder permissions -> '" + uploadsFolder + "'");
											// If we got a null permissions JSON, we check if we can see the uploads folder. If so, we have upload permissions!
											if (this.folderExists(ROOT_BUCKET, uploadsFolder))
											{
												System.out.println("    adding permissions");
												// Add a permission for my own permissions
												model.s3.Permission myPermission = new model.s3.Permission();
												myPermission.setOwner(false);
												myPermission.setUsername(SanimalData.getInstance().getUsername());
												myPermission.setUpload(this.canWriteFolder(ROOT_BUCKET, uploadsFolder));
												myPermission.setRead(this.canReadFolder(ROOT_BUCKET, uploadsFolder));
												imageCollection.getPermissions().add(myPermission);
											}
										}
									}
								}
								catch (JsonSyntaxException e)
								{
									System.out.println("pullRemoteCollections(): JSON exception -> " + e.getMessage());
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
			System.out.println("pullRemoteCollections(): top-level exception -> " + e.getMessage());
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"JSON collection download error",
					"Could not pull the collection list from S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}

		System.out.println("pullRemoteCollections(): DONE");
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
		System.out.println("pushLocalCollection(): owner -> " + ownerUsername + "  Username -> " + SanimalData.getInstance().getUsername());
		if (ownerUsername != null && ownerUsername.equals(SanimalData.getInstance().getUsername()))
		{
			try
			{
				// The name of the collection directory is the UUID of the collection
				String collectionDirName = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString());
				System.out.println("pushLocalCollection(): collectionDirName -> " + collectionDirName);

				// Create the directory, and set the permissions appropriately
				if (!this.folderExists(ROOT_BUCKET, collectionDirName))
					this.createFolder(ROOT_BUCKET, collectionDirName);
				System.out.println("pushLocalCollection(): Set collection dir ACL -> " + collection.getPermissions());
				this.setFilePermissions(ROOT_BUCKET, collectionDirName, collection.getPermissions(), false, false);

				if (messageCallback != null)
					messageCallback.setValue("Writing collection JSON file...");

				// Create a collections JSON file to hold the settings
				String collectionJSONFile = String.join("/", collectionDirName, COLLECTIONS_JSON_FILE);
				String json = SanimalData.getInstance().getGson().toJson(collection);
				System.out.println("pushLocalCollection(): collection JSON file -> '" + collectionJSONFile + "'  " + json);
				this.writeRemoteFile(ROOT_BUCKET, collectionJSONFile, json);
				// Set the file's permissions. We force read only so that even users with write permissions cannot change this file
				System.out.println("pushLocalCollection(): Set collection JSON file ACL -> " + collection.getPermissions());
				this.setFilePermissions(ROOT_BUCKET, collectionJSONFile, collection.getPermissions(), true, false);

				if (messageCallback != null)
					messageCallback.setValue("Writing permissions JSON file...");

				// Create a permissions JSON file to hold the permissions
				String collectionPermissionFile = String.join("/", collectionDirName, COLLECTIONS_PERMISSIONS_FILE);
				json = SanimalData.getInstance().getGson().toJson(collection.getPermissions());
				System.out.println("pushLocalCollection(): collection permissions file -> '" + collectionPermissionFile + "'  " + json);
				this.writeRemoteFile(ROOT_BUCKET, collectionPermissionFile, json);

				if (messageCallback != null)
					messageCallback.setValue("Writing collection Uploads directory...");

				// Create the folder containing uploads, and set its permissions
				String collectionDirUpload = String.join("/", collectionDirName, "Uploads");
				System.out.println("pushLocalCollection(): collection upload dir -> '" + collectionDirUpload + "'");
				if (!this.folderExists(ROOT_BUCKET, collectionDirUpload))
					this.createFolder(ROOT_BUCKET, collectionDirUpload);
				System.out.println("pushLocalCollection(): collection upload dir ACL -> '" + collection.getPermissions());
				this.setFilePermissions(ROOT_BUCKET, collectionDirUpload, collection.getPermissions(), false, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("pushLocalCollection(): DONE");
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
		String collectionsDirName = String.join(delimiter, COLLECTIONS_FOLDER_NAME, collection.getID().toString()) + delimiter;
		try
		{
			// If it exists, delete it
			if (this.folderExists(ROOT_BUCKET, collectionsDirName))
				this.deleteFolder(ROOT_BUCKET, collectionsDirName);
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
	 * @param fileName The path of the file to update permissions of
	 * @param permissions The list of permissions to set
	 * @param forceReadOnly If the highest level of permission should be READ not WRITE
	 * @param recursive If the permissions are to be recursive
	 * @throws Exception Thrown if something goes wrong
	 */
	private void setFilePermissions(String bucket, String fileName, ObservableList<model.s3.Permission> permissions, boolean forceReadOnly, boolean recursive) throws Exception
	{
/*		List<String> objectList = new ArrayList<String>();

		// Remove all permissions from it
		this.removeAllFilePermissions(bucket, fileName);

		// If the file is a directory, set the directory permissions
		if (this.folderExists(bucket, fileName))
		{
			objectList = this.listFolderObjects(ROOT_BUCKET, fileName);
		}
		else if (this.objectExists(bucket, fileName))
		{
			objectList.add(fileName);
		}

		// Set the permissions for all the objects
		for (String oneObject: objectList)
		{
			AccessControlList acl = new AccessControlList();

			try
			{
				permissions.filtered(permission -> !permission.isOwner()).forEach(permission -> {
					// If the user can upload, and we're not forcing read only, set the permission to write
					if (permission.canUpload() && !forceReadOnly) {
						acl.grantPermission(new EmailAddressGrantee(permission.getUsername()), Permission.Write);
						
					}
					// Set the read permission
					if (permission.canRead()) {
						acl.grantPermission(new EmailAddressGrantee(permission.getUsername()), Permission.Read);
					}
				});

				if (acl.getGrantsAsList().size() > 0)
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
*/	}

	/**
	 * Removes all file permissions except the owner
	 *
	 * @param bucket The bucket the object is in
	 * @param objectName The object to remove permission from
	 * @throws Exception Thrown if something goes wrong
	 */
	private void removeAllFilePermissions(String bucket, String objectName) throws Exception
	{
/*		List<String> objectList = new ArrayList<String>();

		// Directories are done differently than files, so test this first
		if (this.folderExists(bucket, objectName))
		{
			objectList = this.listFolderObjects(ROOT_BUCKET, objectName);
		}
		else if (this.objectExists(bucket, objectName))
		{
			objectList.add(objectName);
		}

		// Change the ACL for all objects we have
		for (String oneObject: objectList)
		{
			try
			{
				// Current set of ACLs
				AccessControlList acl = this.s3Client.getObjectAcl(ROOT_BUCKET, oneObject);

				Owner owner = acl.getOwner();

				// Remove everyone but the owner
				boolean removedAcl = false;
				for (Grant oneGrant: acl.getGrantsAsList())
				{
					if (oneGrant.getGrantee().getIdentifier() != owner.getDisplayName())
					{
						acl.revokeAllPermissions(oneGrant.getGrantee());
						removedAcl = true;
					}
				}

				// Update the Object if we changed ACLs
				if (removedAcl == true)
				{
					this.s3Client.setObjectAcl(ROOT_BUCKET, oneObject, acl);
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
*/	}

	/**
	 * Checks for write permission on a folder
	 *
	 * @param bucket The bucket the folder is in
	 * @param objectName The folder to check
	 */
	private boolean canWriteFolder(String bucket, String folderName)
	{
		System.out.println("canWriteFolder(): BEGIN");
		if (this.folderExists(bucket, folderName))
		{
			// Current set of ACLs
			AccessControlList acl = this.s3Client.getObjectAcl(bucket, folderName);
			for (Grant oneGrant: acl.getGrantsAsList())
			{
				if (Objects.equals(oneGrant.getPermission().toString(), "WRITE"))
				{
					return true;
				}
			}
		}

		// Default return
		return false;
	}

	/**
	 * Checks for read permission on a folder
	 *
	 * @param bucket The bucket the folder is in
	 * @param objectName The folder to check
	 */
	private boolean canReadFolder(String bucket, String folderName)
	{
		System.out.println("canReadFolder(): BEGIN");
		if (this.folderExists(bucket, folderName))
		{
			// Current set of ACLs
			AccessControlList acl = this.s3Client.getObjectAcl(bucket, folderName);
			for (Grant oneGrant: acl.getGrantsAsList())
			{
				if (Objects.equals(oneGrant.getPermission().toString(), "READ"))
				{
					return true;
				}
			}
		}

		// Default return
		return false;
	}

	/**
	 * Test to see if the given username is valid on the cloud system
	 *
	 * @param username The username to test
	 * @return True if the username exists on cloud, false otherwise
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
	 * Uploads a set of images to the cloud
	 *
	 * @param collection The collection to upload to
	 * @param directoryToWrite The directory to write
	 * @param description The description of the upload
	 * @param messageCallback Optional message callback that will show what is currently going on
	 */
	public void uploadImages(ImageCollection collection, ImageDirectory directoryToWrite, String description, StringProperty messageCallback)
	{
		this.retryDelayReset();
		System.out.println("uploadImages(): BEGIN -> description: '" + description + "'");
		try
		{
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);
			System.out.println("uploadImages(): collection upload dir: " + collectionUploadDirStr);

			// If the uploads directory exists and we can write to it, upload
			if (this.folderExists(ROOT_BUCKET, collectionUploadDirStr))
			{
				System.out.println("    upload dir exists");
				if (messageCallback != null)
					messageCallback.setValue("Creating upload folder on S3...");

				// Create a new folder for the upload, we will use the current date as the name plus our username
				String uploadFolderName = this.formatNowTimestamp(FOLDER_TIMESTAMP_FORMAT) + "_" + SanimalData.getInstance().getUsername();
				String uploadDirName = String.join("/", collectionUploadDirStr, uploadFolderName);
				System.out.println("uploadImages(): current upload dir name: " + uploadDirName);

				if (messageCallback != null)
					messageCallback.setValue("Creating internal files before uploading...");

				// Create the JSON file representing the upload
				Integer imageCount = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).count());
				Integer imagesWithSpecies = Math.toIntExact(directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry && !((ImageEntry) imageContainer).getSpeciesPresent().isEmpty()).count());
				System.out.println("  imageCount: " + imageCount + "   with species: " + imagesWithSpecies);
				CloudUploadEntry uploadEntry = new CloudUploadEntry(SanimalData.getInstance().getUsername(), ZonedDateTime.now(), imagesWithSpecies, imageCount, uploadDirName, description);
				System.out.println("uploadImages(): Post Cloud Upload Entry");

				// Folder for storing the metadata
				File metaFolder = SanimalData.getInstance().getTempDirectoryManager().createTempFolder("meta");
				System.out.println("uploadImages(): meta folder -> '" + metaFolder.getAbsolutePath() + "'");
				// Convert the upload entry to JSON format
				String json = SanimalData.getInstance().getGson().toJson(uploadEntry);
				System.out.println("uploadImages(): Post toJson()");
				// Create the UploadMeta json file
				File directoryMetaJSON = new File(String.join("/", metaFolder.getAbsolutePath(), UPLOAD_JSON_FILE));
				System.out.println("    JSON: folder -> '" + directoryMetaJSON.getAbsolutePath() + "' " + json);
				directoryMetaJSON.createNewFile();
				try (PrintWriter out = new PrintWriter(directoryMetaJSON))
				{
					out.println(json);
				}

				// Create the meta data files representing the metadata for all images in the tar file
				String localDirAbsolutePath = directoryToWrite.getFile().getAbsolutePath();
				String localDirName = directoryToWrite.getFile().getName();
				MetaData collectionIDTag = new MetaData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");
				System.out.println("uploadImages(): Local Path -> '" + localDirAbsolutePath + "'  dir -> '" + localDirName + "'");

				// List of images to be uploaded
				List<ImageEntry> imageEntries = directoryToWrite.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList());
				System.out.println("uploadImages(): image count -> " + imageEntries.size());

				// Create the meta data file to upload
				Camtrap metaCSV = new Camtrap();
				System.out.println("uploadImages(): Create image Meta entries");
				this.createImageMetaEntries(metaCSV, imageEntries, (imageEntry, metaCamtrap) ->
				{
					try
					{
						// Compute the image's cloud path
						String fileRelativePath = localDirName + StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), localDirAbsolutePath);
						fileRelativePath = fileRelativePath.replace('\\', '/');
						System.out.println("    fileRelativePath: '" + fileRelativePath + "'");
						List<MetaData> imageMetadata = imageEntry.convertToMetadata();
						imageMetadata.add(collectionIDTag);
						this.mapMetadataToCamtrap(imageMetadata, fileRelativePath, metaCamtrap);
					}
					catch (Exception e)
					{
						SanimalData.getInstance().getErrorDisplay().printError("Could not add metadata to image: " + imageEntry.getFile().getAbsolutePath() + ", error was: ");
						e.printStackTrace();
					}
				});

				// Save the meta data to the correct folder
				System.out.println("uploadImages(): Save meta data: '" + metaFolder.getAbsolutePath() + "'");
				metaCSV.saveTo(metaFolder.getAbsolutePath());
				System.out.println("uploadImages(): After saving meta data");

				// Get initial list of files to upload
				String[] metaFiles = metaCSV.getFilePaths(metaFolder.getAbsolutePath());
				File[] transferFiles = new File[imageEntries.size() + 1 + metaFiles.length];
				Integer fileIndex = 0;
				System.out.println("uploadImages(): Upload files");
				for (; fileIndex < imageEntries.size(); fileIndex++)
				{
					System.out.println("    i: " + imageEntries.get(fileIndex).getFile().getAbsolutePath());
					transferFiles[fileIndex] = imageEntries.get(fileIndex).getFile();
				}
				System.out.println("    d: " + directoryMetaJSON);
				transferFiles[fileIndex++] = directoryMetaJSON;
				for (String oneFile: metaFiles)
				{
					System.out.println("    m: " + oneFile);
					transferFiles[fileIndex++] = new File(oneFile);
				}
				System.out.println("uploadImages(): file transfer count: " + transferFiles.length);

				// Transfer the files with retry attempts
				RetryTransferStatusCallbackListener retryListener = new RetryTransferStatusCallbackListener();
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
						System.out.println("uploadImages(): transferring: " + toWrite.getAbsolutePath());

						// Upload the file
						try
						{
							String remotePath;
							if (toWrite.getAbsolutePath().startsWith(localDirAbsolutePath))
							{
								remotePath = String.join("/", uploadDirName, localDirName, FilenameUtils.getName(toWrite.getAbsolutePath()));
							}
							else
							{
								remotePath = String.join("/", uploadDirName, FilenameUtils.getName(toWrite.getAbsolutePath()));
							}
							System.out.println("    remotePath: " + remotePath);
							this.uploadFile(ROOT_BUCKET, remotePath, toWrite);
						}
						catch (Exception e)
						{
							// Check if we're still trying to resend files or giving up because we've tried enough times
							System.out.println("    exception: " + e.getMessage());
							if (!keepRetrying)
							{
								// Give up
								System.out.println("    giving up on uploads retries");
								SanimalData.getInstance().getErrorDisplay().printError("Giving up on UploadImage retries");
								throw e;
							}

							retryListener.addFailedFile(toWrite.getAbsolutePath());
							messageCallback.setValue("Failed to upload file to S3: " + toWrite.getAbsolutePath());
							System.out.println("    adding failed file to try again");

							// Add remaining files to failed list and break the loop
							for (Integer rem_part = filePart + 1; rem_part < transferFiles.length; rem_part++)
							{
								retryListener.addFailedFile(transferFiles[rem_part].getAbsolutePath());
								System.out.println("    adding remaining files");
							}
							System.out.println("    breaking loop for retries");
							break;
						}
					}

					// Get list of upload failures so we can handle them
					List<String> failedTransfers = retryListener.getFailedFiles();

					// If we have failed transfers, retry them
					if (failedTransfers.size() > 0)
					{
						System.out.println("uploadImages(): preparing failed files");
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
							System.out.println("    waiting before retryingfailed files");
							keepRetrying = this.retryDelayWait();
						}
					}

					System.out.println("uploadImages(): while have failed files: " + retryListener.hasFailedFiles());
				} while (retryListener.hasFailedFiles());
				// Let rules do the rest!

				// Remove local files
				directoryMetaJSON.delete();
				for (String oneFile: metaCSV.getFilePaths(metaFolder.getAbsolutePath()))
				{
					System.out.println("uploadImages(): Cleaning file: " + oneFile);
					File delFile = new File(oneFile);
					delFile.delete();

				}
			}
		}
		catch (IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Upload error",
					"Could not upload the images to S3!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
		System.out.println("uploadImages(): Done");
	}

	/**
	 * Save the set of images that were downloaded from S3
	 *
	 * @param collection The collection to upload to
	 * @param uploadEntryToSave The directory to write
	 * @param messageCallback Message callback that will show what is currently going on
	 */
	public void saveImages(ImageCollection collection, CloudUploadEntry uploadEntryToSave, StringProperty messageCallback)
	{
		try
		{
			System.out.println("saveImages(): BEGIN");

			// Grab the save folder for a given collection
			String collectionSaveDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);
			System.out.println("saveImages(): collectionSaveDirStr -> '" + collectionSaveDirStr + "'");

			// If the save directory exists and we can write to it, save
			if (this.folderExists(ROOT_BUCKET, collectionSaveDirStr))
			{
				// Grab the image directory to save
				ImageDirectory imageDirectory = uploadEntryToSave.getCloudImageDirectory();
				System.out.println("saveImages(): imageDirectory -> " + imageDirectory);
				// Make sure we have the metadata associated with this collection
				if (uploadEntryToSave.getMetadata().getValue() == null)
				{
					System.out.println("saveImages(): Reading camtrap data -> '" + collectionSaveDirStr + "'");
					uploadEntryToSave.setMetadata(this.readRemoteCamtrap(ROOT_BUCKET, collectionSaveDirStr));
				}
				// Grab the list of images to upload
				List<CloudImageEntry> toUpload = imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof CloudImageEntry).map(imageContainer -> (CloudImageEntry) imageContainer).collect(Collectors.toList());
				Platform.runLater(() -> imageDirectory.setUploadProgress(0.0));

				messageCallback.setValue("Saving " + toUpload.size() + " images to S3...");

				Double numberOfImagesToUpload = (double) toUpload.size();
				Integer numberOfDetaggedImages = 0;
				Integer numberOfRetaggedImages = 0;
				// Begin saving
				System.out.println("saveImages(): number of images -> " + numberOfImagesToUpload);
				for (int i = 0; i < toUpload.size(); i++)
				{
					// Grab the cloud image entry to upload
					CloudImageEntry cloudImageEntry = toUpload.get(i);
					// If it has been pulled save it
					System.out.println("saveImages(): current entry -> " + cloudImageEntry.getCloudFile().toString());
					if (cloudImageEntry.hasBeenPulledFromCloud() && cloudImageEntry.isCloudDirty())
					{
						System.out.println("    -> been pulled and is cloud dirty");
						if (cloudImageEntry.getSpeciesPresent().isEmpty() && cloudImageEntry.wasTaggedWithSpecies())
							numberOfDetaggedImages++;
						else if (!cloudImageEntry.getSpeciesPresent().isEmpty() && !cloudImageEntry.wasTaggedWithSpecies())
							numberOfRetaggedImages++;

						// Save that specific cloud image
						System.out.println("saveImages(): upload file -> '" + cloudImageEntry.getCloudFile().toString() + "' '" + cloudImageEntry.getFile().getAbsolutePath() + "'");
						this.uploadFile(ROOT_BUCKET, cloudImageEntry.getCloudFile().toString(), cloudImageEntry.getFile());

						// Get the absolute path of the uploaded file
						String fileAbsoluteCloudPath = cloudImageEntry.getCloudFile().toString();
						// Update the collection tag
						MetaData collectionIDTag = new MetaData(SanimalMetadataFields.A_COLLECTION_ID, collection.getID().toString(), "");
						// Write image metadata to the file
						List<MetaData> imageMetadata = cloudImageEntry.convertToMetadata();
						imageMetadata.add(collectionIDTag);
						this.addUpdateMetadataCamtrap(imageMetadata, fileAbsoluteCloudPath, uploadEntryToSave.getMetadata().getValue());

						// Update the progress every 20 uploads
						if (i % 20 == 0)
						{
							int finalI = i;
							Platform.runLater(() -> imageDirectory.setUploadProgress(finalI / numberOfImagesToUpload));
						}
					}
				}

				// Add an edit comment so users know the file was edited
				uploadEntryToSave.getEditComments().add("Edited by " + SanimalData.getInstance().getUsername() + " on " + this.formatNowTimestamp(FOLDER_TIMESTAMP_FORMAT));
				Integer imagesWithSpecies = uploadEntryToSave.getImagesWithSpecies() - numberOfDetaggedImages + numberOfRetaggedImages;
				uploadEntryToSave.setImagesWithSpecies(imagesWithSpecies);
				// Convert the upload entry to JSON format
				String json = SanimalData.getInstance().getGson().toJson(uploadEntryToSave);
				// Write the UploadMeta json file to the server
				String uploadPath = uploadEntryToSave.getUploadPath();
				System.out.println("saveImages(): write remote file -> '" + String.join("/", uploadPath, UPLOAD_JSON_FILE) + "'");
				this.writeRemoteFile(ROOT_BUCKET, String.join("/", uploadPath, UPLOAD_JSON_FILE), json);
				// Write the metadata file(s)
				File metaFolder = SanimalData.getInstance().getTempDirectoryManager().createTempFolder("meta");
				(uploadEntryToSave.getMetadata().getValue()).saveTo(metaFolder.getAbsolutePath());
				String[] paths = (uploadEntryToSave.getMetadata().getValue()).getFilePaths(metaFolder.getAbsolutePath());
				for (String oneFile: paths)
				{
					String remoteFilePath = String.join("/", uploadPath, FilenameUtils.getName(oneFile));
					System.out.println("saveImages(): write Camtrap file -> '" + remoteFilePath + "' '" + oneFile + "'");
					this.uploadFile(ROOT_BUCKET, remoteFilePath, oneFile);
				}
			}
		}
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Saving error",
					"Could not save the image list to the collection on S3!\n" + ExceptionUtils.getStackTrace(e),
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
			System.out.println("retrieveAndInsertUploadList(): BEGIN");

			// Clear the current collection uploads
			Platform.runLater(() -> collection.getUploads().clear());
			// Grab the uploads folder for a given collection
			String collectionUploadDirStr = String.join("/", COLLECTIONS_FOLDER_NAME, collection.getID().toString(), UPLOADS_FOLDER_NAME);
			// If the uploads directory exists and we can read it, read
			if (this.folderExists(ROOT_BUCKET, collectionUploadDirStr))
			{
				List<String> folders = this.listFolders(ROOT_BUCKET, collectionUploadDirStr);
				double totalFolders = folders.size();
				int numDone = 0;
				System.out.println("retrieveAndInsertUploadList(): folder count: " + folders.size() + " '" + collectionUploadDirStr + "'");
				for (String folder : folders)
				{
					progressProperty.setValue(++numDone / totalFolders);
					// We recognize uploads by their UploadMeta json file
					System.out.println("retrieveAndInsertUploadList(): Reading folder -> '" + folder + "'");
					String contents = this.readRemoteFile(ROOT_BUCKET, String.join("/", folder, UPLOAD_JSON_FILE));
					if (contents != null)
					{
						try
						{
							// Download the cloud upload entry
							CloudUploadEntry uploadEntry = SanimalData.getInstance().getGson().fromJson(contents, CloudUploadEntry.class);
							System.out.println("retrieveAndInsertUploadList(): check upload entry from JSON ->" + contents);
							if (uploadEntry != null)
							{
								System.out.println("    not null");
								uploadEntry.initFromJSON();
								// Get the Camtrap data
								uploadEntry.setMetadata(this.readRemoteCamtrap(ROOT_BUCKET, folder));

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
									"Could not read the upload metadata for the upload " + folder + "!\n" + ExceptionUtils.getStackTrace(e),
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
		System.out.println("retrieveAndInsertUploadList(): DONE");
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
			System.out.println("downloadUploadDirectory(): BEGIN");
			// Grab the uploads folder for a given collection
			String cloudDirectoryStr = uploadEntry.getUploadPath();
			CloudImageDirectory cloudImageDirectory = new CloudImageDirectory(cloudDirectoryStr);
			this.createDirectoryAndImageTree(cloudImageDirectory);

			// We need to make sure we remove the UploadMeta json "image entry" and all CSV files
			cloudImageDirectory.getChildren().removeIf(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).getCloudFile().contains(UPLOAD_JSON_FILE));
			cloudImageDirectory.getChildren().removeIf(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).getCloudFile().endsWith(".csv"));
			System.out.println("downloadUploadDirectory(): DONE");
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
	 * @param current The current directory to work on
	 */
	private void createDirectoryAndImageTree(CloudImageDirectory current)
	{
		System.out.println("createDirectoryAndImageTree(): -> " + current.getCloudDirectory());
		List<String> subFiles = this.listAllObjects(ROOT_BUCKET, current.getCloudDirectory());

		System.out.println("createDirectoryAndImageTree(): object count: " + subFiles.size());
		if (subFiles.size() > 0)
		{
			// Get all files in the directory
			for (String file : subFiles)
			{
				System.out.println("createDirectoryAndImageTree(): Checking object: " + file);
				// Add all image files to the directory
				if (!this.folderExists(ROOT_BUCKET, file))
				{
					System.out.println("    new cloud image: '" + file + "'");
					current.addImage(new CloudImageEntry(file));
				}
				// Add all subdirectories to the directory
				else
				{
					System.out.println("    new folder: '" + file + "'");

					CloudImageDirectory subDirectory = new CloudImageDirectory(file);
					current.addChild(subDirectory);
					this.createDirectoryAndImageTree(subDirectory);
				}
			}
		}
	}

	/**
	 * Performs a query given an S3Query object and returns a list of image paths that correspond with the query
	 *
	 * @param queryBuilder query builder with all specified options
	 * @param collections list of collections to query
	 * @return A list of image CyVerse paths instead of local paths
	 */
	public List<String> performQuery(S3Query queryBuilder, final List<ImageCollection> collections)
	{
		try
		{
			S3QueryResultSet resultSet = S3QueryExecute.executeQuery(queryBuilder.build(), collections);

			List<String> matchingFilePaths = new ArrayList<>();
			
			// Don't bother looping unless we have something
			if (resultSet != null)
			{					
				// Grab each row
				for (S3QueryResultRow resultRow : resultSet.getResults())
				{
					// Get the path to the image and the image name, create an absolute path with the info
					String pathToImage = resultRow.get(0);
					String imageName = resultRow.get(1);

					matchingFilePaths.add(pathToImage + "/" + imageName);
				}
			}
			return matchingFilePaths;
		}
		catch (Exception e)
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

		return Collections.emptyList();
	}

	/**
	 * Given a list of cloud absolute paths, this fetches the metadata for each image and returns it as an image entry
	 *
	 * @param absoluteRemotePaths The list of absolute paths on the cloud
	 * @param collections list of collections to query
	 * @return A list of images with metadata on the cloud
	 */
	public List<ImageEntry> fetchMetadataFor(List<String> absoluteRemotePaths, final List<ImageCollection> collections)
	{
		List<ImageEntry> toReturn = new ArrayList<>();

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

			for (String remoteAbsolutePath : absoluteRemotePaths)
			{
				localDateTime = LocalDateTime.now();
				locationName = "";
				locationID = "";
				locationLatitude = 0D;
				locationLongitude = 0D;
				locationElevation = 0D;
				speciesIDToCommonName.clear();
				speciesIDToScientificName.clear();
				speciesIDToCount.clear();

				// Perform a second query that returns ALL metadata from a given image
				for (S3MetaDataAndDomainData fileDataField : this.findMetadataValuesForDataObject(remoteAbsolutePath, collections))
				{
					// Test what type of attribute we got, if it's important store the result for later
					switch (fileDataField.getAttribute())
					{
						case SanimalMetadataFields.A_DATE_TIME_TAKEN:
							Long timeTaken = Long.parseLong(fileDataField.getValue());
							localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeTaken), ZoneId.systemDefault());
							break;
						case SanimalMetadataFields.A_LOCATION_NAME:
							locationName = fileDataField.getValue();
							break;
						case SanimalMetadataFields.A_LOCATION_ID:
							locationID = fileDataField.getValue();
							break;
						case SanimalMetadataFields.A_LOCATION_LATITUDE:
							locationLatitude = Double.parseDouble(fileDataField.getValue());
							break;
						case SanimalMetadataFields.A_LOCATION_LONGITUDE:
							locationLongitude = Double.parseDouble(fileDataField.getValue());
							break;
						case SanimalMetadataFields.A_LOCATION_ELEVATION:
							locationElevation = Double.parseDouble(fileDataField.getValue());
							break;
						case SanimalMetadataFields.A_SPECIES_COMMON_NAME:
							speciesIDToCommonName.put(Integer.parseInt(fileDataField.getUnit()), fileDataField.getValue());
							break;
						case SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME:
							speciesIDToScientificName.put(Integer.parseInt(fileDataField.getUnit()), fileDataField.getValue());
							break;
						case SanimalMetadataFields.A_SPECIES_COUNT:
							speciesIDToCount.put(Integer.parseInt(fileDataField.getUnit()), Integer.parseInt(fileDataField.getValue()));
							break;
						case SanimalMetadataFields.A_COLLECTION_ID:
							collectionID = UUID.fromString(fileDataField.getValue());
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
				ImageEntry entry = new ImageEntry(new File(remoteAbsolutePath));
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
		catch (Exception e)
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
		System.out.println("downloadImages(): BEGIN");
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
				this.saveRemoteFile(ROOT_BUCKET, absoluteImagePath, localFile);
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
			System.out.println("remoteToLocalImageFile(): BEGIN");
			// Create a temporary file to write to with the same name
			File localImageFile = SanimalData.getInstance().getTempDirectoryManager().createTempFile(cloudFile);

			// Download the file locally
			this.saveRemoteFile(ROOT_BUCKET, cloudFile, localImageFile);

			return localImageFile;
		}
		catch (Exception e)
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"JSON error",
					"Could not pull the remote file (" + cloudFile + ")!\n" + ExceptionUtils.getStackTrace(e),
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
		String delimiter = "/";
		System.out.println("folderExists(): -> '" + bucket + "' '" + folderPath + "'");
		if (folderPath.endsWith(delimiter))
		{
			folderPath = folderPath.substring(0, folderPath.length() - 1);
		}

		boolean folderFound = false;
		ListObjectsV2Result result = this.s3Client.listObjectsV2(new ListObjectsV2Request()
																	.withBucketName(bucket)
																	.withPrefix(folderPath)
																	.withDelimiter(delimiter)
																);

		for (String onePrefix: result.getCommonPrefixes())
		{
			if (onePrefix.equals(folderPath + delimiter))
			{
				folderFound = true;
				break;
			}
		}

		if (!folderFound)
		{
			for (S3ObjectSummary oneSummary: result.getObjectSummaries())
			{
				System.out.println("    summary: '" + oneSummary.getKey() + "' vs '" + folderPath + delimiter + "'");
				if (oneSummary.getKey().equals(folderPath + delimiter))
				{
					folderFound = true;
					break;
				}
			}
		}

		System.out.println("folderExists(): result: " + folderFound);
		return folderFound;
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
			System.out.println("objectExists(): -> '" + bucket + "' '" + objectName + "'");
			ObjectMetadata objectMetadata = this.s3Client.getObjectMetadata(bucket, objectName);
			System.out.println("    ObjectMetadata success");
			System.out.println("    " + String.valueOf(objectMetadata));
			return true;
		}
		catch (AmazonS3Exception e)
		{
			System.out.println("objectExists(): exception -> " + e.getMessage());
			// First check if it isn't found, otherwise throw the exception
	        if (e.getStatusCode() == 404)
	        {
	        	System.out.println("    Status code 404: not found");
	        	return false;
	        }
	        else
	        {
	        	System.out.println("    rethrowing exception");
	        	throw e;
	        }
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
	            return s3Client.getObjectAsString(bucket, objectName);
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
	 * Creates a Camtrap instance initialized from the remote location
	 * 
	 * @param bucket the bucket to load the data from
	 * @param prefix the path prefix for the data location
	 * @return the initialized instance of the Camtrap data
	 * @throws IOExeption if a problem ocurrs reading Camtrap files
	 * @throws CsvValidationException if there's a problem with a CSV file
	 */
	private Camtrap readRemoteCamtrap(String bucket, String prefix) throws IOException, CsvValidationException
	{
		Camtrap metadata = new Camtrap();

		String remotePath = String.join("/", prefix, Camtrap.CAMTRAP_DEPLOYMENTS_FILE);
		String csvData = this.readRemoteFile(bucket, remotePath);
		metadata.setDeployments(csvData);

		remotePath = String.join("/", prefix, Camtrap.CAMTRAP_MEDIA_FILE);
		csvData = this.readRemoteFile(bucket, remotePath);
		metadata.setMedia(csvData);

		remotePath = String.join("/", prefix, Camtrap.CAMTRAP_OBSERVATIONS_FILE);
		csvData = this.readRemoteFile(bucket, remotePath);
		metadata.setObservations(csvData);

		return metadata;
	}

	/**
	 * Reads a file from S3 assuming a user is already logged in
	 *
	 * @param bucket The bucket to load the object from
	 * @param objectName The name of the Object to read
	 * @param saveFile The file to save the download to
	 * @throws FileNotFoundException If the file can't be saved
	 * @throws IOException If the file can't be written
	 */
	private void saveRemoteFile(String bucket, String objectName, File saveFile) throws FileNotFoundException, IOException
	{
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucket, objectName);
        S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

		// Write the contents of the file
		FileOutputStream outputStream = new FileOutputStream(saveFile);
		S3ObjectInputStream s3is = objectPortion.getObjectContent();
	    byte[] read_buf = new byte[1024];
	    int read_len = 0;
	    while ((read_len = s3is.read(read_buf)) > 0) {
	        outputStream.write(read_buf, 0, read_len);
	    }
	    s3is.close();
	}

	/**
	 * Uploads a file to the S3 server
	 * 
	 * @param bucket The path to the object to write
	 * @param objectName The name of the object to write
	 * @param localFilePath The path of the file to upload
	 */
	private void uploadFile(String bucket, String objectName, String localFilePath)
	{
		File localFile = new File(localFilePath);

        // Upload file
        System.out.println("uploadFile (String): '" + bucket + "' '" + objectName + "'  From: '" + localFilePath + "'");
        this.uploadFile(bucket, objectName, localFile);
	}

	/**
	 * Uploads a file to the S3 server
	 * 
	 * @param bucket The path to the object to write
	 * @param objectName The name of the object to write
	 * @param sourceFile The file to upload
	 */
	private void uploadFile(String bucket, String objectName, File sourceFile)
	{
        // Upload file
        System.out.println("uploadFile (File): '" + bucket + "' '" + objectName + "'  From: '" + sourceFile.getAbsolutePath() + "'");
        this.s3Client.putObject(new PutObjectRequest(bucket, objectName, sourceFile));
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
																	"sanimalTemp." + FilenameUtils.getExtension(objectName));
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
	            this.uploadFile(bucket, objectName, localFile);
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
					"Error pushing remote file (" + objectName + ")!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	/**
	 * Returns a list of the folders in the prefix path of the bucket
	 * 
	 * @param bucket The path to the bucket to search
	 * @param folder The folder path to search
	 * @return returns the list of found folders 
	 */
	private List<String> listFolders(String bucket, String folder)
	{
		System.out.println("listFolders(): '" + bucket + "' '" + folder + "'");
	    String delimiter = "/";
	    if (!folder.endsWith(delimiter)) {
	        folder += delimiter;
	    }

	    System.out.println("  request folder: '" + folder + "'");
	    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
											            .withBucketName(bucket)
											            .withPrefix(folder)
											            .withDelimiter(delimiter);
	    ObjectListing objects = this.s3Client.listObjects(listObjectsRequest);

	    List<String> results = new ArrayList<String>();

	    for (String onePrefix: objects.getCommonPrefixes())
	    {
	    	System.out.println("    Common prefix: '" + onePrefix + "'");
	    	if (onePrefix.endsWith(delimiter))
	    	{
	    		results.add(onePrefix.substring(0, onePrefix.length() - 1));
	    	}
	    	else
	    	{
	    		results.add(onePrefix);
	    	}
	    }

	    return results;
	}

	/**
	 * Returns a list of the objects in the prefix path of the bucket
	 * 
	 * @param bucket The path to the bucket to search
	 * @param prefix Additional path information for the search
	 * @return returns the list of found Objects 
	 */
	private List<String> listAllObjects(String bucket, String prefix)
	{
		System.out.println("listAllObjects(): '" + bucket + "' '" + prefix + "'");
	    String delimiter = "/";
	    if (!prefix.endsWith(delimiter)) {
	        prefix += delimiter;
	    }

		System.out.println("listAllObjects(): prefix -> '" + prefix + "'");
	    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
											            .withBucketName(bucket)
											            .withPrefix(prefix)
											            .withDelimiter(delimiter);
	    ObjectListing objects = this.s3Client.listObjects(listObjectsRequest);

	    List<String> results = new ArrayList();

	    // All folders
	    for (String onePrefix: objects.getCommonPrefixes())
	    {
			System.out.println("listAllObjects(): folder -> '" + onePrefix + "'");
	    	if (onePrefix.endsWith(delimiter))
	    	{
	    		results.add(onePrefix.substring(0, onePrefix.length() - 1));
	    	}
	    	else
	    	{
	    		results.add(onePrefix);
	    	}
	    }
	    // All other objects
		for (S3ObjectSummary oneSummary: objects.getObjectSummaries())
		{
			System.out.println("listAllObjects(): object -> '" + oneSummary.getKey() + "'");
			results.add(oneSummary.getKey());
		}

		System.out.println("listAllObjects(): DONE");
	    return results;
	}

	/**
	 * Creates an Object using the folder path
	 * 
	 * @param bucket The path to the bucket to create the folder path in
	 * @param folderPath The path of the folder to create
	 */
	private void createFolder(String bucket, String folderPath)
	{
		System.out.println("createFolder(): -> '" + bucket + "' '" + folderPath + "'");
	    String delimiter = "/";
	    if (!folderPath.endsWith(delimiter))
	    {
	    	folderPath += delimiter;
			System.out.println("    updated folder '" + folderPath + "'");
	    }

        // Create metadata with content 0 
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0L);
        
        // Empty content
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        
        // Creates a PutObjectRequest by passing the folder name with the suffix
		System.out.println("createFolder(): putting object request -> '" + bucket + "' '" + folderPath + "'");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, folderPath, inputStream, metadata);
        
        //Send the request to s3 to create the folder
        this.s3Client.putObject(putObjectRequest);
        System.out.println("createFolder() DONE");
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
	    if (!folderPath.endsWith(delimiter))
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
	    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(keysList);

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
	 * Formats the metadata for images
	 * 
	 * @param meta The file to write to
	 * @param imageEntries The image entries to write
	 * @param imageToMetadata The CSV file representing each image's metadata
	 * @throws FileNotFoundException if the metadata file can't be written
	 */
	private void createImageMetaEntries(Camtrap meta, List<ImageEntry> imageEntries, BiConsumer<ImageEntry, Camtrap> imageToMetadata) throws FileNotFoundException
	{
		for (ImageEntry imageEntry: imageEntries)
		{
			// Create metadata entries into our meta file
			imageToMetadata.accept(imageEntry, meta);
		}
	}

	/**
	 * Maps MetaData fields to CamTrap formats
	 * 
	 * @param imageMetadata the metadata fields, values, and units
	 * @param fileRelativePath the relative to the media
	 * @param metaCamtrap the Camtrap metadata entries
	 * @throws InvalidParameterException if critical information is missing
	 */
	private void mapMetadataToCamtrap(List<MetaData> imageMetadata, String fileRelativePath, Camtrap metaCamtrap)
	{
		/*
		 * NOTE: Changing the information stored here may have an impact on the S3QueryExecute class
		 */

		// Create a new Media instance
		Media med = new Media();

		med.mediaID = fileRelativePath;
		med.sequenceID = fileRelativePath;
		med.filePath = fileRelativePath;
		med.fileName = FilenameUtils.getBaseName(fileRelativePath) + "." + FilenameUtils.getExtension(fileRelativePath);
		med.fileMediaType = "image/jpeg";

		// Create a new Observations instance
		Observations obs = new Observations();
		String locName = null;
		String locID = null;
		Double locLat = null;
		Double locLon = null;
		Double locHeight = null;
		String deploymentID = null;

		obs.mediaID = med.mediaID;
		for (MetaData oneMeta: imageMetadata)
		{
			switch (oneMeta.getAttribute())
			{
				case SanimalMetadataFields.A_SANIMAL:
					obs.deploymentID = "sanimal";
					break;
				case SanimalMetadataFields.A_DATE_TIME_TAKEN:
					obs.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(oneMeta.getValue())),
                               								TimeZone.getDefault().toZoneId());
					break;
				/* Not used since we have the Epoch seconds (see A_DATE_TIME_TAKEN)
				case SanimalMetadataFields.A_DATE_YEAR_TAKEN:
					break;
				case SanimalMetadataFields.A_DATE_MONTH_TAKEN:
					break;
				case SanimalMetadataFields.A_DATE_HOUR_TAKEN:
					break;
				case SanimalMetadataFields.A_DATE_DAY_OF_YEAR_TAKEN:
					break;
				case SanimalMetadataFields.A_DATE_DAY_OF_WEEK_TAKEN:
					break;
				*/
				case SanimalMetadataFields.A_LOCATION_NAME:
					locName = oneMeta.getValue();
					break;
				case SanimalMetadataFields.A_LOCATION_ID:
					locID = oneMeta.getValue();
					break;
				case SanimalMetadataFields.A_LOCATION_LATITUDE:
					locLat = Double.parseDouble(oneMeta.getValue());
					break;
				case SanimalMetadataFields.A_LOCATION_LONGITUDE:
					locLon = Double.parseDouble(oneMeta.getValue());
					break;
				case SanimalMetadataFields.A_LOCATION_ELEVATION:
					locHeight = Double.parseDouble(oneMeta.getValue());
					break;
				case SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME:
					obs.scientificName = oneMeta.getValue();
					break;
				case SanimalMetadataFields.A_SPECIES_COMMON_NAME:
					obs.comments = "[COMMONNAME:" + oneMeta.getValue() + "]";
					break;
				case SanimalMetadataFields.A_SPECIES_COUNT:
					obs.count = Integer.parseInt(oneMeta.getValue());
					break;
				case SanimalMetadataFields.A_COLLECTION_ID:
					deploymentID = oneMeta.getValue();
					break;
			}
		}

		// Make sure we have enough information to continue
		if (locID == null)
		{
			throw new InvalidParameterException("Missing location ID for Camtrap metadata");
		}
		if ((locLat == null) || (locLon == null))
		{
			throw new InvalidParameterException("Missing location lat-lon for Camtrap metadata");
		}

		// Look for a deployment that matches our LocationID
		Deployments ourDep = null;
		boolean newDep = false;
		for (Deployments oneDep: metaCamtrap.deployments)
		{
			if (Objects.equals(oneDep.locationID, locID))
			{
				ourDep = oneDep;
				break;
			}
		}

		// Create a new deployment if needed
		if (ourDep == null)
		{
			newDep = true;
			ourDep = new Deployments();
			ourDep.deploymentID = deploymentID;
			ourDep.locationName = locName;
			ourDep.locationID = locID;
			ourDep.latitude = locLat;
			ourDep.longitude = locLon;
			ourDep.cameraHeight = locHeight;
		}

		// Assign the deployment ID
		obs.deploymentID = ourDep.deploymentID;

		// Add new items to the Camtrap metadata store
		metaCamtrap.media.add(med);
		metaCamtrap.observations.add(obs);
		if (newDep == true)
		{
			metaCamtrap.deployments.add(ourDep);
		}
	}

	/**
	 * Looks for an entry and updates the metadata if found, otherwise creates a new entry
	 * 
	 * @param imageMetadata the metadata fields, values, and units
	 * @param fileRelativePath the relative to the media
	 * @param metaCamtrap the Camtrap metadata entries
	 * @throws InvalidParameterException if critical information is missing
	 */
	private void addUpdateMetadataCamtrap(List<MetaData> imageMetadata, String fileRelativePath, Camtrap metaCamtrap)
	{
		// Create a Camtrap instance to hold the generated data
		Camtrap newMeta =  new Camtrap();
		this.mapMetadataToCamtrap(imageMetadata, fileRelativePath, newMeta);

		// Either replace or add the data
		Media oldMedia = null;
		int index = 0;
		while (index < metaCamtrap.media.size())
		{
			// Check for a media match
			Media curMedia = metaCamtrap.media.get(index);
			if (Objects.equals(curMedia.filePath, fileRelativePath))
			{
				oldMedia = curMedia;
				curMedia = newMeta.media.get(0);
				break;
			}
			index++;
		}
		if (index >= metaCamtrap.media.size())
		{
			metaCamtrap.media.add(newMeta.media.get(0));
		}
	}

	/**
	 * Returns the set of metadata associated with the remote path
	 * 
	 * @param remotePath the path to return the metadata for
	 * @param collections the list of collections to search
	 * @return list of metadata associated with the path
	 */
	private List<S3MetaDataAndDomainData> findMetadataValuesForDataObject(final String remotePath, final List<ImageCollection> collections)
	{
		List<S3MetaDataAndDomainData> imageMetaData = new ArrayList<S3MetaDataAndDomainData>();

		// Loop through the collections and find the file
		for (ImageCollection oneCollection: collections)
		{
            if (!oneCollection.uploadsWereSynced())
            {
                // TODO: Fetch remote -> see SanimalUploadController.java:210-234
            }

            // Find the meta data associated with the path
            List<CloudUploadEntry> uploads = oneCollection.getUploads();
            for (CloudUploadEntry oneEntry: uploads)
            {
                Camtrap metaData = oneEntry.getMetadata().getValue();

                for (Media med: metaData.media)
                {
                	if (med.filePath.equals(remotePath))
                	{
                		Observations obs = this.findObservation(med, metaData);
                		Deployments dep = this.findDeployment(med, metaData);

						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_SANIMAL, SanimalMetadataFields.A_SANIMAL));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_TIME_TAKEN, 
										Long.toString(obs.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_YEAR_TAKEN, 
										Long.toString(obs.timestamp.getYear())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_MONTH_TAKEN, 
										Long.toString(obs.timestamp.getMonth().getValue())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_HOUR_TAKEN, 
										Long.toString(obs.timestamp.getHour())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_DAY_OF_YEAR_TAKEN, 
										Long.toString(obs.timestamp.getDayOfYear())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_DATE_DAY_OF_WEEK_TAKEN, 
										Long.toString(obs.timestamp.getDayOfWeek().getValue())));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_LOCATION_NAME, dep.locationName));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_LOCATION_ID, dep.locationID));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_LOCATION_LATITUDE, Double.toString(dep.latitude)));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_LOCATION_LONGITUDE, Double.toString(dep.longitude)));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_LOCATION_ELEVATION, Double.toString(dep.cameraHeight)));
						imageMetaData.add(S3MetaDataAndDomainData.instanceWithUnits(SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME, obs.scientificName));
						imageMetaData.add(S3MetaDataAndDomainData.instanceWithUnits(SanimalMetadataFields.A_SPECIES_COMMON_NAME, this.getCommonName(obs.comments)));
						imageMetaData.add(S3MetaDataAndDomainData.instanceWithUnits(SanimalMetadataFields.A_SPECIES_COUNT, Long.toString(obs.count)));
						imageMetaData.add(S3MetaDataAndDomainData.instance(SanimalMetadataFields.A_COLLECTION_ID, dep.deploymentID));
                	}
                }
            }
		}

        return imageMetaData;
	}

	/**
	 * Finds the observation associated with the media
	 * 
	 * @param med the media to find the observation for
	 * @param metadata the complete set of metadata to search
	 * @return the found observation
	 */
	private Observations findObservation(Media med, Camtrap metadata)
	{
		for (Observations obs: metadata.observations)
		{
			if (obs.mediaID.equals(med.mediaID))
			{
				return obs;
			}
		}

		return null;
	}

	/**
	 * Finds the deployment associated with the media
	 * 
	 * @param med the media to find the deployment for
	 * @param metadata the complete set of metadata to search
	 * @return the found deployment
	 */
	private Deployments findDeployment(Media med, Camtrap metadata)
	{
		for (Deployments dep: metadata.deployments)
		{
			if (dep.deploymentID.equals(med.deploymentID))
			{
				return dep;
			}
		}

		return null;
	}

	/**
	 * Returns the string of the common name from the observation comments
	 * 
	 * @param observationComments the comments to try and extract the common name from
	 * @return the found common name
	 */
	private String getCommonName(final String observationComments)
	{
        final String commonNameTag = "[COMMONNAME:";
        final String commonNameEndTag = "]";

	    if (observationComments.startsWith(commonNameTag))
	    {
	        int endIndex = observationComments.indexOf(commonNameEndTag);
	        if (endIndex > -1)
	        {
	            return observationComments.substring(commonNameTag.length(), endIndex);
	        }
	    }

		return "";
	}
}
