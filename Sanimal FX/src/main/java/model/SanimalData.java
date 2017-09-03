package model;

import com.google.gson.Gson;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import model.cyverse.CyVerseConnectionManager;
import model.cyverse.ImageCollection;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.util.FinishableTask;
import org.hildan.fxgson.FxGson;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
	private AtomicBoolean needSpeciesSync = new AtomicBoolean(false);
	private AtomicBoolean speciesSyncInProgress = new AtomicBoolean(false);

	// A global list of locations
	private final ObservableList<Location> locationList;
	private AtomicBoolean needLocationSync = new AtomicBoolean(false);
	private AtomicBoolean locationSyncInProgress = new AtomicBoolean(false);

	// A global list of image collections
	private final ObservableList<ImageCollection> collectionList;

	// A base directory to which we add all extra directories
	private final ImageDirectory imageTree;

	// Use a thread pool executor to perform tasks that take a while
	private ExecutorService taskPerformer = Executors.newSingleThreadExecutor();
	private final ObjectProperty<Task> currentTask = new SimpleObjectProperty<>(null);

	// GSon object used to serialize data
	private final Gson gson = FxGson.fullBuilder().setPrettyPrinting().serializeNulls().create();

	// The connection manager used to authenticate the CyVerse user
	private CyVerseConnectionManager connectionManager = new CyVerseConnectionManager();

	// Preferences used to save the user's username
	private final Preferences sanimalPreferences = Preferences.userNodeForPackage(SanimalData.class);

	/**
	 * Private constructor since we're using the singleton design pattern
	 */
	private SanimalData()
	{
		// Create the species list, and add some default species
		this.speciesList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(species -> new Observable[]{species.nameProperty(), species.scientificNameProperty(), species.speciesIconURLProperty(), species.keyBindingProperty()}));

		// When the species list changes we push the changes to the CyVerse servers
		this.setupAutoSpeciesSync();

		// Create the location list and add some default locations
		this.locationList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(location -> new Observable[]{location.nameProperty(), location.idProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty()}));

		// When the location list changes we push the changes to the CyVerse servers
		this.setupAutoLocationSync();

		// Create the image collection list
		this.collectionList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(collection -> new Observable[]{collection.nameProperty()}));

		// The tree just starts in the current directory which is a dummy directory
		this.imageTree = new ImageDirectory(new File("./"));
	}

	/**
	 * Ensures that when the species list has any changes, they get pushed to the CyVerse servers
	 */
	private void setupAutoSpeciesSync()
	{
		// When the species list changes...
		this.speciesList.addListener((ListChangeListener<Species>) c -> {
			// If a sync is already in progress, we set a flag telling the current sync to perform another sync right after it finishes
			if (this.speciesSyncInProgress.get())
			{
				this.needSpeciesSync.set(true);
			}
			// If a sync is not in progress, go ahead and sync
			else
			{
				// Create a task
				FinishableTask<Void> syncTask = new FinishableTask<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						// Perform the push of the location data
						this.updateMessage("Syncing new species list to CyVerse...");
						SanimalData.getInstance().getConnectionManager().pushLocalSpecies(SanimalData.getInstance().getSpeciesList());
						return null;
					}
				};
				// When we finish syncing...
				syncTask.setOnFinished(event -> {
					// After finishing the sync, check if we need to sync again. If so set the flag to false and sync once again
					if (this.needSpeciesSync.get())
					{
						this.needSpeciesSync.set(false);
						this.addTask(syncTask);
					}
					// If we don't need to sync again set the sync in progress flag to false
					else
					{
						this.speciesSyncInProgress.set(false);
					}
				});
				// Perform the task
				this.addTask(syncTask);
			}
		});
	}

	/**
	 * Ensures that when the location list has any changes, they get pushed to the CyVerse servers
	 */
	private void setupAutoLocationSync()
	{
		// When the location list changes...
		this.locationList.addListener((ListChangeListener<Location>) c -> {
			// If a sync is already in progress, we set a flag telling the current sync to perform another sync right after it finishes
			if (this.locationSyncInProgress.get())
			{
				this.needLocationSync.set(true);
			}
			// If a sync is not in progress, go ahead and sync
			else
			{
				// Create a task
				FinishableTask<Void> syncTask = new FinishableTask<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						// Perform the push of the location data
						this.updateMessage("Syncing new location list to CyVerse...");
						SanimalData.getInstance().getConnectionManager().pushLocalLocations(SanimalData.getInstance().getLocationList());
						return null;
					}
				};
				// When we finish syncing...
				syncTask.setOnFinished(event -> {
					// After finishing the sync, check if we need to sync again. If so set the flag to false and sync once again
					if (this.needLocationSync.get())
					{
						this.needLocationSync.set(false);
						this.addTask(syncTask);
					}
					// If we don't need to sync again set the sync in progress flag to false
					else
					{
						this.locationSyncInProgress.set(false);
					}
				});
				// Perform the task
				this.addTask(syncTask);
			}
		});
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
	 * @return The global collection list
	 */
	public ObservableList<ImageCollection> getCollectionList()
	{
		return collectionList;
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
