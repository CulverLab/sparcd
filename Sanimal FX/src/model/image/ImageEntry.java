package model.image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossless;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;


/**
 * A class representing an image file
 * 
 * @author David Slovikosky
 */
public class ImageEntry extends ImageContainer
{
	// The format with which to print the date out in
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY MM dd hh mm ss");
	// The icon to use for all images at the moment
	private static final Image DEFAULT_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIcon.png").toString());
	// The icon to use for all tagged images at the moment
	private static final Image CHECKED_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageIconDone.png").toString());
	// A property to wrap the currently selected image property. Must not be static!
	private final ObjectProperty<Image> SELECTED_IMAGE_PROPERTY = new SimpleObjectProperty<>(DEFAULT_IMAGE_ICON);
	// The actual file 
	private ObjectProperty<File> imageFileProperty = new SimpleObjectProperty<File>();
	// The date that the image was taken
	private ObjectProperty<Date> dateTakenProperty = new SimpleObjectProperty<Date>();
	// The location that the image was taken
	private ObjectProperty<Location> locationTakenProperty = new SimpleObjectProperty<Location>();
	// The species present in the image
	private ObservableList<SpeciesEntry> speciesPresent = FXCollections.<SpeciesEntry> observableArrayList(image -> new Observable[] {
			image.getAmountProperty(),
			image.getSpeciesProperty()
	});

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
			this.dateTakenProperty.setValue(new Date(Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis()));
		}
		catch (IOException e)
		{
		}
		// Bind the image property to a conditional expression.
		// The image is checked if the location is valid and the species present list is not empty
		SELECTED_IMAGE_PROPERTY.bind(Bindings.createObjectBinding(() -> this.getLocationTaken() != null && this.getLocationTaken().locationValid() && !this.getSpeciesPresent().isEmpty() ? CHECKED_IMAGE_ICON : DEFAULT_IMAGE_ICON, this.locationTakenProperty, this.speciesPresent));

		// We create the EXIF data we'll need on the image entry to write to later
		// We do this in a thread since it takes some time to complete...
		this.speciesPresent.addListener((ListChangeListener<SpeciesEntry>) change -> SanimalData.getInstance().addTask(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				ImageEntry.this.rewriteSpecies();
				return null;
			}
		}));
		this.locationTakenProperty.addListener(change -> SanimalData.getInstance().addTask(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				ImageEntry.this.rewriteLocation();
				return null;
			}
		}));
	}

	/**
	 * Getter for the tree icon property
	 *
	 * @return The tree icon to be used
	 */
	@Override
	public ObjectProperty<Image> getTreeIconProperty()
	{
		return SELECTED_IMAGE_PROPERTY;
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
	 * Set the image file that this image represents
	 * 
	 * @param file
	 *            The file that this class represents
	 */
	public void setFile(File file)
	{
		this.imageFileProperty.setValue(file);
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

	/**
	 * Returns the date taken as a formatted string
	 * 
	 * @return The formatted date
	 */
	public String getDateTakenFormatted()
	{
		//this.validateDate();
		return this.getDateTaken().toString();
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
	public ObjectProperty<Date> getDateTakenProperty()
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

	public ObjectProperty<Location> getLocationTakenProperty()
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

	private void rewriteSpecies()
	{
		try
		{
			TiffOutputSet outputSet = MetadataUtils.readOutputSet(this);

			TiffOutputDirectory directory = MetadataUtils.getOrCreateSanimalDirectory(outputSet);
			directory.removeField(SanimalMetadataFields.SPECIES_ENTRY);
			String[] metaVals = this.speciesPresent.stream().map(speciesEntry -> speciesEntry.getSpecies().getName() + ", " + speciesEntry.getSpecies().getScientificName() + ", " + speciesEntry.getAmount()).toArray(String[]::new);
			directory.add(SanimalMetadataFields.SPECIES_ENTRY, metaVals);

			MetadataUtils.writeOutputSet(outputSet, this);
		}
		catch (ImageReadException | IOException | ImageWriteException e)
		{
			System.err.println("Exception occurred when trying to read/write the metadata from the file: " + this.getFile().getAbsolutePath());
			System.err.println("The error was: ");
			e.printStackTrace();
		}
	}

	private void rewriteLocation()
	{
		if (this.getLocationTaken().locationValid())
		{
			try
			{
				TiffOutputSet outputSet = MetadataUtils.readOutputSet(this);

				if (this.getLocationTaken() != null && this.getLocationTaken().locationValid())
					outputSet.setGPSInDegrees(this.getLocationTaken().getLng(), this.getLocationTaken().getLat());

				TiffOutputDirectory directory = MetadataUtils.getOrCreateSanimalDirectory(outputSet);
				directory.removeField(SanimalMetadataFields.LOCATION_ENTRY);
				directory.add(SanimalMetadataFields.LOCATION_ENTRY, this.getLocationTaken().getName(), this.getLocationTaken().getElevation().toString());

				MetadataUtils.writeOutputSet(outputSet, this);
			}
			catch (ImageReadException | IOException | ImageWriteException e)
			{
				System.err.println("Exception occurred when trying to read/write the metadata from the file: " + this.getFile().getAbsolutePath());
				System.err.println("The error was: ");
				e.printStackTrace();
			}
		}
	}
}
