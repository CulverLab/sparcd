package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.CharacterStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.location.UTMCoord;
import model.util.RoundingUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.controlsfx.validation.decoration.ValidationDecoration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the species creator form
 */
public class LocationCreatorController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// Location Name
	@FXML
	public TextField txtName;

	// Location location in latitude and longitude
	@FXML
	public TextField txtLatitude;
	@FXML
	public TextField txtLongitude;

	// Location elevation
	@FXML
	public TextField txtElevation;

	// Radio buttons for choosing UTM or lat/lng
	@FXML
	public ToggleGroup locTypeBtnGroup;
	@FXML
	public RadioButton rbnUTM;
	@FXML
	public RadioButton rbnLatLng;

	// Two panes each with a different way of entering location data
	@FXML
	public GridPane pneUTM;
	@FXML
	public GridPane pneLatLng;

	// Location location in UTM given a letter, zone, easting, and northing
	@FXML
	public TextField txtLetter;
	@FXML
	public TextField txtZone;
	@FXML
	public TextField txtEasting;
	@FXML
	public TextField txtNorthing;

	@FXML
	public ToggleButton tbnMeters;
	@FXML
	public ToggleButton tbnFeet;
	@FXML
	public SegmentedButton sbnUnits;

	@FXML
	public Button btnConfirm;

	///
	/// FXML bound fields end
	///

	private static final Double FEET_TO_METERS = 0.3048;

	// This allows for fields to be validated and displays a red X if input is invalid
	private ValidationSupport basicFieldValidator = new ValidationSupport();
	private ValidationSupport latLngValidator = new ValidationSupport();
	private ValidationSupport UTMValidator = new ValidationSupport();

	// The location that this creator is currently editing
	private Location locationToEdit;
	// The fields to bind text fields to for easy access
	private StringProperty newName = new SimpleStringProperty("");
	private StringProperty newLatitude = new SimpleStringProperty("");
	private StringProperty newLongitude = new SimpleStringProperty("");
	private StringProperty newElevation = new SimpleStringProperty("");
	private StringProperty newLetter = new SimpleStringProperty("");
	private StringProperty newZone = new SimpleStringProperty("");
	private StringProperty newEasting = new SimpleStringProperty("");
	private StringProperty newNorthing = new SimpleStringProperty("");

	// Invalid UTM letter letters
	private static final Character[] INVALID_UTM_LETTERS = new Character[]
			{'A', 'B', 'I', 'O', 'Y', 'Z'};

	/**
	 * Initialize initializes the form by creating bindings
	 *
	 * @param location  ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Bind the text fields to their fields
		this.txtName.textProperty().bindBidirectional(newName);
		this.txtLatitude.textProperty().bindBidirectional(newLatitude);
		this.txtLongitude.textProperty().bindBidirectional(newLongitude);
		this.txtElevation.textProperty().bindBidirectional(newElevation);
		this.txtLetter.textProperty().bindBidirectional(newLetter);
		this.txtZone.textProperty().bindBidirectional(newZone);
		this.txtEasting.textProperty().bindBidirectional(newEasting);
		this.txtNorthing.textProperty().bindBidirectional(newNorthing);

		// Used below, simply consumes the event if the toggle button is selected so it does not get deselected
		EventHandler<MouseEvent> consumeMouseEventfilter = (MouseEvent mouseEvent) -> {
			if (((Toggle) mouseEvent.getSource()).isSelected()) {
				mouseEvent.consume();
			}
		};

		// Ensure that clicking a selected button does not deselect the button otherwise. We do this by registering a click, press, and release event filter
		// for each of the toggle buttons.
		this.tbnFeet.addEventFilter(MouseEvent.MOUSE_PRESSED, consumeMouseEventfilter);
		this.tbnFeet.addEventFilter(MouseEvent.MOUSE_CLICKED, consumeMouseEventfilter);
		this.tbnFeet.addEventFilter(MouseEvent.MOUSE_RELEASED, consumeMouseEventfilter);
		this.tbnMeters.addEventFilter(MouseEvent.MOUSE_PRESSED, consumeMouseEventfilter);
		this.tbnMeters.addEventFilter(MouseEvent.MOUSE_CLICKED, consumeMouseEventfilter);
		this.tbnMeters.addEventFilter(MouseEvent.MOUSE_RELEASED, consumeMouseEventfilter);

		// We enable decoration so that errors are represented by a red outline of the text box
		this.latLngValidator.setErrorDecorationEnabled(true);
		this.UTMValidator.setErrorDecorationEnabled(true);
		this.basicFieldValidator.setErrorDecorationEnabled(true);

		// The name must not be empty
		this.basicFieldValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Location Name must not be empty!"));

		// The latitude must be between 85 and -85
		this.latLngValidator.registerValidator(this.txtLatitude, true, Validator.<String>createPredicateValidator(this::latitudeValid, "Latitude must be +/-85!"));
		// The latitude must be between 180 and -180
		this.latLngValidator.registerValidator(this.txtLongitude, true, Validator.<String>createPredicateValidator(this::longitudeValid, "Longitude must be +/-180!"));

		// The letter must be C to X excluding I and O
		this.UTMValidator.registerValidator(this.txtLetter, true, Validator.<String>createPredicateValidator(this::letterValid, "UTM Letter must be a letter from C to X excluding I and O"));
		// The zone must be between 1 and 60
		this.UTMValidator.registerValidator(this.txtZone, true, Validator.<String>createPredicateValidator(this::zoneValid, "UTM Zone must be a number between 1 and 60"));
		// The easting must be a double between 1000000 and 0
		this.UTMValidator.registerValidator(this.txtEasting, true, Validator.<String>createPredicateValidator(this::eastingValid, "Easting must be between 0 and 1000000"));
		// The northing must be a double between 10000000 and 0
		this.UTMValidator.registerValidator(this.txtNorthing, true, Validator.<String>createPredicateValidator(this::northingValid, "Northing must be between 0 and 1000000"));

		// Elevation must be a double
		this.basicFieldValidator.registerValidator(this.txtElevation, true, Validator.createPredicateValidator(this::isValidDouble, "Elevation must be a number!"));

		// Bind the pane's visibility to if the radio button is checked
		this.pneLatLng.visibleProperty().bind(this.rbnLatLng.selectedProperty());
		this.pneUTM.visibleProperty().bind(this.rbnUTM.selectedProperty());

		// When we select latLng, recalculate latLng from UTM
		this.rbnLatLng.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue && !this.UTMValidator.isInvalid())
				this.recalculateLatLngFromUTM();
		}));

		// When we select UTM, recalculate UTM from latLng
		this.rbnUTM.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue && !this.latLngValidator.isInvalid())
				this.recalculateUTMFromLatLng();
		}));

		// Disable the confirm button if the validators are set to invalid
		this.btnConfirm.disableProperty().bind(
				this.basicFieldValidator.invalidProperty()
						.or(Bindings.when(this.rbnLatLng.selectedProperty())
										.then(this.latLngValidator.invalidProperty())
										.otherwise(this.UTMValidator.invalidProperty())));
	}

	/**
	 * We can set the location to edit using this function
	 *
	 * @param location The location to edit
	 */
	public void setLocation(Location location)
	{
		// if the various fields are valid, then we set our fields that are data bound
		this.locationToEdit = location;
		if (this.locationToEdit.nameValid())
			this.newName.set(location.getName());
		if (this.locationToEdit.latValid())
			this.newLatitude.set(locationToEdit.getLat().toString());
		if (this.locationToEdit.lngValid())
			this.newLongitude.set(locationToEdit.getLng().toString());
		if (this.locationToEdit.elevationValid())
			this.newElevation.set(locationToEdit.getElevation().toString());
		// If the lat and long are valid, we calculate the equivalent UTM coordinates and set our easting/northing/letter/zone
		if (this.locationToEdit.latValid() && this.locationToEdit.lngValid())
		{
			this.recalculateUTMFromLatLng();
		}
	}

	/**
	 * When confirm is pressed we update the fields in the location only if they're all valid
	 *
	 * @param actionEvent ignored
	 */
	public void confirmPressed(ActionEvent actionEvent)
	{
		// We store the data as Lat & Lng, so if UTM is selected, recalculate them from UTM since that must be valid by the above condition
		if (this.rbnUTM.isSelected())
			this.recalculateLatLngFromUTM();

		// Set the location's fields, and close the editor window
		locationToEdit.setName(newName.getValue());
		locationToEdit.setLat(RoundingUtils.roundLat(Double.parseDouble(newLatitude.getValue())));
		locationToEdit.setLng(RoundingUtils.roundLng(Double.parseDouble(newLongitude.getValue())));
		if (this.tbnMeters.isSelected())
			locationToEdit.setElevation((double) Math.round(Double.parseDouble(newElevation.getValue())));
		else if (this.tbnFeet.isSelected())
			locationToEdit.setElevation((double) Math.round(Double.parseDouble(newElevation.getValue()) * FEET_TO_METERS));
		((Stage) this.txtName.getScene().getWindow()).close();
	}

	/**
	 * Cancel just closes the window
	 *
	 * @param actionEvent ignored
	 */
	public void cancelPressed(ActionEvent actionEvent)
	{
		((Stage) this.txtName.getScene().getWindow()).close();
	}

	/**
	 * Given a valid lat/lng this re-calculates UTM
	 */
	private void recalculateUTMFromLatLng()
	{
		UTMCoord equivalent = SanimalAnalysisUtils.Deg2UTM(Double.parseDouble(this.newLatitude.getValue()), Double.parseDouble(this.newLongitude.getValue()));
		this.newLetter.setValue(equivalent.getLetter().toString());
		this.newZone.setValue(equivalent.getZone().toString());
		this.newEasting.setValue(Long.toString(Math.round(equivalent.getEasting())));
		this.newNorthing.setValue(Long.toString(Math.round(equivalent.getNorthing())));
	}

	/**
	 * Given a valid UTM, this re-calculates lat/lng
	 */
	private void recalculateLatLngFromUTM()
	{
		Double[] equivalent = SanimalAnalysisUtils.UTM2Deg(new UTMCoord(Double.parseDouble(this.newEasting.getValue()), Double.parseDouble(this.newNorthing.getValue()), Integer.parseInt(this.newZone.getValue()), this.newLetter.getValue().charAt(0)));
		// Round to 4 decimal places
		this.newLatitude.setValue(Double.toString(RoundingUtils.roundLat(equivalent[0])));
		this.newLongitude.setValue(Double.toString(RoundingUtils.roundLng(equivalent[1])));
	}

	///
	/// Validators for text fields start
	///

	private Boolean isValidDouble(String number)
	{
		try
		{
			return !Double.isNaN(Double.parseDouble(number));
		}
		catch (NumberFormatException ignored)
		{
			return false;
		}
	}

	private Boolean isValidInteger(String number)
	{
		try
		{
			Integer.parseInt(number);
			return true;
		}
		catch (NumberFormatException ignored)
		{
			return false;
		}
	}

	private Boolean latitudeValid(String latitude)
	{
		return this.isValidDouble(latitude) && Double.parseDouble(latitude) <= 85 && Double.parseDouble(latitude) >= -85;
	}

	private Boolean longitudeValid(String longitude)
	{
		return this.isValidDouble(longitude) && Double.parseDouble(longitude) <= 180 && Double.parseDouble(longitude) >= -180;
	}

	private Boolean letterValid(String letter)
	{
		return letter.length() == 1 && Character.isLetter(letter.charAt(0)) && !ArrayUtils.contains(INVALID_UTM_LETTERS, letter.charAt(0));
	}

	private Boolean zoneValid(String zone)
	{
		return this.isValidInteger(zone) && Integer.parseInt(zone) >= 1 && Integer.parseInt(zone) <= 60;
	}

	private Boolean eastingValid(String easting)
	{
		return this.isValidDouble(easting) && Double.parseDouble(easting) <= 1000000 && Double.parseDouble(easting) >= 0;
	}

	private Boolean northingValid(String northing)
	{
		return this.isValidDouble(northing) && Double.parseDouble(northing) <= 10000000 && Double.parseDouble(northing) >= 0;
	}

	///
	/// Validators for text fields end
	///
}
