package model.analysis;

import model.species.Species;

/**
 * A simple class representing a lunar activity entry
 * 
 * @author David Slovikosky
 */
public class LunarActivityEntry
{
	// The species in the entry
	private Species species;
	// The difference in the entry
	private Double difference;
	// The number of records at that time
	private Integer numRecords;

	/**
	 * Constructor initializes the fields
	 *
	 * @param species The species in the entry
	 * @param difference The difference in the entry
	 * @param numRecords The number of records at that time
	 */
	public LunarActivityEntry(Species species, Double difference, Integer numRecords)
	{
		this.species = species;
		this.difference = difference;
		this.numRecords = numRecords;
	}

	/**
	 * Getter for difference
	 *
	 * @return The difference in the entry
	 */
	public Double getDifference()
	{
		return difference;
	}

	/**
	 * Getter for number of records
	 *
	 * @return The number of records at that time
	 */
	public Integer getNumRecords()
	{
		return numRecords;
	}

	/**
	 * Getter for species
	 *
	 * @return The species of this entry
	 */
	public Species getSpecies()
	{
		return species;
	}
}
