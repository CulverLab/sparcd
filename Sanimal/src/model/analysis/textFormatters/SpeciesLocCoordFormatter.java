/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.List;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.UTMCoord;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;
import model.analysis.SanimalAnalysisUtils;

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
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			toReturn = toReturn + species.getName() + "\n";
			toReturn = toReturn + "Location                        UTMe-w   UTMn-s    Elevation   Lat        Long\n";
			for (Location location : analysis.locationsForImageList(withSpecies))
			{
				UTMCoord coord = SanimalAnalysisUtils.Deg2UTM(location.getLat(), location.getLng());
				toReturn = toReturn + String.format("%-28s  %8d  %8d  %7.0f      %8.6f  %8.6f\n", location.getName(), Math.round(coord.getEasting()), Math.round(coord.getNorthing()), location.getElevation(), location.getLat(), location.getLng());
			}
			toReturn = toReturn + "\n";
		}

		return toReturn;
	}
}
