package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Data model used by the "Hour filter" query condition
 */
public class HourCondition implements IQueryCondition
{
	// A map of hour -> if the hour is selected to be filtered
	private Map<Integer, BooleanProperty> hourToSelected = new HashMap<>();
	private ObservableList<Integer> hourList = FXCollections.observableArrayList(IntStream.range(0, 24).boxed().collect(Collectors.toList()));

	public HourCondition()
	{
		for (Integer hour : hourList)
			if (!this.hourToSelected.containsKey(hour))
				this.hourToSelected.put(hour, new SimpleBooleanProperty(true));
	}

	/**
	 * This query condition ensures only selected hours are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Integer hour : this.getHourList())
			if (hourToSelected.containsKey(hour) && hourToSelected.get(hour).getValue())
				query.addHour(hour);
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "HourCondition.fxml";
	}

	/**
	 * Gets the property defining if a hour is selected
	 *
	 * @param hour The hour to test if it's selected
	 * @return The property representing if the hour is selected
	 */
	public BooleanProperty hourSelectedProperty(Integer hour)
	{
		return this.hourToSelected.get(hour);
	}

	public ObservableList<Integer> getHourList()
	{
		return hourList;
	}

	/**
	 * Selects all hours
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.hourToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all hours
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.hourToSelected.values())
			selected.set(false);
	}
}
