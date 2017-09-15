/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.image;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import library.HierarchyData;

import java.io.File;
import java.io.Serializable;

/**
 * A recursive datatype containing more image containers
 */
public abstract class ImageContainer implements HierarchyData<ImageContainer>
{
	// The file that this container represents. May be a directory or file
	public abstract File getFile();

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
