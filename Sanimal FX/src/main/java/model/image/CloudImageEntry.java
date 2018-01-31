package model.image;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import model.SanimalData;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.util.MetadataUtils;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class CloudImageEntry extends ImageEntry
{
	// The icon to use for all images at the moment
	private static final Image DEFAULT_CLOUD_IMAGE_ICON = new Image(ImageEntry.class.getResource("/images/importWindow/imageCloudIcon.png").toString());

	private ObjectProperty<IRODSFile> cyverseFileProperty = new SimpleObjectProperty<>();

	private transient final AtomicBoolean hasBeenPulledFromCloud = new AtomicBoolean(false);

	/**
	 * Create a new image entry with an image file
	 *
	 * @param cloudFile The file which can be a temporary local file
	 */
	public CloudImageEntry(IRODSFile cloudFile)
	{
		super(null);
		selectedImageProperty.setValue(DEFAULT_CLOUD_IMAGE_ICON);
		this.setCyverseFile(cloudFile);
	}

	/**
	 * We don't do anything because a cloud image does not get loaded right away.
	 *
	 * @param file ignored
	 */
	@Override
	void readFileMetadataIntoImage(File file)
	{
	}

	@Override
	void initIconBindings()
	{
	}

	@Override
	public File getFile()
	{
		this.pullFromCloudIfNotPulled();
		return super.getFile();
	}

	@Override
	public void setDateTaken(Date date)
	{
		this.pullFromCloudIfNotPulled();
		super.setDateTaken(date);
	}

	@Override
	public Date getDateTaken()
	{
		this.pullFromCloudIfNotPulled();
		return super.getDateTaken();
	}

	@Override
	public void setLocationTaken(Location location)
	{
		this.pullFromCloudIfNotPulled();
		super.setLocationTaken(location);
	}

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
		this.pullFromCloudIfNotPulled();
		super.markDirty(dirty);
	}

	@Override
	public Boolean isDirty()
	{
		if (!this.hasBeenPulledFromCloud.get())
			return false;
		return super.isDirty();
	}

	@Override
	public synchronized void writeToDisk()
	{
		if (this.hasBeenPulledFromCloud.get())
			super.writeToDisk();
	}

	private void pullFromCloud()
	{
		File localFile = SanimalData.getInstance().getConnectionManager().remoteToLocalImageFile(this.getCyverseFile());
		super.readFileMetadataIntoImage(localFile);
		this.hasBeenPulledFromCloud.set(true);
	}

	private void pullFromCloudIfNotPulled()
	{
		if (!this.hasBeenPulledFromCloud.get())
		{
			System.out.println("Pulling image: " + this.getCyverseFile().getName());
			this.pullFromCloud();
		}
	}

	@Override
	public String toString()
	{
		return this.getCyverseFile().getName();
	}

	private IRODSFile getCyverseFile()
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
}
