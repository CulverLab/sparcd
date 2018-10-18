package controller.importView;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.species.Species;
import org.apache.commons.validator.routines.UrlValidator;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the species creator page
 */
public class SpeciesCreatorController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The species name
	@FXML
	public TextField txtName;

	// The scientific name
	@FXML
	public TextField txtScientificName;

	// The Image URL
	@FXML
	public TextField txtImageURL;

	// The background pane
	@FXML
	public GridPane gridBackground;

	// OK button
	@FXML
	public Button btnConfirm;

	///
	/// FXML bound fields end
	///

	// Create a validator for the fields
	private ValidationSupport nameAndImageValidator = new ValidationSupport();
	// Create a validator to validate URLS
	private UrlValidator URLvalidator = new UrlValidator();

	// The current species being edited
	private Species speciesToEdit;
	// Properties to bind to the text fields
	private StringProperty newName = new SimpleStringProperty("");
	private StringProperty newScientificName = new SimpleStringProperty("");
	private StringProperty newIconURL = new SimpleStringProperty("");

	/**
	 * Initialize sets up the species creator window and bindings
	 *
	 * @param location  ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Initialize the bindings between the fields and the text
		this.txtName.textProperty().bindBidirectional(newName);
		this.txtScientificName.textProperty().bindBidirectional(newScientificName);
		this.txtImageURL.textProperty().bindBidirectional(newIconURL);

		this.btnConfirm.disableProperty().bind(this.nameAndImageValidator.invalidProperty());

		// Register validators
		nameAndImageValidator.setErrorDecorationEnabled(true);
		nameAndImageValidator.setValidationDecorator(new GraphicValidationDecoration());
		// Name must not be empty
		nameAndImageValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Species Name is Required!"));
		// Image URL must be valid
		nameAndImageValidator.registerValidator(this.txtImageURL, false, Validator.<String>createPredicateValidator(url ->
				url.trim().isEmpty() ||
				URLvalidator.isValid(url.trim()) && !new Image(url.trim()).isError(), "URL Given is invalid, it must be an image!"));
	}

	/**
	 * Sets the species to edit of this creator
	 *
	 * @param species The species to begin editing
	 */
	public void setSpecies(Species species)
	{
		// Setup all the fields for this given species
		this.speciesToEdit = species;
		if (this.speciesToEdit.nameValid())
			this.newName.set(species.getName());
		if (this.speciesToEdit.scientificNameValid())
			this.newScientificName.set(species.getScientificName());
		if (this.speciesToEdit.iconValid())
			this.newIconURL.set(this.speciesToEdit.getSpeciesIcon());
	}

	/**
	 * Getter for the species to be used after the edit is done
	 *
	 * @return The edited species
	 */
	public Species getSpecies()
	{
		return this.speciesToEdit;
	}

	/**
	 * Confirm writes the changes in the form to the species to edit
	 *
	 * @param actionEvent Consumed when the button is pressed
	 */
	public void confirmPressed(ActionEvent actionEvent)
	{
		// Set the name
		speciesToEdit.setName(newName.getValue());
		// Set the scientific name to unknown if no name was given
		speciesToEdit.setScientificName(newScientificName.getValue().trim().isEmpty() ? "Unknown" : newScientificName.getValue());
		// Set the icon either to the local file name or URL
		speciesToEdit.setSpeciesIcon(!this.newIconURL.getValue().isEmpty() ? this.newIconURL.getValue().trim() : Species.DEFAULT_ICON);
		((Stage) this.gridBackground.getScene().getWindow()).close();

		actionEvent.consume();
	}

	/**
	 * Closes the form without committing changes
	 *
	 * @param actionEvent ignored
	 */
	public void cancelPressed(ActionEvent actionEvent)
	{
		((Stage) this.gridBackground.getScene().getWindow()).close();
	}
}
