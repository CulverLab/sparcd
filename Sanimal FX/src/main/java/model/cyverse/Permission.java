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
	private StringProperty username = new SimpleStringProperty("Unnamed");
	private BooleanProperty read = new SimpleBooleanProperty(false);
	private BooleanProperty upload = new SimpleBooleanProperty(false);
	private BooleanProperty owner = new SimpleBooleanProperty(false);

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
		this.upload.addListener((observable, oldValue, newValue) -> {
			if (!newValue && this.owner.getValue())
				this.setUpload(true);
			else if (newValue)
				this.setRead(true);
		});

		// If the owner property is set and we try to disable any of the other properties, ignore the change
		this.read.addListener((observable, oldValue, newValue) -> {
			if (!newValue && (this.owner.getValue() || this.upload.getValue()))
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
		this.username.setValue(username);
	}

	public String getUsername()
	{
		return username.getValue();
	}

	public StringProperty usernameProperty()
	{
		return username;
	}

	public void setUpload(boolean upload)
	{
		this.upload.setValue(upload);
	}

	public boolean canUpload()
	{
		return this.upload.getValue();
	}

	public BooleanProperty uploadProperty()
	{
		return this.upload;
	}

	public void setRead(boolean read)
	{
		this.read.setValue(read);
	}

	public boolean canRead()
	{
		return this.read.getValue();
	}

	public BooleanProperty readProperty()
	{
		return read;
	}

	public void setOwner(boolean owner)
	{
		this.owner.setValue(owner);
	}

	public boolean isOwner()
	{
		return this.owner.getValue();
	}

	public BooleanProperty ownerProperty()
	{
		return this.owner;
	}
}
