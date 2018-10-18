package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.query.IQueryCondition;
import model.query.conditions.YearCondition;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Class used as a controller for the "Year filter" UI component
 */
public class YearConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The text field with the starting year
	@FXML
	public TextField txtStartYear;
	// The text field with the end year
	@FXML
	public TextField txtEndYear;

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
		fieldValidator.registerValidator(this.txtStartYear, true, Validator.createPredicateValidator(this::validInteger, "Start year must be an integer!"));
		fieldValidator.registerValidator(this.txtEndYear,   true, Validator.createPredicateValidator(this::validInteger, "End year must be an integer!"));
	}

	/**
	 * Initializes the controller with a data model to bind to
	 *
	 * @param iQueryCondition The data model which should be a year filter condition
	 */
	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof YearCondition)
		{
			YearCondition yearCondition = (YearCondition) iQueryCondition;
			this.txtStartYear.setText(yearCondition.startYearProperty().getValue().toString());
			this.txtEndYear.setText(yearCondition.endYearProperty().getValue().toString());
			// Bind the year start and end properties to the text parsed to an integer
			yearCondition.startYearProperty().bind(EasyBind.map(this.txtStartYear.textProperty(), year -> parseOrDefault(year, LocalDateTime.MIN.getYear())));
			yearCondition.endYearProperty().bind(EasyBind.map(this.txtEndYear.textProperty(), year -> parseOrDefault(year, LocalDateTime.MAX.getYear())));
		}
	}

	/**
	 * Parses the string number into an integer, or returns the default number if the parse fails
	 *
	 * @param number The number to parse as a string
	 * @param defaultNumber The default return value
	 * @return The string as a number or the default number if the parse fails
	 */
	private Integer parseOrDefault(String number, Integer defaultNumber)
	{
		if (this.validInteger(number))
			return Integer.parseInt(number);
		else
			return defaultNumber;
	}

	/**
	 * Tests if a string is a valid integer
	 *
	 * @param number The number to test
	 * @return True if the number is a valid integer, false otherwise
	 */
	private Boolean validInteger(String number)
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
}
