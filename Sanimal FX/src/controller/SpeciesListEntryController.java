package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.species.Species;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesListEntryController extends ListCell<Species>
{
    @FXML
    private HBox mainPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Label lblName;
    @FXML
    private Label lblScientificName;

    private FXMLLoader loader = null;

    @Override
    protected void updateItem(Species species, boolean empty)
    {
        super.updateItem(species, empty);

        this.setText(null);

        if (empty && species == null)
        {
            this.setGraphic(null);
        }
        else
        {
            if (this.loader == null)
            {
                this.loader = new FXMLLoader(getClass().getResource("../view/SpeciesListEntry.fxml"));
                this.loader.setController(this);

                try
                {
                    this.loader.load();
                }
                catch (IOException exception)
                {
                    System.err.println("Could not load the FXML file for the species list entry!");
                    exception.printStackTrace();
                    return;
                }
            }

            this.lblName.setText(species.getName());
            this.lblScientificName.setText(species.getScientificName());
            this.imageView.setImage(new Image(species.getSpeciesIcon()));
            this.setGraphic(mainPane);
        }
    }
}
