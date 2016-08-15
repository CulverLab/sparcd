package model.species;

import java.io.Serializable;

/**
 * Wrapper around the species class which allows for the addition of the amount field that specifies how many animals are in an image
 * 
 * @author David Slovikosky
 */
public class SpeciesEntry implements Comparable<SpeciesEntry>, Serializable
{
	// The species represented
	private final Species species;
	// The number of that species
	private final Integer amount;

	/**
	 * Construct a species entry with a species and a number of that species
	 * 
	 * @param species
	 *            The species to add
	 * @param amount
	 *            The number of that species to add
	 */
	public SpeciesEntry(Species species, Integer amount)
	{
		this.species = species;
		this.amount = amount;
	}

	/**
	 * @return The species of this specific entry
	 */
	public Species getSpecies()
	{
		return species;
	}

	/**
	 * @return The amount of the given species in the entry
	 */
	public Integer getAmount()
	{
		return amount;
	}

	/**
	 * Returns the name of the species and the amount of that species
	 */
	@Override
	public String toString()
	{
		return this.getSpecies().getName() + " (" + this.getAmount() + ")";
	}

	/**
	 * Comparing species entries does not compare objects but the species and amount
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SpeciesEntry)
		{
			SpeciesEntry other = (SpeciesEntry) obj;
			return other.amount == this.amount && other.species.equals(this.species);
		}
		return false;
	}

	/**
	 * Comparing species entries does not compare objects but the species and amount
	 */
	@Override
	public int compareTo(SpeciesEntry other)
	{
		if (other.amount == this.amount && other.species.equals(this.species))
			return 0;
		else
			return 1;
	}
}
