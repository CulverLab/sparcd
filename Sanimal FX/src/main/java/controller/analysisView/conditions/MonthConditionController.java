package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import model.query.IQueryCondition;
import model.query.conditions.MonthCondition;

import java.net.URL;
import java.time.Month;
import java.util.ResourceBundle;

public class MonthConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<Month> monthFilterListView;

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
		if (iQueryCondition instanceof MonthCondition)
		{
			MonthCondition monthCondition = (MonthCondition) iQueryCondition;
			this.monthFilterListView.setItems(monthCondition.getMonthList());
			this.monthFilterListView.setCellFactory(CheckBoxListCell.forListView(monthCondition::monthSelectedProperty));
			this.monthFilterListView.setEditable(true);
		}
	}
}
