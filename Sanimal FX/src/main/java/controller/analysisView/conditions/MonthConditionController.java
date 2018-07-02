package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;
import model.query.IQueryCondition;
import model.query.conditions.MonthCondition;

import java.net.URL;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
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

	// The data modle associated with this controller
	private MonthCondition monthCondition;

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
			this.monthCondition = (MonthCondition) iQueryCondition;
			// Set the items of the hour list view to the months specified by the condition
			this.monthFilterListView.setItems(this.monthCondition.getMonthList());
			this.monthFilterListView.setCellFactory(CheckBoxListCell.forListView(this.monthCondition::monthSelectedProperty, new StringConverter<Month>()
			{
				@Override
				public String toString(Month month) { return month.getDisplayName(TextStyle.FULL, Locale.getDefault()); }
				@Override
				public Month fromString(String string) { return Month.valueOf(string.toUpperCase()); }
			}));
			this.monthFilterListView.setEditable(true);
		}
	}

	/**
	 * Selects all collections for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectAllMonths(ActionEvent actionEvent)
	{
		if (monthCondition != null)
			monthCondition.selectAll();
		actionEvent.consume();
	}

	/**
	 * Selects no collections for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectNoMonths(ActionEvent actionEvent)
	{
		if (monthCondition != null)
			monthCondition.selectNone();
		actionEvent.consume();
	}
}
