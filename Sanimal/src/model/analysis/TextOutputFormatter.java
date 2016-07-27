/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

public class TextOutputFormatter
{
	public String format(List<ImageEntry> images, Integer eventInterval)
	{
		String toReturn = "";

		if (images.isEmpty())
			return "No images found under directory";

		DataAnalysis analysis = new DataAnalysis(images, eventInterval);

		// location

		toReturn = toReturn + "LOCATIONS " + analysis.getAllImageLocations().size() + "\n";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + location.getName() + " ";
		if (analysis.nullLocationsFound())
			toReturn = toReturn + "Unknown ";
		toReturn = toReturn + "\n\n";

		// species

		toReturn = toReturn + "SPECIES " + analysis.getAllImageSpecies().size() + "\n";
		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + species.getName() + " ";
		toReturn = toReturn + "\n";

		// Image analysis header

		toReturn = toReturn + "FOR ALL SPECIES AT ALL LOCATIONS\n";
		toReturn = toReturn + "Number of pictures processed = " + images.size() + "\n";
		toReturn = toReturn + "Number of pictures used in activity calculation = \n";
		toReturn = toReturn + "Number of independent pictures used in analysis = \n";
		toReturn = toReturn + "Number of sequential pictures of same species at same location within a PERIOD = \n";
		toReturn = toReturn + "\n";

		// First/Last pic difference

		Date firstImageDate = analysis.getImagesSortedByDate().get(0).getDateTaken();
		Date lastImageDate = analysis.getImagesSortedByDate().get(analysis.getImagesSortedByDate().size() - 1).getDateTaken();

		toReturn = toReturn + "NUMBER OF DAYS IN CAMERA TRAP PROGRAM = " + daysBetween(firstImageDate, lastImageDate) + "\n";
		Calendar calendar = analysis.getCalendar(firstImageDate);
		toReturn = toReturn + "First picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		calendar.setTime(lastImageDate);
		toReturn = toReturn + "Last picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		toReturn = toReturn + "\n";

		// First pic of each species

		Map<Species, ImageEntry> speciesToFirstImage = analysis.getSpeciesToFirstImage();
		Map<Species, ImageEntry> speciesToLastImage = analysis.getSpeciesToLastImage();

		toReturn = toReturn + "FIRST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToFirstImage.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = analysis.getCalendar(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s\n", speciesToPrint, daysBetween(firstImageDate, dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH), dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR),
					dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()));
		}
		toReturn = toReturn + "\n";
		toReturn = toReturn + "LAST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location                   Duration\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToLastImage.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = analysis.getCalendar(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s %4d\n", speciesToPrint, daysBetween(firstImageDate, dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH), dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(
					Calendar.HOUR), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()), daysBetween(speciesToFirstImage.get(speciesToPrint).getDateTaken(), dateToPrint.getTime()));
		}

		toReturn = toReturn + "\n";

		// ACCUMULATION CURVE

		List<Map.Entry<Species, ImageEntry>> firstImageEntriesSorted = new ArrayList<Map.Entry<Species, ImageEntry>>(analysis.getSpeciesToFirstImage().entrySet());
		Collections.<Map.Entry<Species, ImageEntry>> sort(firstImageEntriesSorted, new Comparator<Map.Entry<Species, ImageEntry>>()
		{
			@Override
			public int compare(Map.Entry<Species, ImageEntry> entry1, Map.Entry<Species, ImageEntry> entry2)
			{
				return entry1.getValue().getDateTaken().compareTo(entry2.getValue().getDateTaken());
			}
		});

		toReturn = toReturn + "SPECIES ACCUMULATION CURVE\n";
		toReturn = toReturn + "  DAY    NUMBER    SPECIES\n";
		int number = 0;
		for (Map.Entry<Species, ImageEntry> entry : firstImageEntriesSorted)
			toReturn = toReturn + String.format("%5d     %3d      %s\n", daysBetween(firstImageDate, entry.getValue().getDateTaken()), ++number, entry.getKey().getName());

		toReturn = toReturn + "\n";

		// NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR

		// Jim's program added 1 to counts greater than 1 in activity count, fixed the issue
		// Jim's program added 1 to counts greater than 1 in period count, fixed the issue
		// Jim's program calculations for Abundance were completely wrong and made no sense
		// When running DataAnalyze, the last period can have X elements. The last period is being added to "Abundance" X times instead of once.
		// ALL = number of images containing the species
		// ACTIVITY = Number of periods containing at least one image in a single hour (ex. 1-1:59, 2-2:59, etc) 
		// PERIOD = Consecutive images that are less than "period" apart where period comes from user input
		// ABUNDANCE = Maximum number of animals photographed in a single image in each period

		toReturn = toReturn + "NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR\n";
		toReturn = toReturn + "        Year       All Activity   Period Abundance Locations\n";

		int imageTotal = 0;
		int activityTotal = 0;
		int periodTotal = 0;
		int abundanceTotal = 0;
		int locationTotal = 0;
		for (Integer year : analysis.getAllImageYears())
		{
			int yearImageTotal = 0;
			int yearActivityTotal = 0;
			int yearPeriodTotal = 0;
			int yearAbundanceTotal = 0;
			int yearLocationTotal = 0;
			for (Species species : analysis.getAllImageSpecies())
			{
				yearImageTotal = yearImageTotal + analysis.getYearToNumberImages().get(species).getOrDefault(year, -1);
				yearActivityTotal = yearActivityTotal + analysis.getYearToActivity().get(species).getOrDefault(year, -1);
				yearPeriodTotal = yearPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, -1);
				yearAbundanceTotal = yearAbundanceTotal + analysis.getYearToAbundance().get(species).getOrDefault(year, -1);
				yearLocationTotal = yearLocationTotal + analysis.getYearToLocations().get(species).getOrDefault(year, new ArrayList<Location>()).size();
			}
			imageTotal = imageTotal + yearImageTotal;
			activityTotal = activityTotal + yearActivityTotal;
			periodTotal = periodTotal + yearPeriodTotal;
			abundanceTotal = abundanceTotal + yearAbundanceTotal;
			locationTotal = locationTotal + yearLocationTotal;
			toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, yearImageTotal, yearActivityTotal, yearPeriodTotal, yearAbundanceTotal, yearLocationTotal);
		}
		toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", imageTotal, activityTotal, periodTotal, abundanceTotal, locationTotal);

		toReturn = toReturn + "\n";

		// NUMBER OF PICTURES BY SPECIES BY YEAR

		toReturn = toReturn + "NUMBER OF PICTURES BY SPECIES BY YEAR\n";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + "  " + species.getName() + "\n";
			toReturn = toReturn + "        Year       All Activity   Period Abundance Locations\n";
			int speciesImageTotal = 0;
			int speciesActivityTotal = 0;
			int speciesPeriodTotal = 0;
			int speciesAbundanceTotal = 0;
			int speciesLocationTotal = 0;
			for (Integer year : analysis.getYearToNumberImages().get(species).keySet())
			{
				speciesImageTotal = speciesImageTotal + analysis.getYearToNumberImages().get(species).getOrDefault(year, -1);
				speciesActivityTotal = speciesActivityTotal + analysis.getYearToActivity().get(species).getOrDefault(year, -1);
				speciesPeriodTotal = speciesPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, -1);
				speciesAbundanceTotal = speciesAbundanceTotal + analysis.getYearToAbundance().get(species).getOrDefault(year, -1);
				speciesLocationTotal = speciesLocationTotal + analysis.getYearToLocations().get(species).getOrDefault(year, new ArrayList<Location>()).size();
				toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, analysis.getYearToNumberImages().get(species).getOrDefault(year, -1), analysis.getYearToActivity().get(species).getOrDefault(year, -1), analysis.getYearToPeriod().get(species).getOrDefault(year, -1),
						analysis.getYearToAbundance().get(species).getOrDefault(year, -1), analysis.getYearToLocations().get(species).getOrDefault(year, new ArrayList<Location>()).size());
			}

			toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", speciesImageTotal, speciesActivityTotal, speciesPeriodTotal, speciesAbundanceTotal, speciesLocationTotal);
		}
		toReturn = toReturn + "\n";

		// SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL
		toReturn = toReturn + "SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL\n";

		toReturn = toReturn + "     Species                   Total  Percent\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer speciesPeriodTotal = 0;
			for (Integer year : analysis.getAllImageYears())
				speciesPeriodTotal = speciesPeriodTotal + analysis.getYearToPeriod().get(species).get(year);
			toReturn = toReturn + String.format("  %-28s %5d  %7.2f\n", species.getName(), speciesPeriodTotal, (speciesPeriodTotal.doubleValue() / periodTotal) * 100.0);
		}
		toReturn = toReturn + String.format("  Total pictures               %5d   100.00\n", periodTotal);
		toReturn = toReturn + "\n";

		// CAMERA TRAP DAYS

		toReturn = toReturn + "CAMERA TRAP DAYS\n";
		toReturn = toReturn + "Location                    Start date  Stop date   Duration   First pic   Species\n";

		long durationTotal = 0;
		for (Location location : analysis.getAllImageLocations())
		{
			ImageEntry firstEntry = analysis.getLocationToFirstImage().get(location);
			ImageEntry lastEntry = analysis.getLocationToLastImage().get(location);
			Calendar firstCal = analysis.getCalendar(firstEntry.getDateTaken());
			Calendar lastCal = analysis.getCalendar(lastEntry.getDateTaken());
			long currentDuration = daysBetween(firstImageDate, lastImageDate);
			durationTotal = durationTotal + currentDuration;

			String speciesPresent = "";
			for (SpeciesEntry entry : firstEntry.getSpeciesPresent())
				speciesPresent = speciesPresent + entry.getSpecies().getName() + " ";

			toReturn = toReturn + String.format("%-27s %4s %2d %2d  %4s %2d %2d %9d   %4s %2d %2d  %s\n", location.getName(), firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH), firstCal.get(Calendar.DAY_OF_MONTH), lastCal.get(Calendar.YEAR), lastCal.get(Calendar.MONTH), lastCal.get(
					Calendar.DAY_OF_MONTH), currentDuration, firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH), firstCal.get(Calendar.DAY_OF_MONTH), speciesPresent);
		}

		toReturn = toReturn + String.format("Total camera trap days                             %9d\n", durationTotal);

		toReturn = toReturn + "\n";

		//CAMERA TRAP EFFORT

		toReturn = toReturn + "CAMERA TRAP EFFORT\n";

		for (Integer year : analysis.getAllImageYears())
		{
			if (analysis.getYearToLocationList().get(year) != null)
			{
				toReturn = toReturn + "Year " + year + "\n";
				int numLocations = analysis.getYearToLocationList().get(year).size();
				toReturn = toReturn + String.format("Location (%3d)              Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n", numLocations);

				for (Map.Entry<Location, Set<Integer>[]> entry : analysis.getYearToLocationAndPicsPerMonth().get(year).entrySet())
				{
					toReturn = toReturn + String.format("%-28s", entry.getKey().getName());
					int monthTotal = 0;
					for (Set<Integer> monthValueSet : entry.getValue())
					{
						int monthValue = monthValueSet == null ? 0 : monthValueSet.size();
						toReturn = toReturn + String.format(" %2d    ", monthValue);
						monthTotal = monthTotal + monthValue;
					}
					toReturn = toReturn + monthTotal + "\n";
				}
			}

			toReturn = toReturn + "\n";
		}

		// CAMERA TRAP EFFORT SUMMARY

		toReturn = toReturn + "CAMERA TRAP EFFORT SUMMARY\n";
		int numYears = analysis.getAllImageYears().size();
		if (numYears != 0)
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "Location                    Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n";

		for (Map.Entry<Location, Set<Integer>[]> entry : analysis.getLocationToPicsPerMonth().entrySet())
		{
			toReturn = toReturn + String.format("%-28s", entry.getKey().getName());
			int monthTotal = 0;
			for (Set<Integer> monthValueSet : entry.getValue())
			{
				int monthValue = monthValueSet == null ? 0 : monthValueSet.size();
				toReturn = toReturn + String.format(" %2d    ", monthValue);
				monthTotal = monthTotal + monthValue;
			}
			toReturn = toReturn + monthTotal + "\n";
		}
		toReturn = toReturn + "\n";

		// FOR EACH LOCATION TOTAL NUMBER AND PERCENT OF EACH SPECIES
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

		// FOR EACH LOCATION AND MONTH TOTAL NUMBER EACH SPECIES
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

		// ALL LOCATIONS ALL SPECIES FOR EACH MONTH FOR ALL YEARS
		toReturn = toReturn + "ALL LOCATIONS ALL SPECIES FOR EACH MONTH FOR ALL YEARS\n";
		toReturn = toReturn + "  Use independent picture\n";

		numYears = analysis.getAllImageYears().size();
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
					double distance = this.distanceBetween(location.getLat(), location.getLng(), other.getLat(), other.getLng());
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
				double distance = this.distanceBetween(location.getLat(), location.getLng(), other.getLat(), other.getLng());
				toReturn = toReturn + String.format("%-28f", distance);
			}
			toReturn = toReturn + "\n";
		}
		toReturn = toReturn + "\n";

		// ACTIVITY PATTERNS

		toReturn = toReturn + "ACTIVITY PATTERNS\n";
		toReturn = toReturn + " Activity in one-hour segments - Species (Number of pictures in one hour segments/Total number of pics)\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			String toAdd = "";
			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			int totalImages = imagesWithSpecies.size();
			// Activity / All
			toAdd = toAdd + "                   All months         Jan              Feb              Mar              Apr              May              Jun              Jul              Aug              Sep              Oct              Nov              Dec\n";
			toAdd = toAdd + "    Hour        Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency\n";

			int[] totals = new int[13];

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
				toAdd = toAdd + String.format("%02d:00-%02d:00   ", i, i + 1);
				// 12 months
				for (int j = -1; j < 12; j++)
				{
					// -1 = all months
					if (j == -1)
					{
						double numImages = imagesWithSpeciesAtTime.size();
						if (numImages != 0)
							toAdd = toAdd + String.format("%6d %10.3f", (int) numImages, numImages / totalImages);
						else
							toAdd = toAdd + "                 ";
						totals[j + 1] = totals[j + 1] + (int) numImages;
					}
					else
					{
						double numImages = new PredicateBuilder().monthOnly(j).query(imagesWithSpeciesAtTime).size();
						if (numImages != 0)
							toAdd = toAdd + String.format("%6d %10.3f", (int) numImages, numImages / totalImages);
						else
							toAdd = toAdd + "                 ";
						totals[j + 1] = totals[j + 1] + (int) numImages;
					}
				}
				toAdd = toAdd + "\n";
			}

			toAdd = toAdd + "Total         ";

			for (int i = 0; i < totals.length; i++)
				toAdd = toAdd + String.format("%6d    100.000", totals[i]);

			toAdd = toAdd + "\n";

			// Print the header first
			toReturn = toReturn + String.format("%-28s (%6d/ %6d)\n", species.getName(), totals[0], totalImages);

			toReturn = toReturn + toAdd;

			toReturn = toReturn + "\n";
		}

		// SPECIES PAIRS ACTIVITY SIMILARITY (LOWER IS MORE SIMILAR)

		toReturn = toReturn + "SPECIES PAIRS ACTIVITY SIMILARITY (LOWER IS MORE SIMILAR)\n";

		toReturn = toReturn + "                            ";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-8s ", StringUtils.left(species.getName(), 8));
		}

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-27s", species.getName());
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
				List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(images);
				int totalImages = imagesWithSpecies.size();
				int totalImagesOther = imagesWithSpeciesOther.size();

				double activitySimilarity = 0;

				// 24 hrs
				for (int i = 0; i < 24; i++)
				{
					List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
					List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
					double numImages = imagesWithSpeciesAtTime.size();
					double numImagesOther = imagesWithSpeciesAtTimeOther.size();
					double frequency = numImages / totalImages;
					double frequencyOther = numImagesOther / totalImagesOther;
					double difference = frequency - frequencyOther;
					// Frequency squared
					activitySimilarity = activitySimilarity + difference * difference;
				}

				activitySimilarity = Math.sqrt(activitySimilarity);

				toReturn = toReturn + String.format("%6.3f   ", activitySimilarity);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		// SPECIES PAIR MOST SIMILAR IN ACTIVITY (FREQUENCY)
		toReturn = toReturn + "SPECIES PAIR MOST SIMILAR IN ACTIVITY (FREQUENCY)\n";
		toReturn = toReturn + "  Consider those species with 25 or more pictures\n";

		Species lowest = null;
		Species lowestOther = null;
		double lowestFrequency = Double.MAX_VALUE;

		for (Species species : analysis.getAllImageSpecies())
		{
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
				List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(images);
				int totalImages = imagesWithSpecies.size();
				int totalImagesOther = imagesWithSpeciesOther.size();
				double activitySimilarity = 0;

				if (totalImages >= 25 && totalImagesOther >= 25 && !species.equals(other))
				{
					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					activitySimilarity = Math.sqrt(activitySimilarity);

					if (lowestFrequency >= activitySimilarity)
					{
						lowestFrequency = activitySimilarity;
						lowest = species;
						lowestOther = other;
					}
				}
			}
		}

		if (lowest != null)
		{
			toReturn = toReturn + String.format("Hour            %-28s %-28s\n", lowest.getName(), lowestOther.getName());

			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(lowest).query(images);
			List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(lowestOther).query(images);
			int totalImages = imagesWithSpecies.size();
			int totalImagesOther = imagesWithSpeciesOther.size();
			double activitySimilarity = 0;

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
				List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
				double numImages = imagesWithSpeciesAtTime.size();
				double numImagesOther = imagesWithSpeciesAtTimeOther.size();
				double frequency = numImages / totalImages;
				double frequencyOther = numImagesOther / totalImagesOther;
				double difference = frequency - frequencyOther;
				// Frequency squared
				activitySimilarity = activitySimilarity + difference * difference;

				toReturn = toReturn + String.format("%02d:00-%02d:00     %5.3f                        %5.3f\n", i, i + 1, frequency, frequencyOther);
			}
		}

		toReturn = toReturn + "\n";

		// CHI-SQUARE ANALYSIS OF PAIRED ACTIVITY PATTERNS

		toReturn = toReturn + "CHI-SQUARE ANALYSIS OF PAIRED ACTIVITY PATTERNS\n";
		toReturn = toReturn + "  H0: Species A and B have similar activity patterns at 95%\n";
		toReturn = toReturn + "  Significant = X, Not significant = Blank\n";
		toReturn = toReturn + "  Consider only species with >= 25 pictures\n";

		toReturn = toReturn + "                            ";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-8s ", StringUtils.left(species.getName(), 8));
		}

		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			int totalImages = imagesWithSpecies.size();
			if (totalImages >= 25)
			{
				toReturn = toReturn + String.format("%-28s", species.getName());
				for (Species other : analysis.getAllImageSpecies())
				{
					List<ImageEntry> imagesWithSpeciesOther = new PredicateBuilder().speciesOnly(other).query(images);
					int totalImagesOther = imagesWithSpeciesOther.size();
					double activitySimilarity = 0;

					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					double chiSquare = (1 - activitySimilarity) / 1.0;

					if (chiSquare >= 0.95)
						toReturn = toReturn + "   X     ";
					else
						toReturn = toReturn + "         ";
				}
				toReturn = toReturn + "\n";
			}
		}

		toReturn = toReturn + "\n";

		// LUNAR ACTIVITY PATTERN

		toReturn = toReturn + "LUNAR ACTIVITY PATTERN\n";
		toReturn = toReturn + "  New and full moon +/- 5 days activity patterns\n";
		toReturn = toReturn + "  Difference (large is greater difference)\n";

		List<ImageEntry> imagesFull = new PredicateBuilder().fullMoonOnly(analysis.getFullMoons()).query(images);
		List<ImageEntry> imagesNew = new PredicateBuilder().newMoonOnly(analysis.getNewMoons()).query(images);
		List<LunarActivityEntry> lunarActivities = new ArrayList<LunarActivityEntry>();

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";
			toReturn = toReturn + "                 Full moon activity    New moon activity\n";
			toReturn = toReturn + "    Hour        Number    Frequency   Number    Frequency\n";

			List<ImageEntry> imagesWithSpeciesFull = new PredicateBuilder().speciesOnly(species).query(imagesFull);
			int totalImagesFull = imagesWithSpeciesFull.size();

			List<ImageEntry> imagesWithSpeciesNew = new PredicateBuilder().speciesOnly(species).query(imagesNew);
			int totalImagesNew = imagesWithSpeciesNew.size();

			int numImagesTotalFull = 0;
			int numImagesTotalNew = 0;

			double totalDifference = 0;

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTimeFull = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesFull);
				int numImagesFull = imagesWithSpeciesAtTimeFull.size();
				double frequencyFull = 0;
				if (totalImagesFull != 0)
					frequencyFull = (double) numImagesFull / totalImagesFull;
				numImagesTotalFull = numImagesTotalFull + numImagesFull;

				List<ImageEntry> imagesWithSpeciesAtTimeNew = new PredicateBuilder().timeFrame(i, i + 1).query(imagesWithSpeciesNew);
				int numImagesNew = imagesWithSpeciesAtTimeNew.size();
				double frequencyNew = 0;
				if (totalImagesNew != 0)
					frequencyNew = (double) numImagesNew / totalImagesNew;
				numImagesTotalNew = numImagesTotalNew + numImagesNew;

				double difference = frequencyFull - frequencyNew;
				totalDifference = totalDifference + difference * difference;

				toReturn = toReturn + String.format("%02d:00-%02d:00      %5d      %5.3f      %5d      %5.3f\n", i, i + 1, numImagesFull, frequencyFull, numImagesNew, frequencyNew);
			}

			totalDifference = Math.sqrt(totalDifference);

			lunarActivities.add(new LunarActivityEntry(species, totalDifference, numImagesTotalFull + numImagesTotalNew));

			toReturn = toReturn + String.format("Total            %5d                 %5d\n", numImagesTotalFull, numImagesTotalNew);
			toReturn = toReturn + String.format("Difference       %5.2f\n", totalDifference);

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		//SPECIES LUNAR ACTIVITY MOST DIFFERENT: 

		if (!lunarActivities.isEmpty())
		{
			Collections.sort(lunarActivities, new Comparator<LunarActivityEntry>()
			{
				@Override
				public int compare(LunarActivityEntry entry1, LunarActivityEntry entry2)
				{
					return entry2.getDifference().compareTo(entry1.getDifference());
				}
			});

			toReturn = toReturn + "SPECIES LUNAR ACTIVITY MOST DIFFERENT: ";

			toReturn = toReturn + lunarActivities.get(0).getSpecies().getName() + "\n";

			toReturn = toReturn + "\nSpecies                   Difference Number of records\n";
			for (LunarActivityEntry entry : lunarActivities)
				toReturn = toReturn + String.format("%-28s %4.2f      %7d\n", entry.getSpecies(), entry.getDifference(), entry.getNumRecords());
			toReturn = toReturn + "\n";
		}

		// ACTIVITY PATTERNS BY SEASON

		toReturn = toReturn + "ACTIVITY PATTERNS BY SEASON\n";
		toReturn = toReturn + "  Activity in one-hour segments by season\n";

		int[][] seasons = new int[][]
		{
				{ 11, 0, 1 }, // 1
				{ 2, 3, 4 }, // 2
				{ 5, 6, 7 }, // 3
				{ 8, 9, 10 } }; // 4

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			List<ImageEntry> withSpeciesSorted = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());

			toReturn = toReturn + species.getName() + "\n";
			toReturn = toReturn + "                     Dec-Jan-Feb           Mar-Apr-May           Jun-Jul-Aug           Sep-Oct-Nov\n";
			toReturn = toReturn + String.format("Camera trap days    ");
			int[] lengthPerSeason = new int[4];
			for (int i = 0; i < 4; i++)
			{
				List<ImageEntry> seasonWithSpecies = new PredicateBuilder().monthOnly(seasons[i]).query(analysis.getImagesSortedByDate());
				long difference = 0;
				if (!seasonWithSpecies.isEmpty())
				{
					Date first = seasonWithSpecies.get(0).getDateTaken();
					Date last = seasonWithSpecies.get(seasonWithSpecies.size() - 1).getDateTaken();
					difference = daysBetween(first, last);
				}
				lengthPerSeason[i] = (int) difference;
				toReturn = toReturn + String.format("%7d               ", difference);
			}
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Number of pictures  ";
			int[] imagesPerSeason = new int[4];
			for (int i = 0; i < 4; i++)
			{
				List<ImageEntry> seasonWithSpecies = new PredicateBuilder().monthOnly(seasons[i]).query(withSpecies);
				toReturn = toReturn + String.format("%7d               ", seasonWithSpecies.size());
				imagesPerSeason[i] = seasonWithSpecies.size();
			}
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Pictures/Effort        ";
			double total = 0;
			double ratios[] = new double[4];
			for (int i = 0; i < 4; i++)
			{
				double currentRatio = 0;
				if (lengthPerSeason[i] != 0)
					currentRatio = (double) imagesPerSeason[i] / lengthPerSeason[i];
				toReturn = toReturn + String.format("%5.4f                ", currentRatio);
				ratios[i] = currentRatio;
				total = total + currentRatio;
			}
			toReturn = toReturn + "\n";
			toReturn = toReturn + "Visitation proportion  ";
			for (int i = 0; i < 4; i++)
			{
				if (total != 0)
					toReturn = toReturn + String.format("%5.4f                ", ratios[i] / total);
				else
					toReturn = toReturn + String.format("%5.4f                ", 0);
			}

			toReturn = toReturn + "\n";

			String toAdd = "";

			toAdd = toAdd + "           Hour        Number      Freq      Number      Freq      Number      Freq      Number      Freq\n";

			int[] hourlyTotals = new int[4];

			// 24 hrs
			for (int j = 0; j < 24; j++)
			{
				List<ImageEntry> withSpeciesAtTime = new PredicateBuilder().timeFrame(j, j + 1).query(withSpecies);

				toAdd = toAdd + String.format("       %02d:00-%02d:00    ", j, j + 1);

				// 4 seasons
				for (int i = 0; i < 4; i++)
				{
					List<ImageEntry> withSpeciesAtTimeInSeason = new PredicateBuilder().monthOnly(seasons[i]).query(withSpeciesAtTime);
					List<ImageEntry> withSpeciesInSeason = new PredicateBuilder().monthOnly(seasons[i]).query(withSpecies);
					int numPics = withSpeciesAtTimeInSeason.size();
					int totalPics = withSpeciesInSeason.size();
					double frequency = 0;
					if (totalPics != 0)
						frequency = (double) numPics / totalPics;
					else
						frequency = 0;

					hourlyTotals[i] = hourlyTotals[i] + numPics;

					toAdd = toAdd + String.format("%5d        %5.3f    ", numPics, frequency);
				}

				toAdd = toAdd + "\n";
			}

			toAdd = toAdd + "       Hourly pics  ";
			for (int i = 0; i < hourlyTotals.length; i++)
				toAdd = toAdd + String.format("%7d               ", hourlyTotals[i]);

			toAdd = toAdd + "\n";

			toReturn = toReturn + toAdd + "\n";
		}

		// SPECIES ABUNDANCE

		toReturn = toReturn + "SPECIES ABUNDANCE\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "SPECIES                      NUMBER PICS      RELATIVE ABUNDANCE     AVG NUM INDIVS     ABUNDANCE OF INDIVS\n";
		Integer periodOverAllSpecies = 0;
		Integer numAnimalsPhotographed = 0;
		for (Map<Integer, Integer> map : analysis.getYearToActivity().values())
			for (Integer integer : map.values())
				periodOverAllSpecies = periodOverAllSpecies + integer;
		for (Species species : analysis.getAllImageSpecies())
			for (Integer abundance : analysis.getYearToAbundance().get(species).values())
				numAnimalsPhotographed = numAnimalsPhotographed + abundance;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> picsWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			Map<Integer, Integer> yearToAbundance = analysis.getYearToAbundance().get(species);
			Map<Integer, Integer> yearToPeriod = analysis.getYearToPeriod().get(species);
			abundanceTotal = 0;
			periodTotal = 0;
			for (Integer abundance : yearToAbundance.values())
				abundanceTotal = abundanceTotal + abundance;
			for (Integer period : yearToPeriod.values())
				periodTotal = periodTotal + period;
			toReturn = toReturn + String.format("%-28s %7d               %7.2f             %7.2f             %7.2f\n", species.getName(), periodTotal, 100.0D * (double) periodTotal / periodOverAllSpecies, (double) abundanceTotal / periodTotal, (double) abundanceTotal / numAnimalsPhotographed * 100);
		}
		toReturn = toReturn + String.format("Total                        %7d                100.00", periodOverAllSpecies);

		toReturn = toReturn + "\n\n";

		// LOCATIONS BY SPECIES AND LOCATION AND SPECIES RICHNESS
		toReturn = toReturn + "LOCATIONS BY SPECIES AND LOCATION AND SPECIES RICHNESS\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "Location                          ";
		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + String.format("%-6s ", StringUtils.left(species.getName(), 6));
		toReturn = toReturn + "Rich\n";

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s       ", location.getName());
			List<ImageEntry> imagesAtLoc = new PredicateBuilder().locationOnly(location).query(images);

			Integer horizontalRichness = 0;
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesAtLocWithSpecies = new PredicateBuilder().speciesOnly(species).query(imagesAtLoc);
				horizontalRichness = horizontalRichness + (imagesAtLocWithSpecies.size() == 0 ? 0 : 1);
				toReturn = toReturn + String.format("%5d  ", imagesAtLocWithSpecies.size());
			}
			toReturn = toReturn + String.format("%5d  ", horizontalRichness);

			toReturn = toReturn + "\n";
		}
		toReturn = toReturn + "Richness                           ";

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer richness = 0;
			List<ImageEntry> imagesWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesWithSpeciesAtLoc = new PredicateBuilder().locationOnly(location).query(imagesWithSpecies);
				richness = richness + (imagesWithSpeciesAtLoc.size() == 0 ? 0 : 1);
			}
			toReturn = toReturn + String.format("%5d  ", richness);
		}
		toReturn = toReturn + "\n";

		toReturn = toReturn + "\n";

		// LOCATION SPECIES FREQUENCY SIMILARITY (LOWER IS MORE SIMILAR)
		toReturn = toReturn + "LOCATION SPECIES FREQUENCY SIMILARITY (LOWER IS MORE SIMILAR)\n";
		toReturn = toReturn + "   One picture of each species per camera per PERIOD\n";
		toReturn = toReturn + "   Square root of sums of squared difference in frequency\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES FREQUENCY\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES FREQUENCY\n\n";
		// ???

		// LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)
		toReturn = toReturn + "LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)\n";
		toReturn = toReturn + "  Is species present at this location? yes=1, no=0\n";
		toReturn = toReturn + "  1.00 means locations are identical; 0.00 means locations have no species in common\n";
		toReturn = toReturn + "  Location, location, JSI, number of species at each location, and number of species in common\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES COMPOSITION\n\n";
		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES COMPOSITION\n\n";

		// SPECIES BY LOCATION WITH UTM AND ELEVATION
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

		// SPECIES OVERLAP AT LOCATIONS

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

		// CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES
		toReturn = toReturn + "CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES\n";
		toReturn = toReturn + "  H0: Species frequencies are independent of site\n";
		toReturn = toReturn + "  Reject null hypothesis = R, Accept null hypothesis = -\n";
		toReturn = toReturn + "Sites                      ";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%-8s", StringUtils.left(location.getName(), 8));
		toReturn = toReturn + "\n";
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s", location.getName());

			toReturn = toReturn + "\n";
		}

		// PICTURES FOR EACH LOCATION BY MONTH AND YEAR
		toReturn = toReturn + "PICTURES FOR EACH LOCATION BY MONTH AND YEAR\n";
		toReturn = toReturn + "  Number of independent pictures per location\n";
		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + year + "\n";
			toReturn = toReturn + "Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesByLocationAndYear = new PredicateBuilder().yearOnly(year).locationOnly(location).query(images);
				toReturn = toReturn + String.format("%-28s", location.getName());
				Integer total = 0;
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesByLocationYearAndMonth = new PredicateBuilder().monthOnly(i).query(imagesByLocationAndYear);
					total = total + imagesByLocationYearAndMonth.size();
					toReturn = toReturn + String.format("%5d ", imagesByLocationYearAndMonth.size());
				}
				toReturn = toReturn + String.format("%7d", total);
				toReturn = toReturn + "\n";
			}
			toReturn = toReturn + "Total pictures              ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByYearAndMonth = new PredicateBuilder().monthOnly(i).yearOnly(year).query(images);
				totalPic = totalPic + imagesByYearAndMonth.size();
				totalPics[i] = imagesByYearAndMonth.size();
				toReturn = toReturn + String.format("%5d ", imagesByYearAndMonth.size());
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                     ";
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			ImageEntry first = yearsPics.get(0);
			ImageEntry last = yearsPics.get(yearsPics.size() - 1);
			Calendar firstCal = analysis.getCalendar(first.getDateTaken());
			Calendar lastCal = analysis.getCalendar(last.getDateTaken());
			Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			int[] daysUsed = new int[12];
			if (firstMonth == lastMonth)
				daysUsed[firstMonth] = lastDay - firstDay + 1;
			else
			{
				daysUsed[firstMonth] = firstDaysInMonth - (firstDay - 1);
				firstMonth++;
				while (firstMonth < lastMonth)
				{
					calendar.set(year, firstMonth, 1);
					daysUsed[firstMonth] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					firstMonth++;
				}
				daysUsed[lastMonth] = lastDay;
			}
			Integer totalDays = 0;
			for (Integer month : daysUsed)
			{
				toReturn = toReturn + String.format("%2d    ", month);
				totalDays = totalDays + month;
			}

			toReturn = toReturn + String.format(" %3d", totalDays);

			toReturn = toReturn + "\n";

			toReturn = toReturn + "Pictures/day               ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : ((double) totalPics[i] / daysUsed[i]));
			}
			toReturn = toReturn + String.format("  %6.2f", totalDays == 0 ? 0 : ((double) totalPic / totalDays));
		}

		toReturn = toReturn + "\n\n";

		// PICTURES FOR EACH LOCATION BY MONTH AND YEAR SUMMARY
		toReturn = toReturn + "PICTURES FOR EACH LOCATION BY MONTH AND YEAR SUMMARY\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s", location.getName());

			List<ImageEntry> imagesAtLoc = new PredicateBuilder().locationOnly(location).query(images);

			Integer picsInYear = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesAtLocWithMonth = new PredicateBuilder().monthOnly(i).query(imagesAtLoc);
				picsInYear = picsInYear + imagesAtLocWithMonth.size();
				toReturn = toReturn + String.format("%5d ", imagesAtLocWithMonth.size());
			}

			toReturn = toReturn + String.format("  %5d", picsInYear);

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures              ";

		Integer totalPic = 0;
		int[] totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			List<ImageEntry> imagesByYearAndMonth = new PredicateBuilder().monthOnly(i).query(images);
			totalPic = totalPic + imagesByYearAndMonth.size();
			totalPics[i] = imagesByYearAndMonth.size();
			toReturn = toReturn + String.format("%5d ", imagesByYearAndMonth.size());
		}
		toReturn = toReturn + String.format("%7d", totalPic);
		toReturn = toReturn + "\n";

		toReturn = toReturn + "Total days                     ";

		int[] daysUsed = new int[]
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			ImageEntry first = yearsPics.get(0);
			ImageEntry last = yearsPics.get(yearsPics.size() - 1);
			Calendar firstCal = analysis.getCalendar(first.getDateTaken());
			Calendar lastCal = analysis.getCalendar(last.getDateTaken());
			Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			if (firstMonth == lastMonth)
				daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
			else
			{
				daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
				firstMonth++;
				while (firstMonth < lastMonth)
				{
					calendar.set(year, firstMonth, 1);
					daysUsed[firstMonth] = daysUsed[firstMonth] + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					firstMonth++;
				}
				daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
			}
		}
		Integer totalDays = 0;
		for (Integer month : daysUsed)
		{
			toReturn = toReturn + String.format("%2d    ", month);
			totalDays = totalDays + month;
		}

		toReturn = toReturn + String.format(" %3d", totalDays);

		toReturn = toReturn + "\n";

		toReturn = toReturn + "Pictures/day               ";

		for (int i = 0; i < 12; i++)
		{
			toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : ((double) totalPics[i] / daysUsed[i]));
		}
		toReturn = toReturn + String.format("  %6.2f", totalDays == 0 ? 0 : ((double) totalPic / totalDays));

		toReturn = toReturn + "\n\n";

		// SPECIES AND SPECIES RICHNESS BY YEAR AND MONTH
		toReturn = toReturn + "SPECIES AND SPECIES RICHNESS BY YEAR AND MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + year + "\n";
			toReturn = toReturn + "Species                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			int[] totalRichness = new int[12];
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesBySpeciesAndYear = new PredicateBuilder().yearOnly(year).speciesOnly(species).query(images);
				toReturn = toReturn + String.format("%-28s", species.getName());
				Integer total = 0;
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesBySpeciesYearAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesAndYear);
					total = total + imagesBySpeciesYearAndMonth.size();
					toReturn = toReturn + String.format("%5d ", imagesBySpeciesYearAndMonth.size());
					totalRichness[i] = totalRichness[i] + (imagesBySpeciesYearAndMonth.isEmpty() ? 0 : 1);
				}
				toReturn = toReturn + String.format("%7d", total);
				toReturn = toReturn + "\n";
			}
			toReturn = toReturn + "Total pictures              ";

			totalPic = 0;
			totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByYearAndMonth = new PredicateBuilder().monthOnly(i).yearOnly(year).query(images);
				totalPic = totalPic + imagesByYearAndMonth.size();
				totalPics[i] = imagesByYearAndMonth.size();
				toReturn = toReturn + String.format("%5d ", imagesByYearAndMonth.size());
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                     ";
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			ImageEntry first = yearsPics.get(0);
			ImageEntry last = yearsPics.get(yearsPics.size() - 1);
			Calendar firstCal = analysis.getCalendar(first.getDateTaken());
			Calendar lastCal = analysis.getCalendar(last.getDateTaken());
			Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			daysUsed = new int[12];
			if (firstMonth == lastMonth)
				daysUsed[firstMonth] = lastDay - firstDay + 1;
			else
			{
				daysUsed[firstMonth] = firstDaysInMonth - (firstDay - 1);
				firstMonth++;
				while (firstMonth < lastMonth)
				{
					calendar.set(year, firstMonth, 1);
					daysUsed[firstMonth] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					firstMonth++;
				}
				daysUsed[lastMonth] = lastDay;
			}
			totalDays = 0;
			for (Integer month : daysUsed)
			{
				toReturn = toReturn + String.format("%2d    ", month);
				totalDays = totalDays + month;
			}

			toReturn = toReturn + String.format(" %3d", totalDays);

			toReturn = toReturn + "\n";

			toReturn = toReturn + "10*Pic/effort              ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
			}

			toReturn = toReturn + "\n";

			toReturn = toReturn + "Species richness            ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%5d ", totalRichness[i]);
			}
		}

		toReturn = toReturn + "\n\n";

		// SPECIES ALL YEARS BY MONTH
		toReturn = toReturn + "SPECIES ALL YEARS BY MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "Species                       Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";
		int[] totalRichness = new int[12];
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());

			List<ImageEntry> imagesBySpecies = new PredicateBuilder().speciesOnly(species).query(images);
			Integer total = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesBySpeciesAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpecies);
				total = total + imagesBySpeciesAndMonth.size();
				toReturn = toReturn + String.format("%5d ", imagesBySpeciesAndMonth.size());
				totalRichness[i] = totalRichness[i] + (imagesBySpeciesAndMonth.isEmpty() ? 0 : 1);
			}
			toReturn = toReturn + String.format("%7d", total);

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures              ";

		totalPic = 0;
		totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			List<ImageEntry> imagesByMonth = new PredicateBuilder().monthOnly(i).query(images);
			totalPic = totalPic + imagesByMonth.size();
			totalPics[i] = imagesByMonth.size();
			toReturn = toReturn + String.format("%5d ", imagesByMonth.size());
		}
		toReturn = toReturn + String.format("%7d", totalPic);
		toReturn = toReturn + "\n";

		toReturn = toReturn + "Total days                     ";

		daysUsed = new int[]
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			ImageEntry first = yearsPics.get(0);
			ImageEntry last = yearsPics.get(yearsPics.size() - 1);
			Calendar firstCal = analysis.getCalendar(first.getDateTaken());
			Calendar lastCal = analysis.getCalendar(last.getDateTaken());
			Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			if (firstMonth == lastMonth)
				daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
			else
			{
				daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
				firstMonth++;
				while (firstMonth < lastMonth)
				{
					calendar.set(year, firstMonth, 1);
					daysUsed[firstMonth] = daysUsed[firstMonth] + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					firstMonth++;
				}
				daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
			}
		}
		totalDays = 0;
		for (Integer month : daysUsed)
		{
			toReturn = toReturn + String.format("%2d    ", month);
			totalDays = totalDays + month;
		}

		toReturn = toReturn + String.format(" %3d", totalDays);

		toReturn = toReturn + "\n";

		toReturn = toReturn + "10*Pic/effort              ";

		for (int i = 0; i < 12; i++)
		{
			toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
		}

		toReturn = toReturn + "\n";

		toReturn = toReturn + "Species richness            ";

		for (int i = 0; i < 12; i++)
		{
			toReturn = toReturn + String.format("%5d ", totalRichness[i]);
		}

		toReturn = toReturn + "\n\n";

		// SPECIES BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION
		toReturn = toReturn + "SPECIES BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";

			for (Integer year : analysis.getAllImageYears())
			{
				toReturn = toReturn + year + "\n";

				toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> imagesBySpeciesLocationAndYear = new PredicateBuilder().yearOnly(year).locationOnly(location).speciesOnly(species).query(images);
					toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesBySpeciesYearLocationAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesLocationAndYear);
						total = total + imagesBySpeciesYearLocationAndMonth.size();
						toReturn = toReturn + String.format("%5d ", imagesBySpeciesYearLocationAndMonth.size());
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
				toReturn = toReturn + "Total pictures                     ";

				totalPic = 0;
				totalPics = new int[12];
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesByYearSpeciesAndMonth = new PredicateBuilder().speciesOnly(species).monthOnly(i).yearOnly(year).query(images);
					totalPic = totalPic + imagesByYearSpeciesAndMonth.size();
					totalPics[i] = imagesByYearSpeciesAndMonth.size();
					toReturn = toReturn + String.format("%5d ", imagesByYearSpeciesAndMonth.size());
				}
				toReturn = toReturn + String.format("%7d", totalPic);
				toReturn = toReturn + "\n";

				toReturn = toReturn + "Total days                            ";
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

				ImageEntry first = yearsPics.get(0);
				ImageEntry last = yearsPics.get(yearsPics.size() - 1);
				Calendar firstCal = analysis.getCalendar(first.getDateTaken());
				Calendar lastCal = analysis.getCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				daysUsed = new int[12];
				if (firstMonth == lastMonth)
					daysUsed[firstMonth] = lastDay - firstDay + 1;
				else
				{
					daysUsed[firstMonth] = firstDaysInMonth - (firstDay - 1);
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						daysUsed[firstMonth] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					daysUsed[lastMonth] = lastDay;
				}
				totalDays = 0;
				for (Integer month : daysUsed)
				{
					toReturn = toReturn + String.format("%2d    ", month);
					totalDays = totalDays + month;
				}

				toReturn = toReturn + String.format(" %3d", totalDays);

				toReturn = toReturn + "\n";

				toReturn = toReturn + "10*Pic/effort                     ";

				for (int i = 0; i < 12; i++)
				{
					toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
				}

			}
			toReturn = toReturn + "\n\n";

			toReturn = toReturn + "SUMMARY ALL YEARS\n";

			if (!analysis.getAllImageYears().isEmpty())
				toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

			toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesBySpeciesLocation = new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images);
				toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
				Integer total = 0;
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesBySpeciesLocationAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesLocation);
					total = total + imagesBySpeciesLocationAndMonth.size();
					toReturn = toReturn + String.format("%5d ", imagesBySpeciesLocationAndMonth.size());
				}
				toReturn = toReturn + String.format("%7d", total);
				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "Total pictures                     ";

			totalPic = 0;
			totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByMonthSpecies = new PredicateBuilder().speciesOnly(species).monthOnly(i).query(images);
				totalPic = totalPic + imagesByMonthSpecies.size();
				totalPics[i] = imagesByMonthSpecies.size();
				toReturn = toReturn + String.format("%5d ", imagesByMonthSpecies.size());
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                            ";

			daysUsed = new int[]
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

				ImageEntry first = yearsPics.get(0);
				ImageEntry last = yearsPics.get(yearsPics.size() - 1);
				Calendar firstCal = analysis.getCalendar(first.getDateTaken());
				Calendar lastCal = analysis.getCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				if (firstMonth == lastMonth)
					daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
				else
				{
					daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						daysUsed[firstMonth] = daysUsed[firstMonth] + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
				}
			}
			totalDays = 0;
			for (Integer month : daysUsed)
			{
				toReturn = toReturn + String.format("%2d    ", month);
				totalDays = totalDays + month;
			}

			toReturn = toReturn + String.format(" %3d", totalDays);

			toReturn = toReturn + "\n";

			toReturn = toReturn + "10*Pic/effort                     ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
			}
			toReturn = toReturn + "\n";

			toReturn = toReturn + "\n";
		}

		// SPECIES ABUNDANCE BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION

		return toReturn;

	}

	private long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}

	private double distanceBetween(double lat1, double lng1, double lat2, double lng2)
	{
		//		var lat1Rad = lat1.toRadians(), lat2Rad = lat2.toRadians(), delta = (lon2-lon1).toRadians(), R = 6371e3; // gives d in metres
		//	    var d = Math.acos( Math.sin(lat1Rad)*Math.sin(lat2Rad) + Math.cos(lat1Rad)*Math.cos(lat2Rad) * Math.cos(delta) ) * R;
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double delta = Math.toRadians(lng2 - lng1);
		double R = 6371.000;
		return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(delta)) * R;
	}
}
