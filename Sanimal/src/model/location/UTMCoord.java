package model.location;

/**
 * A class representing a UTM coordinate, not lat/lng
 * 
 * @author David Slovikosky
 */
public class UTMCoord
{
	// Easting part of the UTM coordinate
	private Double easting;
	// Northing part of the UTM coordinate
	private Double northing;
	// Zone part of the UTM coordinate
	private Integer zone;
	// Letter of the UTM coordinate
	private Character letter;

	/**
	 * Constructor for the UTM coordinate
	 * 
	 * @param easting
	 *            Easting part of the UTM coordinate
	 * @param northing
	 *            Northing part of the UTM coordinate
	 * @param zone
	 *            Zone part of the UTM coordinate
	 * @param letter
	 *            Letter of the UTM coordinate
	 */
	public UTMCoord(Double easting, Double northing, Integer zone, Character letter)
	{
		this.easting = easting;
		this.northing = northing;
		this.zone = zone;
		this.letter = letter;
	}

	/**
	 * @return Get the easting part of the UTM coordinate
	 */
	public Double getEasting()
	{
		return easting;
	}

	/**
	 * @return Get the northing part of the UTM coordinate
	 */
	public Double getNorthing()
	{
		return northing;
	}

	/**
	 * @return Get the zone of the UTM coordinate
	 */
	public Integer getZone()
	{
		return zone;
	}

	/**
	 * @return The letter of the UTM coordinate
	 */
	public Character getLetter()
	{
		return letter;
	}
}