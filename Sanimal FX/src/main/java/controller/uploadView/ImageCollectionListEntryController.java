package controller.uploadView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.image.CloudImageDirectory;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.threading.ErrorTask;
import model.util.FXMLLoaderUtils;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.io.File;
import java.util.Optional;
import java.util.stream.Collectors;

import static model.constant.SanimalDataFormats.IMAGE_DIRECTORY_FILE_FORMAT;

/**
 * Controller class for the image collection list
 */
public class ImageCollectionListEntryController extends ListCell<ImageCollection>
{
	///
	/// FXML Bound Fields Start
	///

	// The main background pane
	@FXML
	public HBox mainPane;

	// The labels for the collection pieces
	@FXML
	public Label lblCollectionName;
	@FXML
	public Label lblCollectionContactInfo;
	@FXML
	public Label lblCollectionOrganization;
	@FXML
	public Label lblCollectionDescription;

	// The button to access colleciton settings
	@FXML
	public Button btnSettings;

	// Images used to display user permissions
	@FXML
	public ImageView imgRead;
	@FXML
	public ImageView imgUpload;
	@FXML
	public ImageView imgOwner;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Called once the controller has been setup
	 */
	@FXML
	public void initialize()
	{
		Tooltip.install(this.imgRead, new Tooltip("You may see images uploaded to this collection."));
		Tooltip.install(this.imgUpload, new Tooltip("You may upload images to this collection."));
		Tooltip.install(this.imgOwner, new Tooltip("You are the owner of this collection."));
	}

	/**
	 * Update item is called when the list cell gets a new collection
	 *
	 * @param collection The new collection
	 * @param empty if the cell will be empty
	 */
	@Override
	protected void updateItem(ImageCollection collection, boolean empty)
	{
		// Update the cell internally
		super.updateItem(collection, empty);

		// Set the text of the cell to nothing
		this.setText(null);

		// If no collection was given and the cell was empty, clear the graphic
		if (empty && collection == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Set the name to the collection name
			this.lblCollectionName.setText(collection.getName());
			// Set the contact info to be the collection contact info
			this.lblCollectionContactInfo.setText(collection.getContactInfo());
			// Set the organization of the collection
			this.lblCollectionOrganization.setText(collection.getOrganization());
			// Set the description of the collection
			this.lblCollectionDescription.setText(collection.getDescription());
			// Hide the settings button if we are not the owner
			Permission forUser = collection.getPermissions().stream().filter(perm -> perm.getUsername().equals(SanimalData.getInstance().getUsername())).findFirst().orElse(null);
			boolean isOwner = forUser != null && forUser.isOwner();
			boolean canUpload = forUser != null && forUser.canUpload();
			boolean canRead = forUser != null && forUser.canRead();
			// Hide the owner, upload and read icons if we do not have the respective permission
			this.btnSettings.setDisable(!isOwner);
			this.imgOwner.setVisible(isOwner);
			this.imgUpload.setVisible(canUpload);
			this.imgRead.setVisible(canRead);
			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}

	/**
	 * Called when the settings button is selected
	 *
	 * @param actionEvent consumed
	 */
	public void settingsClicked(ActionEvent actionEvent)
	{
		// Load the FXML file of the editor window
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("uploadView/ImageCollectionSettings.fxml");
		// Grab the controller and set the species of that controller
		ImageCollectionSettingsController controller = loader.getController();
		controller.setCollectionToEdit(this.getItem());

		// Create the stage that will have the Image Collection Editor
		Stage dialogStage = new Stage();
		// Set the title
		dialogStage.setTitle("Image Collection Editor");
		// Set the modality and initialize the owner to be this current window
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.mainPane.getScene().getWindow());
		// Set the scene to the root of the FXML file
		Scene scene = new Scene(loader.getRoot());
		// Set the scene of the stage, and show it!
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
		actionEvent.consume();
	}

