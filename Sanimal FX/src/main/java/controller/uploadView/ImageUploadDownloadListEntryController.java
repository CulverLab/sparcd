package controller.uploadView;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import model.SanimalData;
import model.image.CloudUploadEntry;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

/**
 * Controller for the download entry which allows downloading/saving of image files
 */
public class ImageUploadDownloadListEntryController extends ListCell<CloudUploadEntry>
{
	///
	/// FXML Bound fields start
	///

	// The primary pane reference
	@FXML
	public HBox mainPane;

	// List of labels used to display edits
	@FXML
	public Label lblDate;
	@FXML
	public Label lblUsername;
	@FXML
	public Label lblTagged;
	@FXML
	public ListView<String> lstEdits;

	// Buttons to download and upload/save data
	@FXML
	public Button btnDownload;
	@FXML
	public Button btnUpload;

	///
	/// FXML Bound fields end
	///

	// The current download and upload tasks
	private Runnable onDownload;
	private Runnable onUpload;

	/**
	 * Nothing needs to be done to initialize this cell
	 */
	@FXML
	public void initialize()
	{
		// If the date or time settings are changed, recompute the label
		SanimalData.getInstance().getSettings().dateFormatProperty().addListener((observable, oldValue, newValue) ->
		{
			if (this.getItem() != null)
				this.lblDate.setText(SanimalData.getInstance().getSettings().formatDateTime(this.getItem().getUploadDate(), " at "));
		});
		SanimalData.getInstance().getSettings().timeFormatProperty().addListener((observable, oldValue, newValue) ->
		{
			if (this.getItem() != null)
				this.lblDate.setText(SanimalData.getInstance().getSettings().formatDateTime(this.getItem().getUploadDate(), " at "));
		});
	}

	/**
	 * Called when we get a new item to display
	 *
	 * @param cloudUploadEntry The image directory to show
	 * @param empty If the cell is empty
	 */
	@Override
	public void updateItem(CloudUploadEntry cloudUploadEntry, boolean empty)
	{
		// Update the underlying item
		super.updateItem(cloudUploadEntry, empty);

		// Set the text of the cell to nothing
		this.setText(null);

		// If no image directory was given and the cell was empty, clear the graphic
		if (empty && cloudUploadEntry == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Update the labels
			this.lblUsername.setText(cloudUploadEntry.getUploadUser());
			this.lblTagged.setText(cloudUploadEntry.getImagesWithSpecies() + "/" + cloudUploadEntry.getImageCount() + " tagged with species.");
			this.lblDate.setText(SanimalData.getInstance().getSettings().formatDateTime(this.getItem().getUploadDate(), " at "));
			// Grab the list of edits and show it
			this.lstEdits.getItems().clear();
			this.lstEdits.getItems().addAll(cloudUploadEntry.getEditComments());
			this.btnDownload.disableProperty().unbind();
			this.btnDownload.disableProperty().bind(cloudUploadEntry.downloadedProperty());
			this.btnUpload.disableProperty().unbind();
			this.btnUpload.disableProperty().bind(
					cloudUploadEntry.downloadedProperty().not().or(
					Bindings.notEqual(
							EasyBind.monadic(cloudUploadEntry.cloudImageDirectoryProperty()).selectProperty(ImageDirectory::uploadProgressProperty).orElse(0),
							-1.0)
			));
			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}

	/**
	 * The runnable code to execute when we click download
	 *
	 * @param onDownload Code to run
	 */
	public void setOnDownload(Runnable onDownload)
	{
		this.onDownload = onDownload;
	}

	/**
	 * The runnable code to execute when we click upload
	 *
	 * @param onUpload Code to run
	 */
	public void setOnUpload(Runnable onUpload)
	{
		this.onUpload = onUpload;
	}

	/**
	 * Action listener when we click download
	 *
	 * @param actionEvent consumed
	 */
	public void downloadPressed(ActionEvent actionEvent)
	{
		// If we have an action listener, call it
		if (onDownload != null)
			onDownload.run();
		actionEvent.consume();
	}

	/**
	 * Action listener when we click upload
	 *
	 * @param actionEvent consumed
	 */
	public void uploadPressed(ActionEvent actionEvent)
	{
		if (onUpload != null)
			onUpload.run();
		actionEvent.consume();
	}
}
