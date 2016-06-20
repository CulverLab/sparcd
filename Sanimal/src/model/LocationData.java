/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LocationData
{
	private List<Location> registeredLocations = new ArrayList<Location>();

	public void addLocation(Location location)
	{
		this.registeredLocations.add(location);
	}

	public void removeLocation(String location)
	{
		this.registeredLocations.removeIf(new Predicate<Location>()
		{
			@Override
			public boolean test(Location loc)
			{
				return loc.getName().equals(location);
			}
		});
	}

	public void removeLocation(Location location)
	{
		this.registeredLocations.remove(location);
	}

	public List<Location> getRegisteredLocations()
	{
		return registeredLocations;
	}
}