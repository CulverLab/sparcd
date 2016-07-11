/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDirectory
{
	private List<ImageEntry> images = new ArrayList<ImageEntry>();
	private List<ImageDirectory> subDirectories = new ArrayList<ImageDirectory>();
	private File directory;

	public ImageDirectory(File directory)
	{
		if (!directory.isDirectory())
			throw new RuntimeException("The specified file is not a directory!");
		this.directory = directory;
	}

	public void addSubDirectory(ImageDirectory subDirectory)
	{
		this.subDirectories.add(subDirectory);
	}

	public void addImage(ImageEntry imageEntry)
	{
		this.images.add(imageEntry);
	}

	public File getDirectory()
	{
		return directory;
	}

	public List<ImageDirectory> getSubDirectories()
	{
		return subDirectories;
	}

	public List<ImageEntry> getImages()
	{
		return images;
	}

	@Override
	public String toString()
	{
		return this.directory.getName();
	}
}
