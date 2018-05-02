package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayOfWeekCondition implements IQueryCondition
{
	// A map of day of week -> if the day of week is selected to be filtered
	private Map<DayOfWeek, BooleanProperty> dayOfWeekToSelected = new HashMap<>();
	private ObservableList<DayOfWeek> dayOfWeekList = FXCollections.observableArrayList(DayOfWeek.values());

	public DayOfWeekCondition()
	{
		for (DayOfWeek dayOfWeek : dayOfWeekList)
			if (!this.dayOfWeekToSelected.containsKey(dayOfWeek))
				this.dayOfWeekToSelected.put(dayOfWeek, new SimpleBooleanProperty(true));
	}

	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (DayOfWeek dayOfWeek : DayOfWeek.values())
			if (this.dayOfWeekToSelected.containsKey(dayOfWeek) && this.dayOfWeekToSelected.get(dayOfWeek).getValue())
				query.addDayOfWeek(dayOfWeek.getValue());
	}

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

	public ObservableList<DayOfWeek> getDayOfWeekList()
	{
		return dayOfWeekList;
	}
}
