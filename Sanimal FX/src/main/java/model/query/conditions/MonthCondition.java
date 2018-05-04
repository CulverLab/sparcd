package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model used by the "Month filter" query condition
 */
public class MonthCondition implements IQueryCondition
{
	// A map of month -> if the month is selected to be filtered
	private Map<Month, BooleanProperty> monthToSelected = new HashMap<>();
	// A list of possible months to filter
	private ObservableList<Month> monthList = FXCollections.observableArrayList(Month.values());

	/**
	 * Constructor ensures that each month maps to a boolean property
	 */
	public MonthCondition()
	{
		for (Month month : monthList)
			if (!this.monthToSelected.containsKey(month))
				this.monthToSelected.put(month, new SimpleBooleanProperty(true));
	}

	/**
	 * This query condition ensures only selected months are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		// Make sure each month maps to a boolean property, this is important for later, since our view will use this to populate checkboxes
		for (Month month : Month.values())
			if (monthToSelected.containsKey(month) && monthToSelected.get(month).getValue())
				query.addMonth(month.getValue());
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "MonthCondition.fxml";
	}

	/**
	 * Gets the property defining if a month is selected
	 *
	 * @param month The month to test if it's selected
	 * @return The property representing if the month is selected
	 */
	public BooleanProperty monthSelectedProperty(Month month)
	{
		return this.monthToSelected.get(month);
	}

	/**
	 * Returns a list of possible months to filter
	 *
	 * @return A list of months jan-dec
	 */
	public ObservableList<Month> getMonthList()
	{
		return monthList;
	}

	/**
	 * Selects all months
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.monthToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all months
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.monthToSelected.values())
			selected.set(false);
	}
}
