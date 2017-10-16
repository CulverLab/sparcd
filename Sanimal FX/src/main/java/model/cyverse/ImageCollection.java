package model.cyverse;

import com.google.gson.annotations.Expose;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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
	// A list containing permissions of this collection
	// We don't serialize it when converting to JSON sicne we want to keep this field transient
	private transient ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.readProperty(), permission.uploadProperty(), permission.ownerProperty()});
	// The unique identifier for the collection
	private ObjectProperty<UUID> idProperty = new SimpleObjectProperty<>(UUID.randomUUID());


	/**
	 * Constructs a new image collection with a default name
	 */
	public ImageCollection()
	{
		this.setName("Untitled");
		this.setOrganization("None");
	}

	public String getOwner()
	{
		return permissions.stream().filter(Permission::isOwner).map(Permission::getUsername).findFirst().orElse(null);
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
}
