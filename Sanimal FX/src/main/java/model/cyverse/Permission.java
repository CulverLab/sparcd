package model.cyverse;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Permission
{
	private StringProperty usernameProperty = new SimpleStringProperty("");
	private BooleanProperty viewProperty = new SimpleBooleanProperty(false);
	private BooleanProperty writeProperty = new SimpleBooleanProperty(false);
	private BooleanProperty deleteProperty = new SimpleBooleanProperty(false);
	private BooleanProperty ownerProperty = new SimpleBooleanProperty(false);

	public Permission()
	{
		// If owner is set, also set the rest of the properties to true
		this.ownerProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				// Bound values can't be set!
				this.setView(true);
				this.setWrite(true);
				this.setDelete(true);
			}
		});

		// If the owner property is set and we try to disable any of the other properties, ignore the change
		this.viewProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.ownerProperty.getValue())
				this.viewProperty.setValue(true);
		});
		this.writeProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.ownerProperty.getValue())
				this.writeProperty.setValue(true);
		});
		this.deleteProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.ownerProperty.getValue())
				this.deleteProperty.setValue(true);
		});
	}

	public void setUsername(String username)
	{
		this.usernameProperty.setValue(username);
	}

	public String getUsername()
	{
		return usernameProperty.getValue();
	}

	public StringProperty usernameProperty()
	{
		return usernameProperty;
	}

	public void setView(boolean view)
	{
		this.viewProperty.setValue(view);
	}

	public boolean getView()
	{
		return this.viewProperty.getValue();
	}

	public BooleanProperty viewProperty()
	{
		return this.viewProperty;
	}

	public void setWrite(boolean write)
	{
		this.writeProperty.setValue(write);
	}

	public boolean getWrite()
	{
		return this.writeProperty.getValue();
	}

	public BooleanProperty writeProperty()
	{
		return this.writeProperty;
	}

	public void setDelete(boolean delete)
	{
		this.deleteProperty.setValue(delete);
	}

	public boolean getDelete()
	{
		return this.deleteProperty.getValue();
	}

	public BooleanProperty deleteProperty()
	{
		return this.deleteProperty;
	}

	public void setOwner(boolean owner)
	{
		this.ownerProperty.setValue(owner);
	}

	public boolean getOwner()
	{
		return this.ownerProperty.getValue();
	}

	public BooleanProperty ownerProperty()
	{
		return this.ownerProperty;
	}

	@Override
	public String toString()
	{
		return "Permission for " + this.getUsername() + ", Owner: " + this.getOwner() + ", Can delete: " + this.getDelete() + ", Can write: " + this.getWrite() + ", Can view: " + this.getView();
	}
}
