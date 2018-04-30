package controller.analysisView.conditions;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import jfxtras.scene.control.LocalDateTimePicker;
import jfxtras.scene.control.LocalDateTimeTextField;
import model.query.IQueryCondition;
import model.query.conditions.StartDateCondition;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ResourceBundle;

public class StartDateConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public LocalDateTimePicker dtpDateTime;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	public void initializeData(IQueryCondition startDateCondition)
	{
		if (startDateCondition instanceof StartDateCondition)
		{
			this.dtpDateTime.localDateTimeProperty().bindBidirectional(((StartDateCondition) startDateCondition).startDateProperty());
		}
	}
}
