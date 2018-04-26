package controller.analysisView.conditions;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import jfxtras.scene.control.LocalDateTimePicker;
import model.query.conditions.EndDateCondition;
import model.query.conditions.StartDateCondition;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ResourceBundle;

public class EndDateConditionController implements Initializable
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

	public void initializeData(EndDateCondition endDateCondition)
	{
		this.dtpDateTime.localDateTimeProperty().bindBidirectional(endDateCondition.endDateProperty());
	}
}
