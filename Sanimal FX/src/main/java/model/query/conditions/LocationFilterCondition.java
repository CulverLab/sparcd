package model.query.conditions;

import javafx.collections.ObservableList;
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
				query.addLocation(location);
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "LocationFilterCondition.fxml";
	}

	public ObservableList<Location> locationListProperty()
	{
		return SanimalData.getInstance().getLocationList();
	}
}
