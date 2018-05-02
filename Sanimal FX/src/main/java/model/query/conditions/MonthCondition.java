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
	private ObservableList<Month> monthList = FXCollections.observableArrayList(Month.values());

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
		for (Month month : Month.values())
			if (monthToSelected.containsKey(month) && monthToSelected.get(month).getValue())
				query.addMonth(month.getValue());
	}

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

	public ObservableList<Month> getMonthList()
	{
		return monthList;
	}
}
