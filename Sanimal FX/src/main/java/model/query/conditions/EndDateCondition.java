package model.query.conditions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;

public class EndDateCondition implements IQueryCondition
{
	private ObjectProperty<LocalDateTime> endDate = new SimpleObjectProperty<>(LocalDateTime.now());

	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		query.setEndDate(endDate.getValue());
	}

	public ObjectProperty<LocalDateTime> endDateProperty()
	{
		return this.endDate;
	}
}
