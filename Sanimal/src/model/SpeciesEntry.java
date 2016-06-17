/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

public class SpeciesEntry
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
		return this.getSpecies().getName();
	}
}
