package model.image;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import library.HierarchyData;
import model.location.Location;

import java.io.File;

/**
 * A recursive datatype containing more image containers
 */
public abstract class ImageContainer implements HierarchyData<ImageContainer>
{
	// The file that this container represents. May be a directory or file
	public abstract File getFile();

	// Sets the location taken of the given image container
	public abstract void setLocationTaken(Location location);

	/**
	 * To string just prints out the file name by default
	 * @return The file name
	 */
	@Override
	public String toString()
	{
		return this.getFile().getName();
	}

	/**
	 * Since this datatype is recursive, return an empty list by default. Override this to get other behavior
	 *
	 * @return A list of children which makes this datatype recursive
	 */
	@Override
	public ObservableList<ImageContainer> getChildren()
	{
		return FXCollections.emptyObservableList();
	}
}
