package controller.uploadView;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import model.image.CloudUploadEntry;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

import java.text.SimpleDateFormat;
import java.util.List;

public class ImageUploadDownloadListEntryController extends ListCell<CloudUploadEntry>
{
	///
	/// FXML Bound fields start
	///

	@FXML
	public HBox mainPane;

	@FXML
	public Label lblDate;

	@FXML
	public Label lblUsername;

	@FXML
	public Label lblTagged;

	@FXML
	public Label lblEdits;

	@FXML
	public Button btnDownload;
	@FXML
	public Button btnUpload;

	///
	/// FXML Bound fields end
	///

	private static final SimpleDateFormat FOLDER_FORMAT = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm");
	private static final String TAGGED_BASE = "Species/Loc Tagged ";
	private static final String CHECK_MARK = "✓";
	private static final String CROSS_MARK = "✕";

	private Runnable onDownload;
	private Runnable onUpload;

	/**
	 * Called when we get a new item to display
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
			this.lblUsername.setText(cloudUploadEntry.getUploadUser());
			this.lblDate.setText(FOLDER_FORMAT.format(cloudUploadEntry.getUploadDate()));
			this.lblTagged.setText(TAGGED_BASE + (cloudUploadEntry.getTagged() ? CHECK_MARK : CROSS_MARK));
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

	public void setOnDownload(Runnable onDownload)
	{
		this.onDownload = onDownload;
	}

	public void setOnUpload(Runnable onUpload)
	{
		this.onUpload = onUpload;
	}

	public void downloadPressed(ActionEvent actionEvent)
	{
		if (onDownload != null)
			onDownload.run();
	}

	public void uploadPressed(ActionEvent actionEvent)
	{
		if (onUpload != null)
			onUpload.run();
	}
}
