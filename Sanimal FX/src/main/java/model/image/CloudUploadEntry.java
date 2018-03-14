package model.image;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CloudUploadEntry
{
	private transient BooleanProperty downloadedProperty = new SimpleBooleanProperty(false);
	private transient ObjectProperty<CloudImageDirectory> cloudImageDirectoryProperty = new SimpleObjectProperty<>();

	private String uploadUser;
	private LocalDateTime uploadDate;
	private Boolean tagged;
	private List<String> editComments = new ArrayList<>();
	private String uploadIRODSPath;

	public CloudUploadEntry(String uploadUser, LocalDateTime uploadDate, Boolean tagged, String uploadIRODSPath)
	{
		this.uploadUser = uploadUser;
		this.uploadDate = uploadDate;
		this.tagged = tagged;
		this.uploadIRODSPath = uploadIRODSPath;
	}

	public void initFromJSON()
	{
		this.downloadedProperty = new SimpleBooleanProperty(false);
		this.cloudImageDirectoryProperty = new SimpleObjectProperty<>();
	}

	public List<String> getEditComments()
	{
		return this.editComments;
	}

	public Boolean getTagged()
	{
		return tagged;
	}

	public String getUploadUser()
	{
		return uploadUser;
	}

	public LocalDateTime getUploadDate()
	{
		return uploadDate;
	}

	public String getUploadIRODSPath()
	{
		return uploadIRODSPath;
	}

	public void setDownloaded(Boolean downloadedProperty)
	{
		this.downloadedProperty.setValue(downloadedProperty);
	}

	public Boolean hasBeenDownloaded()
	{
		return this.downloadedProperty.getValue();
	}

	public BooleanProperty downloadedProperty()
	{
		return this.downloadedProperty;
	}

	public void setCloudImageDirectory(CloudImageDirectory cloudImageDirectory)
	{
		this.cloudImageDirectoryProperty.setValue(cloudImageDirectory);
	}

	public CloudImageDirectory getCloudImageDirectory()
	{
		return cloudImageDirectoryProperty.getValue();
	}

	public ObjectProperty<CloudImageDirectory> cloudImageDirectoryProperty()
	{
		return this.cloudImageDirectoryProperty;
	}
}
