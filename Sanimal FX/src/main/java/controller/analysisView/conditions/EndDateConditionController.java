package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.fxml.FXML;
import jfxtras.scene.control.LocalDateTimePicker;
import model.query.IQueryCondition;
import model.query.conditions.EndDateCondition;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "End date filter" UI component
 */
public class EndDateConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The date picker that selects the end date cap
	@FXML
	public LocalDateTimePicker dtpDateTime;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Does nothing for the end date condition controller
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	/**
	 * Initializes this controller with data
	 *
	 * @param endDateCondition The data model for this end date condition
	 */
	@Override
	public void initializeData(IQueryCondition endDateCondition)
	{
		if (endDateCondition instanceof EndDateCondition)
			// Bind the date to the end condition's end date property
			this.dtpDateTime.localDateTimeProperty().bindBidirectional(((EndDateCondition) endDateCondition).endDateProperty());
	}
}
