package model.query.conditions;

import model.SanimalData;
import model.location.Location;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

public class LocationFilterCondition implements IQueryCondition
{
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Location location : SanimalData.getInstance().getLocationList())
			if (location.shouldBePartOfAnalysis())
				query = query.addLocation(location);
	}
}