	/**
	 * If our mouse hovers over the image pane and we're dragging, we accept the transfer
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void cellDragOver(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging from a directory we accept the transfer
		if (dragboard.hasContent(IMAGE_DIRECTORY_FILE_FORMAT))
			dragEvent.acceptTransferModes(TransferMode.COPY);
		dragEvent.consume();
	}

	/**
	 * When the drag from the image directory enters the collection
	 *
	 * @param dragEvent The event that means we are dragging over the collection
	 */
	public void cellDragEntered(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the image directory and the dragboard has a string we update the CSS and consume the event
		if (dragboard.hasContent(IMAGE_DIRECTORY_FILE_FORMAT))
			if (!this.mainPane.getStyleClass().contains("draggedOver"))
				this.mainPane.getStyleClass().add("draggedOver");
		dragEvent.consume();
	}

	/**
	 * When the drag from the image directory exits the collection
	 *
	 * @param dragEvent The event that means we are dragging away from the collection
	 */
	public void cellDragExited(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the image directory and the dragboard has a string we update the CSS and consume the event
		if (dragboard.hasContent(IMAGE_DIRECTORY_FILE_FORMAT))
			this.mainPane.getStyleClass().remove("draggedOver");
		dragEvent.consume();
	}

	/**
	 * When we drop the image directory onto the collection, we perform the upload
	 *
	 * @param dragEvent The event used to ensure the drag is valid
	 */
	public void cellDragDropped(DragEvent dragEvent)
	{
		// Grab the dragboard
		Dragboard dragboard = dragEvent.getDragboard();
		// If our dragboard has a string we have data which we need
		if (dragboard.hasContent(IMAGE_DIRECTORY_FILE_FORMAT))
		{
			File imageDirectoryFile = (File) dragboard.getContent(IMAGE_DIRECTORY_FILE_FORMAT);
			// Filter our list of images by directory that has the right file path
			Optional<ImageDirectory> imageDirectoryOpt = SanimalData.getInstance().getImageTree().flattened().filter(
					imageContainer -> imageContainer instanceof ImageDirectory &&
					!(imageContainer instanceof CloudImageDirectory) &&
					imageContainer.getFile().getAbsolutePath().equals(imageDirectoryFile.getAbsolutePath())).map(imageContainer -> (ImageDirectory) imageContainer).findFirst();

			imageDirectoryOpt.ifPresent(imageDirectory ->
			{
				// Make sure we've got a valid directory
				boolean validDirectory = true;
				// Each image must have a location and species tagged
				for (ImageEntry imageEntry : imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList()))
				{
					if (imageEntry.getLocationTaken() == null)
					{
						validDirectory = false;
						break;
					}
				}

				// If we have a valid directory, perform the upload
				if (validDirectory)
				{
					// Show an alert to tell the user that they are uploading images at this point
					Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
					confirmation.initOwner(this.mainPane.getScene().getWindow());
					confirmation.setTitle("Upload Images");
					confirmation.setHeaderText("Uploading to " + this.getItem().getName());
					confirmation.setContentText("Are you sure you want to upload these " + imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).count() + " images to the collection " + this.getItem().getName() + "?");
					Optional<ButtonType> result = confirmation.showAndWait();

					// Test the result...
					result.ifPresent(buttonType ->
					{
						// OK means upload
						if (buttonType == ButtonType.OK)
						{
							// Set the upload to 0% so that we don't edit it anymore
							imageDirectory.setUploadProgress(0.0);
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
									SanimalData.getInstance().getConnectionManager().uploadImages(ImageCollectionListEntryController.this.getItem(), imageDirectory, new TransferStatusCallbackListener()
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
										public void overallStatusCallback(TransferStatus transferStatus)
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
								// Remove the directory because it's uploaded now
								SanimalData.getInstance().getImageTree().removeChildRecursive(imageDirectory);
							});
							uploadTask.setOnCancelled(event -> imageDirectory.setUploadProgress(-1));
							dragEvent.setDropCompleted(true);
							SanimalData.getInstance().getSanimalExecutor().getImmediateExecutor().addTask(uploadTask);
						}
					});
				}
				else
				{
					// If an invalid directory is selected, show an alert
					SanimalData.getInstance().getErrorDisplay().showPopup(
							Alert.AlertType.WARNING,
							this.mainPane.getScene().getWindow(),
							"Invalid Directory",
							"Invalid Directory (" + imageDirectory.getFile().getName() + ") Selected",
							"An image in the directory (" + imageDirectory.getFile().getName() + ") you selected does not have a location. Please ensure all images are tagged with a location!",
							true);
				}
			});
		}
		dragEvent.consume();
	}
}
