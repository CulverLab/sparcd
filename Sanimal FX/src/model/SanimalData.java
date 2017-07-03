package model;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.image.ImageDirectory;
import model.location.Location;
import model.species.Species;

import java.io.File;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A singleton class containing all data SANIMAL needs
 */
public class SanimalData
{
    // The one instance of the data
    private static final SanimalData INSTANCE = new SanimalData();

    // Get the one instance
    public static SanimalData getInstance()
    {
        return SanimalData.INSTANCE;
    }

    // A global list of species
    private ObservableList<Species> speciesList;

    // A global list of locations
    private ObservableList<Location> locationList;

    // A base directory to which we add all extra directories
    private ImageDirectory imageTree;

    // Use a thread pool executor to perform tasks that take a while
    private final ThreadPoolExecutor taskPerformer = new ScheduledThreadPoolExecutor(5);

    /**
     * Private constructor since we're using the singleton design pattern
     */
    private SanimalData()
    {
        // Create the species list, and add some default species
        this.speciesList = FXCollections.<Species>observableArrayList(species -> new Observable[]{species.getNameProperty(), species.getScientificNameProperty(), species.getSpeciesIconURLProperty()});
        this.speciesList.add(new Species("Tiger", "Panthera tigris", "http://kids.nationalgeographic.com/content/dam/kids/photos/articles/Other%20Explore%20Photos/R-Z/Wacky%20Weekend/Wild%20Cats/ww-wild-cats-tiger.adapt.945.1.jpg"));
        this.speciesList.add(new Species("Lion", "Panthera leo", "https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg"));
        this.speciesList.add(new Species("Unicorn", "Fakus Imaginus", "https://s-media-cache-ak0.pinimg.com/736x/3b/ca/b6/3bcab6f591ac1d61b1e6abded3ea06a7.jpg"));

        // Create the location list and add some default locations
        this.locationList = FXCollections.<Location> observableArrayList( location -> new Observable[] {location.getNameProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty()});
        this.locationList.add(new Location("Tucson", 32D, 110D, 2388D));
        this.locationList.add(new Location("Munich", 48D, 11D, 520D));

        // The tree just starts in the current directory which is a dummy directory
        this.imageTree = new ImageDirectory(new File("./"));
    }

    /**
     * @return The global species list
     */
    public ObservableList<Species> getSpeciesList()
    {
        return speciesList;
    }

    /**
     * @return The global location list
     */
    public ObservableList<Location> getLocationList()
    {
        return locationList;
    }

    /**
     * @return The root of the data tree
     */
    public ImageDirectory getImageTree()
    {
        return imageTree;
    }

    /**
     * @return The task performing thread
     */
    public ThreadPoolExecutor getTaskPerformer()
    {
        return taskPerformer;
    }
}
