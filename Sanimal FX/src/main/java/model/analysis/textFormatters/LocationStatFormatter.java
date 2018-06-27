package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.collections4.ListUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The text formatter for statistics about locations
 * 
 * @author David Slovikosky
 */
public class LocationStatFormatter extends TextFormatter
{
	public LocationStatFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A species-location table showing the total number of independent pictures, and percent of the total for each location for each species
	 * analyzed. The total number of independent pictures for all species is given for each location.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPercentOfSpeciesInLoc()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("FOR EACH LOCATION TOTAL NUMBER AND PERCENT OF EACH SPECIES\n");
		toReturn.append("  Use independent picture\n");

		for (Location location : analysis.getAllImageLocations())
			toReturn.append(String.format("%31s ", location.getName()));
		toReturn.append("\n");
		toReturn.append("Species");
		for (Location ignored : analysis.getAllImageLocations())
			toReturn.append("                   Total Percent");
		toReturn.append("\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(String.format("%-26s", species.getCommonName()));
			for (Location location : analysis.getAllImageLocations())
			{
				Integer totalPeriod = analysis.periodForImageList(new ImageQuery().locationOnly(location).anyValidSpecies().query(analysis.getImagesSortedByDate()));
				Integer period = analysis.periodForImageList(new ImageQuery().locationOnly(location).speciesOnly(species).query(analysis.getImagesSortedByDate()));
				toReturn.append(String.format("%5d %7.2f                   ", period, (period / (double) totalPeriod) * 100));
			}
			toReturn.append("\n");
		}

		toReturn.append("Total pictures            ");

