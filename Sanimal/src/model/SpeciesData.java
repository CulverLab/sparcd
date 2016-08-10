package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data storage for a list of registered species
 * 
 * @author David Slovikosky
 */
public class SpeciesData
{
	// The list of registered species
	private List<Species> species = new ArrayList<Species>();

	/**
	 * Add a species to the list
	 * 
	 * @param species
	 *            The species to add
	 */
	public void addSpecies(Species species)
	{
		this.species.add(species);
	}

	/**
	 * Remove a species from the list
	 * 
	 * @param species
	 *            The species to remove
	 */
	public void removeSpecies(Species species)
	{
		this.species.remove(species);
	}

	/**
	 * Get a list of registered species
	 * 
	 * @return the list of registered species
	 */
	public List<Species> getRegisteredSpecies()
	{
		return species;
	}
}
