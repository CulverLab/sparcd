package model.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.SanimalData;
import model.analysis.SanimalAnalysisUtils;
import model.constant.SanimalMetadataFields;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.MetadataUtils;
import model.util.RoundingUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A class that imports images into a more easily readable structure
 * 
 * @author David Slovikosky
 */
public class DirectoryManager
{
	/**
	 * Given a directory, this function reads all the images in the directory and tags them with the species in the list, or asks the user to input a new species
	 *
	 * @param imageDirectory The directory to read from
	 *
	 * @return A list of newly added species
	 */
	public static List<Species> detectRegisterAndTagSpecies(ImageDirectory imageDirectory)
	{
		// Grab all images in the directory
		List<ImageEntry> newImages = imageDirectory.flattened().filter(container -> container instanceof ImageEntry).map(container -> (ImageEntry) container).collect(Collectors.toList());
		List<Species> newlyAddedSpecies = new ArrayList<>();

		// Go through each of the images
		for (ImageEntry current : newImages)
		{
			try
			{
				// Read the image's metadata
				TiffImageMetadata metadata = MetadataUtils.readImageMetadata(current.getFile());


			}
			catch (ImageReadException | IOException e)
			{
				System.err.println("Exception occurred when trying to read the metadata from the file: " + current.getFile().getAbsolutePath());
				System.err.println("The error was: ");
				e.printStackTrace();
			}
		}

		// These images are not dirty, since we loaded them off disk
		newImages.forEach(imageEntry -> imageEntry.markDirty(false));

		return newlyAddedSpecies;
	}

	/**
	 * Given a directory this function validates that each file exists and if they don't adds them to the invalid containers list
	 *
	 * @param directory The directory to validate
	 * @param invalidContainers The invalid containers in this directory
	 */
	public static void performDirectoryValidation(ImageContainer directory, List<ImageContainer> invalidContainers)
	{
		if (invalidContainers == null)
			return;

		// Ensure that the file exists, otherwise add it to the invalid containers list
		if (!directory.getFile().exists())
			invalidContainers.add(directory);

		// Go through each of the children and validate them
		for (ImageContainer container : directory.getChildren())
			DirectoryManager.performDirectoryValidation(container, invalidContainers);
	}

	/**
	 * Wipe out any empty sub-directories
	 *
	 * @param directory The directory to remove empty sub-directories from
	 */
	public static void removeEmptyDirectories(ImageDirectory directory)
	{
		// Go through each child
		for (int i = 0; i < directory.getChildren().size(); i++)
		{
			// Grab the current image container
			ImageContainer imageContainer = directory.getChildren().get(i);
			// If it's a directory, recursively remove image directories from it
			if (imageContainer instanceof ImageDirectory)
			{
				// Remove empty directories from this directory
				DirectoryManager.removeEmptyDirectories((ImageDirectory) imageContainer);
				// If it's empty, remove this directory and reduce I since we don't want to get an index out of bounds exception
				if (imageContainer.getChildren().isEmpty())
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
	public static ImageDirectory loadDirectory(File imageOrLocation, List<Location> knownLocations, List<Species> knownSpecies)
	{
		ImageDirectory toReturn;
		if (!imageOrLocation.isDirectory())
		{
			// If it's not a directory, then just add the image
			toReturn = new ImageDirectory(imageOrLocation.getParentFile());
			toReturn.addImage(new ImageEntry(imageOrLocation, knownLocations, knownSpecies));
		}
		else
		{
			// If it is a directory, recursively create it
			toReturn = new ImageDirectory(imageOrLocation);
			DirectoryManager.createDirectoryAndImageTree(toReturn, knownLocations, knownSpecies);
		}
		return toReturn;
	}

	/**
	 * Recursively create the directory structure
	 * 
	 * @param current
	 *            The current directory to work on
	 */
	private static void createDirectoryAndImageTree(ImageDirectory current, List<Location> knownLocations, List<Species> knownSpecies)
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
					current.addImage(new ImageEntry(file, knownLocations, knownSpecies));
				}
				// Add all subdirectories to the directory
				else if (file.isDirectory())
				{
					ImageDirectory subDirectory = new ImageDirectory(file);
					current.addChild(subDirectory);
					DirectoryManager.createDirectoryAndImageTree(subDirectory, knownLocations, knownSpecies);
				}
			}
		}
	}

	/**
	 * Given an image directory, this will create a TAR file out of the directory
	 *
	 * @param directory The image directory to TAR
	 *
	 * @return The TAR file
	 */
	public static File directoryToTar(ImageDirectory directory)
	{
		try
		{
			// Create a temporarily TAR file to write to
			File tempZip = SanimalData.getInstance().getTempDirectoryManager().createTempFile("tarToUpload.tar");
			// Create a TAR output stream to write to
			TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(tempZip));

			// Write the directory to the TAR file
			writeSpecificDirectoryToTar(tarOut, directory, "/");

			// Flush the file and close it. We delete the TAR after the program closes
			tarOut.flush();
			tarOut.close();
			return tempZip;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// If something goes wrong, return null
		return null;
	}

	/**
	 * Given an output stream and a current directory, this will recursively build the TAR file given the current directory level
	 *
	 * @param tarOut The tar file to write to
	 * @param currentDir The current directory in the recursion
	 * @param currentPath The path in the TAR file in this recursion iteration
	 */
	private static void writeSpecificDirectoryToTar(TarArchiveOutputStream tarOut, ImageDirectory currentDir, String currentPath)
	{
		// The new path to write to this iteration of the recursion
		String newPath = currentPath + currentDir.getFile().getName() + "/";

		// Go through each child, and add it to the tar
		currentDir.getChildren().filtered(imageContainer -> imageContainer instanceof ImageEntry).forEach(imageContainer ->
		{
			ImageEntry imageEntry = (ImageEntry) imageContainer;
			try
			{
				// Create an archive entry for the image
				ArchiveEntry archiveEntry = tarOut.createArchiveEntry(imageEntry.getFile(), newPath + imageEntry.getFile().getName());
				// Put the archive entry into the TAR file
				tarOut.putArchiveEntry(archiveEntry);
				// Write all the bytes in the file into the TAR file
				tarOut.write(Files.readAllBytes(imageEntry.getFile().toPath()));
				// Finish writing the TAR entry
				tarOut.closeArchiveEntry();
			}
			catch (IOException e)
			{
				System.out.println("Error creating tar archive entry!");
				e.printStackTrace();
			}
		});
		// After doing all the images in the directory, we recursively move down the structure and do sub-directories
		currentDir.getChildren().filtered(imageContainer -> imageContainer instanceof ImageDirectory).forEach(imageContainer -> {
			writeSpecificDirectoryToTar(tarOut, (ImageDirectory) imageContainer, newPath);
		});
	}
}
