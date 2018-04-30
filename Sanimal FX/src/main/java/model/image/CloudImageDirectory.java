package model.image;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import model.cyverse.ImageCollection;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * Image directory that's originally on a cloud
 */
public class CloudImageDirectory extends ImageDirectory
{
	// The icon to use for all images at the moment
	private static final Image DEFAULT_CLOUD_DIR_IMAGE = new Image(ImageEntry.class.getResource("/images/importWindow/cloudDirectoryIcon.png").toString());

	// The file representing the directory
	private ObjectProperty<IRODSFile> cyverseDirectoryProperty = new SimpleObjectProperty<>();

	/**
	 * Construct a cyverse Directory
	 *
	 * @param cyverseDirectory The file that represents the cyverseDirectoryProperty
	 */
	public CloudImageDirectory(IRODSFile cyverseDirectory)
	{
		// No local file
		super(null);

		// Initialize values
		DEFAULT_DIRECTORY_ICON.setValue(DEFAULT_CLOUD_DIR_IMAGE);
		this.cyverseDirectoryProperty.setValue(cyverseDirectory);
	}

	/**
	 * String representation is just the directory name
	 *
	 * @return The directory name
	 */
	@Override
	public String toString()
	{
		return this.getCyverseDirectory().getName();
	}

	///
	/// Setters/Getters
	///

	public IRODSFile getCyverseDirectory()
	{
		return cyverseDirectoryProperty.getValue();
	}

	public void setCyverseDirectory(IRODSFile file)
	{
		this.cyverseDirectoryProperty.setValue(file);
	}

	public ObjectProperty<IRODSFile> getCyverseDirectoryProperty()
	{
		return this.cyverseDirectoryProperty;
	}
}
