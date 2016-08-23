package model.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a directory containing images
 * 
 * @author David Slovikosky
 */
public class ImageDirectory implements IImageContainer
{
	// A list of images in the directory
	private List<ImageEntry> images = new ArrayList<ImageEntry>();
	// A list of subdirectories
	private List<ImageDirectory> subDirectories = new ArrayList<ImageDirectory>();
	// The file representing the directory
	private File directory;

	/**
	 * Construct an image directory
	 * 
	 * @param directory
	 *            The file that represents the directory
	 */
	public ImageDirectory(File directory)
	{
		if (!directory.isDirectory())
			throw new RuntimeException("The specified file is not a directory!");
		this.directory = directory;
	}

	/**
	 * Add a new subdirctory to this directory
	 * 
	 * @param subDirectory
	 *            The directory to add
	 */
	public void addSubDirectory(ImageDirectory subDirectory)
	{
		this.subDirectories.add(subDirectory);
	}

	/**
	 * Add an image to this directory
	 * 
	 * @param imageEntry
	 *            The image to add
	 */
	public void addImage(ImageEntry imageEntry)
	{
		this.images.add(imageEntry);
	}

	/**
	 * Get the file representing this directory
	 * 
	 * @return The file representing this directory
	 */
	@Override
	public File getFile()
	{
		return directory;
	}

	/**
	 * Set the file that this directory represents
	 * 
	 * @param file
	 *            The file that this directory represents
	 */
	@Override
	public void setFile(File file)
	{
		this.directory = file;
	}

	/**
	 * Get the subdirectories
	 * 
	 * @return The list of subdirectories
	 */
	public List<ImageDirectory> getSubDirectories()
	{
		return subDirectories;
	}

	/**
	 * Get the list of images in the directory
	 * 
	 * @return The list of images
	 */
	public List<ImageEntry> getImages()
	{
		return images;
	}

	/**
	 * @return The string representing this directory
	 */
	@Override
	public String toString()
	{
		return this.directory.getName();
	}
}
