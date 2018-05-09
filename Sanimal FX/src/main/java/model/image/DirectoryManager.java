package model.image;

import javafx.scene.control.Alert;
import model.SanimalData;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.species.Species;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.irods.jargon.core.pub.domain.AvuData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A class that imports images into a more easily readable structure
 * 
 * @author David Slovikosky
 */
public class DirectoryManager
{
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
			ImageEntry imageEntry = new ImageEntry(imageOrLocation);
			imageEntry.readFileMetadataIntoImage(knownLocations, knownSpecies);
			imageEntry.initIconBindings();
			toReturn.addImage(imageEntry);
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
					ImageEntry imageEntry = new ImageEntry(file);
					imageEntry.readFileMetadataIntoImage(knownLocations, knownSpecies);
					imageEntry.initIconBindings();
					current.addImage(imageEntry);
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
	 * @param directoryMetaJSON The JSON file representing this image directory
	 * @param imageToMetadata The CSV file representing each image's metadata
	 * @return The TAR file
	 */
	public static File[] directoryToTars(ImageDirectory directory, File directoryMetaJSON, Function<ImageEntry, String> imageToMetadata, Integer maxImagesPerTar)
	{
		maxImagesPerTar = maxImagesPerTar - 1;
		try
		{
			// List of images to be uploaded
			List<ImageEntry> imageEntries = directory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList());

			// Take the number of images / maximum number of images per tar to get the number of tar files we need
			Integer numberOfTars = (int) Math.ceil((double) imageEntries.size() / (double) maxImagesPerTar);
			Integer imagesPerTar = (int) Math.ceil((double) imageEntries.size() / (double) numberOfTars);
			// Create an array of tars
			File[] tars = new File[numberOfTars];

			// Get the path to the top level directory
			String topDirectory = directory.getFile().getParentFile().getAbsolutePath();

			for (Integer tarIndex = 0; tarIndex < numberOfTars; tarIndex++)
			{
				// Create a temporarily TAR file to write to
				File tempTar = SanimalData.getInstance().getTempDirectoryManager().createTempFile("tarToUpload.tar");
				// Create a TAR output stream to write to
				TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(tempTar));

				File tempMetaCSV = SanimalData.getInstance().getTempDirectoryManager().createTempFile("meta.csv");
				tempMetaCSV.createNewFile();

				PrintWriter metaOut = new PrintWriter(tempMetaCSV);
				for (Integer imageIndex = tarIndex * imagesPerTar; imageIndex < (tarIndex + 1) * imagesPerTar && imageIndex < imageEntries.size(); imageIndex++)
				{
					ImageEntry imageEntry = imageEntries.get(imageIndex);
					// Create an archive entry for the image
					String tarPath = StringUtils.substringAfter(imageEntry.getFile().getAbsolutePath(), topDirectory).replace('\\', '/');
					ArchiveEntry archiveEntry = tarOut.createArchiveEntry(imageEntry.getFile(), tarPath);
					// Put the archive entry into the TAR file
					tarOut.putArchiveEntry(archiveEntry);
					// Write all the bytes in the file into the TAR file
					tarOut.write(Files.readAllBytes(imageEntry.getFile().toPath()));
					// Finish writing the TAR entry
					tarOut.closeArchiveEntry();

					// Write a metadata entry into our meta-X.csv file
					metaOut.write(imageToMetadata.apply(imageEntry));
				}
				// Close the writer to the metadata file
				metaOut.close();

				// If this is the first tar file, include the UploadMeta.csv file
				if (tarIndex == 0)
				{
					// Create an archive entry for the upload meta file
					ArchiveEntry archiveEntry = tarOut.createArchiveEntry(directoryMetaJSON, "/UploadMeta.json");
					// Put the archive entry into the TAR file
					tarOut.putArchiveEntry(archiveEntry);
					// Write all the bytes in the file into the TAR file
					tarOut.write(Files.readAllBytes(directoryMetaJSON.toPath()));
					// Finish writing the TAR entry
					tarOut.closeArchiveEntry();
				}

				// Create an archive entry for the metaCSV file
				ArchiveEntry archiveEntry = tarOut.createArchiveEntry(tempMetaCSV, "/meta-" + tarIndex.toString() + ".csv");
				// Put the archive entry into the TAR file
				tarOut.putArchiveEntry(archiveEntry);
				// Write all the bytes in the file into the TAR file
				tarOut.write(Files.readAllBytes(tempMetaCSV.toPath()));
				// Finish writing the TAR entry
				tarOut.closeArchiveEntry();

				// Flush the file and close it. We delete the TAR after the program closes
				tarOut.flush();
				tarOut.close();

				// Store the tar path
				tars[tarIndex] = tempTar;
			}

