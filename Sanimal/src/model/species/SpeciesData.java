package model.species;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Data storage for a list of registered species
 * 
 * @author David Slovikosky
 */
public class SpeciesData extends Observable implements Serializable
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
		for (Species speciesOther : this.species)
			if (species.getName().equals(speciesOther.getName()))
				return;
		this.species.add(species);
		this.species.sort((species1, species2) ->
		{
			return species1.getName().compareToIgnoreCase(species2.getName());
		});
		this.setChanged();
		this.notifyObservers(SpeciesUpdate.SpeciesListChanged);
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
		this.notifyObservers(SpeciesUpdate.SpeciesListChanged);
	}

	/**
	 * Clears the list of registered species
	 */
	public void clearRegisteredSpecies()
	{
		this.species.clear();
		this.setChanged();
		this.notifyObservers(SpeciesUpdate.SpeciesListChanged);
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
