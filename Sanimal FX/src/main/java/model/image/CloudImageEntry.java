package model.image;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import model.threading.ErrorTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an image on the cloud
 */
public class CloudImageEntry extends ImageEntry
{
	// The icon to use for all downloaded untagged images
	private static final Image DEFAULT_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIcon.png").toString());
	// The icon to use for all location only tagged images
	private static final Image LOCATION_ONLY_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIconLocation.png").toString());
	// The icon to use for all species only tagged images
	private static final Image SPECIES_ONLY_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIconSpecies.png").toString());
	// The icon to use for all tagged images
	private static final Image CHECKED_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIconDone.png").toString());
	// The icon to use for an undownloaded images
	private static final Image NO_DOWNLOAD_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIconNotDownloaded.png").toString());

	// Placeholder file used before the file has been downloaded
	private static File PLACEHOLDER_FILE = null;

	// The Cloud bucket
	private ObjectProperty<String> cloudBucket = new SimpleObjectProperty<>();
	// The Cloud file
	private ObjectProperty<String> cloudFileProperty = new SimpleObjectProperty<>();

	// Transient because we don't want these to be written to disk

	// If the image entry has been downloaded from the cloud
	private transient final BooleanProperty hasBeenPulledFromCloud = new SimpleBooleanProperty(false);
	// If the image entry is currently being downloaded from the cloud
	private transient final BooleanProperty isBeingPulledFromCloud = new SimpleBooleanProperty(false);
	// If the image entry was tagged with species on the cloud
	private transient final AtomicBoolean wasTaggedWithSpecies = new AtomicBoolean(false);
	// If the current version of the image is dirty compared to the one on the cloud
	private transient final AtomicBoolean isCloudDirty = new AtomicBoolean(false);

	/**
	 * Create a new image entry with an image file
	 *
	 * @param cloudFile The file which can be a temporary local file
	 */
	public CloudImageEntry(String cloudFile)
	{
		// No local file
		super(null);

		// Make sure that the placeholder image has been initialized. If not initialize it
		if (PLACEHOLDER_FILE == null)
		{
			// For some reason we can't just do new File(getResource("/files/placeholderImage.jpg")) because we're working with resources inside JAR files
			// This is a work around
			InputStream inputStream = ImageEntry.class.getResourceAsStream("/files/placeholderImage.jpg");
			PLACEHOLDER_FILE = SanimalData.getInstance().getTempDirectoryManager().createTempFile("placeholderImage.jpg");
			try
			{
				FileUtils.copyInputStreamToFile(inputStream, PLACEHOLDER_FILE);
			}
			catch (IOException e)
			{
				SanimalData.getInstance().getErrorDisplay().showPopup(
						Alert.AlertType.ERROR,
						null,
						"Error",
						"Error initializing",
						"Error initializing placeholder image for cloud images!\n" + ExceptionUtils.getStackTrace(e),
						false);
			}
		}

		// Bind the image property to a conditional expression.
		// The image is checked if the location is valid and the species present list is not empty
		Binding<Image> imageBinding = Bindings.createObjectBinding(() ->
		{
			if (!this.hasBeenPulledFromCloud.getValue())
				return NO_DOWNLOAD_CLOUD_IMAGE_ICON;
			else if (this.getLocationTaken() != null && this.getLocationTaken().locationValid() && !this.getSpeciesPresent().isEmpty())
				return CHECKED_CLOUD_IMAGE_ICON;
			else if (!this.getSpeciesPresent().isEmpty())
				return SPECIES_ONLY_CLOUD_IMAGE_ICON;
			else if (this.getLocationTaken() != null && this.getLocationTaken().locationValid())
				return LOCATION_ONLY_CLOUD_IMAGE_ICON;
			else
				return DEFAULT_CLOUD_IMAGE_ICON;
		}, this.locationTakenProperty(), this.getSpeciesPresent(), this.hasBeenPulledFromCloud);
		this.selectedImageProperty.bind(imageBinding);

		this.getFileProperty().setValue(PLACEHOLDER_FILE);
		this.setCloudFile(cloudFile);
	}

	/**
	 * If we're asked for a file we return a temporary file until the real one is pulled from the cloud
	 *
	 * @return A temporary file or a downloaded file if it has been pulled from the cloud
	 */
	@Override
	public File getFile()
	{
		this.pullFromCloudIfNotPulled();
		return super.getFile();
	}

	/**
	 * We can set the date taken without the image but don't write to disk
	 *
	 * @param date The new date taken
	 */
	@Override
	public void setDateTaken(LocalDateTime date)
	{
		this.pullFromCloudIfNotPulled();
		super.setDateTaken(date);
	}

	/**
	 * If we haven't pulled yet we just return null
	 *
	 * @return Null or a real date if we have pulled from the cloud
	 */
	@Override
	public LocalDateTime getDateTaken()
	{
		this.pullFromCloudIfNotPulled();
		return super.getDateTaken();
	}

