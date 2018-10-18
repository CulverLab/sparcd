package model.image;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The cloud upload entry that represents some upload at some point in time
 */
public class CloudUploadEntry
{
	// If the upload has been downloaded yet
	private transient BooleanProperty downloadedProperty = new SimpleBooleanProperty(false);
	// A reference to the cloud image directory that we may download at some point
	private transient ObjectProperty<CloudImageDirectory> cloudImageDirectoryProperty = new SimpleObjectProperty<>();

	// The username of the person that uploaded images
	private String uploadUser;
	// The date the upload happened on
	private LocalDateTime uploadDate;
	// The number of species imagesWithSpecies images
	private Integer imagesWithSpecies;
	// The number of images in this upload
	private Integer imageCount;
	// A list of edits made to the upload
	private List<String> editComments = new ArrayList<>();
	// A path to the upload on CyVerse
	private String uploadIRODSPath;

	/**
	 * Constructor initializes all fields
	 *
	 * @param uploadUser The user that uploaded the images
	 * @param uploadDate The date the upload happened on
	 * @param imagesWithSpecies The number of images with species tagged
	 * @param uploadIRODSPath The path to the file on CyVerse
	 */
	public CloudUploadEntry(String uploadUser, LocalDateTime uploadDate, Integer imagesWithSpecies, Integer imageCount, String uploadIRODSPath)
	{
		this.uploadUser = uploadUser;
		this.uploadDate = uploadDate;
		this.imagesWithSpecies = imagesWithSpecies;
		this.imageCount = imageCount;
		this.uploadIRODSPath = uploadIRODSPath;
	}

	/**
	 * When we create the class from JSON we need to initialize things that may not have been, so do so here
	 */
	public void initFromJSON()
	{
		this.downloadedProperty = new SimpleBooleanProperty(false);
		this.cloudImageDirectoryProperty = new SimpleObjectProperty<>();
	}

	/**
	 * After saving this cloud upload we reset it so that we need to re-download it if we want to edit it again
	 */
	public void clearLocalCopy()
	{
		this.setDownloaded(false);
		this.setCloudImageDirectory(null);
	}

	///
	/// Getters/Setters
	///

	public List<String> getEditComments()
	{
		return this.editComments;
	}

	public String getUploadUser()
	{
		return uploadUser;
	}

	public LocalDateTime getUploadDate()
	{
		return uploadDate;
	}

	public void setImagesWithSpecies(Integer imagesWithSpecies)
	{
		this.imagesWithSpecies = imagesWithSpecies;
	}

	public Integer getImagesWithSpecies()
	{
		return imagesWithSpecies;
	}

	public Integer getImageCount()
	{
		return imageCount;
	}

	public String getUploadIRODSPath()
	{
		return uploadIRODSPath;
	}

	public void setDownloaded(Boolean downloadedProperty)
	{
		this.downloadedProperty.setValue(downloadedProperty);
	}

	public Boolean hasBeenDownloaded()
	{
		return this.downloadedProperty.getValue();
	}

	public BooleanProperty downloadedProperty()
	{
		return this.downloadedProperty;
	}

	public void setCloudImageDirectory(CloudImageDirectory cloudImageDirectory)
	{
		this.cloudImageDirectoryProperty.setValue(cloudImageDirectory);
	}

	public CloudImageDirectory getCloudImageDirectory()
	{
		return cloudImageDirectoryProperty.getValue();
	}

	public ObjectProperty<CloudImageDirectory> cloudImageDirectoryProperty()
	{
		return this.cloudImageDirectoryProperty;
	}
}
