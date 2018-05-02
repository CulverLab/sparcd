package model.query.conditions;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;

public class YearCondition implements IQueryCondition
{
	private IntegerProperty startYear = new SimpleIntegerProperty(LocalDateTime.now().getYear());
	private IntegerProperty endYear = new SimpleIntegerProperty(LocalDateTime.now().getYear());

	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Integer year = startYear.getValue(); year <= endYear.getValue(); year++)
		{
			query.addYear(year);
		}
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "YearCondition.fxml";
	}

	public IntegerProperty startYearProperty()
	{
		return startYear;
	}

	public IntegerProperty endYearProperty()
	{
		return endYear;
	}
}
