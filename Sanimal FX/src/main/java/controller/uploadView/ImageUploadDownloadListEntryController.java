package controller.uploadView;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import model.SanimalData;
import model.image.CloudUploadEntry;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

import java.text.SimpleDateFormat;
import java.util.List;

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
	public Label lblEdits;

	// Buttons to download and upload/save data
	@FXML
	public Button btnDownload;
	@FXML
	public Button btnUpload;

	///
	/// FXML Bound fields end
	///

	// String constants used in labels
	private static final String TAGGED_BASE = "Species/Loc Tagged ";
	private static final String CHECK_MARK = "✓";
	private static final String CROSS_MARK = "✕";

	// The current download and upload tasks
	private Runnable onDownload;
	private Runnable onUpload;

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
			this.lblDate.setText(SanimalData.getInstance().getSettings().formatDateTime(cloudUploadEntry.getUploadDate(), " at "));
			this.lblTagged.setText(TAGGED_BASE + (cloudUploadEntry.getTagged() ? CHECK_MARK : CROSS_MARK));
			// Grab the list of edits and show it
			List<String> editComments = cloudUploadEntry.getEditComments();
			this.lblEdits.setText(editComments.isEmpty() ? "No edits to upload made." : editComments.get(editComments.size() - 1));
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
	 * @param actionEvent ignored
	 */
	public void downloadPressed(ActionEvent actionEvent)
	{
		// If we have an action listener, call it
		if (onDownload != null)
			onDownload.run();
	}

	/**
	 * Action listener when we click upload
	 *
	 * @param actionEvent ignored
	 */
	public void uploadPressed(ActionEvent actionEvent)
	{
		if (onUpload != null)
			onUpload.run();
	}
}
