package controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.image.CloudImageDirectory;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.util.ErrorTask;
import model.util.FXMLLoaderUtils;
import org.fxmisc.easybind.EasyBind;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.net.URL;
import java.util.*;
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

	// Button to upload images
	@FXML
	public Button btnUpload;

	// The primary split pane
	@FXML
	public SplitPane spnMain;

	// The upload box pane
	@FXML
	public VBox vbxUpload;


	// Warning labels used to show if there is a problem uploading
	@FXML
	public Label lblCantUpload;
	@FXML
	public Label lblNoImagesToUpload;

	// A list of items to upload
	@FXML
	public ListView<ImageDirectory> lstItemsToUpload;

	// ADDED, no idea what to do here yet...
	@FXML
	public ListView<String> uploadListDownloadListView;
	@FXML
	public Button btnDownload;

	// The download box pane
	@FXML
	public VBox vbxDownloadList;

	@FXML
	public VBox vbxLoadingCollection;

	@FXML
	public Label lblStatus;

	///
	/// FXML Bound Fields End
	///

	private static final String STATUS_LOADING = "Loading collection uploads...";
	private static final String STATUS_DOWNLOADING = "Downloading collection uploads to edit...";

	// The currently selected image collection
	private ObjectProperty<ImageCollection> selectedCollection = new SimpleObjectProperty<>();

	private ErrorTask<Void> collectionUploadDownloader;

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
		this.collectionListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("uploadView/ImageCollectionListEntry.fxml").getController());
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

		// Disable the can't upload label if we can't upload to a given collection
		this.lblCantUpload.visibleProperty().bind(
				EasyBind.monadic(this.selectedCollection)
					.map(collection -> collection.getPermissions()
						.filtered(perm -> !(perm.getUsername().equals(SanimalData.getInstance().getUsername()) && perm.canUpload())).size() == 1));

		// Disable the no images imported label if no images are imported into the program
		this.lblNoImagesToUpload.visibleProperty().bind(Bindings.size(SanimalData.getInstance().getImageTree().getChildren()).isEqualTo(0));

		// Disable the upload button if either of the labels are visible
		this.vbxUpload.disableProperty().bind(this.lblCantUpload.visibleProperty().or(this.lblNoImagesToUpload.visibleProperty()));

		// Set the cell factory to be our custom cell
		this.lstItemsToUpload.setCellFactory(list -> FXMLLoaderUtils.loadFXML("uploadView/ImageUploadListEntry.fxml").getController());
		// Populate the list of directories
		ObservableList<ImageContainer> containers = SanimalData.getInstance().getImageTree().getChildren();
		ObservableList<ImageDirectory> directories = EasyBind.map(containers.filtered(imageContainer -> imageContainer instanceof ImageDirectory), imageContainer -> (ImageDirectory) imageContainer);
		this.lstItemsToUpload.setItems(directories);

		this.btnDownload.disableProperty().bind(this.uploadListDownloadListView.getSelectionModel().selectedItemProperty().isNull());

		this.vbxLoadingCollection.setVisible(false);
		// When we select a new collection download the list of uploads to display
		this.selectedCollection.addListener((observable, oldValue, newValue) ->
		{
			if (collectionUploadDownloader != null && collectionUploadDownloader.isRunning())
				collectionUploadDownloader.cancel();

			if (newValue != null)
			{
				this.vbxLoadingCollection.setVisible(true);
				this.lblStatus.setText(STATUS_LOADING);
				this.vbxDownloadList.setDisable(true);

				collectionUploadDownloader = new ErrorTask<Void>()
				{
					@Override
					protected Void call()
					{
						List<String> uploadDirectories = SanimalData.getInstance().getConnectionManager().retrieveUploadList(newValue);
						Platform.runLater(() -> {
							uploadListDownloadListView.getItems().clear();
							uploadListDownloadListView.getItems().addAll(uploadDirectories);
						});
						return null;
					}
				};

				collectionUploadDownloader.setOnSucceeded(event -> {
					this.vbxLoadingCollection.setVisible(false);
					this.vbxDownloadList.setDisable(false);
				});

				SanimalData.getInstance().getSanimalExecutor().addTask(collectionUploadDownloader);
			}
		});
	}

	/**
	 * When we click the new collection button
	 *
	 * @param actionEvent
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
	 * When we click the upload button
	 * @param actionEvent
	 */
	public void uploadImages(ActionEvent actionEvent)
	{
		// Disable the upload button so that we can't click it twice
		this.btnUpload.setDisable(true);

		// Need to make sure that we have a selected collection
		if (this.selectedCollection.getValue() != null)
		{
			Integer numberOfDirectoriesToUpload = 0;

			// Go over each directory in the list of items to upload and upload them
			for (ImageDirectory imageDirectory : this.lstItemsToUpload.getItems())
			{
				// If the image directory is selected for upload, upload it
				if (imageDirectory.isSelectedForUpload())
				{
					// Make sure we've got a valid directory
					boolean validDirectory = true;
					// Each image must have a location and species tagged
					for (ImageEntry imageEntry : imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList()))
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
						numberOfDirectoriesToUpload++;
						// Create an upload task
						Task<Void> uploadTask = new ErrorTask<Void>()
						{
							@Override
							protected Void call()
							{
								// Update the progress
								this.updateProgress(0, 1);

								// Create a string property used as a callback
								StringProperty messageCallback = new SimpleStringProperty("");
								this.updateMessage("Uploading image directory " + imageDirectory.getFile().getName() + " to CyVerse.");
								messageCallback.addListener((observable, oldValue, newValue) -> this.updateMessage(newValue));
								// Upload images to CyVerse, we give it a transfer status callback so that we can show the progress
								SanimalData.getInstance().getConnectionManager().uploadImages(selectedCollection.getValue(), imageDirectory, new TransferStatusCallbackListener()
								{
									@Override
									public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus)
									{
										// Set the upload progress in the directory we get a callback
										Platform.runLater(() -> imageDirectory.setUploadProgress(transferStatus.getBytesTransfered() / (double) transferStatus.getTotalSize()));
										// Set the upload progress whenever we get a callback
										updateProgress((double) transferStatus.getBytesTransfered(), (double) transferStatus.getTotalSize());
										return FileStatusCallbackResponse.CONTINUE;
									}

									// Ignore this status callback
									@Override
									public void overallStatusCallback(TransferStatus transferStatus) throws JargonException
									{
									}

									// Ignore this as well
									@Override
									public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection)
									{
										return CallbackResponse.YES_FOR_ALL;
									}
								}, messageCallback);
								return null;
							}
						};
						// When the upload finishes, we enable the upload button
						uploadTask.setOnSucceeded(event ->
						{
							imageDirectory.setUploadProgress(-1);
							this.btnUpload.setDisable(false);
						});
						SanimalData.getInstance().getSanimalExecutor().addTask(uploadTask);
					}
					else
					{
						// If an invalid directory is selected, show an alert
						Alert alert = new Alert(Alert.AlertType.WARNING);
						alert.initOwner(this.collectionListView.getScene().getWindow());
						alert.setTitle("Invalid Directory");
						alert.setHeaderText("Invalid Directory (" + imageDirectory.getFile().getName() + ") Selected");
						alert.setContentText("An image in the directory (" + imageDirectory.getFile().getName() + ") you selected does not have a location or species tagged. Please ensure all images are tagged with at least one species and a location!");
						alert.showAndWait();
					}
				}
			}

			// If no directories were uploaded show the upload button again
			if (numberOfDirectoriesToUpload == 0)
			{
				this.btnUpload.setDisable(false);
			}
		}
		else
		{
			// If an invalid collection is selected, show an alert
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("No Collection");
			alert.setHeaderText("No image collection selected to upload to!");
			alert.setContentText("Please select an image collection to upload the images to.");
			alert.showAndWait();

			// Enable the upload button
			this.btnUpload.setDisable(false);
		}
	}

	public void newDownloadPressed(ActionEvent actionEvent)
	{
		if (this.selectedCollection.getValue() != null && this.uploadListDownloadListView.getSelectionModel().getSelectedItem() != null)
		{
			this.vbxLoadingCollection.setVisible(true);
			this.lblStatus.setText(STATUS_DOWNLOADING);
			this.vbxDownloadList.setDisable(true);

			ErrorTask<Void> downloadTask = new ErrorTask<Void>()
			{
				@Override
				protected Void call()
				{
					CloudImageDirectory cloudDirectory = SanimalData.getInstance().getConnectionManager().downloadUploadDirectory(selectedCollection.getValue(), uploadListDownloadListView.getSelectionModel().getSelectedItem());
					SanimalData.getInstance().getImageTree().addChild(cloudDirectory);
					return null;
				}
			};

			downloadTask.setOnSucceeded(event ->
			{
				this.vbxLoadingCollection.setVisible(false);
				this.vbxDownloadList.setDisable(false);
			});

			SanimalData.getInstance().getSanimalExecutor().addTask(downloadTask);
		}
	}
}
