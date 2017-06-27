package controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import model.location.Location;
import org.apache.commons.validator.routines.UrlValidator;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LocationCreatorController implements Initializable
{

	@FXML
	public TextField txtName;

	@FXML
	public TextField txtLatitude;

	@FXML
	public TextField txtLongitude;

	@FXML
	public TextField txtElevation;

	private ValidationSupport fieldValidator = new ValidationSupport();

	private Location locationToEdit;
	private StringProperty newName = new SimpleStringProperty("");
	private StringProperty newLatitude = new SimpleStringProperty("");
	private StringProperty newLongitude = new SimpleStringProperty("");
	private StringProperty newElevation = new SimpleStringProperty("");

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.txtName.textProperty().bindBidirectional(newName);
		this.txtLatitude.textProperty().bindBidirectional(newLatitude);
		this.txtLongitude.textProperty().bindBidirectional(newLongitude);
		this.txtElevation.textProperty().bindBidirectional(newElevation);

		Pattern validDoubleText = Pattern.compile("-?((\\d*)|(\\d+\\.\\d*))");

		this.txtLatitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (validDoubleText.matcher(newText).matches())
				return change;
			else
				return null;
		}));
		this.txtLongitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (validDoubleText.matcher(newText).matches())
				return change;
			else
				return null;
		}));
		this.txtElevation.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change ->
		{
			String newText = change.getControlNewText();
			if (validDoubleText.matcher(newText).matches())
				return change;
			else
				return null;
		}));

		this.fieldValidator.setErrorDecorationEnabled(true);
		this.fieldValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Location Name must not be empty!"));
		this.fieldValidator.registerValidator(this.txtLatitude, true, Validator.<String>createPredicateValidator(value ->
		{
			return this.isValidNumber(value) && Double.parseDouble(value) <= 85 && Double.parseDouble(value) >= -85;
		}, "Latitude must be +/-85!"));
		this.fieldValidator.registerValidator(this.txtLongitude, true, Validator.<String>createPredicateValidator(value ->
		{
			return this.isValidNumber(value) && Double.parseDouble(value) <= 180 && Double.parseDouble(value) >= -180;
		}, "Longitude must be +/-180!"));
		this.fieldValidator.registerValidator(this.txtElevation, true, Validator.createPredicateValidator(this::isValidNumber, "Elevation must be a number!"));
	}

	public void setLocation(Location location)
	{
		this.locationToEdit = location;
		if (this.locationToEdit.nameValid())
			this.newName.set(location.getName());
		if (this.locationToEdit.latValid())
			this.newLatitude.set(locationToEdit.getLat().toString());
		if (this.locationToEdit.lngValid())
			this.newLongitude.set(locationToEdit.getLng().toString());
		if (this.locationToEdit.elevationValid())
			this.newElevation.set(locationToEdit.getElevation().toString());
	}

	public void confirmPressed(ActionEvent actionEvent)
	{
		if (!this.fieldValidator.isInvalid())
		{
			locationToEdit.setName(newName.getValue());
			locationToEdit.setLat(Double.parseDouble(newLatitude.getValue()));
			locationToEdit.setLng(Double.parseDouble(newLongitude.getValue()));
			locationToEdit.setElevation(Double.parseDouble(newElevation.getValue()));
			((Stage) this.txtName.getScene().getWindow()).close();
		}
	}

	public void cancelPressed(ActionEvent actionEvent)
	{
		((Stage) this.txtName.getScene().getWindow()).close();
	}

	private Boolean isValidNumber(String number)
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
}
