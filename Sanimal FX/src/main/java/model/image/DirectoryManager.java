package model.image;

import java.io.File;
import java.io.IOException;
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
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
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
				TiffImageMetadata metadata = MetadataUtils.readImageMetadata(current);

				// Make sure it actually has metadata to read...
				if (metadata != null)
				{
					String[] fieldValue = metadata.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
					// 2015:07:21 02:02:44

					// Grab the species field from the metadata
					String[] speciesField = metadata.getFieldValue(SanimalMetadataFields.SPECIES_ENTRY);
					// Ensure that the field does actually exist...
					if (speciesField != null)
					{
						// Go through each of the species entries in the species field
						// For some reason, the last element of the speciesField array will always be null. No idea why...
						for (String speciesEntry : speciesField)
						{
							if (speciesEntry != null)
							{
								// Unpack the species entry by splitting it by the comma delimiter
								String[] speciesEntryUnpacked = StringUtils.splitByWholeSeparator(speciesEntry, ",");
								// Should be in the format: Name, ScientificName, Amount, so the length should be 3
								if (speciesEntryUnpacked.length == 3)
								{
									// Grab the three fields
									String speciesName = StringUtils.trim(speciesEntryUnpacked[0]);
									String speciesScientificName = StringUtils.trim(speciesEntryUnpacked[1]);
									String speciesCount = StringUtils.trim(speciesEntryUnpacked[2]);

									// Check to see if we already have a species with the scientific and regular name
									Optional<Species> correctSpecies =
											Stream.concat(
													SanimalData.getInstance().getSpeciesList().stream(),
													newlyAddedSpecies.stream())
											.filter(species ->
													StringUtils.equalsIgnoreCase(species.getName(), speciesName) &&
													StringUtils.equalsIgnoreCase(species.getScientificName(), speciesScientificName))
											.findFirst();

									// We need to parse a string into an integer so ensure that this doesn't crash using a try & catch
									try
									{
										// Do we have a species? If so tag this image with the species and amount
										if (correctSpecies.isPresent())
										{
											current.getSpeciesPresent().add(new SpeciesEntry(correctSpecies.get(), Integer.parseInt(speciesCount)));
										}
										// We got a species that was not registered in the program, what do we do?
										else
										{
											Species newSpecies = new Species(speciesName, speciesScientificName);
											newlyAddedSpecies.add(newSpecies);
											current.addSpecies(newSpecies, Integer.parseInt(speciesCount));
										}
									}
									catch (NumberFormatException ignored)
									{
										System.err.println("Found an image with an invalid species count. The count was: " + speciesCount);
									}
								}
							}
						}
					}
				}
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
	 * Given a directory, this function reads all the images in the directory and tags them with the location
	 *
	 * @param imageDirectory The directory to read from
	 *
	 * @return A list of newly added locations
	 */
	public static List<Location> detectRegisterAndTagLocations(ImageDirectory imageDirectory)
	{
		// Grab all images in the directory
		List<ImageEntry> newImages = imageDirectory.flattened().filter(container -> container instanceof ImageEntry).map(container -> (ImageEntry) container).collect(Collectors.toList());
		List<Location> newlyAddedLocations = new ArrayList<>();

		// Go through each of the images
		for (ImageEntry current : newImages)
		{
			try
			{
				// Read the image's metadata
				TiffImageMetadata metadata = MetadataUtils.readImageMetadata(current);

				// Make sure it actually has metadata to read...
				if (metadata != null)
				{
					// Grab the species field from the metadata
					String[] locationField = metadata.getFieldValue(SanimalMetadataFields.LOCATION_ENTRY);
					// Ensure that the field does actually exist...
					if (locationField != null)
					{
						// We look for length 3
						if (locationField.length == 3)
						{
							// Grab the location, location id, elevation, and lat/lng
							String locationName = locationField[0];
							String locationElevation = locationField[1];
							String locationId = locationField[2];
							double locationLatitude = RoundingUtils.roundLat(metadata.getGPS().getLatitudeAsDegreesNorth());
							double locationLongitude = RoundingUtils.roundLng(metadata.getGPS().getLongitudeAsDegreesEast());

							// Use a try & catch to parse the elevation
							try
							{
								// Find a matching location. It must have:
								// The same name
								// A latitude .00001 units apart from the original
								// A longitude .00001 units apart from the original
								// An elevation 25 units apart from the original location
								Optional<Location> correctLocation =
										Stream.concat(
												SanimalData.getInstance().getLocationList().stream(),
												newlyAddedLocations.stream())
										.filter(location ->
												StringUtils.equalsIgnoreCase(location.getId(), locationId) &&
												Math.abs(location.getLat() - locationLatitude) < 0.0001 &&
												Math.abs(location.getLng() - locationLongitude) < 0.0001)// For now, ignore elevation Math.abs(location.getElevation() - Double.parseDouble(locationElevation)) < 25)
										.findFirst();

								if (correctLocation.isPresent())
								{
									current.setLocationTaken(correctLocation.get());
								}
								else
								{
									Location newLocation = new Location(locationName, locationId, locationLatitude, locationLongitude, Double.parseDouble(locationElevation));
									newlyAddedLocations.add(newLocation);
									current.setLocationTaken(newLocation);
								}
							}
							catch (NumberFormatException ignored)
							{
								System.err.println("Found an image with an invalid location elevation. The elevation was: " + locationElevation);
							}
						}
					}
				}
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

		return newlyAddedLocations;
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
			DirectoryManager.createDirectoryAndImageTree(toReturn);
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
					current.addChild(subDirectory);
					DirectoryManager.createDirectoryAndImageTree(subDirectory);
				}
			}
		}
	}
}
