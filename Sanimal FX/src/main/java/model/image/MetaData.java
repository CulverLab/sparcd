package model.image;

/**
 * Representation of an metadata items. Replacing AvuData
 * 
 * @author Chris Schnaufer
 */

public final class MetaData
{
	// The name of the metadata
	private String attribute = "";
	// The value of the metadata
	private String value = "";
	// Optional unit of the metadata
	private String unit = "";

	/**
	 * Static initializer returns an {@code MetaData}. Note that unused values should
	 * be set to 'blank' rather then {@code null}. An
	 * {@code IllegalArgumentException} will be thrown if something is null.
	 *
	 * @param attribute {@code String} the MetaData attribute.
	 * @param value {@code String} the MetaData value.
	 * @param unit {@code String} any MetaData unit.
	 * @return {@link MetaData}
	 *
	 */
	public static MetaData instance(final String attribute, final String value, final String unit)
	{
		return new MetaData(attribute, value, unit);
	}

	/**
	 * Default constructor initializes to empty strings
	 */
	public MetaData()
	{
	}

	/**
	 * Constructor for MetaData that takes the attribute, value, unit
	 *
	 * @param attribute {@code MetaData} with the attribute.
	 * @param value {@code MetaData} with the value.
	 * @param unit {@code MetaData} with unit.
	 * @throws IllegalArgumentException when an invalid value is specified
	 * 
	 */
	public MetaData(final String attribute, final String value, final String unit) throws IllegalArgumentException
	{
		if (attribute == null || attribute.isEmpty())
		{
			throw new IllegalArgumentException("attribute is null or empty");
		}

		if (value == null || value.isEmpty())
		{
			throw new IllegalArgumentException("value is null or empty");
		}

		if (unit == null)
		{
			throw new IllegalArgumentException("unit is null, leave blank String if empty");
		}

		this.attribute = attribute;
		this.value = value;
		this.unit = unit;
	}

	/**
	 * Returns the attribute (name)
	 */
	public String getAttribute()
	{
		return attribute;
	}

	/**
	 * Returns the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Returns the unit (may be an empty string)
	 */
	public String getUnit()
	{
		return unit;
	}

	/**
	 * Converts the metadata value to a printable string
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("metadata:\n");
		sb.append("   attribute:");
		sb.append(attribute);
		sb.append("\n     value:");
		sb.append(value);
		sb.append("\n     unit:");
		sb.append(unit);
		return sb.toString();
	}

	/**
	 * Sets the attribute (name)
	 * 
	 * @param attribute {@code String} the attribute
	 * @throws IllegalArgumentException if the attribute is {@code null} or an empty string
	 */
	public void setAttribute(final String attribute) throws IllegalArgumentException
	{
		if (attribute == null || attribute.isEmpty())
		{
			throw new IllegalArgumentException("attribute is null or empty");
		}

		this.attribute = attribute;
	}

	/**
	 * Sets the value
	 * 
	 * @param value {@code String} the value
	 * @throws IllegalArgumentException if the value is {@code null} or an empty string
	 */
	public void setValue(final String value) throws IllegalArgumentException
	{
		if (value == null || value.isEmpty())
		{
			throw new IllegalArgumentException("value is null or empty");
		}
		this.value = value;
	}

	/**
	 * Sets the unit
	 * 
	 * @param value {@code String} the unit
	 * @throws IllegalArgumentException if the unit is an empty string
	 */
	public void setUnit(final String unit) throws IllegalArgumentException
	{
		if (unit == null)
		{
			throw new IllegalArgumentException("unit is null, leave blank String if empty");
		}
		this.unit = unit;
	}

	/**
	 * Compares if the parameter's fields are the same as ours (string compared). The obj parameter
	 * must be an instance of {@code MetaData} (or derived from MetaData)
	 * 
	 * @param obj {@code Object} the object to compare to
	 * @return {@code true} if the fields are the same using string comparisons, otherwise {@code false}
	 */
	@Override
	public boolean equals(final Object obj)
	{
		// Make sure we're the same thing
		if (!(obj instanceof MetaData))
		{
			return false;
		}

		// Perform the comparisons
		MetaData other = (MetaData)obj;
		return (attribute.equals(other.attribute) && value.equals(other.value) && unit.equals(other.unit));
	}

	/**
	 * Returns the hash code of the concatenated class strings
	 * 
	 * @return the integer hash of this class instance
	 */
	@Override
	public int hashCode()
	{
		return (attribute + value + unit).hashCode();
	}

}
