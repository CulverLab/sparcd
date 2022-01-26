package model.cyverse;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.image.CloudUploadEntry;

import java.util.UUID;

/**
 * The image collection class represents a collection of photos on CyVerse
 */
public class ImageCollection
{
	// The collection name, does not need to be unique
	private StringProperty nameProperty = new SimpleStringProperty("");
	// The organization that owns the collection
	private StringProperty organizationProperty = new SimpleStringProperty("");
	// The contact info of the owner of the collection
	private StringProperty contactInfoProperty = new SimpleStringProperty("");
	// The description of the collection used to display collection purpose
	private StringProperty descriptionProperty = new SimpleStringProperty("");
	// The unique identifier for the collection
	private ObjectProperty<UUID> idProperty = new SimpleObjectProperty<>(UUID.randomUUID());
	// A list containing permissions of this collection
	// We don't serialize it when converting to JSON since we want to keep this field transient (Because permissions are private!)
	private transient ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.readProperty(), permission.uploadProperty(), permission.ownerProperty()});
	// Keep a list of uploads that is also transient so it will not be serialized. We serialize this differently because uploads should not be public
	private transient ObservableList<CloudUploadEntry> uploads = FXCollections.observableArrayList(upload -> new Observable[] {});
	private transient Boolean uploadsWereSynced = false;

	/**
	 * Constructs a new image collection with a default name
	 */
	public ImageCollection()
	{
		this.setName("Untitled");
		this.setOrganization("None");
	}

	/**
	 * Returns the name of the owner as a string
	 *
	 * @return The owner's name, or null if no owner is present
	 */
	public String getOwner()
	{
		return permissions.stream().filter(Permission::isOwner).map(Permission::getUsername).findFirst().orElse(null);
	}

	/**
	 * Returns the name of the collection with the organization too
	 *
	 * @return CollectionName (OrganizationName)
	 */
	@Override
	public String toString()
	{
		return this.getName() + " (" + this.getOrganization() + ")";
	}

	///
	/// Getters/Setters
	///

	public void setName(String name)
	{
		this.nameProperty.setValue(name);
	}

	public String getName()
	{
		return this.nameProperty.getValue();
	}

	public StringProperty nameProperty()
	{
		return this.nameProperty;
	}

	public void setOrganization(String organization)
	{
		this.organizationProperty.setValue(organization);
	}

	public String getOrganization()
	{
		return this.organizationProperty.getValue();
	}

	public StringProperty organizationProperty()
	{
		return this.organizationProperty;
	}

	public void setContactInfo(String contactInfo)
	{
		this.contactInfoProperty.setValue(contactInfo);
	}

	public String getContactInfo()
	{
		return this.contactInfoProperty.getValue();
	}

	public StringProperty contactInfoProperty()
	{
		return contactInfoProperty;
	}

	public void setDescription(String description)
	{
		this.descriptionProperty.setValue(description);
	}

	public String getDescription()
	{
		return this.descriptionProperty.getValue();
	}

	public StringProperty descriptionProperty()
	{
		 return this.descriptionProperty;
	}

	// NO SETTER for id since it's a one time thing

	public UUID getID()
	{
		return this.idProperty.getValue();
	}

	public ObjectProperty<UUID> idProperty()
	{
		return idProperty;
	}

	public ObservableList<Permission> getPermissions()
	{
		return this.permissions;
	}

	public ObservableList<CloudUploadEntry> getUploads()
	{
		return this.uploads;
	}

	public void setUploadsWereSynced(Boolean uploadsWereSynced)
	{
		this.uploadsWereSynced = uploadsWereSynced;
	}

	public Boolean uploadsWereSynced()
	{
		return uploadsWereSynced;
	}
}
