package model.image;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.MetadataUtils;
import model.util.RoundingUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * A class representing an image file
 * 
 * @author David Slovikosky
 */
public class ImageEntry extends ImageContainer
{
	private static final DateTimeFormatter DATE_FORMAT_FOR_DISK = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

	// The icon to use for all images at the moment
	private static final Image DEFAULT_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIcon.png").toString());
	// The icon to use for all location only tagged images at the moment
	private static final Image LOCATION_ONLY_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconLocation.png").toString());
	// The icon to use for all species only tagged images at the moment
	private static final Image SPECIES_ONLY_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconSpecies.png").toString());
	// The icon to use for all tagged images at the moment
	private static final Image CHECKED_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconDone.png").toString());

	// A property to wrap the currently selected image property. Must not be static!
	transient final ObjectProperty<Image> selectedImageProperty = new SimpleObjectProperty<>(DEFAULT_IMAGE_ICON);
	// The actual file 
	private final ObjectProperty<File> imageFileProperty = new SimpleObjectProperty<File>();
	// The date that the image was taken
	private final ObjectProperty<LocalDateTime> dateTakenProperty = new SimpleObjectProperty<>();
	// The location that the image was taken
	private final ObjectProperty<Location> locationTakenProperty = new SimpleObjectProperty<Location>();
	// The species present in the image
	private final ObservableList<SpeciesEntry> speciesPresent = FXCollections.<SpeciesEntry> observableArrayList(image -> new Observable[] {
			image.countProperty(),
			image.speciesProperty()
	});
	// If this image is dirty, we set a flag to write it to disk at some later point
	private transient final AtomicBoolean isDiskDirty = new AtomicBoolean(false);

	/**
	 * Create a new image entry with an image file
	 * 
	 * @param file
	 *            The file (must be an image file)
	 */
	public ImageEntry(File file)
	{
		this.imageFileProperty.setValue(file);

		this.locationTakenProperty.addListener((observable, oldValue, newValue) -> this.markDiskDirty(true));
		this.speciesPresent.addListener((ListChangeListener<SpeciesEntry>) c -> this.markDiskDirty(true));
		this.dateTakenProperty.addListener((observable, oldValue, newValue) -> this.markDiskDirty(true));
	}

