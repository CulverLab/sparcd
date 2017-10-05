package model.cyverse;

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
	private ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.uploadProperty(), permission.ownerProperty()});
	// The unique identifier for the collection
	private ObjectProperty<UUID> idProperty = new SimpleObjectProperty<>(UUID.randomUUID());


	/**
	 * Constructs a new image collection with a default name
	 */
	public ImageCollection()
	{
		this.setName("Untitled");
		this.setOrganization("None");

		// When the permission list changes we perform checks to ensure that the list is in a valid state
		// When the collection changes
		this.permissions.addListener((ListChangeListener<Permission>) change -> {
			while (change.next())
			{
				// If the permission was updated, perform the checks
				if (change.wasUpdated())
				{
					for (int i = change.getFrom(); i < change.getTo(); i++)
					{
						// Grab the updated permission
						Permission updated = change.getList().get(i);
						// Check to see if the new permission does not have owner set
						if (!updated.isOwner())
						{
							// If not, test to ensure that this was not the only owner
							boolean noOwner = change.getList().filtered(Permission::isOwner).isEmpty();
							if (noOwner)
								updated.setOwner(true);
						// If the new user is the owner, then ensure no one else has owner permissions
						} else if (updated.isOwner())
							change.getList().filtered(perm -> !perm.equals(updated)).forEach(perm -> perm.setOwner(false));
					}
				}
			}
		});
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
