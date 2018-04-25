package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;

import java.util.List;

/**
 * The text formatter for the header of the various text output
 * 
 * @author David Slovikosky
 */
public class HeaderFormatter extends TextFormatter
{
	public HeaderFormatter(List<ImageEntry> images, DataAnalyzer analysis)
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
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("LOCATIONS ").append(analysis.getAllImageLocations().size()).append("\n");
		for (Location location : analysis.getAllImageLocations())
			toReturn.append(location.getName()).append(" ");
		if (analysis.nullLocationsFound())
			toReturn.append("Unknown ");
		toReturn.append("\n\n");

		return toReturn.toString();
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
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES ").append(analysis.getAllImageSpecies().size()).append("\n");
		for (Species species : analysis.getAllImageSpecies())
			toReturn.append(species.getName()).append(" ");
		toReturn.append("\n\n");

		return toReturn.toString();
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
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new ImageQuery().locationOnly(location).query(withSpecies);
				totalActivity = totalActivity + analysis.activityForImageList(withSpeciesLoc);
			}
		}

		toReturn = toReturn + "Number of pictures used in activity calculation = " + totalActivity + "\n";

		Integer totalPeriod = 0;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new ImageQuery().locationOnly(location).query(withSpecies);
				totalPeriod = totalPeriod + analysis.periodForImageList(withSpeciesLoc);
			}
		}

		toReturn = toReturn + "Number of independent pictures used in analysis = " + totalPeriod + "\n";

		toReturn = toReturn + "Number of sequential pictures of same species at same location within a PERIOD = " + (images.size() - totalPeriod) + "\n";
		toReturn = toReturn + "\n";

		return toReturn;
	}
}
