package model.cyverse;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
	private StringProperty name = new SimpleStringProperty("");
	// The organization that owns the collection
	private StringProperty organization = new SimpleStringProperty("");
	// The contact info of the owner of the collection
	private StringProperty contactInfo = new SimpleStringProperty("");
	// The description of the collection used to display collection purpose
	private StringProperty description = new SimpleStringProperty("");
	// The unique identifier for the collection
	private ObjectProperty<UUID> id = new SimpleObjectProperty<>(UUID.randomUUID());
	// A list containing permissions of this collection
	// We don't serialize it when converting to JSON since we want to keep this field transient (Because permissions are private!)
	private ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.readProperty(), permission.uploadProperty(), permission.ownerProperty()});
	// Keep a list of uploads that is also transient so it will not be serialized. We serialize this differently because uploads should not be public
	private ObservableList<CloudUploadEntry> uploads = FXCollections.observableArrayList(upload -> new Observable[] {});
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
		this.name.setValue(name);
	}

	public String getName()
	{
		return this.name.getValue();
	}

	public StringProperty nameProperty()
	{
		return this.name;
	}

	public void setOrganization(String organization)
	{
		this.organization.setValue(organization);
	}

	public String getOrganization()
	{
		return this.organization.getValue();
	}

	public StringProperty organizationProperty()
	{
		return this.organization;
	}

	public void setContactInfo(String contactInfo)
	{
		this.contactInfo.setValue(contactInfo);
	}

	public String getContactInfo()
	{
		return this.contactInfo.getValue();
	}

	public StringProperty contactInfoProperty()
	{
		return contactInfo;
	}

	public void setDescription(String description)
	{
		this.description.setValue(description);
	}

	public String getDescription()
	{
		return this.description.getValue();
	}

	public StringProperty descriptionProperty()
	{
		 return this.description;
	}

	// NO SETTER for id since it's a one time thing

	public UUID getID()
	{
		return this.id.getValue();
	}

	public ObjectProperty<UUID> idProperty()
	{
		return id;
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
