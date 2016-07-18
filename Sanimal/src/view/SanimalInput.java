/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import javax.swing.JOptionPane;

import model.Location;
import model.Species;

public class SanimalInput
{
	public static Species askUserForNewSpecies()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new species");
			if (name == null)
				return null;
		}
		return new Species(name);
	}

	public static Location askUserForNewLocation()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new location");
			if (name == null)
				return null;
		}
		Double latitude = Double.MAX_VALUE;
		while (latitude > 85 || latitude < -85)
		{
			try
			{
				String latitudeString = JOptionPane.showInputDialog("Enter the latitude (+/- 85) of location '" + name + "'");
				if (latitudeString == null)
					return null;
				latitude = Double.parseDouble(latitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double longitude = Double.MAX_VALUE;
		while (longitude > 180 || longitude < -180)
		{
			try
			{
				String longitudeString = JOptionPane.showInputDialog("Enter the longitude (+/- 180) of location '" + name + "'");
				if (longitudeString == null)
					return null;
				longitude = Double.parseDouble(longitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double elevation = Double.MAX_VALUE;
		while (elevation == Double.MAX_VALUE)
		{
			try
			{
				String elevationString = JOptionPane.showInputDialog("Enter the elevation (in feet) of location '" + name + "'");
				if (elevationString == null)
					return null;
				elevation = Double.parseDouble(elevationString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		return new Location(name, latitude, longitude, elevation);
	}
}