	/**
	 * Reads the file metadata and initializes fields
	 */
	public void readFileMetadataIntoImage(List<Location> knownLocations, List<Species> knownSpecies)
	{
		try
		{
			// Set the date to a default
			this.dateTakenProperty.setValue(LocalDateTime.now());
			//Read the metadata off of the image
			TiffImageMetadata tiffImageMetadata = MetadataUtils.readImageMetadata(this.getFile());

			// Read date, location, and species
			this.readDateFromMetadata(tiffImageMetadata);
			this.readLocationFromMetadata(tiffImageMetadata, knownLocations);
			this.readSpeciesFroMetadata(tiffImageMetadata, knownSpecies);

			this.markDiskDirty(false);
		}
		catch (ImageReadException | IOException e)
		{
			SanimalData.getInstance().getErrorDisplay().notify("Error reading image metadata for file " + this.getFile().getName() + "!\n" + ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Reads the date off of an image given metadata
	 *
	 * @param tiffImageMetadata The image metadata
	 * @throws ImageReadException If the image read fails
	 */
	private void readDateFromMetadata(TiffImageMetadata tiffImageMetadata) throws ImageReadException
	{
		if (tiffImageMetadata != null)
		{
			// Grab the date taken from the metadata
			String[] dateTaken = tiffImageMetadata.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			if (dateTaken != null && dateTaken.length == 1)
				this.dateTakenProperty.setValue(LocalDateTime.parse(dateTaken[0], DATE_FORMAT_FOR_DISK));
		}
	}

	/**
	 * Reads the location off of an image given metadata
	 *
	 * @param tiffImageMetadata The image metadata
	 * @param knownLocations The current list of known locations
	 * @throws ImageReadException If the image read fails
	 */
	private void readLocationFromMetadata(TiffImageMetadata tiffImageMetadata, List<Location> knownLocations) throws ImageReadException
	{
		// Make sure it actually has metadata to read...
		if (tiffImageMetadata != null)
		{
			// Grab the species field from the metadata
			String[] locationField = tiffImageMetadata.getFieldValue(SanimalMetadataFields.LOCATION_ENTRY);
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
					double locationLatitude = RoundingUtils.roundLat(tiffImageMetadata.getGPS().getLatitudeAsDegreesNorth());
					double locationLongitude = RoundingUtils.roundLng(tiffImageMetadata.getGPS().getLongitudeAsDegreesEast());

					// Use a try & catch to parse the elevation
					try
					{
						// Find a matching location. It must have:
						// The same name
						// A latitude .00001 units apart from the original
						// A longitude .00001 units apart from the original
						// An elevation 25 units apart from the original location
						Optional<Location> correctLocation =
							knownLocations
								.stream()
								.filter(location ->
										StringUtils.equalsIgnoreCase(location.getId(), locationId) &&
												Math.abs(location.getLatitude() - locationLatitude) < 0.0001 &&
												Math.abs(location.getLongitude() - locationLongitude) < 0.0001)// For now, ignore elevation Math.abs(location.elevationProperty() - Double.parseDouble(locationElevation)) < 25)
								.findFirst();

						if (correctLocation.isPresent())
						{
							this.setLocationTaken(correctLocation.get());
						}
						else
						{
							Location newLocation = new Location(locationName, locationId, locationLatitude, locationLongitude, Double.parseDouble(locationElevation));
							knownLocations.add(newLocation);
							this.setLocationTaken(newLocation);
						}
					}
					catch (NumberFormatException ignored)
					{
						SanimalData.getInstance().getErrorDisplay().notify("Error parsing elevation for image, it was " + locationElevation + "!");
					}
				}
			}
		}
	}

	/**
	 * Reads the species off of an image given metadata
	 *
	 * @param tiffImageMetadata The image metadata
	 * @param knownSpecies The current list of known species
	 * @throws ImageReadException If the image read fails
	 */
	private void readSpeciesFroMetadata(TiffImageMetadata tiffImageMetadata, List<Species> knownSpecies) throws ImageReadException
	{
		// Make sure it actually has metadata to read...
		if (tiffImageMetadata != null)
		{
			// Grab the species field from the metadata
			String[] speciesField = tiffImageMetadata.getFieldValue(SanimalMetadataFields.SPECIES_ENTRY);
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
								knownSpecies
									.stream()
									.filter(species ->
											StringUtils.equalsIgnoreCase(species.getCommonName(), speciesName) &&
													StringUtils.equalsIgnoreCase(species.getScientificName(), speciesScientificName))
									.findFirst();

							// We need to parse a string into an integer so ensure that this doesn't crash using a try & catch
							try
							{
								// Do we have a species? If so tag this image with the species and amount
								if (correctSpecies.isPresent())
								{
									this.getSpeciesPresent().add(new SpeciesEntry(correctSpecies.get(), Integer.parseInt(speciesCount)));
								}
								// We got a species that was not registered in the program, what do we do?
								else
								{
									Species newSpecies = new Species(speciesName, speciesScientificName);
									knownSpecies.add(newSpecies);
									this.addSpecies(newSpecies, Integer.parseInt(speciesCount));
								}
							}
							catch (NumberFormatException ignored)
							{
								SanimalData.getInstance().getErrorDisplay().notify("Error parsing species count for image, it was " + speciesCount + "!");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Used to initialize icon bindings to their default
	 */
	public void initIconBindings()
	{
		// Bind the image property to a conditional expression.
		// The image is checked if the location is valid and the species present list is not empty
		Binding<Image> imageBinding = Bindings.createObjectBinding(() ->
		{
			if (this.getLocationTaken() != null && this.getLocationTaken().locationValid() && !this.getSpeciesPresent().isEmpty())
				return CHECKED_IMAGE_ICON;
			else if (!this.getSpeciesPresent().isEmpty())
				return SPECIES_ONLY_IMAGE_ICON;
			else if (this.getLocationTaken() != null && this.getLocationTaken().locationValid())
				return LOCATION_ONLY_IMAGE_ICON;
			return DEFAULT_IMAGE_ICON;
		}, this.locationTakenProperty, this.speciesPresent);
		selectedImageProperty.bind(imageBinding);
	}

	/**
	 * Getter for the tree icon property
	 *
	 * @return The tree icon to be used
	 */
	@Override
	public ObjectProperty<Image> getTreeIconProperty()
	{
		return selectedImageProperty;
	}

	/**
	 * Get the image file
	 * 
	 * @return The image file
	 */
	public File getFile()
	{
		return this.imageFileProperty.getValue();
	}

	/**
	 * Get the image file property that this image represents
	 *
	 * @return The file property that this image represents
	 */
	public ObjectProperty<File> getFileProperty()
	{
		return this.imageFileProperty;
	}

	public void setDateTaken(LocalDateTime date)
	{
		this.dateTakenProperty.setValue(date);
	}

	/**
	 * Returns the date the image was taken
	 * 
	 * @return The date the image was taken
	 */
	public LocalDateTime getDateTaken()
	{
		//this.validateDate();
		return dateTakenProperty.getValue();
	}

	/**
	 * Returns the date property of the image
	 *
	 * @return The date the image was taken property
	 */
	public ObjectProperty<LocalDateTime> dateTakenProperty()
	{
		return dateTakenProperty;
	}

	/**
	 * Set the location that the image was taken at
	 * 
	 * @param location
	 *            The location
	 */
	public void setLocationTaken(Location location)
	{
		this.locationTakenProperty.setValue(location);
	}

	/**
	 * Return the location that the image was taken
	 * 
	 * @return The location
	 */
	public Location getLocationTaken()
	{
		return locationTakenProperty.getValue();
	}

	public ObjectProperty<Location> locationTakenProperty()
	{
		return locationTakenProperty;
	}

	/**
	 * Add a new species to the image
	 *
	 * @param species
	 *            The species of the animal
	 * @param amount
	 *            The number of animals in the image
	 */
	public void addSpecies(Species species, Integer amount)
	{
		// Grab the old species entry for the given species if present, and then add the amounts
		Optional<SpeciesEntry> currentEntry = this.speciesPresent.stream().filter(speciesEntry -> speciesEntry.getSpecies().equals(species)).findFirst();
		int oldAmount = currentEntry.map(SpeciesEntry::getCount).orElse(0);
		this.removeSpecies(species);
		this.speciesPresent.add(new SpeciesEntry(species, amount + oldAmount));
	}

	/**
	 * Remove a species from the list of image species
	 * 
	 * @param species
	 *            The species to remove
	 */
	public void removeSpecies(Species species)
	{
		this.speciesPresent.removeIf(entry -> entry.getSpecies() == species);
	}

	/**
	 * Get the list of present species
	 * 
	 * @return A list of present species
	 */
	public ObservableList<SpeciesEntry> getSpeciesPresent()
	{
		return speciesPresent;
	}

	public void markDiskDirty(Boolean dirty)
	{
		this.isDiskDirty.set(dirty);
	}

	public Boolean isDiskDirty()
	{
		return this.isDiskDirty.get();
	}

	/**
	 * Writes the species and location tagged in this image to the disk
	 */
	public synchronized void writeToDisk()
	{
		try
		{
			// Read the output set from the image entry
			TiffOutputSet outputSet = MetadataUtils.readOutputSet(this);

			// Grab the EXIF directory from the output set
			TiffOutputDirectory exif = outputSet.getOrCreateExifDirectory();
			exif.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			exif.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, DATE_FORMAT_FOR_DISK.format(this.getDateTaken()));

			// Grab the sanimal directory from the output set
			TiffOutputDirectory directory = MetadataUtils.getOrCreateSanimalDirectory(outputSet);

			// Remove the species field if it exists
			directory.removeField(SanimalMetadataFields.SPECIES_ENTRY);
			// Use the species format name, scientific name, count
			String[] metaVals = this.speciesPresent.stream().map(speciesEntry -> speciesEntry.getSpecies().getCommonName() + ", " + speciesEntry.getSpecies().getScientificName() + ", " + speciesEntry.getCount()).toArray(String[]::new);
			// Add the species entry field
			directory.add(SanimalMetadataFields.SPECIES_ENTRY, metaVals);

			// If we have a valid location, write that too
			if (this.getLocationTaken() != null && this.getLocationTaken().locationValid())
			{
				// Write the lat/lng
				outputSet.setGPSInDegrees(this.getLocationTaken().getLongitude(), this.getLocationTaken().getLatitude());
				// Remove the location entry name and elevation
				directory.removeField(SanimalMetadataFields.LOCATION_ENTRY);
				// Add the new location entry name and elevation
				directory.add(SanimalMetadataFields.LOCATION_ENTRY, this.getLocationTaken().getName(), this.getLocationTaken().getElevation().toString(), this.getLocationTaken().getId());
			}

			// Write the metadata
			MetadataUtils.writeOutputSet(outputSet, this);

			this.markDiskDirty(false);
		}
		catch (ImageReadException | IOException | ImageWriteException e)
		{
			SanimalData.getInstance().getErrorDisplay().notify("Error writing metadata to the image " + this.getFile().getName() + "!\n" + ExceptionUtils.getStackTrace(e));
		}
	}
}
