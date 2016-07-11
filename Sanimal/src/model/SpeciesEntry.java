/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

public class SpeciesEntry implements Comparable<SpeciesEntry>
{
	private final Species species;
	private final Integer amount;

	public SpeciesEntry(Species species, Integer amount)
	{
		this.species = species;
		this.amount = amount;
	}

	public Species getSpecies()
	{
		return species;
	}

	public Integer getAmount()
	{
		return amount;
	}

	@Override
	public String toString()
	{
		return this.getSpecies().getName() + " (" + this.getAmount() + ")";
	}

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

	@Override
	public int compareTo(SpeciesEntry other)
	{
		if (other.amount == this.amount && other.species.equals(this.species))
			return 0;
		else
			return 1;
	}
}
