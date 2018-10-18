package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.location.Location;
import model.location.UTMCoord;
import model.species.Species;

import java.util.List;

/**
 * The text formatter for species with location/utm/latlng coordinates
 * 
 * @author David Slovikosky
 */
public class SpeciesLocCoordFormatter extends TextFormatter
{
	public SpeciesLocCoordFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species a list of locations where the species was recorded, and the UTM and elevation of the location.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesByLocWithUTM()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES BY LOCATION WITH UTM AND ELEVATION\n");
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(images);
			toReturn.append(species.getName()).append("\n");
			toReturn.append("Location                        UTMe-w   UTMn-s    Elevation   Lat        Long\n");
			for (Location location : analysis.locationsForImageList(withSpecies))
			{
				UTMCoord coord = SanimalAnalysisUtils.Deg2UTM(location.getLat(), location.getLng());
				toReturn.append(String.format("%-28s  %8d  %8d  %7.0f      %8.6f  %8.6f\n", location.getName(), Math.round(coord.getEasting()), Math.round(coord.getNorthing()), location.getElevation(), location.getLat(), location.getLng()));
			}
			toReturn.append("\n");
		}

		return toReturn.toString();
	}
}
