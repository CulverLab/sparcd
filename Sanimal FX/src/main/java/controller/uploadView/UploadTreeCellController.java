package controller.uploadView;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import model.constant.SanimalDataFormats;
import model.image.ImageContainer;
import model.image.ImageDirectory;

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
	public HBox mainPane;

	///
	/// FXML Bound Fields end
	///

	private ChangeListener<Boolean> tempListenerRef;

	private ChangeListener<Number> expandedListener = (observable, oldValue, newValue) ->
	{
		if (newValue.doubleValue() != -1)
			if (this.getTreeItem() != null)
				this.getTreeItem().setExpanded(false);
	};

	@FXML
	public void initialize()
	{
		// If a new tree item is expanded that is also disabled, hide it
		this.treeItemProperty().addListener((observable, oldValue, newValue) ->
		{
			// Unbind any old values since we don't need them to be listened too anymore
			if (oldValue != null && tempListenerRef != null)
				oldValue.expandedProperty().removeListener(tempListenerRef);
			// Bind the new value
			if (newValue != null)
			{
				tempListenerRef = (ignored, oldExpanded, newExpanded) ->
				{
					if (UploadTreeCellController.this.isDisabled() && newExpanded)
						newValue.setExpanded(false);
				};
				newValue.expandedProperty().addListener(tempListenerRef);
			}
		});
	}

	/**
	 * Called when we want to display a new image container
	 *
	 * @param item The new item to display
	 * @param empty If the item is null and the cell should be empty
	 */
	@Override
	protected void updateItem(ImageContainer item, boolean empty)
	{
		// Remove the previous listener if there was one
		if (this.getItem() instanceof ImageDirectory)
		{
			((ImageDirectory) this.getItem()).uploadProgressProperty().removeListener(expandedListener);
			this.disableProperty().unbind();
			this.setDisable(false);
		}

		// Update the internal item
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
			// Update the icon property
			this.imgIcon.imageProperty().unbind();
			this.imgIcon.imageProperty().bind(item.getTreeIconProperty());
			this.lblText.setText(item.toString());

			// If the item is a directory that is being uploaded, disable it
			if (item instanceof ImageDirectory)
			{
				ImageDirectory imageDirectory = (ImageDirectory) item;
				this.disableProperty().bind(imageDirectory.uploadProgressProperty().isNotEqualTo(-1));
				imageDirectory.uploadProgressProperty().addListener(expandedListener);
			}

			// Update the graphic
			this.setGraphic(mainPane);
		}
	}

	/**
	 * Called when a drag is performed on the upload tree cell
	 *
	 * @param mouseEvent Consumed if the drag is valid
 	 */
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
