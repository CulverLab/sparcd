package model.cyverse;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.SanimalData;

public class ImageCollection
{
	private StringProperty nameProperty = new SimpleStringProperty("");
	private StringProperty organizationProperty = new SimpleStringProperty("");
	private ObservableList<Permission> permissions = FXCollections.observableArrayList(permission -> new Observable[] { permission.usernameProperty(), permission.viewProperty(), permission.writeProperty(), permission.deleteProperty(), permission.ownerProperty()});

	public ImageCollection()
	{
		this.setName("Untitled");
		this.setOrganization("None");
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

	public ObservableList<Permission> getPermissions()
	{
		return this.permissions;
	}
}
