package controller.importView;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import library.ToggleButtonSelector;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.location.UTMCoord;
import model.util.RoundingUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.easybind.EasyBind;

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
	// Location Id
	@FXML
	public TextField txtId;

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

	// Location location in UTM given a letter, zone, easting, and northing
	@FXML
	public Label lblZoneOrLat;
	@FXML
	public TextField txtZoneOrLat;
	@FXML
	public Label lblLetterOrLng;
	@FXML
	public TextField txtLetterOrLng;
	@FXML
	public Label lblEasting;
	@FXML
	public TextField txtEasting;
	@FXML
	public Label lblNorthing;
	@FXML
	public TextField txtNorthing;

	// The toggle buttons for meters and feet
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
	private static final Double METERS_TO_FEET = 1 / 0.3048;


	// This allows for fields to be validated and displays a red X if input is invalid
	private ValidationSupport fieldValidator = new ValidationSupport();

	// The location that this creator is currently editing
	private Location locationToEdit;
	// The fields to bind text fields to for easy access
	private StringProperty newName = new SimpleStringProperty("");
	private StringProperty newId = new SimpleStringProperty("");
	private StringProperty newElevation = new SimpleStringProperty("");
	private StringProperty newZoneOrLat = new SimpleStringProperty("");
	private StringProperty newLetterOrLng = new SimpleStringProperty("");
	private StringProperty newEasting = new SimpleStringProperty("");
	private StringProperty newNorthing = new SimpleStringProperty("");

	private BooleanProperty utmValid = new SimpleBooleanProperty(false);
	private BooleanProperty latLngValid = new SimpleBooleanProperty(false);

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
		this.txtId.textProperty().bindBidirectional(newId);
		this.txtLetterOrLng.textProperty().bindBidirectional(newLetterOrLng);
		this.txtElevation.textProperty().bindBidirectional(newElevation);
		this.txtZoneOrLat.textProperty().bindBidirectional(newZoneOrLat);
		this.txtEasting.textProperty().bindBidirectional(newEasting);
		this.txtNorthing.textProperty().bindBidirectional(newNorthing);

		// Ensure that clicking a selected button does not deselect the button otherwise. We do this by registering a click, press, and release event filter
		// for each of the toggle buttons.
		ToggleButtonSelector.makeUnselectable(this.tbnFeet);
		ToggleButtonSelector.makeUnselectable(this.tbnMeters);

		// We enable decoration so that errors are represented by a red outline of the text box
		this.fieldValidator.setErrorDecorationEnabled(true);

		// The name must not be empty
		this.fieldValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Location Name must not be empty!"));
		// The id must not be empty
		this.fieldValidator.registerValidator(this.txtId, true, Validator.createEmptyValidator("Location Id must not be empty!"));

		// The letter must be C to X excluding I and O or latitude must be between 85 and -85
		this.fieldValidator.registerValidator(this.txtLetterOrLng, true, Validator.createPredicateValidator(this::letterOrLngValid, "UTM Letter must be a letter from C to X excluding I and O or longitude between -85 and 85"));
		// The zone must be between 1 and 60 or latitude must be between 180 and -180
		this.fieldValidator.registerValidator(this.txtZoneOrLat, true, Validator.createPredicateValidator(this::zoneOrLatValid, "UTM Zone must be a number between 1 and 60 or latitude between -85 and 85"));
		// The easting must be a double between 1000000 and 0
		this.fieldValidator.registerValidator(this.txtEasting, true, Validator.createPredicateValidator(this::eastingValid, "Easting must be between 0 and 1000000"));
		// The northing must be a double between 10000000 and 0
		this.fieldValidator.registerValidator(this.txtNorthing, true, Validator.createPredicateValidator(this::northingValid, "Northing must be between 0 and 1000000"));

		// Elevation must be a double
		this.fieldValidator.registerValidator(this.txtElevation, true, Validator.createPredicateValidator(this::isValidDouble, "Elevation must be a number!"));

		// Boolean property which is true if we have latlng selected
		BooleanProperty showingLatLng = this.rbnLatLng.selectedProperty();
		// The first text field is either lat or zone, and the second is either long or letter
		this.lblZoneOrLat.textProperty().bind(EasyBind.map(showingLatLng, showingLL -> showingLL ? "Latitude (+/-85): " : "Zone (1-60): "));
		this.lblLetterOrLng.textProperty().bind(EasyBind.map(showingLatLng, showingLL -> showingLL ? "Longitude (+/-180): " : "Letter (C-X excluding I and O): "));
		// Hide the other fields if we're showing lat/lng
		this.lblEasting.visibleProperty().bind(showingLatLng.not());
		this.txtEasting.visibleProperty().bind(showingLatLng.not());
		this.lblNorthing.visibleProperty().bind(showingLatLng.not());
		this.txtNorthing.visibleProperty().bind(showingLatLng.not());

		// UTM is valid if the following is true:
		utmValid.bind(EasyBind.combine(this.txtZoneOrLat.textProperty(), this.txtLetterOrLng.textProperty(), this.txtEasting.textProperty(), this.txtNorthing.textProperty(), (zone, letter, easting, northing) ->
				this.zoneValid(zone) && this.letterValid(letter) && this.eastingValid(easting) && this.northingValid(northing)));

		// Lat/lng is valid if the following is true:
		latLngValid.bind(EasyBind.combine(this.txtZoneOrLat.textProperty(), this.txtLetterOrLng.textProperty(), (latitude, longitude) ->
			this.latitudeValid(latitude) && this.longutudeValid(longitude)));

		// When we select latLng, recalculate latLng from UTM
		this.rbnLatLng.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue && utmValid.getValue())
				this.recalculateLatLngFromUTM();
			else if (newValue && !utmValid.getValue())
			{
				this.txtZoneOrLat.clear();
				this.txtLetterOrLng.clear();
				this.txtEasting.clear();
				this.txtNorthing.clear();
			}
		}));

		// When we select UTM, recalculate UTM from latLng
		this.rbnUTM.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue && latLngValid.getValue())
				this.recalculateUTMFromLatLng();
			else if (newValue && !latLngValid.getValue())
			{
				this.txtZoneOrLat.clear();
				this.txtLetterOrLng.clear();
				this.txtEasting.clear();
				this.txtNorthing.clear();
			}
		}));

		// Disable the confirm button if the validators are set to invalid
		this.btnConfirm.disableProperty().bind(
			this.fieldValidator.invalidProperty()
				.or(Bindings.when(this.rbnLatLng.selectedProperty())
					.then(latLngValid.not())
					.otherwise(utmValid.not())));


		// When we select the meters button, we convert the existing elevation to meters
		this.tbnMeters.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (!oldValue && newValue)
				if (this.isValidDouble(this.txtElevation.getText()))
					this.txtElevation.setText(RoundingUtils.round(Double.parseDouble(this.txtElevation.getText()) * FEET_TO_METERS, 1) + "");
		});

		// When we select the feet button, we convert the existing elevation to feet
		this.tbnFeet.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (!oldValue && newValue)
				if (this.isValidDouble(this.txtElevation.getText()))
					this.txtElevation.setText(RoundingUtils.round(Double.parseDouble(this.txtElevation.getText()) * METERS_TO_FEET, 1) + "");
		});
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
		if (this.locationToEdit.idValid())
			this.newId.set(location.getId());
		if (this.locationToEdit.latValid())
			this.newZoneOrLat.set(locationToEdit.getLat().toString());
		if (this.locationToEdit.lngValid())
			this.newLetterOrLng.set(locationToEdit.getLng().toString());
		if (this.locationToEdit.elevationValid())
			this.newElevation.set(locationToEdit.getElevation().toString());
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
		locationToEdit.setId(newId.getValue());
		// Round latitude/longitude
		locationToEdit.setLat(RoundingUtils.roundLat(Double.parseDouble(newZoneOrLat.getValue())));
		locationToEdit.setLng(RoundingUtils.roundLng(Double.parseDouble(newLetterOrLng.getValue())));
		// If feet is selected, convert meters to feet
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
		UTMCoord equivalent = SanimalAnalysisUtils.Deg2UTM(Double.parseDouble(this.newZoneOrLat.getValue()), Double.parseDouble(this.newLetterOrLng.getValue()));
		this.newLetterOrLng.setValue(equivalent.getLetter().toString());
		this.newZoneOrLat.setValue(equivalent.getZone().toString());
		this.newEasting.setValue(Long.toString(Math.round(equivalent.getEasting())));
		this.newNorthing.setValue(Long.toString(Math.round(equivalent.getNorthing())));
	}

	/**
	 * Given a valid UTM, this re-calculates lat/lng
	 */
	private void recalculateLatLngFromUTM()
	{
		Double[] equivalent = SanimalAnalysisUtils.UTM2Deg(new UTMCoord(Double.parseDouble(this.newEasting.getValue()), Double.parseDouble(this.newNorthing.getValue()), Integer.parseInt(this.newZoneOrLat.getValue()), this.newLetterOrLng.getValue().charAt(0)));
		// Round to 4 decimal places
		this.newZoneOrLat.setValue(Double.toString(RoundingUtils.roundLat(equivalent[0])));
		this.newLetterOrLng.setValue(Double.toString(RoundingUtils.roundLng(equivalent[1])));
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

	private Boolean letterValid(String letter)
	{
		return letter.length() == 1 && Character.isLetter(letter.charAt(0)) && !ArrayUtils.contains(INVALID_UTM_LETTERS, letter.charAt(0));
	}

	private Boolean longutudeValid(String longitude)
	{
		return this.isValidDouble(longitude) && Double.parseDouble(longitude) <= 180 && Double.parseDouble(longitude) >= -180;
	}

	private Boolean letterOrLngValid(String letterOrLng)
	{
		if (this.rbnLatLng.isSelected())
			return longutudeValid(letterOrLng);
		else if (this.rbnUTM.isSelected())
			return letterValid(letterOrLng);
		else
			return false;
	}

	private Boolean zoneValid(String zone)
	{
		return this.isValidInteger(zone) && Integer.parseInt(zone) >= 1 && Integer.parseInt(zone) <= 60;
	}

	private Boolean latitudeValid(String latitude)
	{
		return this.isValidDouble(latitude) && Double.parseDouble(latitude) <= 85 && Double.parseDouble(latitude) >= -85;
	}

	private Boolean zoneOrLatValid(String zoneOrLat)
	{
		if (this.rbnLatLng.isSelected())
			return latitudeValid(zoneOrLat);
		else if (this.rbnUTM.isSelected())
			return zoneValid(zoneOrLat);
		else
			return false;
	}

	private Boolean eastingValid(String easting)
	{
		if (this.rbnLatLng.isSelected())
			return true;
		else
			return this.isValidDouble(easting) && Double.parseDouble(easting) <= 1000000 && Double.parseDouble(easting) >= 0;
	}

	private Boolean northingValid(String northing)
	{
		if (this.rbnLatLng.isSelected())
			return true;
		else
			return this.isValidDouble(northing) && Double.parseDouble(northing) <= 10000000 && Double.parseDouble(northing) >= 0;
	}

	///
	/// Validators for text fields end
	///
}
