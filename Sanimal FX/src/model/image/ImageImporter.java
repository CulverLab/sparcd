package model.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.analysis.SanimalAnalysisUtils;

/**
 * A class that imports images into a more easily readable structure
 * 
 * @author David Slovikosky
 */
public class ImageImporter
{
	public static void performDirectoryValidation(ImageDirectory directory, List<ImageContainer> invalidContainers)
	{
		if (!directory.getFile().exists())
			invalidContainers.add(directory);

		for (ImageContainer container : directory.getChildren())
		{
			if (!container.getFile().exists())
				invalidContainers.add(container);
			if (container instanceof ImageDirectory)
				ImageImporter.performDirectoryValidation((ImageDirectory) container, invalidContainers);
		}
	}

	public static void removeEmptyDirectories(ImageDirectory directory)
	{
		for (int i = 0; i < directory.getChildren().size(); i++)
		{
			ImageContainer imageContainer = directory.getChildren().get(i);
			if (imageContainer instanceof ImageDirectory)
			{
				removeEmptyDirectories((ImageDirectory) imageContainer);
				if (((ImageDirectory) imageContainer).getChildren().isEmpty())
				{
					directory.getChildren().remove(i);
					i--;
				}
			}
		}
	}

	/**
	 * Set the head directory to the given file
	 * 
	 * @param imageOrLocation
	 *            The file to make into a directory
	 */
	public static ImageDirectory loadDirectory(File imageOrLocation)
	{
		ImageDirectory toReturn;
		if (!imageOrLocation.isDirectory())
		{
			// If it's not a directory, then just add the image
			toReturn = new ImageDirectory(imageOrLocation.getParentFile());
			toReturn.addImage(new ImageEntry(imageOrLocation));
		}
		else
		{
			// If it is a directory, recursively create it
			toReturn = new ImageDirectory(imageOrLocation);
			ImageImporter.createDirectoryAndImageTree(toReturn);
		}
		return toReturn;
	}

	/**
	 * Recursively create the directory structure
	 * 
	 * @param current
	 *            The current directory to work on
	 */
	private static void createDirectoryAndImageTree(ImageDirectory current)
	{
		File[] subFiles = current.getFile().listFiles();

		if (subFiles != null)
		{
			// Get all files in the directory
			for (File file : subFiles)
			{
				// Add all image files to the directory
				if (SanimalAnalysisUtils.fileIsImage(file))
				{
					current.addImage(new ImageEntry(file));
				}
				// Add all subdirectories to the directory
				else if (file.isDirectory())
				{
					ImageDirectory subDirectory = new ImageDirectory(file);
					current.addSubDirectory(subDirectory);
					ImageImporter.createDirectoryAndImageTree(subDirectory);
				}
			}
		}
	}
}
