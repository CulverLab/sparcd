package model.location;

import java.io.Serializable;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * A class representing a location (latitude, longitude, and elevation)
 * 
 * @author David Slovikosky
 */
public class Location implements Serializable
{
	private final String name;
	private final double lat;
	private final double lng;
	private final double elevation;

	/**
	 * Location constructor
	 * 
	 * @param name
	 *            The name of the location
	 * @param lat
	 *            The latitude of the location
	 * @param lng
	 *            The longitude of the location
	 * @param elevation
	 *            The location elevation
	 */
	public Location(String name, double lat, double lng, double elevation)
	{
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.elevation = elevation;
	}

	/**
	 * Get the name of the location
	 * 
	 * @return the name of the location
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get the latitude of the location
	 * 
	 * @return the latitude of the location
	 */
	public double getLat()
	{
		return lat;
	}

	/**
	 * Get the longitude of the location
	 * 
	 * @return the longitude of the location
	 */
	public double getLng()
	{
		return lng;
	}

	/**
	 * Get the elevation of the location
	 * 
	 * @return the elevation of the location
	 */
	public double getElevation()
	{
		return elevation;
	}

	/**
	 * To string just returns the name of the location
	 */
	@Override
	public String toString()
	{
		return this.getName();
	}

	/**
	 * Get a formatted string
	 * 
	 * @return a string formatted to show more information about the location
	 */
	public String formattedString()
	{
		return this.getName() + " Latitude: " + this.getLat() + ", Longitude: " + this.getLng() + ", Elevation: " + this.getElevation();
	}

	/**
	 * Convert the location to a geoposition
	 * 
	 * @return A geoposition
	 */
	public GeoPosition toGeoPosition()
	{
		return new GeoPosition(lat, lng);
	}
}
