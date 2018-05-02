package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.SanimalData;
import model.location.Location;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model used by the "Location filter" query condition
 */
public class LocationFilterCondition implements IQueryCondition
{
	// A map of location -> if the location is selected to be filtered
	private Map<Location, BooleanProperty> locationToSelected = new HashMap<>();

	public LocationFilterCondition()
	{
		for (Location location : this.getLocationList())
			if (!this.locationToSelected.containsKey(location))
				this.locationToSelected.put(location, new SimpleBooleanProperty(true));
		this.getLocationList().addListener((ListChangeListener<Location>) c ->
		{
			while (c.next())
				if (c.wasAdded())
					for (Location location : c.getAddedSubList())
						if (!this.locationToSelected.containsKey(location))
							this.locationToSelected.put(location, new SimpleBooleanProperty(true));
		});
	}

	/**
	 * This query condition ensures only selected locations are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Location location : this.getLocationList())
			if (locationToSelected.containsKey(location) && locationToSelected.get(location).getValue())
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
		return "LocationCondition.fxml";
	}

	/**
	 * Returns a list of locations to filter
	 *
	 * @return A list of locations
	 */
	public ObservableList<Location> getLocationList()
	{
		return SanimalData.getInstance().getLocationList();
	}

	/**
	 * Gets the property defining if a location is selected
	 *
	 * @param location The location to test if it's selected
	 * @return The property representing if the location is selected
	 */
	public BooleanProperty locationSelectedProperty(Location location)
	{
		if (!this.locationToSelected.containsKey(location))
			this.locationToSelected.put(location, new SimpleBooleanProperty(true));
		return this.locationToSelected.get(location);
	}

	/**
	 * Selects all locations
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.locationToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all locations
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.locationToSelected.values())
			selected.set(false);
	}
}