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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LocationCreatorController implements Initializable {

    @FXML
    public TextField txtName;

    @FXML
    public TextField txtLatitude;

    @FXML
    public TextField txtLongitude;

    @FXML
    public TextField txtElevation;

    @FXML
    public Label lblErrorBox;

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

        this.txtLatitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change -> {
            String newText = change.getControlNewText();
            if (validDoubleText.matcher(newText).matches())
                return change;
            else
                return null;
        }));
        this.txtLongitude.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change -> {
            String newText = change.getControlNewText();
            if (validDoubleText.matcher(newText).matches())
                return change;
            else
                return null;
        }));
        this.txtElevation.setTextFormatter(new TextFormatter<Double>(new DoubleStringConverter(), 0.0, change -> {
            String newText = change.getControlNewText();
            if (validDoubleText.matcher(newText).matches())
                return change;
            else
                return null;
        }));
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

    public void confirmPressed(ActionEvent actionEvent) {
        if (this.fieldsValid())
        {
            locationToEdit.setName(newName.getValue());
            locationToEdit.setLat(Double.parseDouble(newLatitude.getValue()));
            locationToEdit.setLng(Double.parseDouble(newLongitude.getValue()));
            locationToEdit.setElevation(Double.parseDouble(newElevation.getValue()));
            ((Stage) this.txtName.getScene().getWindow()).close();
        }
    }

    public void cancelPressed(ActionEvent actionEvent) {
        ((Stage) this.txtName.getScene().getWindow()).close();
    }

    private Boolean fieldsValid() {
        if (this.newName.getValue().trim().isEmpty())
        {
            this.lblErrorBox.setText("*Location Name is Invalid!");
            return false;
        }

        if (!isValidNumber(this.newLatitude.getValue()))
        {
            this.lblErrorBox.setText("*Location Latitude is Invalid! It must be a number.");
            return false;
        }

        if (!isValidNumber(this.newLongitude.getValue()))
        {
            this.lblErrorBox.setText("*Location Longitude is Invalid! It must be a number.");
            return false;
        }

        if (!isValidNumber(this.newElevation.getValue()))
        {
            this.lblErrorBox.setText("*Location Elevation is Invalid! It must be a number.");
            return false;
        }

        return true;
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