			return tars;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// If something goes wrong, return a blank array
		return new File[0];
	}

	/**
	 * Parses a directory assuming its in Dr. Sanderson's format
	 *
	 * @param directory The directory in dr. sanderson's format
	 * @param knownLocations The current list of locations
	 * @param knownSpecies The current list of species
	 */
	public static void parseLegacyDirectory(ImageDirectory directory, List<Location> knownLocations, List<Species> knownSpecies)
	{
		// Iterate over all location directories
		directory.getChildren().stream().filter(imageContainer -> imageContainer instanceof ImageDirectory).map(imageContainer -> (ImageDirectory) imageContainer).forEach(locationDirectory ->
		{
			// Get the location name
			String locationName = locationDirectory.getFile().getName();
			Optional<Location> locationOpt = knownLocations.stream().filter(location -> location.getName().equalsIgnoreCase(locationName)).findFirst();
			Location currentLocation;
			// Get the location if it exists
			if (locationOpt.isPresent())
			{
				currentLocation = locationOpt.get();
			}
			// Create the location if it does not
			else
			{
				currentLocation = new Location();
				currentLocation.setName(locationName);
				currentLocation.setId("None");
				currentLocation.setElevation(0.0);
				currentLocation.setLat(0.0);
				currentLocation.setLng(0.0);
				knownLocations.add(currentLocation);
			}

			// Iterate over all species directories
			locationDirectory.getChildren().stream().filter(imageContainer -> imageContainer instanceof ImageDirectory).map(imageContainer -> (ImageDirectory) imageContainer).forEach(speciesDirectory ->
			{
				// Get the species name
				String speciesName = speciesDirectory.getFile().getName();
				Optional<Species> speciesOpt = knownSpecies.stream().filter(species -> species.getName().equalsIgnoreCase(speciesName)).findFirst();
				Species currentSpecies;
				// Get the species if it exists
				if (speciesOpt.isPresent())
				{
					currentSpecies = speciesOpt.get();
				}
				// Create the species if it does not
				else
				{
					currentSpecies = new Species();
					currentSpecies.setName(speciesName);
					currentSpecies.setSpeciesIcon(Species.DEFAULT_ICON);
					knownSpecies.add(currentSpecies);
				}

				// Iterate over all species count directories
				speciesDirectory.getChildren().stream().filter(imageContainer -> imageContainer instanceof ImageDirectory).map(imageContainer -> (ImageDirectory) imageContainer).forEach(countDirectory ->
				{
					try
					{
						// Try to parse the count
						Integer speciesCount = Integer.parseInt(countDirectory.getFile().getName());
						// If the parse succeeds, overwrite the location if a location is on the image, and overwrite the species if species are on the image
						countDirectory.getChildren().stream().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).forEach(imageEntry ->
						{
							if (imageEntry.getLocationTaken() == null)
								imageEntry.setLocationTaken(currentLocation);
							if (imageEntry.getSpeciesPresent().isEmpty())
								imageEntry.addSpecies(currentSpecies, speciesCount);
						});
					}
					catch (NumberFormatException ignored) {}
				});
			});
		});
	}
}
