package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.converter.CharacterStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.location.UTMCoord;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

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

	///
	/// FXML bound fields end
	///

	// This allows for fields to be validated and displays a red X if input is invalid
	private ValidationSupport fieldValidator = new ValidationSupport();

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

		// SIDE NOTE: You must have 1 formatter per field so we can't just create 1 double formatter =(

		// Set the text formatter for latitude to accept any doubles
		this.txtLatitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidDouble(newText))
				return change;
			else
				return null;
		}));
		// Set the text formatter for longitude to accept any doubles
		this.txtLongitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidDouble(newText))
				return change;
			else
				return null;
		}));
		// Set the text formatter for the letter in UTM to accept any single character
		this.txtLetter.setTextFormatter(new TextFormatter<Character>(new CharacterStringConverter(), null, change ->
		{
			String character = change.getControlNewText();
			if (character.length() == 1 && Character.isLetter(character.charAt(0)))
				return change;
			else
				return null;
		}));
		// Set the text formatter for the zone in UTM to be an integer
		this.txtZone.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 1, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidInteger(newText))
				return change;
			else
				return null;
		}));
		// Set the text formatter for the easting to accept any doubles
		this.txtEasting.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidDouble(newText))
				return change;
			else
				return null;
		}));
		// Set the text formatter for the northing to accept any doubles
		this.txtNorthing.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidDouble(newText))
				return change;
			else
				return null;
		}));
		// Set the text formatter for the elevation to accept any doubles
		this.txtElevation.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (this.isValidDouble(newText))
				return change;
			else
				return null;
		}));

		// We enable decoration so that errors are represented by a red outline of the text box
		this.fieldValidator.setErrorDecorationEnabled(true);

		// The name must not be empty
		this.fieldValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Location Name must not be empty!"));

		// The latitude must be between 85 and -85
		this.fieldValidator.registerValidator(this.txtLatitude, true, Validator.<String>createPredicateValidator(this::latitudeValid, "Latitude must be +/-85!"));
		// The latitude must be between 180 and -180
		this.fieldValidator.registerValidator(this.txtLongitude, true, Validator.<String>createPredicateValidator(this::longitudeValid, "Longitude must be +/-180!"));

		// The letter must be C to X excluding I and O
		this.fieldValidator.registerValidator(this.txtLetter, true, Validator.<String>createPredicateValidator(this::letterValid, "UTM Letter must be a letter from C to X excluding I and O"));
		// The zone must be between 1 and 60
		this.fieldValidator.registerValidator(this.txtZone, true, Validator.<String>createPredicateValidator(this::zoneValid, "UTM Zone must be a number between 1 and 60"));
		// The easting must be a double between 1000000 and 0
		this.fieldValidator.registerValidator(this.txtEasting, true, Validator.<String>createPredicateValidator(this::eastingValid, "Easting must be between 0 and 1000000"));
		// The northing must be a double between 10000000 and 0
		this.fieldValidator.registerValidator(this.txtNorthing, true, Validator.<String>createPredicateValidator(this::northingValid, "Northing must be between 0 and 1000000"));

		// Elevation must be a double
		this.fieldValidator.registerValidator(this.txtElevation, true, Validator.createPredicateValidator(this::isValidDouble, "Elevation must be a number!"));

		// Bind the pane's visibility to if the radio button is checked
		pneLatLng.visibleProperty().bind(rbnLatLng.selectedProperty());
		pneUTM.visibleProperty().bind(rbnUTM.selectedProperty());

		// If the lat & long are valid, recalculate the new UTM equivalent
		this.newLatitude.addListener(((observable, oldValue, newValue) -> {if (this.longitudeValid(this.newLongitude.getValue()) && this.latitudeValid(newValue)) { this.recalculateUTMFromLatLng(); }}));
		this.newLongitude.addListener(((observable, oldValue, newValue) -> {if (this.longitudeValid(newValue) && this.latitudeValid(this.newLatitude.getValue())) { this.recalculateUTMFromLatLng(); }}));

		// If the UTM is valid, recalculate the new Lat/Lng equivalent
		this.newLetter.addListener(((observable, oldValue, newValue) -> {if (
						this.letterValid(newValue) &&
						this.zoneValid(this.newZone.getValue()) &&
						this.northingValid(this.newNorthing.getValue()) &&
						this.eastingValid(this.newEasting.getValue()))
							this.recalculateLatLngFromUTM();}));
		this.newZone.addListener(((observable, oldValue, newValue) -> {if (
						this.letterValid(this.newLetter.getValue()) &&
						this.zoneValid(newValue) &&
						this.northingValid(this.newNorthing.getValue()) &&
						this.eastingValid(this.newEasting.getValue()))
							this.recalculateLatLngFromUTM();}));
		this.newNorthing.addListener(((observable, oldValue, newValue) -> {if (
						this.letterValid(this.newLetter.getValue()) &&
						this.zoneValid(this.newZone.getValue()) &&
						this.northingValid(newValue) &&
						this.eastingValid(this.newEasting.getValue()))
							this.recalculateLatLngFromUTM();}));
		this.newEasting.addListener(((observable, oldValue, newValue) -> {if (
						this.letterValid(this.newLetter.getValue()) &&
						this.zoneValid(this.newZone.getValue()) &&
						this.northingValid(this.newNorthing.getValue()) &&
						this.eastingValid(newValue))
							this.recalculateLatLngFromUTM();}));
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
		// If the fields are valid...
		if (!this.fieldValidator.isInvalid())
		{
			// Set the location's fields, and close the editor window
			locationToEdit.setName(newName.getValue());
			// Round to 4 decimal places
			locationToEdit.setLat(Math.round(Double.parseDouble(newLatitude.getValue()) * 1000.0) / 1000.0);
			locationToEdit.setLng(Math.round(Double.parseDouble(newLongitude.getValue()) * 1000.0) / 1000.0);
			locationToEdit.setElevation((double) Math.round(Double.parseDouble(newElevation.getValue())));
			((Stage) this.txtName.getScene().getWindow()).close();
		}
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
		this.newEasting.setValue(equivalent.getEasting().toString());
		this.newNorthing.setValue(equivalent.getNorthing().toString());
	}

	/**
	 * Given a valid UTM, this re-calculates lat/lng
	 */
	private void recalculateLatLngFromUTM()
	{
		Double[] equivalent = SanimalAnalysisUtils.UTM2Deg(new UTMCoord(Double.parseDouble(this.newEasting.getValue()), Double.parseDouble(this.newNorthing.getValue()), Integer.parseInt(this.newZone.getValue()), this.newLetter.getValue().charAt(0)));
		this.newLatitude.setValue(equivalent[0].toString());
		this.newLongitude.setValue(equivalent[1].toString());
	}

	///
	/// Validators for text fields start
	///

	private Boolean isValidDouble(String number)
	{
		try
		{
			Double.parseDouble(number);
			return true;
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
