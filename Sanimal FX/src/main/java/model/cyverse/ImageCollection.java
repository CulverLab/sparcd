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

public class ImageCollection
{
	private StringProperty nameProperty = new SimpleStringProperty("");
	private StringProperty organizationProperty = new SimpleStringProperty("");
	private StringProperty contactInfoProperty = new SimpleStringProperty("");
	private StringProperty descriptionProperty = new SimpleStringProperty("");
	private ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.uploadProperty(), permission.ownerProperty()});
	private ObjectProperty<UUID> idProperty = new SimpleObjectProperty<>(UUID.randomUUID());


	public ImageCollection()
	{
		this.setName("Untitled");
		this.setOrganization("None");

		// When the permission list changes we perform checks to ensure that the list is in a valid state
		// When the collection changes
		this.permissions.addListener((ListChangeListener<Permission>) change -> {
			while (change.next())
			{
				if (change.wasUpdated())
				{
					for (int i = change.getFrom(); i < change.getTo(); i++)
					{
						Permission updated = change.getList().get(i);
						if (!updated.isOwner())
						{
							boolean noOwner = change.getList().filtered(Permission::isOwner).isEmpty();
							if (noOwner)
								updated.setOwner(true);
						} else if (updated.isOwner())
							change.getList().filtered(perm -> !perm.equals(updated)).forEach(perm -> perm.setOwner(false));
					}
				}
			}
		});
	}

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
