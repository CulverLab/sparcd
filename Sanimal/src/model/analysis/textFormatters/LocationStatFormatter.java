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

import org.apache.commons.collections4.SetUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;
import model.analysis.SanimalAnalysisUtils;

public class LocationStatFormatter extends TextFormatter
{
	public LocationStatFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printPercentOfSpeciesInLoc()
	{
		String toReturn = "";

		toReturn = toReturn + "FOR EACH LOCATION TOTAL NUMBER AND PERCENT OF EACH SPECIES\n";
		toReturn = toReturn + "  Use independent picture\n";

		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%30s ", location.getName());
		toReturn = toReturn + "\n";
		toReturn = toReturn + "Species";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + "                   Total Percent";
		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-26s", species.getName());
			for (Location location : analysis.getAllImageLocations())
			{
				int imagesAtLoc = new PredicateBuilder().locationOnly(location).anyValidSpecies().query(images).size();
				List<ImageEntry> filtered = new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images);
				toReturn = toReturn + String.format("%5d %7.2f                   ", filtered.size(), (filtered.size() / (double) imagesAtLoc) * 100);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures            ";

		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%5d  100.00                   ", new PredicateBuilder().locationOnly(location).query(images).size());

		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printSpeciesByMonthByLocByYear()
	{
		String toReturn = "";

		toReturn = toReturn + "FOR EACH LOCATION AND MONTH TOTAL NUMBER EACH SPECIES\n";
		toReturn = toReturn + "  Use independent picture\n";

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + year + "\n";

			for (Location location : analysis.getAllImageLocations())
			{
				toReturn = toReturn + String.format("%-28s  Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec   Total\n", location.getName());
				List<ImageEntry> atLocation = new PredicateBuilder().yearOnly(year).locationOnly(location).query(images);
				// All species
				for (Species species : analysis.getAllImageSpecies())
				{
					int totalPics = 0;
					List<ImageEntry> atLocationWithSpecies = new PredicateBuilder().speciesOnly(species).query(atLocation);
					toReturn = toReturn + String.format("%-28s", species.getName());
					// Months 0-12
					for (int i = 0; i < 12; i++)
					{
						int numPics = new PredicateBuilder().monthOnly(i).query(atLocationWithSpecies).size();
						toReturn = toReturn + String.format("%5d  ", numPics);
						totalPics = totalPics + numPics;
					}
					toReturn = toReturn + String.format("%5d  ", totalPics);
					toReturn = toReturn + "\n";
				}
				toReturn = toReturn + "Total pictures              ";
				int totalPics = 0;
				for (int i = 0; i < 12; i++)
				{
					int numPics = new PredicateBuilder().monthOnly(i).query(atLocation).size();
					toReturn = toReturn + String.format("%5d  ", numPics);
					totalPics = totalPics + numPics;
				}
				toReturn = toReturn + String.format("%5d  ", totalPics);
				toReturn = toReturn + "\n";
				toReturn = toReturn + "Total effort                ";
				int totalEffort = 0;
				for (int i = 0; i < 12; i++)
				{
					int effort = new PredicateBuilder().monthOnly(i).removeMonthlyDuplicates().query(atLocation).size();
					toReturn = toReturn + String.format("%5d  ", effort);
					totalEffort = totalEffort + effort;
				}
				toReturn = toReturn + String.format("%5d  ", totalEffort);
				toReturn = toReturn + "\n";
				toReturn = toReturn + "Total/Total effort          ";
				for (int i = 0; i < 12; i++)
				{
					int numPics = new PredicateBuilder().monthOnly(i).query(atLocation).size();
					int effort = new PredicateBuilder().monthOnly(i).removeMonthlyDuplicates().query(atLocation).size();
					double ratio = 0;
					if (effort != 0)
						ratio = (double) numPics / (double) effort;
					toReturn = toReturn + String.format("%5.2f  ", ratio);
				}
				double totalRatio = 0;
				if (totalEffort != 0)
					totalRatio = (double) totalPics / (double) totalEffort;
				toReturn = toReturn + String.format("%5.2f  ", totalRatio);
				toReturn = toReturn + "\n\n";
			}
		}

		return toReturn;
	}

