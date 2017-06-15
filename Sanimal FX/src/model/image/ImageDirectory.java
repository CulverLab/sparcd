package model.image;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.File;

/**
 * A class representing a directoryProperty containing images
 * 
 * @author David Slovikosky
 */
public class ImageDirectory extends ImageContainer
{
	// The icon to use for all images at the moment
	private static final Image DEFAULT_DIRECTORY_ICON = new Image(ImageEntry.class.getResource("../../images/importWindow/directoryIcon.png").toString());

	private ObservableList<ImageContainer> children = FXCollections.observableArrayList(imageContainer -> {
		if (imageContainer instanceof ImageEntry)
		{
			ImageEntry image = (ImageEntry) imageContainer;
			return new Observable[] {
					image.getDateTakenProperty(),
					image.getFileProperty(),
					image.getLocationTakenProperty(),
					// Do we need a ListProperty?
					image.getSpeciesPresent()
			};
		}
		else if (imageContainer instanceof ImageDirectory)
		{
			ImageDirectory directory = (ImageDirectory) imageContainer;
			return new Observable[] {
					directory.getFileProperty(),
					// Do we need a ListProperty?
					directory.getChildren()
			};
		}
		else
			return new Observable[0];
	});

	// The file representing the directory
	private ObjectProperty<File> directoryProperty = new SimpleObjectProperty<File>();

	/**
	 * Construct an image directoryProperty
	 * 
	 * @param directory
	 *            The file that represents the directoryProperty
	 */
	public ImageDirectory(File directory)
	{
		if (!directory.isDirectory())
			throw new RuntimeException("The specified file is not a directory!");
		this.directoryProperty.setValue(directory);
	}

	@Override
	public Image getTreeIcon()
	{
		return DEFAULT_DIRECTORY_ICON;
	}

	@Override
	public ObservableList<ImageContainer> getChildren()
	{
		return this.children;
	}

	/**
	 * Add a new subdirctory to this directoryProperty
	 * 
	 * @param subDirectory
	 *            The directoryProperty to add
	 */
	public void addSubDirectory(ImageDirectory subDirectory)
	{
		this.children.add(subDirectory);
	}

	/**
	 * Add an image to this directoryProperty
	 * 
	 * @param imageEntry
	 *            The image to add
	 */
	public void addImage(ImageEntry imageEntry)
	{
		this.children.add(imageEntry);
	}

	/**
	 * Get the file representing this directoryProperty
	 * 
	 * @return The file representing this directoryProperty
	 */
	public File getFile()
	{
		return directoryProperty.getValue();
	}

	/**
	 * Set the file that this directoryProperty represents
	 * 
	 * @param file
	 *            The file that this directoryProperty represents
	 */
	public void setFile(File file)
	{
		this.directoryProperty.setValue(file);
	}

	/**
	 * Grab the file property
	 * @return The source file property
	 */
	public ObjectProperty<File> getFileProperty()
	{
		return this.directoryProperty;
	}

	/**
	 * @return The string representing this directoryProperty
	 */
	@Override
	public String toString()
	{
		return this.getFile().getName();
	}

}
