package model.image;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.location.Location;

import java.io.File;
import java.util.stream.Stream;

/**
 * A class representing a directoryProperty containing images
 * 
 * @author David Slovikosky
 */
public class ImageDirectory extends ImageContainer
{
	// The icon to use for all images at the moment
	private static final Image DEFAULT_DIR_IMAGE = new Image(ImageEntry.class.getResource("/images/importWindow/directoryIcon.png").toString());
	private final ObjectProperty<Image> DEFAULT_DIRECTORY_ICON = new SimpleObjectProperty<>(DEFAULT_DIR_IMAGE);

	private ObservableList<ImageContainer> children = FXCollections.observableArrayList(imageContainer -> {
		if (imageContainer instanceof ImageEntry)
		{
			ImageEntry image = (ImageEntry) imageContainer;
			return new Observable[] {
					image.dateTakenProperty(),
					image.getFileProperty(),
					image.locationTakenProperty(),
					image.getSpeciesPresent(),
					image.getTreeIconProperty()
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

	// If this image directory is currently selected to be uploaded
	private transient BooleanProperty selectedForUpload = new SimpleBooleanProperty(false);

	// The progress of the directory upload to CyVerse
	private transient DoubleProperty uploadProgress = new SimpleDoubleProperty(-1);

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
	public ObjectProperty<Image> getTreeIconProperty()
	{
		return DEFAULT_DIRECTORY_ICON;
	}

	@Override
	public ObservableList<ImageContainer> getChildren()
	{
		return this.children;
	}

	/**
	 * Turns the recursive tree-like image directory format into a flat list of image containers
	 *
	 * @return A flat list of all image directories and image entries found in the recursive data structure
	 */
	public Stream<ImageContainer> flattened()
	{
		return Stream.concat(
				Stream.of(this),
				Stream.concat(
						this.getChildren()
								.stream()
								.filter(child -> !(child instanceof ImageDirectory)),
						this.getChildren()
								.stream()
								.filter(child -> child instanceof ImageDirectory)
								.map(child -> (ImageDirectory) child)
								.flatMap(ImageDirectory::flattened)));
	}

	/**
	 * Add a new child to this directory
	 *
	 * @param container
	 *            The container to add
	 */
	public void addChild(ImageContainer container)
	{
		this.children.add(container);
	}

	/**
	 * Remove the given container from the directory
	 * @param container The container to remove from this directory
	 * @return If the removal was successful
	 */
	public Boolean removeChild(ImageContainer container)
	{
		return this.children.remove(container);
	}

	/**
	 * Remove the container from the directory and all sub-directories
	 * @param container The container to remove
	 * @return True if the removal was successful
	 */
	public Boolean removeChildRecursive(ImageContainer container)
	{
		if (this.removeChild(container))
			return true;

		for (int i = 0; i < this.children.size(); i++)
		{
			ImageContainer containerInList = this.children.get(i);
			if (containerInList instanceof ImageDirectory && ((ImageDirectory) containerInList).removeChildRecursive(container))
				return true;
		}
		return false;
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
	 * Grab the file property
	 * @return The source file property
	 */
	public ObjectProperty<File> getFileProperty()
	{
		return this.directoryProperty;
	}

	/**
	 * Set to true if the directory is selected to be uploaded
	 *
	 * @param selectedForUpload if the directory is selected to be uploaded to cyverse
	 */
	public void setSelectedForUpload(boolean selectedForUpload)
	{
		this.selectedForUpload.setValue(selectedForUpload);
	}

	/**
	 * @return True if the directory is selected to be uploaded
	 */
	public boolean isSelectedForUpload()
	{
		return this.selectedForUpload.getValue();
	}

	/**
	 * @return The property representing if this directory is selected for upload
	 */
	public BooleanProperty selectedForUploadProperty()
	{
		return this.selectedForUpload;
	}

	public void setUploadProgress(double uploadProgress)
	{
		this.uploadProgress.setValue(uploadProgress);
	}

	public double getUploadProgress()
	{
		return this.uploadProgress.getValue();
	}

	public DoubleProperty uploadProgressProperty()
	{
		return this.uploadProgress;
	}

	/**
	 * Setting the location taken on a directory sets the location on all children recursively
	 * @param location The location to set to
	 */
	@Override
	public void setLocationTaken(Location location)
	{
		this.getChildren().forEach(child -> child.setLocationTaken(location));
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
