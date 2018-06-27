package model.query;

public interface IQueryCondition
{
	void appendConditionToQuery(ElasticSearchQuery query);

	String getFXMLConditionEditor();
}
