package controller.analysisView.conditions;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import jfxtras.scene.control.LocalDateTimePicker;
import model.query.IQueryCondition;
import model.query.conditions.EndDateCondition;
import model.query.conditions.StartDateCondition;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ResourceBundle;

public class EndDateConditionController implements IConditionController
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

	@Override
	public void initializeData(IQueryCondition endDateCondition)
	{
		if (endDateCondition instanceof EndDateCondition)
			this.dtpDateTime.localDateTimeProperty().bindBidirectional(((EndDateCondition) endDateCondition).endDateProperty());
	}
}
