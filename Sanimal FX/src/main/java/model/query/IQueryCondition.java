package model.query;

public interface IQueryCondition
{
	void appendConditionToQuery(S3Query query);

	String getFXMLConditionEditor();
}
