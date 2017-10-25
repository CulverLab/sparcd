package controller;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import model.image.ImageDirectory;

public class ImageUploadListEntryController extends ListCell<ImageDirectory>
{
	@FXML
	public Label lblName;
	@FXML
	public CheckBox cbxSelected;

	@FXML
	public HBox mainPane;

	private BooleanProperty selected;

	@Override
	public void updateItem(ImageDirectory imageDirectorySelectable, boolean empty)
	{
		super.updateItem(imageDirectorySelectable, empty);

		// Set the text of the cell to nothing
		this.setText(null);

		// If no imageEntry was given and the cell was empty, clear the graphic
		if (empty && imageDirectorySelectable == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Set the name to the imageEntry name
			this.lblName.setText(imageDirectorySelectable.getFile().getName());

			if (selected != null)
				this.cbxSelected.selectedProperty().unbindBidirectional(selected);
			selected = imageDirectorySelectable.selectedForUploadProperty();
			if (selected != null)
				this.cbxSelected.selectedProperty().bindBidirectional(selected);

			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}
}
