/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.SpeciesEntry;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;
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
			List<ImageEntry> withLocation = new PredicateBuilder().locationOnly(location).query(images);
			ImageEntry firstEntry = analysis.getFirstImageInList(withLocation);
			ImageEntry lastEntry = analysis.getLastImageInList(withLocation);
			Calendar firstCal = DateUtils.toCalendar(firstEntry.getDateTaken());
			Calendar lastCal = DateUtils.toCalendar(lastEntry.getDateTaken());
			long currentDuration = SanimalAnalysisUtils.daysBetween(firstEntry.getDateTaken(), lastEntry.getDateTaken()) + 1;
			durationTotal = durationTotal + currentDuration;

			String speciesPresent = "";
			for (SpeciesEntry entry : firstEntry.getSpeciesPresent())
				speciesPresent = speciesPresent + entry.getSpecies().getName() + " ";

			toReturn = toReturn + String.format("%-27s %4s %2d %2d  %4s %2d %2d %9d   %4s %2d %2d  %s\n", location.getName(), firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH) + 1, firstCal.get(Calendar.DAY_OF_MONTH), lastCal.get(Calendar.YEAR), lastCal.get(Calendar.MONTH) + 1, lastCal.get(
					Calendar.DAY_OF_MONTH), currentDuration, firstCal.get(Calendar.YEAR), firstCal.get(Calendar.MONTH) + 1, firstCal.get(Calendar.DAY_OF_MONTH), speciesPresent);
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
			List<ImageEntry> withYear = new PredicateBuilder().yearOnly(year).query(images);
			List<Location> locations = analysis.locationsForImageList(withYear);
			if (!locations.isEmpty())
			{
				toReturn = toReturn + "Year " + year + "\n";
				int numLocations = locations.size();
				toReturn = toReturn + String.format("Location (%3d)              Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n", numLocations);

				int[] monthlyTotals = new int[12];

				for (Location location : locations)
				{
					List<ImageEntry> withYearLocation = new PredicateBuilder().locationOnly(location).query(withYear);
					Calendar firstCal = DateUtils.toCalendar(analysis.getFirstImageInList(withYearLocation).getDateTaken());
					Calendar lastCal = DateUtils.toCalendar(analysis.getLastImageInList(withYearLocation).getDateTaken());
					Integer firstMonth = firstCal.get(Calendar.MONTH);
					Integer lastMonth = lastCal.get(Calendar.MONTH);
					Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
					Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
					Calendar calendar = Calendar.getInstance();
					toReturn = toReturn + String.format("%-28s", location.getName());
					int monthTotal = 0;
					for (int i = 0; i < 12; i++)
					{
						int monthValue = 0;
						if (firstMonth == lastMonth && firstMonth == i)
							monthValue = lastDay - firstDay + 1;
						else if (firstMonth == i)
							monthValue = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDay + 1;
						else if (lastMonth == i)
							monthValue = lastDay;
						else if (firstMonth < i && lastMonth > i)
						{
							calendar.set(Calendar.MONTH, i);
							monthValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						}

						toReturn = toReturn + String.format(" %2d    ", monthValue);
						monthTotal = monthTotal + monthValue;
						monthlyTotals[i] = monthlyTotals[i] + monthValue;
					}
					toReturn = toReturn + monthTotal + "\n";
				}

				toReturn = toReturn + "Total days                  ";

				Integer totalTotal = 0;

				for (int i = 0; i < 12; i++)
				{
					totalTotal = totalTotal + monthlyTotals[i];
					toReturn = toReturn + String.format(" %2d    ", monthlyTotals[i]);
				}

				toReturn = toReturn + String.format("%2d", totalTotal);

				toReturn = toReturn + "\n";
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

		int[] monthlyTotals = new int[12];

		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new PredicateBuilder().locationOnly(location).query(images);
			Calendar firstCal = DateUtils.toCalendar(analysis.getFirstImageInList(withLocation).getDateTaken());
			Calendar lastCal = DateUtils.toCalendar(analysis.getLastImageInList(withLocation).getDateTaken());
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Calendar calendar = Calendar.getInstance();
			toReturn = toReturn + String.format("%-28s", location.getName());
			int monthTotal = 0;
			for (int i = 0; i < 12; i++)
			{
				int monthValue = 0;
				if (firstMonth == lastMonth && firstMonth == i)
					monthValue = lastDay - firstDay + 1;
				else if (firstMonth == i)
					monthValue = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) - firstDay + 1;
				else if (lastMonth == i)
					monthValue = lastDay;
				else if (firstMonth < i && lastMonth > i)
				{
					calendar.set(Calendar.MONTH, i);
					monthValue = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				}

				toReturn = toReturn + String.format(" %2d    ", monthValue);
				monthTotal = monthTotal + monthValue;
				monthlyTotals[i] = monthlyTotals[i] + monthValue;
			}
			toReturn = toReturn + monthTotal + "\n";
		}

		toReturn = toReturn + "Total days                  ";

		Integer totalTotal = 0;

		for (int i = 0; i < 12; i++)
		{
			totalTotal = totalTotal + monthlyTotals[i];
			toReturn = toReturn + String.format(" %2d    ", monthlyTotals[i]);
		}

		toReturn = toReturn + String.format("%2d", totalTotal);

		toReturn = toReturn + "\n\n";

		return toReturn;
	}
}
