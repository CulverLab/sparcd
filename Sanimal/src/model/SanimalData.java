/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

public class SanimalData
{
	private ImageData imageData = new ImageData();
	private LocationData locationData = new LocationData();
	private SpeciesData speciesData = new SpeciesData();

	public ImageData getImageData()
	{
		return imageData;
	}

	public LocationData getLocationData()
	{
		return locationData;
	}

	public SpeciesData getSpeciesData()
	{
		return speciesData;
	}
}
