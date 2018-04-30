package model.query.conditions;

import javafx.collections.ObservableList;
import model.SanimalData;
import model.location.Location;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

/**
 * Data model used by the "Location filter" query condition
 */
public class LocationFilterCondition implements IQueryCondition
{
	/**
	 * This query condition ensures only selected locations are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Location location : this.locationListProperty())
			if (location.shouldBePartOfAnalysis())
				query.addLocation(location);
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "LocationFilterCondition.fxml";
	}

	/**
	 * Returns a list of locations to filter
	 *
	 * @return A list of locations
	 */
	public ObservableList<Location> locationListProperty()
	{
		return SanimalData.getInstance().getLocationList();
	}
}
