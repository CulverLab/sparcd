package controller;

import controller.uploadView.ImageCollectionListEntryController;
import controller.uploadView.ImageUploadDownloadListEntryController;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import library.TreeViewAutomatic;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.image.*;
import model.util.ErrorTask;
import model.util.FXMLLoaderUtils;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller class for the upload page
 */
public class SanimalUploadController implements Initializable
{
	///
	/// FXML Bound Fields Start
	///

	// The list view of collections
	@FXML
	public ListView<ImageCollection> collectionListView;

	// The new and delete collection buttons
	@FXML
	public Button btnNewCollection;
	@FXML
	public Button btnDeleteCollection;

	// The primary split pane
	@FXML
	public SplitPane spnMain;

	// Upload entry on the cloud used to download...
	@FXML
	public ListView<CloudUploadEntry> uploadListDownloadListView;

	// The download box pane
	@FXML
	public VBox vbxDownloadList;

	// Box containing loading screen
	@FXML
	public VBox vbxLoadingCollection;
	// The label containing the loading status
	@FXML
	public Label lblStatus;

	// Pane containing savable images
	@FXML
	public SplitPane spnUploadSave;

	// The tree view of uploadable images
	@FXML
	public TreeViewAutomatic<ImageContainer> imageTree;

	///
	/// FXML Bound Fields End
	///

	// Constant strings used to display status
	private static final String STATUS_LOADING = "Loading collection uploads...";
	private static final String STATUS_DOWNLOADING = "Downloading collection uploads to edit...";

	// The currently selected image collection
	private ObjectProperty<ImageCollection> selectedCollection = new SimpleObjectProperty<>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// First setup the collection list

