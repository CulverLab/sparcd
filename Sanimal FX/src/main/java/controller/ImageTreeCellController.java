package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import model.SanimalData;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;

import java.util.Optional;

import static model.constant.SanimalDataFormats.*;


public class ImageTreeCellController extends TreeCell<ImageContainer>
{
	///
	/// FXML Bound Fields start
	///

	@FXML
	public ImageView imgIcon;
	@FXML
	public Label lblText;
	@FXML
	public HBox mainPane;

	///
	/// FXML Bound Fields end
	///


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
			this.lblText.setText(item.getFile().getName());
			this.setGraphic(mainPane);
		}
	}

	/**
	 * If our mouse hovers over the image pane and we're dragging, we accept the transfer
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void cellDragOver(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(LOCATION_NAME_FORMAT) && dragboard.hasContent(LOCATION_ID_FORMAT)) || (dragboard.hasContent(SPECIES_NAME_FORMAT) && dragboard.hasContent(SPECIES_SCIENTIFIC_NAME_FORMAT) && this.getItem() instanceof ImageEntry))
			dragEvent.acceptTransferModes(TransferMode.COPY);
		dragEvent.consume();
	}

	/**
	 * When the drag from the species or location list enters the image
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void cellDragEntered(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(LOCATION_NAME_FORMAT) && dragboard.hasContent(LOCATION_ID_FORMAT)) || (dragboard.hasContent(SPECIES_NAME_FORMAT) && dragboard.hasContent(SPECIES_SCIENTIFIC_NAME_FORMAT) && this.getItem() instanceof ImageEntry))
			if (!this.mainPane.getStyleClass().contains("draggedOver"))
				this.mainPane.getStyleClass().add("draggedOver");
		dragEvent.consume();
	}

	/**
	 * When the drag from the species or location list exits the image
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void cellDragExited(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(LOCATION_NAME_FORMAT) && dragboard.hasContent(LOCATION_ID_FORMAT)) || (dragboard.hasContent(SPECIES_NAME_FORMAT) && dragboard.hasContent(SPECIES_SCIENTIFIC_NAME_FORMAT) && this.getItem() instanceof ImageEntry))
			if (this.mainPane.getStyleClass().contains("draggedOver"))
				this.mainPane.getStyleClass().remove("draggedOver");
		dragEvent.consume();
	}

	/**
	 * When we drop the species or location onto the image, we add that species or location to the list
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void cellDragDropped(DragEvent dragEvent)
	{
		// Create a flag that will be set to true if everything went well
		Boolean success = false;
		// Grab the dragboard
		Dragboard dragboard = dragEvent.getDragboard();
		// If our dragboard has a string we have data which we need
		if (dragboard.hasContent(SPECIES_NAME_FORMAT) && dragboard.hasContent(SPECIES_SCIENTIFIC_NAME_FORMAT) && this.getItem() instanceof ImageEntry)
		{
			String commonName = (String) dragboard.getContent(SPECIES_NAME_FORMAT);
			String scientificName = (String) dragboard.getContent(SPECIES_SCIENTIFIC_NAME_FORMAT);
			// Grab the species with the given ID
			Optional<Species> toAdd = SanimalData.getInstance().getSpeciesList().stream().filter(species -> species.getScientificName().equals(scientificName) && species.getName().equals(commonName)).findFirst();
			// Add the species to the image
			ImageContainer item = this.getItem();
			if (toAdd.isPresent())
			{
				((ImageEntry) item).addSpecies(toAdd.get(), 1);
				success = true;
			}
		}
		else if (dragboard.hasContent(LOCATION_NAME_FORMAT) && dragboard.hasContent(LOCATION_ID_FORMAT))
		{
			String locationName = (String) dragboard.getContent(LOCATION_NAME_FORMAT);
			String locationId = (String) dragboard.getContent(LOCATION_ID_FORMAT);
			// Grab the species with the given ID
			Optional<Location> toAdd = SanimalData.getInstance().getLocationList().stream().filter(location -> location.getName().equals(locationName) && location.getId().equals(locationId)).findFirst();
			// Add the species to the image
			if (toAdd.isPresent())
			{
				this.getItem().setLocationTaken(toAdd.get());
				success = true;
			}
		}
		// Set the success equal to the flag, and consume the event
		dragEvent.setDropCompleted(success);
		dragEvent.consume();
	}
}
