package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.ElasticSearchQuery;
import model.query.IQueryCondition;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model used by the "Day of Week filter" query condition
 */
public class DayOfWeekCondition implements IQueryCondition
{
	// A map of day of week -> if the day of week is selected to be filtered
	private Map<DayOfWeek, BooleanProperty> dayOfWeekToSelected = new HashMap<>();
	// A list of possible days of week (monday - sunday)
	private ObservableList<DayOfWeek> dayOfWeekList = FXCollections.observableArrayList(DayOfWeek.values());

	/**
	 * Constructor ensures that each day of the week maps to a boolean property
	 */
	public DayOfWeekCondition()
	{
		// Make sure each day of the week maps to a boolean property, this is important for later, since our view will use this to populate checkboxes
		for (DayOfWeek dayOfWeek : dayOfWeekList)
			if (!this.dayOfWeekToSelected.containsKey(dayOfWeek))
				this.dayOfWeekToSelected.put(dayOfWeek, new SimpleBooleanProperty(true));
	}

	/**
	 * This query condition ensures only selected days of the week are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(ElasticSearchQuery query)
	{
		for (DayOfWeek dayOfWeek : DayOfWeek.values())
			if (this.dayOfWeekToSelected.containsKey(dayOfWeek) && this.dayOfWeekToSelected.get(dayOfWeek).getValue())
				query.addDayOfWeek(dayOfWeek.getValue());
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "DayOfWeekCondition.fxml";
	}

	/**
	 * Gets the property defining if a dayOfWeek is selected
	 *
	 * @param dayOfWeek The dayOfWeek to test if it's selected
	 * @return The property representing if the dayOfWeek is selected
	 */
	public BooleanProperty dayOfWeekSelectedProperty(DayOfWeek dayOfWeek)
	{
		return this.dayOfWeekToSelected.get(dayOfWeek);
	}

	/**
	 * Returns the list of possible days of the week
	 *
	 * @return A list of days of the week to filter
	 */
	public ObservableList<DayOfWeek> getDayOfWeekList()
	{
		return dayOfWeekList;
	}

	/**
	 * Selects all days in the week
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.dayOfWeekToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all days in the week
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.dayOfWeekToSelected.values())
			selected.set(false);
	}
}
