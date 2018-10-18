package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import model.query.IQueryCondition;
import model.query.conditions.ElevationCondition;
import model.util.SettingsData;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Elevation filter" UI component
 */
public class ElevationConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ComboBox<ElevationCondition.ElevationComparisonOperators> cbxOperators;
	@FXML
	public TextField txtElevation;
	@FXML
	public ComboBox<SettingsData.DistanceUnits> cbxUnit;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Initialize sets up validators to ensure that start and end year are valid numbers
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ValidationSupport fieldValidator = new ValidationSupport();
		// The elevation must be a double!
		fieldValidator.registerValidator(this.txtElevation, true, Validator.createPredicateValidator(this::validDouble, "Elevation must be a decimal value!"));
	}

	/**
	 * Initializes the controller with a data model to bind to
	 *
	 * @param iQueryCondition The data model which should be an elevation filter condition
	 */
	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof ElevationCondition)
		{
			ElevationCondition elevationCondition = (ElevationCondition) iQueryCondition;

			// Initialize our fields
			this.txtElevation.setText(elevationCondition.elevationProperty().getValue().toString());
			this.cbxOperators.setItems(elevationCondition.getOperatorList());
			this.cbxOperators.getSelectionModel().select(elevationCondition.comparisonOperatorProperty().getValue());
			this.cbxUnit.setItems(elevationCondition.getUnitList());
			this.cbxUnit.getSelectionModel().select(elevationCondition.unitsProperty().getValue());

			// Bind the new values to our model
			elevationCondition.elevationProperty().bind(EasyBind.map(this.txtElevation.textProperty(), elevation -> parseOrDefault(elevation, 0.0)));
			elevationCondition.comparisonOperatorProperty().bind(this.cbxOperators.getSelectionModel().selectedItemProperty());
			elevationCondition.unitsProperty().bind(this.cbxUnit.getSelectionModel().selectedItemProperty());
		}
	}

	/**
	 * Parses the string number into a double, or returns the default number if the parse fails
	 *
	 * @param number The number to parse as a string
	 * @param defaultNumber The default return value
	 * @return The string as a number or the default number if the parse fails
	 */
	private Double parseOrDefault(String number, Double defaultNumber)
	{
		if (this.validDouble(number))
			return Double.parseDouble(number);
		else
			return defaultNumber;
	}

	/**
	 * Tests if a string is a valid double
	 *
	 * @param number The number to test
	 * @return True if the number is a valid double, false otherwise
	 */
	private Boolean validDouble(String number)
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
