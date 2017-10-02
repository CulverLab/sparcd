package model.cyverse;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Permission
{
	private StringProperty usernameProperty = new SimpleStringProperty("");
	private BooleanProperty uploadProperty = new SimpleBooleanProperty(false);
	private BooleanProperty ownerProperty = new SimpleBooleanProperty(false);

	public Permission()
	{
		// If owner is set, also set the rest of the properties to true
		this.ownerProperty.addListener((observable, oldValue, newValue) -> {
			if (newValue)
			{
				// Bound values can't be set!
				this.setUpload(true);
			}
		});

		// If the owner property is set and we try to disable any of the other properties, ignore the change
		this.uploadProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.ownerProperty.getValue())
				this.uploadProperty.setValue(true);
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

	public void setUpload(boolean upload)
	{
		this.uploadProperty.setValue(upload);
	}

	public boolean canUpload()
	{
		return this.uploadProperty.getValue();
	}

	public BooleanProperty uploadProperty()
	{
		return this.uploadProperty;
	}

	public void setOwner(boolean owner)
	{
		this.ownerProperty.setValue(owner);
	}

	public boolean isOwner()
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
		return "Permission for " + this.getUsername() + ", Owner: " + this.isOwner() + ", Can upload: " + this.canUpload();
	}
}
