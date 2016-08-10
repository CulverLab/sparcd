package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a list of registered locations
 * 
 * @author David Slovikosky
 */
public class LocationData
{
	// The list of registered locations
	private List<Location> registeredLocations = new ArrayList<Location>();

	/**
	 * Adds a location to the list
	 * 
	 * @param location
	 *            The location to add
	 */
	public void addLocation(Location location)
	{
		this.registeredLocations.add(location);
	}

	/**
	 * Remove a location by name
	 * 
	 * @param location
	 *            The name of the location to remove
	 */
	public void removeLocation(String location)
	{
		this.registeredLocations.removeIf(loc ->
		{
			return loc.getName().equals(location);
		});
	}

	/**
	 * Remove a location from the list
	 * 
	 * @param location
	 *            The location to remove
	 */
	public void removeLocation(Location location)
	{
		this.registeredLocations.remove(location);
	}

	/**
	 * Returns a list of registered locations
	 * 
	 * @return The list of locations
	 */
	public List<Location> getRegisteredLocations()
	{
		return registeredLocations;
	}
}
