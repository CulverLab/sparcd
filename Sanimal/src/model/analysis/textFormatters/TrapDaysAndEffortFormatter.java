/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.SpeciesEntry;
import model.analysis.DataAnalysis;
import model.analysis.SanimalAnalysisUtils;

public class TrapDaysAndEffortFormatter extends TextFormatter
{
	public TrapDaysAndEffortFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printCameraTrapDays()
	{
		String toReturn = "";

		toReturn = toReturn + "CAMERA TRAP DAYS\n";
		toReturn = toReturn + "Location                    Start date  Stop date   Duration   First pic   Species\n";

		long durationTotal = 0;
		for (Location location : analysis.getAllImageLocations())
		{
			ImageEntry firstEntry = analysis.getLocationToFirstImage().get(location);
			ImageEntry lastEntry = analysis.getLocationToLastImage().get(location);
			Calendar firstCal = DateUtils.toCalendar(firstEntry.getDateTaken());
			Calendar lastCal = DateUtils.toCalendar(lastEntry.getDateTaken());
			long currentDuration = SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), analysis.getImagesSortedByDate().get(analysis.getImagesSortedByDate().size() - 1).getDateTaken());
			durationTotal = durationTotal + currentDuration;

			String speciesPresent = "";
			for (SpeciesEntry entry : firstEntry.getSpeciesPresent())
				speciesPresent = speciesPresent + entry.getSpecies().getName() + " ";

			toReturn = toReturn + String.format("%-27s %4s %2d %2d  %4s %2d %2d %9d   %4s %2d %2d  %s\n", location.getName(), firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH), firstCal.get(Calendar.DAY_OF_MONTH), lastCal.get(Calendar.YEAR), lastCal.get(Calendar.MONTH), lastCal.get(
					Calendar.DAY_OF_MONTH), currentDuration, firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH), firstCal.get(Calendar.DAY_OF_MONTH), speciesPresent);
		}

		toReturn = toReturn + String.format("Total camera trap days                             %9d\n", durationTotal);

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printCameraTrapEffort()
	{
		String toReturn = "";

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

		return toReturn;
	}

	public String printCameraTrapEffortSummary()
	{
		String toReturn = "";

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

		return toReturn;
	}
}
