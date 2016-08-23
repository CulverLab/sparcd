package model.image;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import model.analysis.SanimalAnalysisUtils;

/**
 * A class that imports images into a more easily readable structure
 * 
 * @author David Slovikosky
 */
public class ImageImporterData extends Observable implements Serializable
{
	// The head of the directory
	private ImageDirectory head;
	// A list of all currently invalid image containers
	private transient List<IImageContainer> invalidContainers = new ArrayList<IImageContainer>();

	public void performDirectoryValidation()
	{
		this.invalidContainers.clear();
		if (this.head != null)
			this.validateDirectory(head, invalidContainers);
		if (!invalidContainers.isEmpty())
		{
			this.setChanged();
			this.notifyObservers(ImageUpdate.InvalidImageContainersDetected);
		}
	}

	private void validateDirectory(ImageDirectory directory, List<IImageContainer> invalidContainers)
	{
		if (!directory.getFile().exists())
			invalidContainers.add(directory);

		for (ImageEntry imageEntry : directory.getImages())
			if (!imageEntry.getFile().exists())
				invalidContainers.add(imageEntry);

		for (ImageDirectory subDirectory : directory.getSubDirectories())
			this.validateDirectory(subDirectory, invalidContainers);
	}

	/**
	 * Loads an existing image directory into the program
	 * 
	 * @param newDirectory
	 *            The new source image directory
	 */
	public void loadImagesFromExistingDirectory(ImageDirectory newDirectory)
	{
		this.head = newDirectory;
		this.setChanged();
		this.notifyObservers(ImageUpdate.NewDirectorySelected);
	}

	/**
	 * Set the head directory to the given file
	 * 
	 * @param imageOrLocation
	 *            The file to make into a directory
	 * @param recursive
	 *            Recursive search will recursively search through the the directories under the current one
	 */
	public void readAndAddImages(File imageOrLocation, boolean recursive)
	{
		if (!imageOrLocation.isDirectory())
		{
			// If it's not a directory, then just add the image
			this.head = new ImageDirectory(imageOrLocation.getParentFile());
			this.head.addImage(new ImageEntry(imageOrLocation));
		}
		else
		{
			// If it is a directory, recursively create it
			this.head = new ImageDirectory(imageOrLocation);
			this.createDirectoryAndImageTree(this.head, recursive);
		}
		this.setChanged();
		this.notifyObservers(ImageUpdate.NewDirectorySelected);
	}

	/**
	 * Recursively create the directory structure
	 * 
	 * @param current
	 *            The current directory to work on
	 * @param recursive
	 *            If it should continue recursively going through directories
	 */
	private void createDirectoryAndImageTree(ImageDirectory current, boolean recursive)
	{
		// Get all files in the directory
		for (File file : current.getFile().listFiles())
		{
			// Add all image files to the directory
			if (SanimalAnalysisUtils.fileIsImage(file))
			{
				current.addImage(new ImageEntry(file));
			}
			// Add all subdirectories to the directory
			else if (recursive && file.isDirectory())
			{
				ImageDirectory subDirectory = new ImageDirectory(file);
				current.addSubDirectory(subDirectory);
				this.createDirectoryAndImageTree(subDirectory, recursive);
			}
		}
	}

	/**
	 * Get the head directory
	 * 
	 * @return the head directory
	 */
	public ImageDirectory getHeadDirectory()
	{
		return this.head;
	}

	/**
	 * Get the list of invalid containers
	 * 
	 * @return A list of invalid containers
	 */
	public List<IImageContainer> getInvalidContainers()
	{
		return invalidContainers;
	}
}
