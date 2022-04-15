package model.image;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Image directory that's originally on a cloud
 */
public class CloudImageDirectory extends ImageDirectory
{
	// The icon to use for all images at the moment
	private static final Image DEFAULT_CLOUD_DIR_IMAGE = new Image(ImageEntry.class.getResource("/images/importWindow/cloudDirectoryIcon.png").toString());

	// The file representing the directory
	private ObjectProperty<String> cloudDirectoryProperty = new SimpleObjectProperty<>();
	// The bucket
	private ObjectProperty<String> cloudBucket = new SimpleObjectProperty<>();

	/**
	 * Construct a cloud Directory
	 *
	 * @param bucket The bucket name
	 * @param cloudDirectory The file that represents the cloudDirectoryProperty
	 */
	public CloudImageDirectory(String bucket, String cloudDirectory)
	{
		// No local file
		super(null);

		// Initialize values
		DEFAULT_DIRECTORY_ICON.setValue(DEFAULT_CLOUD_DIR_IMAGE);
		this.cloudBucket.setValue(bucket);
		this.cloudDirectoryProperty.setValue(cloudDirectory);
	}

	/**
	 * String representation is just the directory name
	 *
	 * @return The directory name
	 */
	@Override
	public String toString()
	{
		return this.getCloudBucket() + ":" + this.getCloudDirectory();
	}

	///
	/// Setters/Getters
	///

	public String getCloudBucket()
	{
		return cloudBucket.getValue();
	}

	public void setCloudBucket(String bucket)
	{
		this.cloudBucket.setValue(bucket);
	}

	public String getCloudDirectory()
	{
		return cloudDirectoryProperty.getValue();
	}

	public void setCloudDirectory(String file)
	{
		this.cloudDirectoryProperty.setValue(file);
	}

	public ObjectProperty<String> getCloudDirectoryProperty()
	{
		return this.cloudDirectoryProperty;
	}
}
