package model.query.conditions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;
import java.time.Month;

public class StartDateCondition implements IQueryCondition
{
	private ObjectProperty<LocalDateTime> startDate = new SimpleObjectProperty<>(LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0));

	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		query.setStartDate(startDate.getValue());
	}

	public ObjectProperty<LocalDateTime> startDateProperty()
	{
		return this.startDate;
	}
}
