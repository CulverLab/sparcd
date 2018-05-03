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

/**
 * Class used as a controller for the "Month filter" UI component
 */
public class MonthConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// List view of all possible months
	@FXML
	public ListView<Month> monthFilterListView;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Initialize does nothing for this specific filter
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	/**
	 * Initializes the controller with a data model to bind to
	 *
	 * @param iQueryCondition The data model which should be a month filter condition
	 */
	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof MonthCondition)
		{
			MonthCondition monthCondition = (MonthCondition) iQueryCondition;
			// Set the items of the hour list view to the months specified by the condition
			this.monthFilterListView.setItems(monthCondition.getMonthList());
			this.monthFilterListView.setCellFactory(CheckBoxListCell.forListView(monthCondition::monthSelectedProperty));
			this.monthFilterListView.setEditable(true);
		}
	}
}
