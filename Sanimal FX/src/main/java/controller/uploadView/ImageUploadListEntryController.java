package controller.uploadView;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import org.fxmisc.easybind.EasyBind;

/**
 * Controller class for the image upload entry list
 */
public class ImageUploadListEntryController extends ListCell<ImageDirectory>
{
	///
	/// FXML bound fields start
	///

	// The name of the directory
	@FXML
	public Label lblName;
	// The checkbox that determines if this directory should be uploaded
	@FXML
	public CheckBox cbxSelected;

	// A reference to the main stack pane used as the background
	@FXML
	public StackPane mainPane;

	// Controls used to show the upload progress
	@FXML
	public ProgressBar pbrUploadProgress;
	@FXML
	public Label lblProgress;

	///
	/// FXML bound fields end
	///

	// Property used to tell if the image upload entry is selected
	private BooleanProperty selected;

	/**
	 * Called when we get a new item to display
	 * @param imageDirectory The image directory to show
	 * @param empty If the cell is empty
	 */
	@Override
	public void updateItem(ImageDirectory imageDirectory, boolean empty)
	{
		// Update the underlying item
		super.updateItem(imageDirectory, empty);

		// Set the text of the cell to nothing
		this.setText(null);

		// If no imageEntry was given and the cell was empty, clear the graphic
		if (empty && imageDirectory == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Set the name to the image directory name
			this.lblName.setText(imageDirectory.getFile().getName() + " (Image Count: " + imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).count() + ")");

			// Update the binding
			if (selected != null)
				this.cbxSelected.selectedProperty().unbindBidirectional(selected);
			selected = imageDirectory.selectedForUploadProperty();
			if (selected != null)
				this.cbxSelected.selectedProperty().bindBidirectional(selected);

			// Update the upload progress when the upload progress property changes
			this.pbrUploadProgress.progressProperty().unbind();
			this.pbrUploadProgress.progressProperty().bind(imageDirectory.uploadProgressProperty());
			// The progress will be show as a formatted string percentage
			this.lblProgress.textProperty().bind(EasyBind.monadic(imageDirectory.uploadProgressProperty()).map(value -> (value.doubleValue() == -1 ? "" : String.format("%4.1f%%", value.doubleValue() * 100.0))));
			this.pbrUploadProgress.visibleProperty().unbind();
			// Hide the upload progress when we are not uploading
			this.pbrUploadProgress.visibleProperty().bind(imageDirectory.uploadProgressProperty().isNotEqualTo(-1));

			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}
}
