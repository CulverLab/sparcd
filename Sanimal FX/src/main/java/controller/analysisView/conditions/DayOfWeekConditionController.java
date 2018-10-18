package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;
import model.query.IQueryCondition;
import model.query.conditions.DayOfWeekCondition;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Day of Week filter" UI component
 */
public class DayOfWeekConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The list of possible days of week to check
	@FXML
	public ListView<DayOfWeek> dayOfWeekFilterListView;

	///
	/// FXML Bound Fields End
	///

	// Data model containing day of week information
	private DayOfWeekCondition dayOfWeekCondition;

	/**
	 * Initializes the controller, does nothing in this specific controller
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	/**
	 * Initializes the data model for this given condition
	 *
	 * @param iQueryCondition The data model which should be a day of week condition
	 */
	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		if (iQueryCondition instanceof DayOfWeekCondition)
		{
			this.dayOfWeekCondition = (DayOfWeekCondition) iQueryCondition;
			// Set the items to be the list specified in the controller
			this.dayOfWeekFilterListView.setItems(this.dayOfWeekCondition.getDayOfWeekList());
			// Use checkbox cells to hold the data
			this.dayOfWeekFilterListView.setCellFactory(CheckBoxListCell.forListView(this.dayOfWeekCondition::dayOfWeekSelectedProperty, new StringConverter<DayOfWeek>()
			{
				@Override
				public String toString(DayOfWeek dayOfWeek) { return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()); }
				@Override
				public DayOfWeek fromString(String string) { return DayOfWeek.valueOf(string.toUpperCase()); }
			}));
			this.dayOfWeekFilterListView.setEditable(true);
		}
	}

	/**
	 * Selects all days for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectAllDays(ActionEvent actionEvent)
	{
		if (this.dayOfWeekCondition != null)
			this.dayOfWeekCondition.selectAll();
		actionEvent.consume();
	}

	/**
	 * Selects no days for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectNoDays(ActionEvent actionEvent)
	{
		if (this.dayOfWeekCondition != null)
			this.dayOfWeekCondition.selectNone();
		actionEvent.consume();
	}
}
