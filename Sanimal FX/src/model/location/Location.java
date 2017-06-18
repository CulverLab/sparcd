package model.location;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * A class representing a location (latitude, longitude, and elevation)
 * 
 * @author David Slovikosky
 */
public class Location implements Serializable
{
	private final StringProperty nameProperty = new SimpleStringProperty();
	private final DoubleProperty latProperty = new SimpleDoubleProperty();
	private final DoubleProperty lngProperty = new SimpleDoubleProperty();
	private final DoubleProperty elevationProperty = new SimpleDoubleProperty();

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
	public Location(String name, Double lat, Double lng, Double elevation)
	{
		this.nameProperty.setValue(name);
		this.latProperty.setValue(lat);
		this.lngProperty.setValue(lng);
		this.elevationProperty.setValue(elevation);
	}

	/**
	 * Default constructor
	 */
	public Location()
	{
		this.nameProperty.setValue("");
		this.latProperty.setValue(-1000);
		this.lngProperty.setValue(-1000);
		this.elevationProperty.setValue(-20000);
	}

	public Boolean nameValid() { return !this.nameProperty.getValue().isEmpty(); }

	public Boolean latValid() { return this.latProperty.getValue() <= 85.0 && this.latProperty.getValue() >= -85.0; }

	public Boolean lngValid() { return this.lngProperty.getValue() <= 180.0 && this.lngProperty.getValue() >= -180; }

	public Boolean elevationValid() { return this.elevationProperty.getValue() != -20000; }

	public Boolean locationValid() { return nameValid() && latValid() && lngValid() && elevationValid(); }

	public void setName(String name)
	{
		this.nameProperty.setValue(name);
	}

	/**
	 * Get the name of the location
	 * 
	 * @return the name of the location
	 */
	public String getName()
	{
		return nameProperty.getValue();
	}

	/**
	 * Get the name property
	 *
	 * @return The name property
	 */
	public StringProperty getNameProperty()
	{
		return nameProperty;
	}

	public void setLat(Double lat)
	{
		this.latProperty.setValue(lat);
	}

	/**
	 * Get the latitude of the location
	 * 
	 * @return the latitude of the location
	 */
	public Double getLat()
	{
		return latProperty.getValue();
	}

	/**
	 * Get the lat property
	 *
	 * @return The lat property
	 */
	public DoubleProperty getLatProperty()
	{
		return latProperty;
	}

	public void setLng(Double lng)
	{
		this.lngProperty.setValue(lng);
	}

	/**
	 * Get the longitude of the location
	 * 
	 * @return the longitude of the location
	 */
	public Double getLng()
	{
		return lngProperty.getValue();
	}

	/**
	 * Get the lng property
	 *
	 * @return The lng property
	 */
	public DoubleProperty getLngProperty()
	{
		return lngProperty;
	}

	public void setElevation(Double elevation)
	{
		this.elevationProperty.setValue(elevation);
	}

	/**
	 * Get the elevation of the location
	 * 
	 * @return the elevation of the location
	 */
	public Double getElevation()
	{
		return elevationProperty.getValue();
	}

	/**
	 * Get the elevation property
	 *
	 * @return The elevation property
	 */
	public DoubleProperty getElevationProperty()
	{
		return elevationProperty;
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
}
