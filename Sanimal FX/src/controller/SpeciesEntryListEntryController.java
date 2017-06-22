package controller;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.SanimalData;
import model.species.Species;
import model.species.SpeciesEntry;

import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesEntryListEntryController extends ListCell<SpeciesEntry>
{
	@FXML
	public GridPane mainPane;

	@FXML
	public Label lblName;

	@FXML
	public Button btnRemove;

	@FXML
	public Spinner<Integer> txtCount;

	@FXML
	public void initialize()
	{
		this.txtCount.valueProperty().addListener(((observable, oldValue, newValue) -> this.getItem().setAmount(newValue)));
	}

	@Override
	protected void updateItem(SpeciesEntry speciesEntry, boolean empty)
	{
		super.updateItem(speciesEntry, empty);

		this.setText(null);

		if (empty && speciesEntry == null)
		{
			this.setGraphic(null);
		}
		else
		{
			this.lblName.setText(speciesEntry.getSpecies().getName());
			this.txtCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, speciesEntry.getAmount()));
			this.setGraphic(mainPane);
		}
	}

	public void removeEntry(ActionEvent actionEvent)
	{
		this.getListView().getItems().remove(this.getItem());
	}
}
