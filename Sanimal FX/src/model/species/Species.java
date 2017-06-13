package model.species;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.net.URL;

/**
 * A class representing a species
 *
 * @author David Slovikosky
 */
public class Species implements Serializable
{
    private StringProperty name = new SimpleStringProperty();
    private StringProperty scientificName = new SimpleStringProperty();
    private StringProperty speciesIconURL = new SimpleStringProperty();

    public static final String UNINITIALIZED = "UNINITIALIZED";

    /**
     * Constructor for the species
     *
     * @param name
     *            The name of the species
     * @param scientificName
     *            The scientific name of the species
     * @param speciesIconURL
     *            The URL of the icon as a string
     */
    public Species(String name, String scientificName, String speciesIconURL)
    {
        this.name.setValue(name);
        this.scientificName.setValue(scientificName);
        this.speciesIconURL.setValue(speciesIconURL);
    }

    /**
     * Default constructor to create an empty species
     */
    public Species()
    {
        this.name.setValue(UNINITIALIZED);
        this.scientificName.setValue(UNINITIALIZED);
        this.speciesIconURL.setValue(UNINITIALIZED);
    }

    public Boolean isUninitialized()
    {
        return !this.nameValid() || !this.scientificNameValid() || !this.iconValid();
    }

    public Boolean nameValid()
    {
        return !this.name.getValue().equals(UNINITIALIZED);
    }

    public Boolean scientificNameValid()
    {
        return !this.scientificName.getValue().equals(UNINITIALIZED);
    }

    public Boolean iconValid()
    {
        return !this.speciesIconURL.getValue().equals(UNINITIALIZED);
    }

    /**
     * Set the name of the species
     * @param name The new name of the species
     */
    public void setName(String name)
    {
        this.name.setValue(name);
    }

    /**
     * @return The name of the species
     */
    public String getName()
    {
        return name.getValue();
    }

    /**
     * Set the scientific name of the species
     * @param scientificName The new scientific name of the species
     */
    public void setScientificName(String scientificName)
    {
        this.scientificName.setValue(scientificName);
    }

    /**
     * @return The scientific name of the species
     */
    public String getScientificName()
    {
        return scientificName.getValue();
    }

    /**
     * Set the species icon using a URL
     * @param speciesIconURL The new species icon of the species
     */
    public void setSpeciesIcon(String speciesIconURL)
    {
        this.speciesIconURL.setValue(speciesIconURL);
    }

    /**
     * @return The species icon as a URL
     */
    public String getSpeciesIcon()
    {
        return speciesIconURL.getValue();
    }

    /**
     * Return a string representing the species
     */
    @Override
    public String toString()
    {
        return this.getName();
    }

    public StringProperty getNameProperty()
    {
        return this.name;
    }

    public StringProperty getScientificNameProperty()
    {
        return this.scientificName;
    }

    public StringProperty getSpeciesIconURLProperty()
    {
        return this.speciesIconURL;
    }
}