		for (Location location : analysis.getAllImageLocations())
			toReturn.append(String.format("%5d  100.00                   ", analysis.periodForImageList(new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate()))));

		toReturn.append("\n\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each year, for each location, a species-month table shows the number of independent records for each species. For each location and species
	 * the total number of independent records for all months is given in the last column (Total). The total number of pictures (Total pictures), the
	 * total number of camera trap days (Total effort), and total number of independent pictures (TotL) normalized by the total number of camera trap
	 * days for each month (Total/(Total effort)) is given. This is followed by a summary for all years.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesByMonthByLocByYear()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("FOR EACH LOCATION AND MONTH TOTAL NUMBER EACH SPECIES\n");
		toReturn.append("  Use independent picture\n");

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn.append(year).append("\n");

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> atLocation = new ImageQuery().yearOnly(year).locationOnly(location).query(analysis.getImagesSortedByDate());
				if (!atLocation.isEmpty())
				{
					toReturn.append(String.format("%-28s  Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec   Total\n", location.getName()));
					// All species
					for (Species species : analysis.getAllImageSpecies())
					{
						int totalPics = 0;
						List<ImageEntry> atLocationWithSpecies = new ImageQuery().speciesOnly(species).query(atLocation);
						if (!atLocationWithSpecies.isEmpty())
						{
							toReturn.append(String.format("%-28s", species.getCommonName()));
							// Months 0-12
							for (int i = 0; i < 12; i++)
							{
								Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocationWithSpecies));
								toReturn.append(String.format("%5d  ", period));
								totalPics = totalPics + period;
							}
							toReturn.append(String.format("%5d  ", totalPics));
							toReturn.append("\n");
						}
					}
					toReturn.append("Total pictures              ");
					int totalPics = 0;
					for (int i = 0; i < 12; i++)
					{
						Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocation));
						toReturn.append(String.format("%5d  ", period));
						totalPics = totalPics + period;
					}
					toReturn.append(String.format("%5d  ", totalPics));
					toReturn.append("\n");
					toReturn.append("Total effort                ");
					int totalEffort = 0;
					LocalDateTime firstCal = analysis.getFirstImageInList(atLocation).getDateTaken();
					LocalDateTime lastCal = analysis.getLastImageInList(atLocation).getDateTaken();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					for (int i = 0; i < 12; i++)
					{
						int effort = 0;
						if (firstMonth == lastMonth && firstMonth == i)
							effort = lastDay - firstDay + 1;
						else if (firstMonth == i)
							effort = 31 - firstDay + 1;
						else if (lastMonth == i)
							effort = lastDay;
						else if (firstMonth < i && lastMonth > i)
						{
							effort = 31;
						}

						toReturn.append(String.format("%5d  ", effort));
						totalEffort = totalEffort + effort;
					}
					toReturn.append(String.format("%5d  ", totalEffort));
					toReturn.append("\n");
					toReturn.append("Total/Total effort          ");
					firstCal = analysis.getFirstImageInList(atLocation).getDateTaken();
					lastCal = analysis.getLastImageInList(atLocation).getDateTaken();
					firstMonth = firstCal.getMonthValue();
					lastMonth = lastCal.getMonthValue();
					firstDay = firstCal.getDayOfMonth();
					lastDay = lastCal.getDayOfMonth();
					for (int i = 0; i < 12; i++)
					{
						Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocation));
						int effort = 0;
						if (firstMonth == lastMonth && firstMonth == i)
							effort = lastDay - firstDay + 1;
						else if (firstMonth == i)
							effort = 31 - firstDay + 1;
						else if (lastMonth == i)
							effort = lastDay;
						else if (firstMonth < i && lastMonth > i)
						{
							effort = 31;
						}
						double ratio = 0;
						if (effort != 0)
							ratio = (double) period / (double) effort;
						toReturn.append(String.format("%5.2f  ", ratio));
					}
					double totalRatio = 0;
					if (totalEffort != 0)
						totalRatio = (double) totalPics / (double) totalEffort;
					toReturn.append(String.format("%5.2f  ", totalRatio));
					toReturn.append("\n\n");
				}
			}
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesByMonthByLoc()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("ALL LOCATIONS ALL SPECIES FOR EACH MONTH FOR ALL YEARS\n");
		toReturn.append("  Use independent picture\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (numYears != 0)
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> atLocation = new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate());

			if (!atLocation.isEmpty())
			{
				toReturn.append(String.format("%-28s  Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec   Total\n", location.getName()));

				for (Species species : analysis.getAllImageSpecies())
				{
					int totalPics = 0;
					List<ImageEntry> atLocationWithSpecies = new ImageQuery().speciesOnly(species).query(atLocation);
					if (!atLocationWithSpecies.isEmpty())
					{
						toReturn.append(String.format("%-28s", species.getCommonName()));
						// Months 0-12
						for (int i = 0; i < 12; i++)
						{
							Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocationWithSpecies));
							toReturn.append(String.format("%5d  ", period));
							totalPics = totalPics + period;
						}
						toReturn.append(String.format("%5d  ", totalPics));
						toReturn.append("\n");
					}
				}
				toReturn.append("Total pictures              ");
				int totalPics = 0;
				for (int i = 0; i < 12; i++)
				{
					Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocation));
					toReturn.append(String.format("%5d  ", period));
					totalPics = totalPics + period;
				}
				toReturn.append(String.format("%5d  ", totalPics));
				toReturn.append("\n");
				toReturn.append("Total effort                ");
				int totalEffort = 0;
				LocalDateTime firstCal = analysis.getFirstImageInList(atLocation).getDateTaken();
				LocalDateTime lastCal = analysis.getLastImageInList(atLocation).getDateTaken();
				Integer firstMonth = firstCal.getMonthValue();
				Integer lastMonth = lastCal.getMonthValue();
				Integer firstDay = firstCal.getDayOfMonth();
				Integer lastDay = lastCal.getDayOfMonth();
				for (int i = 0; i < 12; i++)
				{
					int effort = 0;
					if (firstMonth == lastMonth && firstMonth == i)
						effort = lastDay - firstDay + 1;
					else if (firstMonth == i)
						effort = 31 - firstDay + 1;
					else if (lastMonth == i)
						effort = lastDay;
					else if (firstMonth < i && lastMonth > i)
					{
						effort = 31;
					}

					toReturn.append(String.format("%5d  ", effort));
					totalEffort = totalEffort + effort;
				}
				toReturn.append(String.format("%5d  ", totalEffort));
				toReturn.append("\n");
				toReturn.append("Total/Total effort          ");
				firstCal = analysis.getFirstImageInList(atLocation).getDateTaken();
				lastCal = analysis.getLastImageInList(atLocation).getDateTaken();
				firstMonth = firstCal.getMonthValue();
				lastMonth = lastCal.getMonthValue();
				firstDay = firstCal.getDayOfMonth();
				lastDay = lastCal.getDayOfMonth();
				for (int i = 0; i < 12; i++)
				{
					Integer period = analysis.periodForImageList(new ImageQuery().monthOnly(i).query(atLocation));
					int effort = 0;
					if (firstMonth == lastMonth && firstMonth == i)
						effort = lastDay - firstDay + 1;
					else if (firstMonth == i)
						effort = 31 - firstDay + 1;
					else if (lastMonth == i)
						effort = lastDay;
					else if (firstMonth < i && lastMonth > i)
					{
						effort = 31;
					}

					double ratio = 0;
					if (effort != 0)
						ratio = (double) period / (double) effort;
					toReturn.append(String.format("%5.2f  ", ratio));
				}
				double totalRatio = 0;
				if (totalEffort != 0)
					totalRatio = (double) totalPics / (double) totalEffort;
				toReturn.append(String.format("%5.2f  ", totalRatio));
				toReturn.append("\n");

				toReturn.append("\n");
			}
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The Minimum distance and also Maximum distance between any two locations is given. The Average distance between all locations is also given.
	 * This is folowed by a table showing all distances of all locations analyzed. Distances are in km.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printDistanceBetweenLocations()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("DISTANCE (km) BETWEEN LOCATIONS\n");

		double maxDistance = 0;
		Location maxLoc1 = null;
		Location maxLoc2 = null;
		Location minLoc1 = null;
		Location minLoc2 = null;
		double minDistance = Double.MAX_VALUE;
		for (Location location : analysis.getAllImageLocations())
			for (Location other : analysis.getAllImageLocations())
				if (!location.equals(other))
				{
					double distance = SanimalAnalysisUtils.distanceBetween(location.getLatitude(), location.getLongitude(), other.getLatitude(), other.getLongitude());
					if (distance >= maxDistance)
					{
						maxDistance = distance;
						maxLoc1 = location;
						maxLoc2 = other;
					}
					if (distance <= minDistance)
					{
						minDistance = distance;
						minLoc1 = location;
						minLoc2 = other;
					}
				}
		if (minLoc1 != null)
		{
			toReturn.append(String.format("Minimum distance = %7.3f Locations: %28s %28s\n", minDistance, minLoc1.getName(), minLoc2.getName()));
			toReturn.append(String.format("Maximum distance = %7.3f Locations: %28s %28s\n", maxDistance, maxLoc1.getName(), maxLoc2.getName()));
			toReturn.append(String.format("Average distance = %7.3f\n\n", (minDistance + maxDistance) / 2.0D));
		}

		toReturn.append("Locations                       ");
		for (Location location : analysis.getAllImageLocations())
			toReturn.append(String.format("%-28s", location.getName()));
		toReturn.append("\n");
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-32s", location.getName()));
			for (Location other : analysis.getAllImageLocations())
			{
				double distance = SanimalAnalysisUtils.distanceBetween(location.getLatitude(), location.getLongitude(), other.getLatitude(), other.getLongitude());
				toReturn.append(String.format("%-28f", distance));
			}
			toReturn.append("\n");
		}
		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A species x species table. Each row has the species name followed by the number of locations in () where the species was recored, then the
	 * number and in () the percent of locations where it was recorded with the species in the column.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesOverlapAtLoc()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES OVERLAP AT LOCATIONS\n");
		toReturn.append("  Number of locations  ").append(analysis.getAllImageLocations().size()).append("\n");
		toReturn.append("                          Locations  Locations and percent of locations where both species were recorded\n");
		toReturn.append("Species                    recorded ");
		for (Species species : analysis.getAllImageSpecies())
			toReturn.append(String.format("%-12s", species.getCommonName()));
		toReturn.append("\n");
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(images);
			toReturn.append(String.format("%-28s", species.getCommonName()));
			List<Location> locations = analysis.locationsForImageList(withSpecies);
			toReturn.append(String.format("%3d    ", locations.size()));
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withSpeciesOther = new ImageQuery().speciesOnly(other).query(images);
				List<Location> locationsOther = analysis.locationsForImageList(withSpeciesOther);
				Integer intersectionSize = ListUtils.<Location> intersection(locations, locationsOther).size();
				toReturn.append(String.format("%2d (%6.1f) ", intersectionSize, (100D * (double) intersectionSize / locations.size())));
			}
			toReturn.append("\n");
		}
		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The area contained in the convex polygon formed by the outer-most locations listed. Also given are the UTM locations of the locations. This is
	 * followed by the Area contained in the convex polygon given in kilometers^2 and miles^2.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printAreaCoveredByTraps()
	{
		String toReturn = "";

		toReturn = toReturn + "AREA COVERED BY CAMERA TRAPS\n";
		toReturn = toReturn + "  List of locations forming convex polygon\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The 10 most similar locations where similarity is based on the frequency of number of independent pictures of each species recorded. Paired
	 * locations are given. The last column shows 10 times the squart toot of the sum of the squared frequency differences. Lower scores represent
	 * more similar species frequency composition. Also given are the top 10 most different locations. Higher scores represent greater differences.
	 * This is followed by a table showing all the scores for each paired of locations.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLocSpeciesFrequencySimiliarity()
	{
		String toReturn = "";

		toReturn = toReturn + "LOCATION SPECIES FREQUENCY SIMILARITY (LOWER IS MORE SIMILAR)\n";
		toReturn = toReturn + "   One picture of each species per camera per PERIOD\n";
		toReturn = toReturn + "   Square root of sums of squared difference in frequency\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES FREQUENCY\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES FREQUENCY\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";
		//      ???

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Each location has a species composition. Pairs of locations have the no species in common, some species incommon, or all species in common. The
	 * Jaccard Similarity Index (JSI) is a similarity index. Shown are the top 10 locations with the most similar list of spcies. Given are the names
	 * of the locations, their JSI index (JSI), and the number of species recorded at each location (N1 N2) and the number of species in common
	 * (N1&N2). Also given is a list of the top 10 most different locations that have fewer species in common. A table of JSI scores comparing all
	 * locations is also given. Locations with no specis in common have JSI = 0.0. If both locations share the same species JSI = 1.000.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLocSpeciesCompositionSimiliarity()
	{
		String toReturn = "";

		toReturn = toReturn + "LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)\n";
		toReturn = toReturn + "  Is species present at this location? yes=1, no=0\n";
		toReturn = toReturn + "  1.00 means locations are identical; 0.00 means locations have no species in common\n";
		toReturn = toReturn + "  Location, location, JSI, number of species at each location, and number of species in common\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES COMPOSITION\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES COMPOSITION\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";
		//      ???

		return toReturn;
	}
}
