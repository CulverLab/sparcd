package model.cyverse;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import model.SanimalData;
import model.image.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class used to wrap the CyVerse Jargon FTP/iRODS library
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
	private static final SimpleDateFormat FOLDER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

	// Cache the authenticated iRODS account
	private IRODSAccount authenticatedAccount;
	// Session manager ensures that we don't leave sessions open
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
			SanimalData.getInstance().getErrorDisplay().notify("Could not authenticate the user!\n" + ExceptionUtils.getStackTrace(e));
		}
		// Default, just return false
		return false;
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
					this.setFilePermissions(collectionDirName, collection.getPermissions(), false);

					if (messageCallback != null)
						messageCallback.setValue("Writing collection Uploads directory...");

					// Create the folder containing uploads, and set its permissions
					IRODSFile collectionDirUploads = fileFactory.instanceIRODSFile(collectionDirName + "/Uploads");
					if (!collectionDirUploads.exists())
						collectionDirUploads.mkdir();
					this.setFilePermissions(collectionDirUploads.getAbsolutePath(), collection.getPermissions(), true);
				}
				catch (JargonException e)
				{
					SanimalData.getInstance().getErrorDisplay().notify("Error creating the collections directory! Error was:\n" + ExceptionUtils.getStackTrace(e));
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
				SanimalData.getInstance().getErrorDisplay().notify("Could not delete the collection from CyVerse!\n" + ExceptionUtils.getStackTrace(e));
			}
			this.sessionManager.closeSession();
		}
	}

	/**
	 * Sets the file permission for a file on the CyVerse system
	 *
	 * @param fileName The name of the file to update permissions of
	 * @param permissions The list of permissions to set
	 * @param recursive If the permissions are to be recursive
	 * @throws JargonException Thrown if something goes wrong in the Jargon library
	 */
	private void setFilePermissions(String fileName, ObservableList<Permission> permissions, boolean recursive) throws JargonException
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
					if (permission.canUpload())
						collectionAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername(), recursive);
					// If the user can read set the permission to write
					else if (permission.canRead())
						collectionAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername(), recursive);
				}
				catch (JargonException e)
				{
					SanimalData.getInstance().getErrorDisplay().notify("Error setting permissions for user!\n" + ExceptionUtils.getStackTrace(e));
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
					if (permission.canUpload())
						dataObjectAO.setAccessPermissionWrite(ZONE, file.getAbsolutePath(), permission.getUsername());
						// If the user can read set the permission to write
					else if (permission.canRead())
						dataObjectAO.setAccessPermissionRead(ZONE, file.getAbsolutePath(), permission.getUsername());
				}
				catch (JargonException e)
				{
					SanimalData.getInstance().getErrorDisplay().notify("Error setting permissions for user!\n" + ExceptionUtils.getStackTrace(e));
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
						SanimalData.getInstance().getErrorDisplay().notify("Error removing permissions from user!\n" + ExceptionUtils.getStackTrace(e));
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
						SanimalData.getInstance().getErrorDisplay().notify("Error removing permissions from user!\n" + ExceptionUtils.getStackTrace(e));
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

					// Create the meta.csv representing the metadata for all images in the tar file
					String localDirAbsolutePath = directoryToWrite.getFile().getAbsolutePath();
					String localDirName = directoryToWrite.getFile().getName();

					// Make a set of tar files from the image files. Don't use a single tar file because we may have > 1000 images in each
					File[] tarsToWrite = DirectoryManager.directoryToTars(directoryToWrite, imageEntry ->
					{
						// Compute the image's "cyverse" path
						String fileRelativePath = localDirName + StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), localDirAbsolutePath);
						fileRelativePath = fileRelativePath.replace('\\', '/');
						return fileRelativePath + "\n";
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

					// Finally we actually index the image metadata using elasticsearch
					SanimalData.getInstance().getEsConnectionManager().indexImages(uploadDirName + "/" + localDirName, collection.getID().toString(), directoryToWrite, uploadEntry);

					// Let rules do the rest!
				}
				else
				{
					SanimalData.getInstance().getErrorDisplay().notify("You don't have permission to upload to this collection!");
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().notify("Could not upload the images to CyVerse!\n" + ExceptionUtils.getStackTrace(e));
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
					List<CloudImageEntry> toUpload = imageDirectory.flattened()
							.filter(imageContainer -> imageContainer instanceof CloudImageEntry)
							.map(imageContainer -> (CloudImageEntry) imageContainer)
							.filter(cloudImageEntry -> cloudImageEntry.hasBeenPulledFromCloud() && cloudImageEntry.isCloudDirty())
							.collect(Collectors.toList());
					Platform.runLater(() -> imageDirectory.setUploadProgress(0.0));

					messageCallback.setValue("Saving " + toUpload.size() + " image(s) to CyVerse...");

					Double numberOfImagesToUpload = (double) toUpload.size();
					Integer numberOfDetaggedImages = 0;
					Integer numberOfRetaggedImages = 0;
					// Begin saving
					for (int i = 0; i < toUpload.size(); i++)
					{
						// Grab the cloud image entry to upload
						CloudImageEntry cloudImageEntry = toUpload.get(i);
						// If it has been pulled save it
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

						// Update the progress every 20 uploads
						if (i % 20 == 0)
						{
							int finalI = i;
							Platform.runLater(() -> imageDirectory.setUploadProgress(finalI / numberOfImagesToUpload));
						}
					}

					// Add an edit comment so users know the file was edited
					uploadEntryToSave.getEditComments().add("Edited by " + SanimalData.getInstance().getUsername() + " on " + FOLDER_FORMAT.format(Calendar.getInstance().getTime()));
					Integer imagesWithSpecies = uploadEntryToSave.getImagesWithSpecies() - numberOfDetaggedImages + numberOfRetaggedImages;
					uploadEntryToSave.setImagesWithSpecies(imagesWithSpecies);

					// Finally we update our metadata index
					SanimalData.getInstance().getEsConnectionManager().updateIndexedImages(toUpload, collection.getID().toString(), uploadEntryToSave);
				}
				else
				{
					SanimalData.getInstance().getErrorDisplay().notify("You don't have permission to save to this collection!");
				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().notify("Could not save the image list to the collection on CyVerse!\n" + ExceptionUtils.getStackTrace(e));
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
				SanimalData.getInstance().getErrorDisplay().notify("Downloading uploaded collection failed!");
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

	public void indexExisitingImages(ImageCollection imageCollection, String absoluteIRODSPath)
	{
		if (this.sessionManager.openSession())
		{
			try
			{
				IRODSFileFactory fileFactory = this.sessionManager.getCurrentAO().getIRODSFileFactory(this.authenticatedAccount);
				IRODSFile topLevelDirectory = fileFactory.instanceIRODSFile(absoluteIRODSPath);
				if (topLevelDirectory.exists() && topLevelDirectory.isDirectory() && topLevelDirectory.canRead())
				{
					RuleProcessingAO ruleProcessingAO = this.sessionManager.getCurrentAO().getRuleProcessingAO(this.authenticatedAccount);

				}
			}
			catch (JargonException e)
			{
				SanimalData.getInstance().getErrorDisplay().notify("Error indexing existing images. Error was:\n" + ExceptionUtils.getStackTrace(e));
			}

			this.sessionManager.closeSession();
		}
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
				SanimalData.getInstance().getErrorDisplay().notify("Could not pull the remote file (" + cyverseFile.getName() + ")!\n" + ExceptionUtils.getStackTrace(e));
			}
			this.sessionManager.closeSession();
		}

		return null;
	}
}
