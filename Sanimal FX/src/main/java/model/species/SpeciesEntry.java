package model.species;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Wrapper around the species class which allows for the addition of the amount field that specifies how many animals are in an image
 * 
 * @author David Slovikosky
 */
public class SpeciesEntry implements Comparable<SpeciesEntry>
{
	// The species represented
	private final ObjectProperty<Species> species = new SimpleObjectProperty<>();
	// The number of that species
	private final IntegerProperty count = new SimpleIntegerProperty();

	/**
	 * Construct a species entry with a species and a number of that species
	 * 
	 * @param species
	 *            The species to add
	 * @param count
	 *            The number of that species to add
	 */
	public SpeciesEntry(Species species, Integer count)
	{
		this.species.setValue(species);
		this.count.setValue(count);
	}

	/**
	 * @return The species of this specific entry
	 */
	public Species getSpecies()
	{
		return species.getValue();
	}

	public void setCount(Integer countProperty)
	{
		this.count.setValue(countProperty);
	}

	/**
	 * @return The amount of the given species in the entry
	 */
	public Integer getCount()
	{
		return count.getValue();
	}

	/**
	 * @return The property representing the amount of a species
	 */
	public IntegerProperty countProperty()
	{
		return this.count;
	}

	/**
	 * @return The property representing the species type
	 */
	public ObjectProperty<Species> speciesProperty()
	{
		return this.species;
	}


	/**
	 * Returns the name of the species and the amount of that species
	 */
	@Override
	public String toString()
	{
		return this.getSpecies().getCommonName() + " (" + this.getCount() + ")";
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
			return other.getCount().equals(this.getCount()) && other.getSpecies().equals(this.getSpecies());
		}
		return false;
	}

	/**
	 * Comparing species entries does not compare objects but the species and amount
	 */
	@Override
	public int compareTo(SpeciesEntry other)
	{
		if (other.getCount().equals(this.getCount()) && other.getSpecies().equals(this.getSpecies()))
			return 0;
		else
			return 1;
	}
}
