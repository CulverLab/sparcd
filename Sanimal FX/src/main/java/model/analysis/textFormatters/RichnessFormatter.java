package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * The text formatter for richness calculations
 * 
 * @author David Slovikosky
 */
public class RichnessFormatter extends TextFormatter
{
	public RichnessFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A table of locations vs. species showing the number of pictures of each species recorded at the location. The last column shows the number of
	 * species recorded at the location (Rich), and the last row shows total number of loations a species was recorded at (Richness)
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLocationSpeciesRichness()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("LOCATIONS BY SPECIES AND LOCATION AND SPECIES RICHNESS\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("Location                          ");
		for (Species species : analysis.getAllImageSpecies())
			toReturn.append(String.format("%-6s ", StringUtils.left(species.getName(), 6)));
		toReturn.append("Rich\n");

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-28s       ", location.getName()));
			List<ImageEntry> imagesAtLoc = new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer horizontalRichness = 0;
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesAtLocWithSpecies = new ImageQuery().speciesOnly(species).query(imagesAtLoc);
				Integer period = analysis.periodForImageList(imagesAtLocWithSpecies);
				horizontalRichness = horizontalRichness + (period == 0 ? 0 : 1);
				toReturn.append(String.format("%5d  ", period));
			}
			toReturn.append(String.format("%5d  ", horizontalRichness));

			toReturn.append("\n");
		}
		toReturn.append("Richness                           ");

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer richness = 0;
			List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(images);
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesWithSpeciesAtLoc = new ImageQuery().locationOnly(location).query(imagesWithSpecies);
				richness = richness + (imagesWithSpeciesAtLoc.size() == 0 ? 0 : 1);
			}
			toReturn.append(String.format("%5d  ", richness));
		}
		toReturn.append("\n");

		toReturn.append("\n");

		return toReturn.toString();
	}
}
