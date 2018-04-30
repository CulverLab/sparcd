package model.cyverse;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class representing permissions for a user and a collection
 */
public class Permission implements Cloneable
{
	private StringProperty usernameProperty = new SimpleStringProperty("Unnamed");
	private BooleanProperty readProperty = new SimpleBooleanProperty(false);
	private BooleanProperty uploadProperty = new SimpleBooleanProperty(false);
	private BooleanProperty ownerProperty = new SimpleBooleanProperty(false);

	/**
	 * Just initializes listeners
	 */
	public Permission()
	{
		this.initListeners();
	}

	/**
	 * When we deserialize this class the properties get wiped and listeners/bindings are lost. To avoid this, we call this method on each permission after the
	 * deserialization
	 */
	public void initListeners()
	{
		// If the owner property is set and we try to disable any of the other properties, ignore the change
		this.uploadProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.ownerProperty.getValue())
				this.setUpload(true);
			else if (newValue)
				this.setRead(true);
		});

		// If the owner property is set and we try to disable any of the other properties, ignore the change
		this.readProperty.addListener((observable, oldValue, newValue) -> {
			if (!newValue && (this.ownerProperty.getValue() || this.uploadProperty.getValue()))
				this.setRead(true);
		});
	}

	/**
	 * Return a string representation of the permission
	 *
	 * @return A string of the permission
	 */
	@Override
	public String toString()
	{
		return "Permission for " + this.getUsername() + ", Owner: " + this.isOwner() + ", Can upload: " + this.canUpload() + ", Can Read: " + this.canRead();
	}

	/**
	 * Used to clone the permission
	 * @return A clone of the current permission
	 */
	@Override
	public Permission clone()
	{
		Permission clone = new Permission();
		clone.setRead(this.canRead());
		clone.setUpload(this.canUpload());
		clone.setOwner(this.isOwner());
		clone.setUsername(this.getUsername());
		return clone;
	}

	///
	/// Getters & Setters
	///

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

	public void setRead(boolean read)
	{
		this.readProperty.setValue(read);
	}

	public boolean canRead()
	{
		return this.readProperty.getValue();
	}

	public BooleanProperty readProperty()
	{
		return readProperty;
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
}
