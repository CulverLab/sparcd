package controller.mapView;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.location.Location;

/**
 * Simple class for displaying location information as a pop-over
 */
public class LocationPopOverController
{
	///
	/// FXML Bound Fields Start
	///

	// The location's name
	@FXML
	public Label lblName;
	// The location's id
	@FXML
	public Label lblID;
	// The location's latitude
	@FXML
	public Label lblLatitude;
	// The location's longitude
	@FXML
	public Label lblLongitude;
	// The location's elevation
	@FXML
	public Label lblElevation;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Nothing to initialize
	 */
	@FXML
	public void initialize()
	{
	}

	/**
	 * Called to update the popover with new data
	 *
	 * @param location The new location to display
	 */
	public void updateLocation(Location location)
	{
		// Unbind all the location popup's labels
		this.lblName.textProperty().unbind();
		this.lblID.textProperty().unbind();
		this.lblLatitude.textProperty().unbind();
		this.lblLongitude.textProperty().unbind();
		this.lblElevation.textProperty().unbind();

		// Re-bind all the location popup's fields
		this.lblName.textProperty().bind(location.nameProperty());
		this.lblID.textProperty().bind(location.idProperty());
		this.lblLatitude.textProperty().bind(location.getLatProperty().asString());
		this.lblLongitude.textProperty().bind(location.getLngProperty().asString());
		this.lblElevation.textProperty().bind(location.getElevationProperty().asString());
	}
}
