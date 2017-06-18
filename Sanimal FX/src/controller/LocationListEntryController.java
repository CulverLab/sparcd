package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import model.location.Location;
import model.species.Species;

/**
 * Created by David on 6/14/2017.
 */
public class LocationListEntryController extends ListCell<Location>
{
    @FXML
    public VBox mainPane;

    @FXML
    public Label lblName;

    @FXML
    public Label lblLocation;

    @FXML
    public Label lblElevation;

    @Override
    protected void updateItem(Location location, boolean empty)
    {
        super.updateItem(location, empty);

        this.setText(null);

        if (empty && location == null)
        {
            this.setGraphic(null);
        }
        else
        {
            this.lblName.setText(location.getName());
            this.lblLocation.setText(location.getLat() + ", " + location.getLng());
            this.lblElevation.setText(location.getElevation() + "ft");
            this.setGraphic(mainPane);
        }
    }
}
