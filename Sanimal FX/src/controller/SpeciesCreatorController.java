package controller;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.commons.validator.util.ValidatorUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SpeciesCreatorController implements Initializable
{
    @FXML
    public TextField txtName;

    @FXML
    public TextField txtScientificName;

    @FXML
    public TextField txtImageURL;

    @FXML
    public Label lblFileName;

    @FXML
    public GridPane gridBackground;

    @FXML
    public Label lblErrorBox;

    private Species speciesToEdit;
    private SimpleStringProperty newName = new SimpleStringProperty("");
    private SimpleStringProperty newScientificName = new SimpleStringProperty("");
    private SimpleStringProperty newIconURL = new SimpleStringProperty("");
    private String newIconLocal = "";

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.txtName.textProperty().bindBidirectional(newName);
        this.txtScientificName.textProperty().bindBidirectional(newScientificName);
        this.txtImageURL.textProperty().bindBidirectional(newIconURL);
        this.txtImageURL.focusedProperty().addListener((observableVal, oldFocused, newFocused) -> {
            if (oldFocused == true && newFocused == false)
            {
                if (!this.newIconURL.getValue().isEmpty())
                {
                    this.newIconLocal = "";
                    this.lblFileName.setText("");
                }
            }
        });
    }

    public void grabFileName(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Find Image to use as Species Icon");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File file = fileChooser.showOpenDialog(this.gridBackground.getScene().getWindow());
        if (file != null)
        {
            this.lblFileName.setText(file.getName());
            newIconLocal = file.toURI().toString();
            this.newIconURL.setValue("");
        }
    }

    public void setSpecies(Species species)
    {
        this.speciesToEdit = species;
        if (this.speciesToEdit.nameValid())
            this.newName.set(species.getName());
        if (this.speciesToEdit.scientificNameValid())
            this.newScientificName.set(species.getScientificName());
        if (this.speciesToEdit.iconValid())
        {
            String icon = species.getSpeciesIcon();
            UrlValidator urlChecker = new UrlValidator();
            if (urlChecker.isValid(icon))
                this.newIconURL.set(icon);
            else
            {
                this.newIconLocal = icon;
                this.lblFileName.setText(new File(icon).getName());
            }
        }
    }

    public Species getSpecies()
    {
        return this.speciesToEdit;
    }

    public void confirmPressed(ActionEvent actionEvent)
    {
        if (this.fieldsValid())
        {
            speciesToEdit.setName(newName.getValue());
            speciesToEdit.setScientificName(newScientificName.getValue());
            speciesToEdit.setSpeciesIcon(!this.newIconURL.getValue().isEmpty() ? this.newIconURL.getValue().trim() : !this.newIconLocal.isEmpty() ? this.newIconLocal : new File("./src/images/importWindow/defaultAnimalIcon.png").toURI().toString());
            ((Stage) this.gridBackground.getScene().getWindow()).close();
        }
    }

    public void cancelPressed(ActionEvent actionEvent)
    {
        ((Stage) this.gridBackground.getScene().getWindow()).close();
    }

    private Boolean fieldsValid()
    {
        if (this.newName.getValue().equals(Species.UNINITIALIZED) || this.newName.getValue().trim().isEmpty())
        {
            lblErrorBox.setText("*Species Name is Invalid!");
            return false;
        }

        if (this.newScientificName.getValue().equals(Species.UNINITIALIZED) || this.newScientificName.getValue().trim().isEmpty())
        {
            lblErrorBox.setText("*Species Scientific Name is Invalid!");
            return false;
        }

        if (!this.newIconURL.getValue().isEmpty())
        {
            UrlValidator validator = new UrlValidator();
            if (!validator.isValid(this.newIconURL.getValue()))
            {
                lblErrorBox.setText("*Species Icon URL is invalid!");
                return false;
            }

            if (new Image(this.newIconURL.getValue()).isError())
            {
                lblErrorBox.setText("*Species Icon URL is not an image!");
                return false;
            }
        }

        return true;
    }
}
