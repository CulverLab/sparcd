/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

public class UTMCoord
{
	private double easting;
	private double northing;
	private int zone;
	private char letter;

	public UTMCoord(double easting, double northing, int zone, char letter)
	{
		this.easting = easting;
		this.northing = northing;
		this.zone = zone;
		this.letter = letter;
	}

	public double getEasting()
	{
		return easting;
	}

	public double getNorthing()
	{
		return northing;
	}

	public int getZone()
	{
		return zone;
	}

	public char getLetter()
	{
		return letter;
	}
}