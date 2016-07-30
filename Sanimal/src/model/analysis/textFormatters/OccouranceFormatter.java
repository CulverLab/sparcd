/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

public class OccouranceFormatter extends TextFormatter
{
	public OccouranceFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printCoOccuranceMatrix()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES CO-OCCURRENCE MATRIX\n";
		toReturn = toReturn + "  The number of locations each species pair co-occurs\n";
		toReturn = toReturn + "                            ";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%3s ", StringUtils.left(species.getName(), 3));
		}

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);

			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withOtherSpecies = new PredicateBuilder().speciesOnly(other).query(images);

				Integer numLocations = 0;

				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
					List<ImageEntry> withOtherSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withOtherSpecies);
					if (!withSpeciesLoc.isEmpty() && !withOtherSpeciesLoc.isEmpty())
						numLocations = numLocations + 1;
				}

				toReturn = toReturn + String.format("%3d ", numLocations);
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printAbsensePresenceMatrix()
	{
		String toReturn = "";

		toReturn = toReturn + "ABSENCE-PRESENCE MATRIX\n";
		toReturn = toReturn + "  Species vs locations matrix (locations in alphabetical order)\n";
		toReturn = toReturn + "          Species (";
		toReturn = toReturn + String.format("%3d", analysis.getAllImageSpecies().size());
		toReturn = toReturn + ")               Locations (";
		toReturn = toReturn + String.format("%3d", analysis.getAllImageLocations().size());
		toReturn = toReturn + ")\n";
		toReturn = toReturn + "                            ";

		List<Location> alphabetical = new ArrayList<Location>(analysis.getAllImageLocations());

		Collections.sort(alphabetical, new Comparator<Location>()
		{
			@Override
			public int compare(Location loc1, Location loc2)
			{
				return loc1.getName().compareTo(loc2.getName());
			}
		});

		for (Integer location = 0; location < alphabetical.size(); location++)
			toReturn = toReturn + String.format("%2d ", location + 1);

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());

			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);

			for (Location location : alphabetical)
			{
				List<ImageEntry> withSpeciesAtLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				toReturn = toReturn + String.format("%2d ", (withSpeciesAtLoc.size() == 0 ? 0 : 1));
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printMaxMinSpeciesElevation()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES MIN AND MAX ELEVATION\n";
		toReturn = toReturn + "  Species vs locations matrix (location sorted from low to high elevation)\n";
		toReturn = toReturn + "          Species (";
		toReturn = toReturn + String.format("%3d", analysis.getAllImageSpecies().size());
		toReturn = toReturn + ")               Locations (";
		toReturn = toReturn + String.format("%3d", analysis.getAllImageLocations().size());
		toReturn = toReturn + ")\n";
		toReturn = toReturn + "                            ";

		List<Location> elevationLocs = new ArrayList<Location>(analysis.getAllImageLocations());

		Collections.sort(elevationLocs, new Comparator<Location>()
		{
			@Override
			public int compare(Location loc1, Location loc2)
			{
				return Double.compare(loc1.getElevation(), loc2.getElevation());
			}
		});

		for (Integer location = 0; location < elevationLocs.size(); location++)
			toReturn = toReturn + String.format("%2d ", location + 1);

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());

			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);

			for (Location location : elevationLocs)
			{
				List<ImageEntry> withSpeciesAtLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				toReturn = toReturn + String.format("%2d ", (withSpeciesAtLoc.size() == 0 ? 0 : 1));
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		toReturn = toReturn + "  List of elevations and locations\n";

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format(" %2d %5.0f ", analysis.getAllImageLocations().indexOf(location) + 1, location.getElevation()) + location.getName() + "\n";
		}

		toReturn = toReturn + "\n";

		toReturn = toReturn + "  Minimum and maximum elevation for each species\n";

		toReturn = toReturn + "   SPECIES                     Min   Max\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			Double minElevation = Double.MAX_VALUE;
			Double maxElevation = 0D;

			List<ImageEntry> bySpecies = new PredicateBuilder().speciesOnly(species).query(images);

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> bySpeciesLocation = new PredicateBuilder().locationOnly(location).query(bySpecies);
				if (!bySpeciesLocation.isEmpty())
				{
					Double elevation = location.getElevation();
					if (elevation > maxElevation)
						maxElevation = elevation;
					if (elevation < minElevation)
						minElevation = elevation;
				}
			}

			toReturn = toReturn + String.format("%-28s %5.0f %5.0f\n", species.getName(), minElevation, maxElevation);
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printNativeOccupancy()
	{
		String toReturn = "";

		toReturn = toReturn + "NAIVE OCCUPANCY\n";
		toReturn = toReturn + "  Species naive location occupancy proportion\n";
		toReturn = toReturn + "  To create occupancy matrix run program OccupancyMatrix\n";
		toReturn = toReturn + "                               Fraction of locations   Number of locations\n";
		toReturn = toReturn + "Species                              Occupied             Occupied (" + String.format("%3d", analysis.getAllImageLocations().size()) + ")";

		Integer totalLocations = analysis.getAllImageLocations().size();

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			Integer locationsWithSpecies = 0;

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				if (!withSpeciesLoc.isEmpty())
					locationsWithSpecies = locationsWithSpecies + 1;
			}

			toReturn = toReturn + String.format("%-28s     %5.3f       3d\n", species.getName(), (double) locationsWithSpecies / totalLocations + locationsWithSpecies);
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printCHISqAnalysisOfPairedSpecieFreq()
	{
		String toReturn = "";

		toReturn = toReturn + "CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES\n";
		toReturn = toReturn + "  H0: Species frequencies are independent of site\n";
		toReturn = toReturn + "  Reject null hypothesis = R, Accept null hypothesis = -\n";
		toReturn = toReturn + "Sites                      \n";
		toReturn = toReturn + "No idea what these numbers are\n\n";

		//		for (Location location : analysis.getAllImageLocations())
		//			toReturn = toReturn + String.format("%-8s", StringUtils.left(location.getName(), 8));
		//		toReturn = toReturn + "\n";
		//		for (Location location : analysis.getAllImageLocations())
		//		{
		//			toReturn = toReturn + String.format("%-28s", location.getName());
		//
		//			toReturn = toReturn + "\n";
		//		}
		// ???

		return toReturn;
	}
}
