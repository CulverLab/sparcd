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

public class YearConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public TextField txtStartYear;
	@FXML
	public TextField txtEndYear;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ValidationSupport fieldValidator = new ValidationSupport();
		fieldValidator.registerValidator(this.txtStartYear, true, Validator.createPredicateValidator(this::validInteger, "Start year must be an integer!"));
		fieldValidator.registerValidator(this.txtEndYear,   true, Validator.createPredicateValidator(this::validInteger, "End year must be an integer!"));
	}

	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof YearCondition)
		{
			YearCondition yearCondition = (YearCondition) iQueryCondition;
			this.txtStartYear.setText(yearCondition.startYearProperty().getValue().toString());
			this.txtEndYear.setText(yearCondition.endYearProperty().getValue().toString());
			yearCondition.startYearProperty().bind(EasyBind.map(this.txtStartYear.textProperty(), year -> parseOrDefault(year, LocalDateTime.MIN.getYear())));
			yearCondition.endYearProperty().bind(EasyBind.map(this.txtEndYear.textProperty(), year -> parseOrDefault(year, LocalDateTime.MAX.getYear())));
		}
	}

	private Integer parseOrDefault(String number, Integer defaultNumber)
	{
		try
		{
			return Integer.parseInt(number);
		}
		catch (NumberFormatException ignored)
		{
			return defaultNumber;
		}
	}

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
