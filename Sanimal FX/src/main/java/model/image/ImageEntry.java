package model.image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.constant.SanimalMetadataFields;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.MetadataUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;


/**
 * A class representing an image file
 * 
 * @author David Slovikosky
 */
public class ImageEntry extends ImageContainer
{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	// The icon to use for all images at the moment
	private static final Image DEFAULT_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIcon.png").toString());
	// The icon to use for all location only tagged images at the moment
	private static final Image LOCATION_ONLY_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconLocation.png").toString());
	// The icon to use for all species only tagged images at the moment
	private static final Image SPECIES_ONLY_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconSpecies.png").toString());
	// The icon to use for all tagged images at the moment
	private static final Image CHECKED_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconDone.png").toString());

	// A property to wrap the currently selected image property. Must not be static!
	private final ObjectProperty<Image> selectedImageProperty = new SimpleObjectProperty<>(DEFAULT_IMAGE_ICON);
	// The actual file 
	private final ObjectProperty<File> imageFileProperty = new SimpleObjectProperty<File>();
	// The date that the image was taken
	private final ObjectProperty<Date> dateTakenProperty = new SimpleObjectProperty<Date>();
	// The location that the image was taken
	private final ObjectProperty<Location> locationTakenProperty = new SimpleObjectProperty<Location>();
	// The species present in the image
	private final ObservableList<SpeciesEntry> speciesPresent = FXCollections.<SpeciesEntry> observableArrayList(image -> new Observable[] {
			image.getAmountProperty(),
			image.getSpeciesProperty()
	});
	// If this image is dirty, we set a flag to write it to disk at some later point
	private final AtomicBoolean isDirty = new AtomicBoolean(false);

	/**
	 * Create a new image entry with an image file
	 * 
	 * @param file
	 *            The file (must be an image file)
	 */
	public ImageEntry(File file)
	{
		this.imageFileProperty.setValue(file);
		try
		{
			TiffImageMetadata tiffImageMetadata = MetadataUtils.readImageMetadata(this);
			if (tiffImageMetadata != null)
			{
				String[] dateTaken = tiffImageMetadata.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
				if (dateTaken.length == 1)
					this.dateTakenProperty.setValue(DATE_FORMAT.parse(dateTaken[0]));
			}
		}
		catch (ImageReadException | ParseException | IOException e)
		{
			System.err.println("Could not read image metadata!!!");
			e.printStackTrace();
		}
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

		this.locationTakenProperty.addListener((observable, oldValue, newValue) -> this.markDirty(true));
		this.speciesPresent.addListener((ListChangeListener<SpeciesEntry>) c -> this.markDirty(true));
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

	public void setDateTaken(Date date)
	{
		this.dateTakenProperty.setValue(date);
	}

	/**
	 * Returns the date the image was taken
	 * 
	 * @return The date the image was taken
	 */
	public Date getDateTaken()
	{
		//this.validateDate();
		return dateTakenProperty.getValue();
	}

	/**
	 * Returns the date property of the image
	 *
	 * @return The date the image was taken property
	 */
	public ObjectProperty<Date> dateTakenProperty()
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
		int oldAmount = currentEntry.map(SpeciesEntry::getAmount).orElse(0);
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
		this.speciesPresent.removeIf(entry ->
				entry.getSpecies() == species);
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

	public void markDirty(Boolean dirty)
	{
		this.isDirty.set(dirty);
	}

	public Boolean isDirty()
	{
		return this.isDirty.get();
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
			exif.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, DATE_FORMAT.format(this.getDateTaken()));

			// Grab the sanimal directory from the output set
			TiffOutputDirectory directory = MetadataUtils.getOrCreateSanimalDirectory(outputSet);

			// Remove the species field if it exists
			directory.removeField(SanimalMetadataFields.SPECIES_ENTRY);
			// Use the species format name, scientific name, count
			String[] metaVals = this.speciesPresent.stream().map(speciesEntry -> speciesEntry.getSpecies().getName() + ", " + speciesEntry.getSpecies().getScientificName() + ", " + speciesEntry.getAmount()).toArray(String[]::new);
			// Add the species entry field
			directory.add(SanimalMetadataFields.SPECIES_ENTRY, metaVals);

			// If we have a valid location, write that too
			if (this.getLocationTaken() != null && this.getLocationTaken().locationValid())
			{
				// Write the lat/lng
				outputSet.setGPSInDegrees(this.getLocationTaken().getLng(), this.getLocationTaken().getLat());
				// Remove the location entry name and elevation
				directory.removeField(SanimalMetadataFields.LOCATION_ENTRY);
				// Add the new location entry name and elevation
				directory.add(SanimalMetadataFields.LOCATION_ENTRY, this.getLocationTaken().getName(), this.getLocationTaken().getElevation().toString(), this.getLocationTaken().getId());
			}

			// Write the metadata
			MetadataUtils.writeOutputSet(outputSet, this);

			this.isDirty.set(false);
		}
		catch (ImageReadException | IOException | ImageWriteException e)
		{
			// If we get an error, print the error
			System.err.println("Exception occurred when trying to read/write the metadata from the file: " + this.getFile().getAbsolutePath());
			System.err.println("The error was: ");
			e.printStackTrace();
		}
	}
}
