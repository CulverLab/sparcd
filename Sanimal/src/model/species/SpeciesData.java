package model.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Data storage for a list of registered species
 * 
 * @author David Slovikosky
 */
public class SpeciesData extends Observable
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
		this.setChanged();
		this.notifyObservers(SpeciesUpdate.SpeciesAdded);
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
		this.setChanged();
		this.notifyObservers(SpeciesUpdate.SpeciesAdded);
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
