package model.species;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import model.image.ImageEntry;


/**
 * A class representing a species
 *
 * @author David Slovikosky
 */
public class Species
{
    // The name of the species
    private StringProperty name = new SimpleStringProperty();
    // The scientific name of the species
    private StringProperty scientificName = new SimpleStringProperty();
    // The species icon URL
    private StringProperty speciesIconURL = new SimpleStringProperty();
    // Each species can have a keybind that represents that species. This is used in the GUI to quickly tag species
    private final ObjectProperty<KeyCode> keyBinding = new SimpleObjectProperty<>(null);

    // Default uninitialized value
    @Expose(serialize = false, deserialize = false)
    public static final String UNINITIALIZED = "UNINITIALIZED";

    // Default Icon
    @Expose(serialize = false, deserialize = false)
    public static final String DEFAULT_ICON = "https://i.imgur.com/4qz5mI0.png";

    /**
     * Constructor for the species, sets a default icon
     *
     * @param name
     *            The name of the species
     * @param scientificName
     *            The scientific name of the species
     */
    public Species(String name, String scientificName)
    {
        this.name.setValue(name);
        this.scientificName.setValue(scientificName);
        this.speciesIconURL.setValue(DEFAULT_ICON);
    }

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

    /**
     * @return True if the name, scientific name, or icon are invalid
     */
    public Boolean isUninitialized()
    {
        return !this.nameValid() || !this.scientificNameValid() || !this.iconValid();
    }

    /**
     * @return True if the name is not uninitialized
     */
    public Boolean nameValid()
    {
        return !this.name.getValue().equals(UNINITIALIZED);
    }

    /**
     * @return True if the scientific name is not uninitialized
     */
    public Boolean scientificNameValid()
    {
        return !this.scientificName.getValue().equals(UNINITIALIZED);
    }

    /**
     * @return True if the icon is not uninitialized
     */
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
     * @return The name property
     */
    public StringProperty nameProperty()
    {
        return this.name;
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
     * @return The scientific name property
     */
    public StringProperty scientificNameProperty()
    {
        return this.scientificName;
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
     * @return The species icon URL property
     */
    public StringProperty speciesIconURLProperty()
    {
        return this.speciesIconURL;
    }

    /**
     * Set the keybinding of this species
     * @param keyBinding The new keybinding of the species
     */
    public void setKeyBinding(KeyCode keyBinding)
    {
        this.keyBinding.setValue(keyBinding);
    }

    /**
     * @return The keybinding of this species as a key code
     */
    public KeyCode getKeyBinding()
    {
        return this.keyBinding.getValue();
    }

    /**
     * @return The keybinding property of this species
     */
    public ObjectProperty<KeyCode> keyBindingProperty()
    {
        return keyBinding;
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