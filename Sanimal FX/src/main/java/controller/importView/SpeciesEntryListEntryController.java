package controller.importView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.species.SpeciesEntry;

/**
 * Controller class for the species entry list entry
 */
public class SpeciesEntryListEntryController extends ListCell<SpeciesEntry>
{
	///
	/// FXML bound fields start
	///

	// The main background pane
	@FXML
	public GridPane mainPane;

	// The name of the species entry
	@FXML
	public Label lblName;

	// The remove button
	@FXML
	public Button btnRemove;

	// The count spinner
	@FXML
	public Spinner<Integer> txtCount;

	///
	/// FXML bound fields end
	///

	/**
	 * Initialize binds the count value to the item's species count value
	 */
	@FXML
	public void initialize()
	{
		this.txtCount.valueProperty().addListener(((observable, oldValue, newValue) -> this.getItem().setCount(newValue)));
	}

	/**
	 * This gets called whenever the cell gets a new species entry
	 *
	 * @param speciesEntry The new entry
	 * @param empty If the cell is now empty
	 */
	@Override
	protected void updateItem(SpeciesEntry speciesEntry, boolean empty)
	{
		// Update the item internally
		super.updateItem(speciesEntry, empty);

		// Set the current text to null
		this.setText(null);

		// If the cell is empty and the entry is null, set the graphic to null, hiding the graphic
		if (empty && speciesEntry == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Set the name to the given species name
			this.lblName.setText(speciesEntry.getSpecies().getCommonName());
			// Set the spinner to a default value
			this.txtCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, speciesEntry.getCount()));
			// Update the graphic to the main pane
			this.setGraphic(mainPane);
		}
	}

	/**
	 * Remove entry removes the species from the given image
	 *
	 * @param actionEvent ignored
	 */
	public void removeEntry(ActionEvent actionEvent)
	{
		this.getListView().getItems().remove(this.getItem());
	}
}
