package controller.importView;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import model.SanimalData;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.location.UTMCoord;
import model.util.SettingsData;

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

    // The ID of the location
    @FXML
    public Label lblId;

    // The location location (lat/lng)
    @FXML
    public Label lblLocationFirst;
    @FXML
    public Label lblLocationSecond;

    // The elevation of the location
    @FXML
    public Label lblLocationThird;

    ///
    /// FXML bound fields end
    ///

    /**
     * Initializes the location list entry by adding a listener to the format property
     */
    @FXML
    public void initialize()
    {
        // When we change the location format refresh the labels
        SanimalData.getInstance().getSettings().locationFormatProperty().addListener((observable, oldValue, newValue) ->
        {
            if (this.getItem() != null)
                this.refreshLabels(this.getItem(), newValue);
        });
    }

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
            this.lblId.setText(location.getId());
            this.refreshLabels(location, SanimalData.getInstance().getSettings().getLocationFormat());
            this.setGraphic(mainPane);
        }
    }

    /**
     * Update the location labels based on the format
     *
     * @param location The new location
     * @param format The format of the location
     */
    private void refreshLabels(Location location, SettingsData.LocationFormat format)
    {
        // If we are using latitude & longitude
        if (format == SettingsData.LocationFormat.LatLong)
        {
            // Locations are stored in lat/lng so we can just use the value
            this.lblLocationFirst.setText(location.getLat().toString());
            this.lblLocationSecond.setText(location.getLng().toString());
            this.lblLocationThird.setText(location.getElevation().intValue() + "m");
        }
        // If we are using UTM
        else if (format == SettingsData.LocationFormat.UTM)
        {
            // Convert to UTM
            UTMCoord utmEquiv = SanimalAnalysisUtils.Deg2UTM(location.getLat(), location.getLng());
            // Update the labels
            this.lblLocationFirst.setText(utmEquiv.getEasting().intValue() + "e");
            this.lblLocationSecond.setText(utmEquiv.getNorthing().intValue() + "n");
            this.lblLocationThird.setText("Zone " + utmEquiv.getZone().toString() + utmEquiv.getLetter().toString() + " at " + location.getElevation().intValue() + "m");
        }
    }
}