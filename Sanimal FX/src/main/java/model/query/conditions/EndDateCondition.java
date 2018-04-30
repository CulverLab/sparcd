package model.query.conditions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.time.LocalDateTime;

/**
 * Data model used by the "End Date filter" query condition
 */
public class EndDateCondition implements IQueryCondition
{
	// Stores the local date time of the current end date
	private ObjectProperty<LocalDateTime> endDate = new SimpleObjectProperty<>(LocalDateTime.now());

	/**
	 * This query condition ensures only images with a date < the end date are selected
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		query.setEndDate(endDate.getValue());
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "EndDateCondition.fxml";
	}

	/**
	 * Returns the end date property used for UI binding
	 *
	 * @return The end date property
	 */
	public ObjectProperty<LocalDateTime> endDateProperty()
	{
		return this.endDate;
	}
}
