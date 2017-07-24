package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import model.location.Location;
import model.species.Species;

/**
 * Controller class for the location list cell
 */
public class LocationListEntryController extends ListCell<Location>
{
    ///
    /// FXML bound fields start
    ///

    // The main background pane
    @FXML
    public VBox mainPane;

    // The name of the location
    @FXML
    public Label lblName;

    // The location location (lat/lng)
    @FXML
    public Label lblLocation;

    // The elevation of the location
    @FXML
    public Label lblElevation;

    ///
    /// FXML bound fields end
    ///

    /**
     * Update item is called whenever the cell gets updated
     *
     * @param location The new location
     * @param empty If the cell is empty
     */
    @Override
    protected void updateItem(Location location, boolean empty)
    {
        // Update the cell first
        super.updateItem(location, empty);

        // Set the text to null
        this.setText(null);

        // If the cell is empty we have no graphic
        if (empty && location == null)
        {
            this.setGraphic(null);
        }
        // if the cell is not empty, set the field's values and set the graphic
        else
        {
            this.lblName.setText(location.getName());
            this.lblLocation.setText(location.getLat() + "\n" + location.getLng());
            this.lblElevation.setText(location.getElevation() + "m");
            this.setGraphic(mainPane);
        }
    }
}
