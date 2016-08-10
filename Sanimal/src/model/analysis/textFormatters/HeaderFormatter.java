package model.analysis.textFormatters;

import java.util.List;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

/**
 * The text formatter for the header of the various text output
 * 
 * @author David Slovikosky
 */
public class HeaderFormatter extends TextFormatter
{
	public HeaderFormatter(List<ImageEntry> images, DataAnalysis analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The number of camera locations analyzed followed by the name of each location analyzed. Location names match the location folder names.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
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

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The number of species analyzed followed by the name of each species. Species names match the species folder names.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpecies()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES " + analysis.getAllImageSpecies().size() + "\n";
		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + species.getName() + " ";
		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Number of pictures processed ... The total number of jpg files processed. Number of pictures used in activity calculation ... The total number
	 * of pictures used to calculate a species' activity pattern. Number of independent pictures used in analysis ... The total number of independent
	 * pictures (this depends on the period entered by the user). Number of sequential pictures of same species at same location within a PERIOD ...
	 * The total number of all sequential pictures of all species at all locations. This also depends on the period entered by the user.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printImageAnalysisHeader()
	{
		String toReturn = "";

		toReturn = toReturn + "FOR ALL SPECIES AT ALL LOCATIONS\n";
		toReturn = toReturn + "Number of pictures processed = " + images.size() + "\n";

		Integer totalActivity = 0;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				totalActivity = totalActivity + analysis.activityForImageList(withSpeciesLoc);
			}
		}

		toReturn = toReturn + "Number of pictures used in activity calculation = " + totalActivity + "\n";

		Integer totalPeriod = 0;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				totalPeriod = totalPeriod + analysis.periodForImageList(withSpeciesLoc);
			}
		}

		toReturn = toReturn + "Number of independent pictures used in analysis = " + totalPeriod + "\n";

		toReturn = toReturn + "Number of sequential pictures of same species at same location within a PERIOD = " + (images.size() - totalPeriod) + "\n";
		toReturn = toReturn + "\n";

		return toReturn;
	}
}
