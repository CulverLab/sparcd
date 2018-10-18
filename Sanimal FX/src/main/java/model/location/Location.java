package model.location;

import javafx.beans.property.*;

/**
 * A class representing a location (latitude, longitude, and elevation)
 * 
 * @author David Slovikosky
 */
public class Location
{
	// Properties of a location are the name, latitude, longitude, and elevation
	private final StringProperty nameProperty = new SimpleStringProperty();
	private final StringProperty idProperty = new SimpleStringProperty();
	private final DoubleProperty latProperty = new SimpleDoubleProperty();
	private final DoubleProperty lngProperty = new SimpleDoubleProperty();
	private final DoubleProperty elevationProperty = new SimpleDoubleProperty();

	/**
	 * Location constructor
	 * 
	 * @param name
	 *            The name of the location
	 * @param id
	 * 			  The id of the location
	 * @param lat
	 *            The latitude of the location
	 * @param lng
	 *            The longitude of the location
	 * @param elevation
	 *            The location elevation
	 */
	public Location(String name, String id, Double lat, Double lng, Double elevation)
	{
		this.nameProperty.setValue(name);
		this.idProperty.setValue(id);
		this.latProperty.setValue(lat);
		this.lngProperty.setValue(lng);
		this.elevationProperty.setValue(elevation);
	}

	/**
	 * Default constructor sets values to invalid values
	 */
	public Location()
	{
		this.nameProperty.setValue("");
		this.idProperty.setValue("");
		this.latProperty.setValue(-1000);
		this.lngProperty.setValue(-1000);
		this.elevationProperty.setValue(-20000);
	}

	/**
	 * @return True if the name is not empty
	 */
	public Boolean nameValid() { return !this.nameProperty.getValue().isEmpty(); }

	/**
	 * @return True if the id is not empty
	 */
	public Boolean idValid() { return !this.idProperty.getValue().isEmpty(); }

	/**
	 * @return True if latitude is between -85 and +85
	 */
	public Boolean latValid() { return this.latProperty.getValue() <= 85.0 && this.latProperty.getValue() >= -85.0; }

	/**
	 * @return True if longitude is between -180 and +180
	 */
	public Boolean lngValid() { return this.lngProperty.getValue() <= 180.0 && this.lngProperty.getValue() >= -180; }

	/**
	 * @return True if elevation is not the default -20000 value
	 */
	public Boolean elevationValid() { return this.elevationProperty.getValue() != -20000; }

	/**
	 * @return True if the name, latitude, longitude, and elevation are valid
	 */
	public Boolean locationValid() { return nameValid() && idValid() && latValid() && lngValid() && elevationValid(); }

	/**
	 * Set the name of the location
	 *
	 * @param name the name of the location
	 */
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
	public StringProperty nameProperty()
	{
		return nameProperty;
	}

	/**
	 * Set the id of the location
	 *
	 * @param id the id of the location
	 */
	public void setId(String id)
	{
		this.idProperty.setValue(id);
	}

	/**
	 * Get the id of the location
	 *
	 * @return the id of the location
	 */
	public String getId()
	{
		return idProperty.getValue();
	}

	/**
	 * Get the id property
	 *
	 * @return The id property
	 */
	public StringProperty idProperty()
	{
		return idProperty;
	}

	/**
	 * Set the latitude property
	 *
	 * @param lat The latitude property
	 */
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

	/**
	 * Set the longitude of the location
	 *
	 * @param lng The new longitude
	 */
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

	/**
	 * Set the elevation of the location
	 *
	 * @param elevation the new elevation in METERS
	 */
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
		return this.getName() + "\nID: " + this.getId() + "\nLatitude: " + this.getLat() + "\nLongitude: " + this.getLng() + "\nElevation: " + this.getElevation();
	}
}
