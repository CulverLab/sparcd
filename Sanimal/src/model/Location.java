/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import org.jxmapviewer.viewer.GeoPosition;

public class Location
{
	private final String name;
	private final double lat;
	private final double lng;
	private final double elevation;

	public Location(String name, double lat, double lng, double elevation)
	{
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.elevation = elevation;
	}

	public String getName()
	{
		return name;
	}

	public double getLat()
	{
		return lat;
	}

	public double getLng()
	{
		return lng;
	}

	public double getElevation()
	{
		return elevation;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public String formattedString()
	{
		return this.getName() + " Latitude: " + this.getLat() + ", Longitude: " + this.getLng() + ", Elevation: " + this.getElevation();
	}

	public GeoPosition toGeoPosition()
	{
		return new GeoPosition(lat, lng);
	}
}
