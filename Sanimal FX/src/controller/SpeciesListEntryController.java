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
            this.lblName.setText(species.getName());
            this.lblScientificName.setText(species.getScientificName());
            this.imageView.setImage(new Image(species.getSpeciesIcon()));
            this.setGraphic(mainPane);
        }
    }
}
