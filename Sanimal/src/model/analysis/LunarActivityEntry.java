/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import model.Species;

public class LunarActivityEntry
{
	private Species species;
	private Double difference;
	private Integer numRecords;

	public LunarActivityEntry(Species species, Double difference, Integer numRecords)
	{
		this.species = species;
		this.difference = difference;
		this.numRecords = numRecords;
	}

	public Double getDifference()
	{
		return difference;
	}

	public Integer getNumRecords()
	{
		return numRecords;
	}

	public Species getSpecies()
	{
		return species;
	}
}
