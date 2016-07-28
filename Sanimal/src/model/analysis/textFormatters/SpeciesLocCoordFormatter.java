/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;

public class SpeciesLocCoordFormatter extends TextFormatter
{
	public SpeciesLocCoordFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printSpeciesByLocWithUTM()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES BY LOCATION WITH UTM AND ELEVATION\n";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";
			toReturn = toReturn + "Location                      Lat?       Long?       Elevation\n";
			Set<Location> locations = new HashSet<Location>(analysis.getYearToLocations().get(species).values().stream().flatMap(x -> x.stream()).collect(Collectors.toList()));
			for (Location location : locations)
			{
				toReturn = toReturn + String.format("%-28s  %8.6f  %8.6f  %7f\n", location.getName(), location.getLat(), location.getLng(), location.getElevation());
			}
			toReturn = toReturn + "\n";
		}

		return toReturn;
	}
}
