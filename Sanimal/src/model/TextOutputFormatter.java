/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			toReturn = toReturn + "Year " + year + "\n";
			toReturn = toReturn + String.format("Location (%3d)              Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n", analysis.getYearToLocationList().get(year).size());

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
				int imagesAtLoc = new ImageQuery(images).locationOnly(location).anyValidSpecies().query().size();
				List<ImageEntry> filtered = new ImageQuery(images).locationOnly(location).speciesOnly(species).query();
				toReturn = toReturn + String.format("%5d %7.2f                   ", filtered.size(), (filtered.size() / (double) imagesAtLoc) * 100);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures            ";

		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%5d  100.00                   ", new ImageQuery(images).locationOnly(location).query().size());

		toReturn = toReturn + "\n";

		return toReturn;
	}

	private long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}
}
