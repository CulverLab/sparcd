package model.query.conditions;

import model.query.CyVerseQuery;
import model.query.IQueryCondition;

/**
 * Data model used by the "Add" query condition
 */
public class AddQueryCondition implements IQueryCondition
{
	/**
	 * This is the only condition that does not affect the query, it simply is a + button
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "AddQueryCondition.fxml";
	}
}
