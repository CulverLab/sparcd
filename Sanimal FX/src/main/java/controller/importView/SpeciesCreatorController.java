package controller.importView;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import org.apache.commons.validator.routines.UrlValidator;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

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

	// The local file name
	@FXML
	public Label lblFileName;

	// The background pane
	@FXML
	public GridPane gridBackground;

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
	private String newIconLocal = "";

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
		// When the user focuses unfocuses the image URL field
		this.txtImageURL.focusedProperty().addListener((observableVal, oldFocused, newFocused) ->
		{
			if (oldFocused == true && newFocused == false)
				// If the icon URL was given, clear out the local icon URL
				if (!this.newIconURL.getValue().isEmpty())
				{
					this.newIconLocal = "";
					this.lblFileName.setText("");
				}
		});

		// Register validators
		nameAndImageValidator.setErrorDecorationEnabled(true);
		// Name must not be empty
		nameAndImageValidator.registerValidator(this.txtName, true, Validator.createEmptyValidator("Species Name is Required!"));
		// Image URL must be valid
		nameAndImageValidator.registerValidator(this.txtImageURL, true, Validator.<String>createPredicateValidator(url ->
		{
			if (url.trim().isEmpty())
				return true;
			else
				return URLvalidator.isValid(url.trim()) && !new Image(url.trim()).isError();
		}, "URL Given is invalid!"));
	}

	/**
	 * When the user clicks browse open a file chooser
	 *
	 * @param actionEvent consumed after a file is chosen
	 */
	public void grabFileName(ActionEvent actionEvent)
	{
		// Create a file chooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Find Image to use as Species Icon");
		// Only allow PNGs and JPGs
		FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
		FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
		fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
		// Open the dialog box
		File file = fileChooser.showOpenDialog(this.gridBackground.getScene().getWindow());
		// if we got a file, update the file name fields
		if (file != null)
		{
			this.lblFileName.setText(file.getName());
			this.newIconLocal = file.toURI().toString();
			this.newIconURL.setValue("");
		}
		actionEvent.consume();
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
		{
			// if we got a valid icon, figure out if it was a local file or a url
			String icon = species.getSpeciesIcon();
			if (URLvalidator.isValid(icon))
				// It's a URL
				this.newIconURL.set(icon);
			else
			{
				// It's a file
				this.newIconLocal = icon;
				this.lblFileName.setText(new File(icon).getName());
			}
		}
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
		// If the fields are valid, begin writing the data
		if (!this.nameAndImageValidator.isInvalid())
		{
			// Set the name
			speciesToEdit.setName(newName.getValue());
			// Set the scientific name to unknown if no name was given
			speciesToEdit.setScientificName(newScientificName.getValue().trim().isEmpty() ? "Unknown" : newScientificName.getValue());
			// Set the icon either to the local file name or URL
			speciesToEdit.setSpeciesIcon(!this.newIconURL.getValue().isEmpty() ? this.newIconURL.getValue().trim() : !this.newIconLocal.isEmpty() ? this.newIconLocal : Species.DEFAULT_ICON);
			((Stage) this.gridBackground.getScene().getWindow()).close();
		}

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
