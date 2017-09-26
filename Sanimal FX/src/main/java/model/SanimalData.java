package model;

import com.google.gson.Gson;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import model.cyverse.CyVerseConnectionManager;
import model.cyverse.ImageCollection;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.util.FinishableService;
import model.util.FinishableTask;
import model.util.SanimalExecutor;
import org.hildan.fxgson.FxGson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private static final Integer NUM_IMAGES_AT_A_TIME = 100;
	private AtomicBoolean metadataSyncInProgress = new AtomicBoolean(false);

	// Executor used to thread off long tasks
	private SanimalExecutor sanimalExecutor = new SanimalExecutor();

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
		this.locationList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(location -> new Observable[]{location.nameProperty(), location.idProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty() }));

		// When the location list changes we push the changes to the CyVerse servers
		this.setupAutoLocationSync();

		// Create the image collection list
		this.collectionList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(collection -> new Observable[]{collection.nameProperty(), collection.getPermissions(), collection.organizationProperty(), collection.contactInfoProperty(), collection.descriptionProperty(), collection.idProperty() }));

		// The tree just starts in the current directory which is a dummy directory
		this.imageTree = new ImageDirectory(new File("./"));

		// When the metadata changes, we push the changes to disk
		this.setupAutoWriteMetadata();
	}

	/**
	 * Ensures that when the species list has any changes, they get pushed to the CyVerse servers
	 */
	private void setupAutoSpeciesSync()
	{
		FinishableService<Void> syncService = new FinishableService<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new FinishableTask<Void>()
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
			}
		};
		// When we finish syncing...
		syncService.setOnSucceeded(event -> {
			// After finishing the sync, check if we need to sync again. If so set the flag to false and sync once again
			if (this.needSpeciesSync.get())
			{
				this.needSpeciesSync.set(false);
				syncService.restart();
			}
			// If we don't need to sync again set the sync in progress flag to false
			else
			{
				this.speciesSyncInProgress.set(false);
			}
		});
		this.sanimalExecutor.registerService(syncService);

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
				this.speciesSyncInProgress.set(true);
				// Perform the task
				syncService.restart();
			}
		});
	}

	/**
	 * Ensures that when the location list has any changes, they get pushed to the CyVerse servers
	 */
	private void setupAutoLocationSync()
	{
		FinishableService<Void> syncService = new FinishableService<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new FinishableTask<Void>()
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
			}
		};
		// When we finish syncing...
		syncService.setOnSucceeded(event -> {
			// After finishing the sync, check if we need to sync again. If so set the flag to false and sync once again
			if (this.needLocationSync.get())
			{
				this.needLocationSync.set(false);
				syncService.restart();
			}
			// If we don't need to sync again set the sync in progress flag to false
			else
			{
				this.locationSyncInProgress.set(false);
			}
		});
		this.sanimalExecutor.registerService(syncService);

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
				this.locationSyncInProgress.set(true);
				// Perform the task
				syncService.restart();
			}
		});
	}

	/**
	 * Ensures that when the image metadata tree has any changes, they get pushed to disk
	 */
	private void setupAutoWriteMetadata()
	{
		FinishableService<Void> syncService = new FinishableService<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new FinishableTask<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						// Notice we perform the stream twice. This is because we cannot re-use streams in Java.
						long dirtyImageCount =
								SanimalData.this.getAllImages()
										.stream()
										.filter(ImageEntry::isDirty)
										.count();
						List<ImageEntry> top100Dirty =
								SanimalData.this.getAllImages()
										.stream()
										.filter(ImageEntry::isDirty)
										.limit(NUM_IMAGES_AT_A_TIME).collect(Collectors.toList());
						this.updateMessage("Writing updated images to disk (" + dirtyImageCount + " left, doing " + NUM_IMAGES_AT_A_TIME + " at a time)...");
						for (int i = 0; i < top100Dirty.size(); i++)
						{
							top100Dirty.get(i).writeToDisk();
							if (i % 10 == 0)
								this.updateProgress(i, top100Dirty.size());
						}
						return null;
					}
				};
			}
		};
		// When we finish syncing...
		syncService.setOnSucceeded(event -> {
			// After finishing the sync, check if we need to sync again. If so set the flag to false and sync once again
			Boolean moreImagesToWrite = this.getAllImages().stream().filter(ImageEntry::isDirty).count() > 0;
			if (moreImagesToWrite)
				syncService.restart();
			// If we don't need to sync again set the sync in progress flag to false
			else
				this.metadataSyncInProgress.set(false);
		});
		this.sanimalExecutor.registerService(syncService);

		this.imageTree.getChildren().addListener((ListChangeListener<ImageContainer>) c ->
		{
			while (c.next())
			{
				if (c.wasUpdated())
				{
					// If a sync is already in progress, we set a flag telling the current sync to perform another sync right after it finishes
					if (!this.metadataSyncInProgress.get())
					{
						this.metadataSyncInProgress.set(true);
						// Perform the task
						syncService.restart();
					}
				}
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
	 * @return The Cyverse connection manager used to authenticate and upload the user's images
	 */
	public CyVerseConnectionManager getConnectionManager()
	{
		return connectionManager;
	}

	/**
	 * @return The CyVerse sanimal executor service
	 */
	public SanimalExecutor getSanimalExecutor()
	{
		return sanimalExecutor;
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
