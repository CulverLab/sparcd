package model.image;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.ErrorTask;
import model.util.MetadataUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private static File PLACEHOLDER_FILE = null;

	private ObjectProperty<IRODSFile> cyverseFileProperty = new SimpleObjectProperty<>();

	// Transient because we don't want these to be written to disk
	private transient final BooleanProperty hasBeenPulledFromCloud = new SimpleBooleanProperty(false);
	private transient final BooleanProperty isBeingPulledFromCloud = new SimpleBooleanProperty(false);

	/**
	 * Create a new image entry with an image file
	 *
	 * @param cloudFile The file which can be a temporary local file
	 */
	public CloudImageEntry(IRODSFile cloudFile)
	{
		super(null, null, null);

		// Make sure that the placeholder image has been initialized. If not initialize it
		if (PLACEHOLDER_FILE == null)
		{
			// For some reason we can't just do new File(getResource("/files/placeholderImage.jpg")) because we're working with resources inside JAR files
			InputStream inputStream = ImageEntry.class.getResourceAsStream("/files/placeholderImage.jpg");
			PLACEHOLDER_FILE = SanimalData.getInstance().getTempDirectoryManager().createTempFile("placeholderImage.jpg");
			try
			{
				FileUtils.copyInputStreamToFile(inputStream, PLACEHOLDER_FILE);
			}
			catch (IOException e)
			{
				System.out.println("Error loading placeholder image.");
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
		this.setCyverseFile(cloudFile);
	}

	@Override
	void initIconBindings()
	{
	}

	/**
	 * We don't do anything because a cloud image does not get loaded right away.
	 *
	 * @param file ignored
	 */
	@Override
	void readFileMetadataIntoImage(File file, List<Location> knownLocations, List<Species> knownSpecies)
	{
	}

	// If we're asked for a file we return a temporary file until the real one is pulled from the cloud
	@Override
	public File getFile()
	{
		this.pullFromCloudIfNotPulled();
		return super.getFile();
	}

	// We can set the date taken without the image but don't write to disk
	@Override
	public void setDateTaken(Date date)
	{
		this.pullFromCloudIfNotPulled();
		super.setDateTaken(date);
	}

	// If we haven't pulled yet we just return null
	@Override
	public Date getDateTaken()
	{
		this.pullFromCloudIfNotPulled();
		return super.getDateTaken();
	}

	// We can set the location taken without the image but don't write to disk
	@Override
	public void setLocationTaken(Location location)
	{
		this.pullFromCloudIfNotPulled();
		super.setLocationTaken(location);
	}

	// If we haven't pulled yet we just return null
	@Override
	public Location getLocationTaken()
	{
		this.pullFromCloudIfNotPulled();
		return super.getLocationTaken();
	}

	@Override
	public void addSpecies(Species species, Integer amount)
	{
		this.pullFromCloudIfNotPulled();
		super.addSpecies(species, amount);
	}

	@Override
	public void removeSpecies(Species species)
	{
		this.pullFromCloudIfNotPulled();
		super.removeSpecies(species);
	}

	@Override
	public void markDirty(Boolean dirty)
	{
		super.markDirty(dirty);
	}

	@Override
	public Boolean isDirty()
	{
		if (!this.hasBeenPulledFromCloud.getValue())
			return false;
		return super.isDirty();
	}

	@Override
	public synchronized void writeToDisk()
	{
		if (this.hasBeenPulledFromCloud.getValue())
			super.writeToDisk();
	}

	private void pullFromCloud()
	{
		this.isBeingPulledFromCloud.setValue(true);
		ErrorTask<File> pullTask = new ErrorTask<File>()
		{
			@Override
			protected File call()
			{
				this.updateMessage("Downloading the image " + getCyverseFile().getName() + " for editing...");
				return SanimalData.getInstance().getConnectionManager().remoteToLocalImageFile(getCyverseFile());
			}
		};

		pullTask.setOnSucceeded(event ->
		{
			File localFile = pullTask.getValue();
			super.readFileMetadataIntoImage(localFile, SanimalData.getInstance().getLocationList(), SanimalData.getInstance().getSpeciesList());
			this.hasBeenPulledFromCloud.setValue(true);
		});

		SanimalData.getInstance().getSanimalExecutor().addTask(pullTask);
	}

	public void pullFromCloudIfNotPulled()
	{
		if (!this.hasBeenPulledFromCloud.getValue() && !this.isBeingPulledFromCloud.getValue())
			this.pullFromCloud();
	}

	@Override
	public String toString()
	{
		return this.getCyverseFile().getName();
	}

	public IRODSFile getCyverseFile()
	{
		return cyverseFileProperty.getValue();
	}

	private void setCyverseFile(IRODSFile file)
	{
		this.cyverseFileProperty.setValue(file);
	}

	public ObjectProperty<IRODSFile> cyverseFileProperty()
	{
		return this.cyverseFileProperty;
	}

	public boolean hasBeenPulledFromCloud()
	{
		return this.hasBeenPulledFromCloud.getValue();
	}
}
