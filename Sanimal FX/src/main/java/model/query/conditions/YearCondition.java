package model.query.conditions;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import model.query.ElasticSearchQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;

/**
 * Data model used by the "Year filter" query condition
 */
public class YearCondition implements IQueryCondition
{
	// The starting year to include
	private IntegerProperty startYear = new SimpleIntegerProperty(LocalDateTime.now().getYear());
	// The end year to include
	private IntegerProperty endYear = new SimpleIntegerProperty(LocalDateTime.now().getYear());

	/**
	 * This query condition ensures only selected years are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(ElasticSearchQuery query)
	{
		query.setStartAndEndYear(startYear.getValue(), endYear.getValue());
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "YearCondition.fxml";
	}

	/**
	 * Returns the start year integer property to be edited by the UI
	 *
	 * @return A property representing start year
	 */
	public IntegerProperty startYearProperty()
	{
		return startYear;
	}

	/**
	 * Returns the end year integer property to be edited by the UI
	 *
	 * @return A property representing end year
	 */
	public IntegerProperty endYearProperty()
	{
		return endYear;
	}
}