	public String printSpeciesByMonthByLoc()
	{
		String toReturn = "";

		toReturn = toReturn + "ALL LOCATIONS ALL SPECIES FOR EACH MONTH FOR ALL YEARS\n";
		toReturn = toReturn + "  Use independent picture\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (numYears != 0)
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s  Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec   Total\n", location.getName());
			List<ImageEntry> atLocation = new PredicateBuilder().locationOnly(location).query(images);

			for (Species species : analysis.getAllImageSpecies())
			{
				int totalPics = 0;
				List<ImageEntry> atLocationWithSpecies = new PredicateBuilder().speciesOnly(species).query(atLocation);
				toReturn = toReturn + String.format("%-28s", species.getName());
				// Months 0-12
				for (int i = 0; i < 12; i++)
				{
					int numPics = new PredicateBuilder().monthOnly(i).query(atLocationWithSpecies).size();
					toReturn = toReturn + String.format("%5d  ", numPics);
					totalPics = totalPics + numPics;
				}
				toReturn = toReturn + String.format("%5d  ", totalPics);
				toReturn = toReturn + "\n";
			}
			toReturn = toReturn + "Total pictures              ";
			int totalPics = 0;
			for (int i = 0; i < 12; i++)
			{
				int numPics = new PredicateBuilder().monthOnly(i).query(atLocation).size();
				toReturn = toReturn + String.format("%5d  ", numPics);
				totalPics = totalPics + numPics;
			}
			toReturn = toReturn + String.format("%5d  ", totalPics);
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Total effort                ";
			int totalEffort = 0;
			for (int i = 0; i < 12; i++)
			{
				int effort = new PredicateBuilder().monthOnly(i).removeMonthlyDuplicates().query(atLocation).size();
				toReturn = toReturn + String.format("%5d  ", effort);
				totalEffort = totalEffort + effort;
			}
			toReturn = toReturn + String.format("%5d  ", totalEffort);
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Total/Total effort          ";
			for (int i = 0; i < 12; i++)
			{
				int numPics = new PredicateBuilder().monthOnly(i).query(atLocation).size();
				int effort = new PredicateBuilder().monthOnly(i).removeMonthlyDuplicates().query(atLocation).size();
				double ratio = 0;
				if (effort != 0)
					ratio = (double) numPics / (double) effort;
				toReturn = toReturn + String.format("%5.2f  ", ratio);
			}
			double totalRatio = 0;
			if (totalEffort != 0)
				totalRatio = (double) totalPics / (double) totalEffort;
			toReturn = toReturn + String.format("%5.2f  ", totalRatio);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

	public String printDistanceBetweenLocations()
	{
		String toReturn = "";

		toReturn = toReturn + "DISTANCE (km) BETWEEN LOCATIONS\n";

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
					double distance = SanimalAnalysisUtils.distanceBetween(location.getLat(), location.getLng(), other.getLat(), other.getLng());
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
			toReturn = toReturn + String.format("Minimum distance = %7.3f Locations: %28s %28s\n", minDistance, minLoc1.getName(), minLoc2.getName());
			toReturn = toReturn + String.format("Maximum distance = %7.3f Locations: %28s %28s\n", maxDistance, maxLoc1.getName(), maxLoc2.getName());
			toReturn = toReturn + String.format("Average distance = %7.3f\n\n", (minDistance + maxDistance) / 2.0D);
		}

		toReturn = toReturn + "Locations                       ";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%-28s", location.getName());
		toReturn = toReturn + "\n";
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-32s", location.getName());
			for (Location other : analysis.getAllImageLocations())
			{
				double distance = SanimalAnalysisUtils.distanceBetween(location.getLat(), location.getLng(), other.getLat(), other.getLng());
				toReturn = toReturn + String.format("%-28f", distance);
			}
			toReturn = toReturn + "\n";
		}
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printSpeciesOverlapAtLoc()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES OVERLAP AT LOCATIONS\n";
		toReturn = toReturn + "  Number of locations  " + analysis.getAllImageLocations().size() + "\n";
		toReturn = toReturn + "                          Locations  Locations and percent of locations where both species were recorded\n";
		toReturn = toReturn + "Species                    recorded ";
		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + String.format("%-12s", species.getName());
		toReturn = toReturn + "\n";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());
			Set<Location> locations = new HashSet<Location>(analysis.getYearToLocations().get(species).values().stream().flatMap(x -> x.stream()).collect(Collectors.toList()));
			toReturn = toReturn + String.format("%3d    ", locations.size());
			for (Species other : analysis.getAllImageSpecies())
			{
				Set<Location> locationsOther = new HashSet<Location>(analysis.getYearToLocations().get(other).values().stream().flatMap(x -> x.stream()).collect(Collectors.toList()));
				Integer intersectionSize = SetUtils.<Location> intersection(locations, locationsOther).size();
				toReturn = toReturn + String.format("%2d (%6.1f) ", intersectionSize, (100D * (double) intersectionSize / locations.size()));
			}
			toReturn = toReturn + "\n";
		}
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printAreaCoveredByTraps()
	{
		String toReturn = "";

		toReturn = toReturn + "AREA COVERED BY CAMERA TRAPS\n";
		toReturn = toReturn + "  List of locations forming convex polygon\n";
		toReturn = toReturn + "No idea what these numbers are\n\n";

		return toReturn;
	}

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
