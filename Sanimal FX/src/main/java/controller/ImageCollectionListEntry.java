package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import model.cyverse.ImageCollection;
import model.species.Species;

import java.net.URL;
import java.util.ResourceBundle;

public class ImageCollectionListEntry extends ListCell<ImageCollection>
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public HBox mainPane;

	@FXML
	public Label lblCollectionName;
	@FXML
	public Label lblCollectionContactInfo;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Called once the controller has been setup
	 */
	@FXML
	public void initialize()
	{

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
			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}
}
