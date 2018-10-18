package model.util;


import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

/**
 * Class used to create the PropertySheet and bind the setting to the sheet
 * @param <T> The type of the setting value
 */
public class CustomPropertyItem<T> implements PropertySheet.Item
{
	// The name of the property
	private final String name;
	// The category of the property
	private final String category;
	// The description of the property
	private final String description;
	// The class type of the property
	private final Class<T> clazz;
	// The actual property to bind to and update
	private final Property<T> value;

	/**
	 * Constructor used to initialize all fields
	 *
	 * @param name The name of the property
	 * @param category The category of the property
	 * @param description The description of the property
	 * @param value The actual property to bind to and update
	 * @param clazz The class type of the property
	 */
	public CustomPropertyItem(String name, String category, String description, Property<T> value, Class<T> clazz)
	{
		this.name = name;
		this.category = category;
		this.description = description;
		this.clazz = clazz;
		this.value = value;
	}

	///
	/// Getters/Setters
	///

	@Override
	public Class<?> getType()
	{
		return clazz;
	}

	@Override
	public String getCategory()
	{
		return this.category;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public Object getValue()
	{
		return this.value.getValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object value)
	{
		this.value.setValue((T) value);
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue()
	{
		return Optional.of(this.value);
	}
}
