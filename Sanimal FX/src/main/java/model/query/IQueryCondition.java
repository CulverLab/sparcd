package model.query;

public interface IQueryCondition
{
	void appendConditionToQuery(CyVerseQuery query);

	String getFXMLConditionEditor();
}
