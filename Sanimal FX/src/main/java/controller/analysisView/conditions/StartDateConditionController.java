package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.fxml.FXML;
import jfxtras.scene.control.LocalDateTimePicker;
import model.query.IQueryCondition;
import model.query.conditions.StartDateCondition;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Start date filter" UI component
 */
public class StartDateConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The date picker that selects the start date cap
	@FXML
	public LocalDateTimePicker dtpDateTime;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Does nothing for the start date condition controller
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
	 * @param startDateCondition The data model for this start date condition
	 */
	public void initializeData(IQueryCondition startDateCondition)
	{
		if (startDateCondition instanceof StartDateCondition)
		{
			this.dtpDateTime.localDateTimeProperty().bindBidirectional(((StartDateCondition) startDateCondition).startDateProperty());
		}
	}
}
