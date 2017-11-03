package controller;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

public class ImageUploadListEntryController extends ListCell<ImageDirectory>
{
	@FXML
	public Label lblName;
	@FXML
	public CheckBox cbxSelected;

	@FXML
	public StackPane mainPane;

	@FXML
	public ProgressBar pbrUploadProgress;
	@FXML
	public Label lblProgress;

	private BooleanProperty selected;

	@Override
	public void updateItem(ImageDirectory imageDirectory, boolean empty)
	{
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
			// Set the name to the imageEntry name
			this.lblName.setText(imageDirectory.getFile().getName());

			if (selected != null)
				this.cbxSelected.selectedProperty().unbindBidirectional(selected);
			selected = imageDirectory.selectedForUploadProperty();
			if (selected != null)
				this.cbxSelected.selectedProperty().bindBidirectional(selected);

			this.pbrUploadProgress.progressProperty().unbind();
			this.pbrUploadProgress.progressProperty().bind(imageDirectory.uploadProgressProperty());
			this.lblProgress.textProperty().bind(EasyBind.monadic(imageDirectory.uploadProgressProperty()).map(value -> (value.doubleValue() == -1 ? "" : String.format("%4.1f%%", value.doubleValue() * 100.0))));
			this.pbrUploadProgress.visibleProperty().unbind();
			this.pbrUploadProgress.visibleProperty().bind(imageDirectory.uploadProgressProperty().isNotEqualTo(-1));

			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}
}
