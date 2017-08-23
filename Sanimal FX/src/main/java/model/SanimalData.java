package model;

import com.google.gson.Gson;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import model.cyverse.CyVerseConnectionManager;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;
import org.hildan.fxgson.FxGson;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.prefs.Preferences;
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
	private final ObservableList<Species> speciesList;

	// A global list of locations
	private final ObservableList<Location> locationList;

	// A base directory to which we add all extra directories
	private final ImageDirectory imageTree;

	// Use a thread pool executor to perform tasks that take a while
	private ExecutorService taskPerformer = Executors.newSingleThreadExecutor();
	private final ObjectProperty<Task> currentTask = new SimpleObjectProperty<>(null);

	// GSon object used to serialize data
	private final Gson gson = FxGson.fullBuilder().setPrettyPrinting().serializeNulls().create();

	// The connection manager used to authenticate the CyVerse user
	private CyVerseConnectionManager connectionManager = new CyVerseConnectionManager();

	private final Preferences sanimalPreferences = Preferences.userNodeForPackage(SanimalData.class);

	/**
	 * Private constructor since we're using the singleton design pattern
	 */
	private SanimalData()
	{
		// Create the species list, and add some default species
		this.speciesList = FXCollections.observableArrayList(species -> new Observable[]{species.nameProperty(), species.scientificNameProperty(), species.speciesIconURLProperty()});

		// Create the location list and add some default locations
		this.locationList = FXCollections.observableArrayList(location -> new Observable[]{location.nameProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty()});

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
	public <T> void addTask(Task<T> task)
	{
		EventHandler<WorkerStateEvent> onSucceeded = task.getOnSucceeded();
		task.setOnSucceeded(taskEvent ->
		{
			if (onSucceeded != null)
				onSucceeded.handle(taskEvent);
			currentTask.setValue(null);
		});
		EventHandler<WorkerStateEvent> onRunning = task.getOnRunning();
		task.setOnRunning(taskEvent ->
		{
			if (onRunning != null)
				onRunning.handle(taskEvent);
			currentTask.setValue(task);
		});
		this.taskPerformer.submit(task);
	}

	/**
	 * @return The property representing the currently active task
	 */
	public ObjectProperty<Task> currentTaskProperty()
	{
		return currentTask;
	}

	/**
	 * Clears the task queue. Can be dangerous if some important task is queued
	 */
	public void clearTasks()
	{
		// Shutdown the task performer and throw away current tasks
		this.taskPerformer.shutdownNow();
		// Set the current task to null, and number of pending tasks to 0
		this.currentTask.setValue(null);
		// Create a new task performer and throw away the old one
		this.taskPerformer = Executors.newSingleThreadExecutor();
	}

	/**
	 * @return The Cyverse connection manager used to authenticate and upload the user's images
	 */
	public CyVerseConnectionManager getConnectionManager()
	{
		return connectionManager;
	}

	/**
	 * @return The Gson serializer used to serialize properties
	 */
	public Gson getGson()
	{
		return this.gson;
	}

	/**
	 * @return Preference file used to store usernames and passwords
	 */
	public Preferences getSanimalPreferences()
	{
		return sanimalPreferences;
	}
}
