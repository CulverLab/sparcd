package model.analysis;

import model.species.Species;

/**
 * A simple class representing a lunar activity entry
 * 
 * @author David Slovikosky
 */
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
