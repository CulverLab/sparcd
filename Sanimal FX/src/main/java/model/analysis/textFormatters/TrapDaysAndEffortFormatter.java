package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.location.Location;
import model.species.SpeciesEntry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The text formatter for trap days and effort calculations
 * 
 * @author David Slovikosky
 */
public class TrapDaysAndEffortFormatter extends TextFormatter
{
	public TrapDaysAndEffortFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A list of all locations (Location) to be analyzed that includes the state and stop date, the total number of days each location was run
	 * (Duration), the date of the first picture recorded at the location (First pic), and the species recorded. This is followed by the total number
	 * of Camera trap days (Duration).
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printCameraTrapDays()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("CAMERA TRAP DAYS\n");
		toReturn.append("Location                    Start date  Stop date   Duration   First pic   Species\n");

		long durationTotal = 0;
		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new ImageQuery().locationOnly(location).query(images);
			ImageEntry firstEntry = analysis.getFirstImageInList(withLocation);
			ImageEntry lastEntry = analysis.getLastImageInList(withLocation);
			LocalDateTime firstCal = firstEntry.getDateTaken();
			LocalDateTime lastCal = lastEntry.getDateTaken();
			long currentDuration = SanimalAnalysisUtils.daysBetween(firstEntry.getDateTaken(), lastEntry.getDateTaken()) + 1;
			durationTotal = durationTotal + currentDuration;

			StringBuilder speciesPresent = new StringBuilder();
			for (SpeciesEntry entry : firstEntry.getSpeciesPresent())
				speciesPresent.append(entry.getSpecies().getCommonName()).append(" ");

			toReturn.append(String.format("%-27s %4s %2d %2d  %4s %2d %2d %9d   %4s %2d %2d  %s\n", location.getName(), firstCal.getYear(), firstCal.getMonthValue() + 1, firstCal.getDayOfMonth(), lastCal.getYear(), lastCal.getMonthValue() + 1, lastCal.getDayOfMonth(), currentDuration, firstCal.getYear(), firstCal.getMonthValue() + 1, firstCal.getDayOfMonth(), speciesPresent.toString()));
		}

		toReturn.append(String.format("Total camera trap days                             %9d\n", durationTotal));

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each year, and for each location, and for each month, the number of camera traps days, and the total number of camera trap days for all
	 * months. This is followed by Total days this is the total of all camera trap days from all locations for each month. The total number of camera
	 * traps days for the year is given. The Summary for all years, for all locations, for all months, and for all years is also given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printCameraTrapEffort()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("CAMERA TRAP EFFORT\n");

		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> withYear = new ImageQuery().yearOnly(year).query(images);
			List<Location> locations = analysis.locationsForImageList(withYear);
			if (!locations.isEmpty())
			{
				toReturn.append("Year ").append(year).append("\n");
				int numLocations = locations.size();
				toReturn.append(String.format("Location (%3d)              Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n", numLocations));

				int[] monthlyTotals = new int[12];

				for (Location location : locations)
				{
					List<ImageEntry> withYearLocation = new ImageQuery().locationOnly(location).query(withYear);
					LocalDateTime firstCal = analysis.getFirstImageInList(withYearLocation).getDateTaken();
					LocalDateTime lastCal = analysis.getLastImageInList(withYearLocation).getDateTaken();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					toReturn.append(String.format("%-28s", location.getName()));
					int monthTotal = 0;
					for (int i = 0; i < 12; i++)
					{
						int monthValue = 0;
						if (firstMonth == lastMonth && firstMonth == i)
							monthValue = lastDay - firstDay + 1;
						else if (firstMonth == i)
							monthValue = 31 - firstDay + 1;
						else if (lastMonth == i)
							monthValue = lastDay;
						else if (firstMonth < i && lastMonth > i)
						{
							monthValue = 31;
						}

						toReturn.append(String.format(" %2d    ", monthValue));
						monthTotal = monthTotal + monthValue;
						monthlyTotals[i] = monthlyTotals[i] + monthValue;
					}
					toReturn.append(monthTotal).append("\n");
				}

				toReturn.append("Total days                  ");

				Integer totalTotal = 0;

				for (int i = 0; i < 12; i++)
				{
					totalTotal = totalTotal + monthlyTotals[i];
					toReturn.append(String.format(" %2d    ", monthlyTotals[i]));
				}

				toReturn.append(String.format("%2d", totalTotal));

				toReturn.append("\n");
			}

			toReturn.append("\n");
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
	public String printCameraTrapEffortSummary()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("CAMERA TRAP EFFORT SUMMARY\n");
		int numYears = analysis.getAllImageYears().size();
		if (numYears != 0)
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("Location                    Jan    Feb    Mar    Apr    May    Jun    Jul    Aug    Sep    Oct    Nov    Dec    Total\n");

		int[] monthlyTotals = new int[12];

		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new ImageQuery().locationOnly(location).query(images);
			LocalDateTime firstCal = analysis.getFirstImageInList(withLocation).getDateTaken();
			LocalDateTime lastCal = analysis.getLastImageInList(withLocation).getDateTaken();
			Integer firstMonth = firstCal.getMonthValue();
			Integer lastMonth = lastCal.getMonthValue();
			Integer firstDay = firstCal.getDayOfMonth();
			Integer lastDay = lastCal.getDayOfMonth();
			toReturn.append(String.format("%-28s", location.getName()));
			int monthTotal = 0;
			for (int i = 0; i < 12; i++)
			{
				int monthValue = 0;
				if (firstMonth == lastMonth && firstMonth == i)
					monthValue = lastDay - firstDay + 1;
				else if (firstMonth == i)
					monthValue = 31 - firstDay + 1;
				else if (lastMonth == i)
					monthValue = lastDay;
				else if (firstMonth < i && lastMonth > i)
				{
					monthValue = 31;
				}

				toReturn.append(String.format(" %2d    ", monthValue));
				monthTotal = monthTotal + monthValue;
				monthlyTotals[i] = monthlyTotals[i] + monthValue;
			}
			toReturn.append(monthTotal).append("\n");
		}

		toReturn.append("Total days                  ");

		Integer totalTotal = 0;

		for (int i = 0; i < 12; i++)
		{
			totalTotal = totalTotal + monthlyTotals[i];
			toReturn.append(String.format(" %2d    ", monthlyTotals[i]));
		}

		toReturn.append(String.format("%2d", totalTotal));

		toReturn.append("\n\n");

		return toReturn.toString();
	}
}
