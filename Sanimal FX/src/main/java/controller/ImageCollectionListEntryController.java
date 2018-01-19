package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.util.FXMLLoaderUtils;

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

	@FXML
	public Button btnSettings;

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
	}
}
