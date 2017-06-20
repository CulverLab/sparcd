package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
	public TextField txtCount;

	public SpeciesEntryListEntryController()
	{
		super();
		/*
		this.txtCount.textProperty().addListener(((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*"))
				this.txtCount.setText(newValue.replaceAll("[^\\d]", ""));
			try
			{

			}
			catch (NumberFormatException ignored)
			{

			}
		}));*/
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
			this.txtCount.setText(speciesEntry.getAmount().toString());
			this.setGraphic(mainPane);
		}
	}
}
