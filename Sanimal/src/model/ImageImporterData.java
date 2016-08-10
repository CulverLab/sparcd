package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A class that imports images into a more easily readable structure
 * 
 * @author David Slovikosky
 */
public class ImageImporterData
{
	// The head of the directory
	private ImageDirectory head;

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
		for (File file : current.getDirectory().listFiles())
		{
			// Add all image files to the directory
			if (fileIsImage(file))
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
	 * Test if a file is an image
	 * 
	 * @param file
	 *            The file to test
	 * @return True if the file is an image, false if not
	 */
	private boolean fileIsImage(File file)
	{
		String result = null;
		try
		{
			result = Files.probeContentType(file.toPath());
		}
		catch (IOException e)
		{
		}
		if (result == null || !result.startsWith("image"))
			return false;
		return true;
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
}
