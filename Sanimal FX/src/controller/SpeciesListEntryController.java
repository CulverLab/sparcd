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

/**
 * Controller class for the species list entry
 */
public class SpeciesListEntryController extends ListCell<Species>
{
    ///
    /// FXML bound fields start
    ///

    // A reference to the main background pane
    @FXML
    private HBox mainPane;

    // A species icon
    @FXML
    private ImageView imageView;

    // The species name
    @FXML
    private Label lblName;
    // The species scientific name
    @FXML
    private Label lblScientificName;

    ///
    /// FXML bound fields end
    ///

    /**
     * Update item is called when the list cell gets a new species
     *
     * @param species The new species
     * @param empty if the cell will be empty
     */
    @Override
    protected void updateItem(Species species, boolean empty)
    {
        // Update the cell internally
        super.updateItem(species, empty);

        // Set the text of the cell to nothing
        this.setText(null);

        // If no species was given and the cell was empty, clear the graphic
        if (empty && species == null)
        {
            this.setGraphic(null);
        }
        else
        {
            // Set the name to the species name
            this.lblName.setText(species.getName());
            // Set the scientific name to the species scientific name
            this.lblScientificName.setText(species.getScientificName());
            // Set the image in the image view to the given species icon
            this.imageView.setImage(new Image(species.getSpeciesIcon()));
            // Set the graphic to display
            this.setGraphic(mainPane);
        }
    }
}
