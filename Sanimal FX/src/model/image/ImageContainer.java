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

public abstract class ImageContainer implements HierarchyData<ImageContainer>
{
	public abstract File getFile();

	public abstract void setFile(File file);

	@Override
	public String toString()
	{
		return this.getFile().getName();
	}

	@Override
	public ObservableList<ImageContainer> getChildren()
	{
		return FXCollections.emptyObservableList();
	}
}