	/**
	 * We can set the location taken without the image but don't write to disk
	 *
	 * @param location The new location the image was taken at
	 */
	@Override
	public void setLocationTaken(Location location)
	{
		this.pullFromCloudIfNotPulled();
		super.setLocationTaken(location);
	}

	/**
	 * If we haven't pulled yet we just return null
	 *
	 * @return The location taken or null if it has not yet been determined
	 */
	@Override
	public Location getLocationTaken()
	{
		this.pullFromCloudIfNotPulled();
		return super.getLocationTaken();
	}

	/**
	 * Add a species and a count to the image
	 *
	 * @param species The species of the animal
	 * @param amount The amount of that species to add
	 */
	@Override
	public void addSpecies(Species species, Integer amount)
	{
		this.pullFromCloudIfNotPulled();
		super.addSpecies(species, amount);
	}

	/**
	 * Remove a species from the image
	 *
	 * @param species The species to remove
	 */
	@Override
	public void removeSpecies(Species species)
	{
		this.pullFromCloudIfNotPulled();
		super.removeSpecies(species);
	}

	/**
	 * Marks the image entry as dirty meaning it needs to be written to disk
	 *
	 * @param dirty If the image is dirty
	 */
	@Override
	public void markDiskDirty(Boolean dirty)
	{
		super.markDiskDirty(dirty);
		if (dirty)
			this.markCloudDirty(true);
	}

	/**
	 * Marks the image entry as dirty meaning it needs to be updated on the cloud
	 *
	 * @param dirty If the image is dirty
	 */
	public void markCloudDirty(Boolean dirty)
	{
		this.isCloudDirty.set(dirty);
	}

	/**
	 * True if the image is dirty, false otherwise
	 *
	 * @return Tells us if the image is dirty
	 */
	@Override
	public Boolean isDiskDirty()
	{
		if (!this.hasBeenPulledFromCloud.getValue())
			return false;
		return super.isDiskDirty();
	}

	/**
	 * True if the image is dirty, false otherwise
	 *
	 * @return Tells us if the image is dirty (aka different from the cloud)
	 */
	public Boolean isCloudDirty()
	{
		if (!this.hasBeenPulledFromCloud.getValue())
			return false;
		return this.isCloudDirty.get();
	}

	/**
	 * Writes the image to disk if it has been downloaded from the cloud
	 */
	@Override
	public synchronized void writeToDisk()
	{
		if (this.hasBeenPulledFromCloud.getValue())
			super.writeToDisk();
	}

	/**
	 * Pulls the given image from the cloud
	 * 
	 * @param bucket the bucket to pull the entry from
	 */
	private void pullFromCloud(String bucket)
	{
		// Set a flag that we're pulling from the cloud
		this.isBeingPulledFromCloud.setValue(true);
		// Download the file
		ErrorTask<File> pullTask = new ErrorTask<File>()
		{
			@Override
			protected File call()
			{
				this.updateMessage("Downloading the image " + getCloudFile() + " for editing...");
				return SanimalData.getInstance().getConnectionManager().remoteToLocalImageFile(bucket, getCloudFile());
			}
		};

		// Once it's done set the local file and
		pullTask.setOnSucceeded(event ->
		{
			File localFile = pullTask.getValue();
			this.getFileProperty().setValue(localFile);
			// Read the metadata into the image file
			super.readFileMetadataIntoImage(SanimalData.getInstance().getLocationList(), SanimalData.getInstance().getSpeciesList());
			// Update flags
			if (!this.getSpeciesPresent().isEmpty())
				wasTaggedWithSpecies.set(true);
			this.hasBeenPulledFromCloud.setValue(true);
			this.isBeingPulledFromCloud.setValue(false);
			this.markCloudDirty(false);
		});

		SanimalData.getInstance().getSanimalExecutor().getImmediateExecutor().addTask(pullTask);
	}

	/**
	 * Pulls the image file from the cloud if it has not yet been downloaded
	 */
	public void pullFromCloudIfNotPulled()
	{
		// Make sure we didnt already or are not already pulling
		if (!this.hasBeenPulledFromCloud.getValue() && !this.isBeingPulledFromCloud.getValue())
			this.pullFromCloud(this.getBucket());
	}

	/**
	 * Tostring just prints the file name
	 *
	 * @return A string representing the file name
	 */
	@Override
	public String toString()
	{
		return this.getCloudFile();
	}

	///
	/// Getters/Setters
	///

	public String getBucket()
	{
		return this.cloudBucket.getValue();
	}

	public void setCloudBucket(String bucket)
	{
		this.cloudBucket.setValue(bucket);
	}

	public String getCloudFile()
	{
		return cloudFileProperty.getValue();
	}

	private void setCloudFile(String file)
	{
		this.cloudFileProperty.setValue(file);
	}

	public ObjectProperty<String> cloudFileProperty()
	{
		return this.cloudFileProperty;
	}

	public Boolean hasBeenPulledFromCloud()
	{
		return this.hasBeenPulledFromCloud.getValue();
	}

	public Boolean wasTaggedWithSpecies()
	{
		return this.wasTaggedWithSpecies.get();
	}
}
