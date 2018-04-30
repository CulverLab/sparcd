package model.query.conditions;

import model.query.CyVerseQuery;
import model.query.IQueryCondition;

public class AddQueryCondition implements IQueryCondition
{
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "AddQueryCondition.fxml";
	}
}
