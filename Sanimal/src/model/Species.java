package model;

/**
 * A class representing a species
 * 
 * @author David Slovikosky
 */
public class Species
{
	private final String name;

	/**
	 * Constructor for the species
	 * 
	 * @param name
	 *            The name of the species
	 */
	public Species(String name)
	{
		this.name = name;
	}

	/**
	 * @return The name of the species
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Return a string representing the species
	 */
	@Override
	public String toString()
	{
		return this.getName();
	}
}
