package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import model.query.IQueryCondition;
import model.query.conditions.DayOfWeekCondition;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.ResourceBundle;

public class DayOfWeekConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<DayOfWeek> dayOfWeekFilterListView;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof DayOfWeekCondition)
		{
			DayOfWeekCondition dayOfWeekCondition = (DayOfWeekCondition) iQueryCondition;
			this.dayOfWeekFilterListView.setItems(dayOfWeekCondition.getDayOfWeekList());
			this.dayOfWeekFilterListView.setCellFactory(CheckBoxListCell.forListView(dayOfWeekCondition::dayOfWeekSelectedProperty));
			this.dayOfWeekFilterListView.setEditable(true);
		}
	}
}
