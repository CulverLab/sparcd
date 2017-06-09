package model.species;

import java.io.Serializable;
import java.net.URL;

/**
 * A class representing a species
 *
 * @author David Slovikosky
 */
public class Species implements Serializable
{
    private String name;
    private String scientificName;
    private String speciesIconURL;

    /**
     * Constructor for the species
     *
     * @param name
     *            The name of the species
     */
    public Species(String name, String scientificName, String speciesIconURL)
    {
        this.name = name;
        this.scientificName = scientificName;
        this.speciesIconURL = speciesIconURL;
    }

    /**
     * Set the name of the species
     * @param name The new name of the species
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return The name of the species
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the scientific name of the species
     * @param scientificName The new scientific name of the species
     */
    public void setScientificName(String scientificName)
    {
        this.scientificName = scientificName;
    }

    /**
     * @return The scientific name of the species
     */
    public String getScientificName()
    {
        return scientificName;
    }

    /**
     * Set the species icon using a URL
     * @param speciesIcon The new species icon of the species
     */
    public void setSpeciesIcon(String speciesIcon)
    {
        this.speciesIconURL = speciesIconURL;
    }

    /**
     * @return The species icon as a URL
     */
    public String getSpeciesIcon()
    {
        return speciesIconURL;
    }

    /**
     * Return a string representing the species
     */
    @Override
    public String toString()
    {
        return this.getName();
    }
}