		// Grab the global collection list
		SortedList<ImageCollection> collections = new SortedList<>(SanimalData.getInstance().getCollectionList());
		// Set the comparator to be the name of the image collection
		collections.setComparator(Comparator.comparing(ImageCollection::getName));
		// Set the list of items to be the collections
		this.collectionListView.setItems(SanimalData.getInstance().getCollectionList());
		// Set the cell factory to be our custom cell factory
		this.collectionListView.setCellFactory(x -> {
			ImageCollectionListEntryController controller = FXMLLoaderUtils.loadFXML("uploadView/ImageCollectionListEntry.fxml").getController();
			// When we double click the collection list view items, we want to edit the collection settings
			controller.setOnMouseClicked(event -> {
				if (event.getClickCount() >= 2 && controller.getItem() != null && !controller.btnSettings.isDisabled())
					controller.settingsClicked(null);
			});
			return controller;
		});
		// When we select a new element, set the property
		this.collectionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.selectedCollection.setValue(newValue));

		// If no collection is selected, disable the text fields and buttons
		BooleanBinding nothingSelected = selectedCollection.isNull();

		// Disable this button when we are not the owner of the collection
		this.btnDeleteCollection.disableProperty().bind(EasyBind.monadic(this.selectedCollection).map(collection ->
		{
			String ownerUsername = collection.getOwner();
			return ownerUsername == null || !ownerUsername.equals(SanimalData.getInstance().getUsername());
		}).orElse(nothingSelected));

		// Initialize root of the right side directory/image tree and make the root invisible
		// This is because a treeview must have ONE root.

		// Create a fake invisible root node whos children
		final TreeItem<ImageContainer> ROOT = new TreeItem<>(SanimalData.getInstance().getImageTree());
		// Hide the fake invisible root
		this.imageTree.setShowRoot(false);
		// Set the fake invisible root
		this.imageTree.setRoot(ROOT);
		// Set the items of the tree to be the children of the fake invisible root
		this.imageTree.setItems(SanimalData.getInstance().getImageTree().getChildren().filtered(imageContainer -> !(imageContainer instanceof ImageEntry) && !(imageContainer instanceof CloudImageDirectory)));
		// Setup the image tree cells so that when they get drag & dropped the species & locations can be tagged
		this.imageTree.setCellFactory(x -> FXMLLoaderUtils.loadFXML("uploadView/UploadTreeCell.fxml").getController());

		this.uploadListDownloadListView.setCellFactory(list ->
		{
			ImageUploadDownloadListEntryController controller = FXMLLoaderUtils.loadFXML("uploadView/ImageUploadDownloadListEntry.fxml").getController();
			controller.setOnDownload(() -> this.downloadImages(controller.getItem()));
			controller.setOnUpload(() -> this.saveImages(controller.getItem()));
			return controller;
		});
		this.uploadListDownloadListView.itemsProperty().bind(EasyBind.monadic(this.selectedCollection).map(ImageCollection::getUploads));

		this.vbxLoadingCollection.setVisible(false);
		// When we select a new collection download the list of uploads to display
		this.selectedCollection.addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				// Test to see if the collection needs to download its list of uploads first
				if (!newValue.uploadsWereSynced())
				{
					newValue.setUploadsWereSynced(true);
					this.syncUploadsForCollection(newValue);
				}
			}
		});
	}

	/**
	 * Method called to sync a list of uploads to a collection
	 *
	 * @param collection The image collection to download uploads for
	 */
	private void syncUploadsForCollection(ImageCollection collection)
	{
		// Disable the download list and show the loading label and circle
		this.vbxLoadingCollection.setVisible(true);
		this.lblStatus.setText(STATUS_LOADING);
		this.vbxDownloadList.setDisable(true);

		// Create a task to pull a collection's uploads
		ErrorTask<Void> collectionUploadDownloader = new ErrorTask<Void>()
		{
			@Override
			protected Void call()
			{
				this.updateMessage("Downloading list of uploads to collection: " + collection.getName());
				SanimalData.getInstance().getConnectionManager().retrieveAndInsertUploadList(collection);
				return null;
			}
		};

		// Once done enable the download list and hide the loading label and circle
		collectionUploadDownloader.setOnSucceeded(event ->
		{
			this.vbxLoadingCollection.setVisible(false);
			this.vbxDownloadList.setDisable(false);
		});

		// Add the task
		SanimalData.getInstance().getSanimalExecutor().addTask(collectionUploadDownloader);
	}

	/**
	 * When we click the new collection button
	 *
	 * @param actionEvent ignored
	 */
	public void newCollectionPressed(ActionEvent actionEvent)
	{
		// Create the collection
		ImageCollection collection = new ImageCollection();
		// Create permissions for the owner
		Permission owner = new Permission();
		// Ensure that the owner has own permissions and then add it to the collection
		owner.setUsername(SanimalData.getInstance().usernameProperty().getValue());
		owner.setRead(true);
		owner.setUpload(true);
		owner.setOwner(true);
		collection.getPermissions().add(owner);
		// Add the collection to the global collection list
		SanimalData.getInstance().getCollectionList().add(collection);
	}

	/**
	 * When we click the delete collection button
	 *
	 * @param actionEvent
	 */
	public void deleteCollectionPressed(ActionEvent actionEvent)
	{
		// Grab the selected collection
		ImageCollection selected = this.collectionListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			// If a collection is selected, show an alert that data may be deleted!
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("Confirmation");
			alert.setHeaderText("Are you sure you want to delete this collection?");
			alert.setContentText("Deleting this collection will result in the permanent removal of all images uploaded to CyVerse to this collection. Are you sure you want to continue?");
			Optional<ButtonType> buttonType = alert.showAndWait();
			if (buttonType.isPresent())
			{
				if (buttonType.get() == ButtonType.OK)
				{
					// Remove the collection on the CyVerse system
					SanimalData.getInstance().getConnectionManager().removeCollection(selected);

					// Remove the selected collection
					SanimalData.getInstance().getCollectionList().remove(selected);
				}
			}
		}
		else
		{
			// If no collection is selected, show an alert
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Collection Selected");
			alert.setContentText("Please select a collection from the collection list to remove.");
			alert.showAndWait();
		}
	}

	/**
	 * Called to download images from a specific upload for editing
	 *
	 * @param uploadEntry The upload entry which we want to pull images from
	 */
	private void downloadImages(CloudUploadEntry uploadEntry)
	{
		// Make sure we have a collection selected
		if (this.selectedCollection.getValue() != null)
		{
			// Show the loading box again and disable the download entries
			this.vbxLoadingCollection.setVisible(true);
			this.lblStatus.setText(STATUS_DOWNLOADING);
			this.vbxDownloadList.setDisable(true);

			// Create a task to execute
			ErrorTask<Void> downloadTask = new ErrorTask<Void>()
			{
				@Override
				protected Void call()
				{
					this.updateMessage("Downloading directory for editing...");
					// Download the directory and add it to our tree structure
					CloudImageDirectory cloudDirectory = SanimalData.getInstance().getConnectionManager().downloadUploadDirectory(selectedCollection.getValue(), uploadEntry);
					Platform.runLater(() ->
					{
						uploadEntry.setCloudImageDirectory(cloudDirectory);
						SanimalData.getInstance().getImageTree().addChild(cloudDirectory);
					});
					return null;
				}
			};

			// Once the download is done hide the download box and enable the download list again
			downloadTask.setOnSucceeded(event ->
			{
				this.vbxLoadingCollection.setVisible(false);
				this.vbxDownloadList.setDisable(false);
				uploadEntry.setDownloaded(true);
			});

			SanimalData.getInstance().getSanimalExecutor().addTask(downloadTask);
		}
	}

	/**
	 * Called to save images we edited from a downloaded upload
	 *
	 * @param uploadEntry The upload entry to save changes to
	 */
	private void saveImages(CloudUploadEntry uploadEntry)
	{
		// Grab the image directory
		CloudImageDirectory imageDirectory = uploadEntry.getCloudImageDirectory();
		// Make sure the upload has been downloaded
		if (uploadEntry.hasBeenDownloaded() && imageDirectory != null)
		{
			// Make sure we've got a valid directory
			boolean validDirectory = true;

			// Each image must have a location and species tagged
			for (CloudImageEntry imageEntry : imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof CloudImageEntry && ((CloudImageEntry) imageContainer).hasBeenPulledFromCloud()).map(imageContainer -> (CloudImageEntry) imageContainer).collect(Collectors.toList()))
			{
				if (imageEntry.getLocationTaken() == null || imageEntry.getSpeciesPresent().isEmpty())
				{
					validDirectory = false;
					break;
				}
			}

			// If we have a valid directory, perform the upload
			if (validDirectory)
			{
				// Create an upload task
				Task<Void> saveTask = new ErrorTask<Void>()
				{
					@Override
					protected Void call()
					{
						// Create a string property used as a callback
						StringProperty messageCallback = new SimpleStringProperty("");
						this.updateMessage("Saving image directory " + imageDirectory.getCyverseDirectory().getName() + " to CyVerse.");
						messageCallback.addListener((observable, oldValue, newValue) -> this.updateMessage(newValue));

						// Save images to CyVerse, we give it a transfer status callback so that we can show the progress
						SanimalData.getInstance().getConnectionManager().saveImages(selectedCollection.getValue(), uploadEntry, messageCallback);
						return null;
					}
				};
				// When the upload finishes, we enable the upload button
				saveTask.setOnSucceeded(event -> imageDirectory.setUploadProgress(-1));
				SanimalData.getInstance().getSanimalExecutor().addTask(saveTask);
			}
			else
			{
				// If an invalid directory is selected, show an alert
				SanimalData.getInstance().getErrorDisplay().showPopup(Alert.AlertType.WARNING,
						this.collectionListView.getScene().getWindow(),
						"Invalid Directory",
						"Invalid Directory (" + imageDirectory.getFile().getName() + ") Selected",
						"An image in the directory (" + imageDirectory.getFile().getName() + ") you selected to save does not have a location or species tagged. Please ensure all images are tagged with at least one species and a location!",
						true);
			}
		}
		else
		{
			SanimalData.getInstance().getErrorDisplay().showPopup(Alert.AlertType.ERROR,
					this.collectionListView.getScene().getWindow(),
					"Error",
					"Directory not downloaded",
					"The Cloud directory has not been downloaded yet, how are you going to save it?",
					false);
		}
	}
}
