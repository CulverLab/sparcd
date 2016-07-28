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
import model.analysis.DataAnalysis;

public class HeaderFormatter extends TextFormatter
{
	public HeaderFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printLocations()
	{
		String toReturn = "";

		toReturn = toReturn + "LOCATIONS " + analysis.getAllImageLocations().size() + "\n";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + location.getName() + " ";
		if (analysis.nullLocationsFound())
			toReturn = toReturn + "Unknown ";
		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printSpecies()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES " + analysis.getAllImageSpecies().size() + "\n";
		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + species.getName() + " ";
		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printImageAnalysisHeader()
	{
		String toReturn = "";

		toReturn = toReturn + "FOR ALL SPECIES AT ALL LOCATIONS\n";
		toReturn = toReturn + "Number of pictures processed = " + images.size() + "\n";
		toReturn = toReturn + "Number of pictures used in activity calculation = \n";
		toReturn = toReturn + "Number of independent pictures used in analysis = \n";
		toReturn = toReturn + "Number of sequential pictures of same species at same location within a PERIOD = \n";
		toReturn = toReturn + "\n";

		return toReturn;
	}
}
