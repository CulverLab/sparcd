package model;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    private final ExecutorService taskPerformer = Executors.newSingleThreadExecutor();
    private final IntegerProperty pendingTasks = new SimpleIntegerProperty(0);

    /**
     * Private constructor since we're using the singleton design pattern
     */
    private SanimalData()
    {
        // Create the species list, and add some default species
        this.speciesList = FXCollections.observableArrayList(species -> new Observable[]{species.getNameProperty(), species.getScientificNameProperty(), species.getSpeciesIconURLProperty()});

        this.loadSpeciesFromFile();

        // Create the location list and add some default locations
        this.locationList = FXCollections.observableArrayList( location -> new Observable[] {location.getNameProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty()});

        // The tree just starts in the current directory which is a dummy directory
        this.imageTree = new ImageDirectory(new File("./"));
    }

    /**
     * Reads all species entries from species.txt
     */
    private void loadSpeciesFromFile()
    {
        File speciesFile = new File("./species.txt");
        if (speciesFile.exists())
        {
            try
            {
                // Read each line of the file as a list of strings
                Files.readAllLines(speciesFile.toPath())
                        .stream()
                        // Remove empty lines and lines that do not have 3 commas
                        .filter(line -> !StringUtils.isEmpty(line) && StringUtils.countMatches(line, ",") == 2)
                        // Map the line to an array of lines that should have 1st element as the name, 2nd element as scientific name, and 3rd element as image URL
                        .map(line -> StringUtils.split(line, ","))
                        // For each of these lines, add a new species entry
                        .forEach(separated -> this.speciesList.add(new Species(separated[0], separated[1], separated[2])));
            }
            catch (IOException e)
            {
                System.err.println("Could not read species.txt. The file might be incorrectly formatted...");
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Could not find species.txt...");
        }
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
     * @return The tree of images as a list
     */
    public List<ImageEntry> getAllImages()
    {
        return this.getImageTree()
                .flattened()
                .filter(container -> container instanceof ImageEntry)
                .map(imageEntry -> (ImageEntry) imageEntry)
                .collect(Collectors.toList());
    }

    /**
     * Add a task to the queue to be done in the background
     *
     * @param task The task to be performed
     */
    public void addTask(Task<Void> task)
    {
        this.taskPerformer.submit(task);
        this.pendingTasks.setValue(this.pendingTasks.getValue() + 1);
        task.setOnSucceeded(taskEvent -> this.pendingTasks.setValue(this.pendingTasks.getValue() - 1));
    }

    /**
     * @return The property representing the number of active thread tasks
     */
    public IntegerProperty getPendingTasksProperty()
    {
        return this.pendingTasks;
    }
}
