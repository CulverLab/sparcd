package controller.uploadView;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import model.constant.SanimalDataFormats;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

/**
 * Class used as the controller for an upload entry in the treeview
 */
public class UploadTreeCellController extends TreeCell<ImageContainer>
{
	///
	/// FXML Bound Fields start
	///

	// Icon of tree entry
	@FXML
	public ImageView imgIcon;
	// The text to display
	@FXML
	public Label lblText;
	// A reference to the main pane
	@FXML
	public StackPane mainPane;

	// The upload progress bar and label
	@FXML
	public ProgressBar pbrUploadProgress;
	@FXML
	public Label lblProgress;

	///
	/// FXML Bound Fields end
	///

	/**
	 * Called when we want to display a new image container
	 *
	 * @param item The new item to display
	 * @param empty If the item is null and the cell should be empty
	 */
	@Override
	protected void updateItem(ImageContainer item, boolean empty)
	{
		super.updateItem(item, empty);

		// Set the text to null
		this.setText(null);

		// If the cell is empty we have no graphic
		if (empty && item == null)
		{
			this.setGraphic(null);
		}
		// if the cell is not empty, set the field's values and set the graphic
		else
		{
			this.imgIcon.imageProperty().unbind();
			this.imgIcon.imageProperty().bind(item.getTreeIconProperty());
			this.lblText.setText(item.toString());

			this.pbrUploadProgress.visibleProperty().unbind();
			if (item instanceof ImageDirectory)
			{
				ImageDirectory imageDirectory = (ImageDirectory) item;
				// Update the upload progress when the upload progress property changes
				this.pbrUploadProgress.progressProperty().unbind();
				this.pbrUploadProgress.progressProperty().bind(imageDirectory.uploadProgressProperty());
				// The progress will be show as a formatted string percentage
				this.lblProgress.setVisible(true);
				this.lblProgress.textProperty().bind(EasyBind.monadic(imageDirectory.uploadProgressProperty()).map(value -> (value.doubleValue() == -1 ? "" : String.format("%4.1f%%", value.doubleValue() * 100.0))));
				// Hide the upload progress when we are not uploading
				this.pbrUploadProgress.visibleProperty().bind(imageDirectory.uploadProgressProperty().isNotEqualTo(-1));
			}
			else
			{
				this.pbrUploadProgress.setVisible(false);
				this.lblProgress.setVisible(false);
			}

			this.setGraphic(mainPane);
		}
	}

	public void cellDragDetected(MouseEvent mouseEvent)
	{
		// Grab the selected image directory, make sure it's not null
		ImageContainer selected = this.getItem();
		if (selected != null)
		{
			// Can only drag & drop if we have a directory selected
			if (selected instanceof ImageDirectory)
			{
				ImageDirectory selectedDirectory = (ImageDirectory) selected;

				// Make sure we're not uploading
				if (selectedDirectory.getUploadProgress() == -1)
				{
					// Create a dragboard and begin the drag and drop
					Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);

					// Create a clipboard and put the location unique ID into that clipboard
					ClipboardContent content = new ClipboardContent();
					content.put(SanimalDataFormats.IMAGE_DIRECTORY_FILE_FORMAT, selectedDirectory.getFile());
					// Set the dragboard's context, and then consume the event
					dragboard.setContent(content);

					mouseEvent.consume();
				}
			}
		}
	}
}
