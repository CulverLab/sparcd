/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

public class DetectionRateFormatter extends TextFormatter
{
	public DetectionRateFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printDetectionRateSpeciesYear()
	{
		String toReturn = "";

		toReturn = toReturn + "DETECTION RATE FOR EACH SPECIES PER YEAR\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "  Number of pictures/prd multiplied by 100\n";

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + "Year " + year + "\n";
			toReturn = toReturn + "                            Total   Total       Pics          Species\n";
			toReturn = toReturn + "Location                     days    pics       /prd    ";

			for (Species species : analysis.getAllImageSpecies())
				toReturn = toReturn + String.format("%5s ", StringUtils.left(species.getName(), 5));

			toReturn = toReturn + "\n";

			List<ImageEntry> byYear = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			Integer totalPics = byYear.size();
			Integer totalDays = 0;
			double[] averageRate = new double[analysis.getAllImageSpecies().size()];
			int[] numberLocations = new int[analysis.getAllImageSpecies().size()];

			for (Location location : analysis.getAllImageLocations())
			{
				toReturn = toReturn + String.format("%-28s", location.getName());

				List<ImageEntry> byYearLocation = new PredicateBuilder().locationOnly(location).query(byYear);

				ImageEntry first = byYearLocation.get(0);
				ImageEntry last = byYearLocation.get(byYearLocation.size() - 1);
				Calendar firstCal = DateUtils.toCalendar(first.getDateTaken());
				Calendar lastCal = DateUtils.toCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				Calendar calendar = Calendar.getInstance();
				Integer totalDaysForLoc = 0;
				if (firstMonth == lastMonth)
					totalDaysForLoc = totalDaysForLoc + lastDay - firstDay + 1;
				else
				{
					totalDaysForLoc = totalDaysForLoc + firstDaysInMonth - (firstDay - 1);
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						totalDaysForLoc = totalDaysForLoc + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					totalDaysForLoc = totalDaysForLoc + lastDay;
				}

				totalDays = totalDays + totalDaysForLoc;

				toReturn = toReturn + String.format("  %3d %7d    %7.2f   ", totalDaysForLoc, byYearLocation.size(), 100D * (double) byYearLocation.size() / totalDaysForLoc);

				for (Species species : analysis.getAllImageSpecies())
				{
					List<ImageEntry> byYearLocSpecies = new PredicateBuilder().speciesOnly(species).query(byYearLocation);
					toReturn = toReturn + String.format(" %5.2f", (double) byYearLocSpecies.size() / totalDaysForLoc);
					if (!byYearLocSpecies.isEmpty())
					{
						averageRate[analysis.getAllImageSpecies().indexOf(species)] = averageRate[analysis.getAllImageSpecies().indexOf(species)] + (double) byYearLocSpecies.size() / totalDaysForLoc;
						numberLocations[analysis.getAllImageSpecies().indexOf(species)]++;
					}
				}

				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "Total days pics; Avg rate   ";

			toReturn = toReturn + String.format("  %3d %7d    %7.2f   ", totalDays, totalPics, (double) totalPics / totalDays);

			for (Integer species = 0; species < analysis.getAllImageSpecies().size(); species++)
			{
				toReturn = toReturn + String.format(" %5.2f", numberLocations[species] == 0 ? 0 : averageRate[species] / numberLocations[species]);
			}

			toReturn = toReturn + "\n\n";
		}

		return toReturn;
	}

	public String printDetectionRateSummary()
	{
		String toReturn = "";

		toReturn = toReturn + "DETECTION RATE SUMMARY FOR EACH SPECIES\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "  Number of pictures/PERIOD multiplied by 100\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "                            Total   Total     Pics          Species\n";
		toReturn = toReturn + "Location                     days    pics     /prd   ";

		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + String.format("%5s ", StringUtils.left(species.getName(), 5));

		toReturn = toReturn + "\n";

		Integer totalDays = 0;
		Integer totalPics = 0;

		double[] averageRate = new double[analysis.getAllImageSpecies().size()];
		int[] numberLocations = new int[analysis.getAllImageSpecies().size()];

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s", location.getName());
			List<ImageEntry> byLocation = new PredicateBuilder().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer totalDaysLoc = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(byLocation);

				ImageEntry first = yearsPics.get(0);
				ImageEntry last = yearsPics.get(yearsPics.size() - 1);
				Calendar firstCal = DateUtils.toCalendar(first.getDateTaken());
				Calendar lastCal = DateUtils.toCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				Calendar calendar = Calendar.getInstance();
				if (firstMonth == lastMonth)
					totalDaysLoc = totalDaysLoc + (lastDay - firstDay + 1);
				else
				{
					totalDaysLoc = totalDaysLoc + (firstDaysInMonth - (firstDay - 1));
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						totalDaysLoc = totalDaysLoc + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					totalDaysLoc = totalDaysLoc + lastDay;
				}
			}

			toReturn = toReturn + String.format("  %3d %7d  %7.2f  ", totalDaysLoc, byLocation.size(), totalDaysLoc == 0 ? 0 : 100D * (double) byLocation.size() / totalDaysLoc);

			totalPics = totalPics + byLocation.size();

			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> byLocSpecies = new PredicateBuilder().speciesOnly(species).query(byLocation);
				toReturn = toReturn + String.format(" %5.2f", (double) byLocSpecies.size() / totalDaysLoc);
				if (!byLocSpecies.isEmpty())
				{
					averageRate[analysis.getAllImageSpecies().indexOf(species)] = averageRate[analysis.getAllImageSpecies().indexOf(species)] + (double) byLocSpecies.size() / totalDaysLoc;
					numberLocations[analysis.getAllImageSpecies().indexOf(species)]++;
				}
			}

			toReturn = toReturn + "\n";

			totalDays = totalDays + totalDaysLoc;
		}

		toReturn = toReturn + "Total days pics; Avg rate   ";

		toReturn = toReturn + String.format("  %3d %7d  %7.2f  ", totalDays, totalPics, 100D * (double) totalPics / totalDays);

		for (Integer species = 0; species < analysis.getAllImageSpecies().size(); species++)
		{
			toReturn = toReturn + String.format(" %5.2f", numberLocations[species] == 0 ? 0 : averageRate[species] / numberLocations[species]);
		}

		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printDetectionRateLocationMonth()
	{
		String toReturn = "";

		toReturn = toReturn + "DETECTION RATE FOR EACH LOCATION BY MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + "Year " + year + "\n";
			toReturn = toReturn + "                            Total   Total       Pics          Months \n";
			toReturn = toReturn + "Location                     days    pics       /prd       Jan     Feb     Mar     Apr     May     Jun     Jul     Aug     Sep     Oct     Nov     Dec\n";

			List<ImageEntry> byYear = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			Integer totalPics = byYear.size();
			Integer totalDays = 0;
			double[] averageRate = new double[12];
			int[] numberLocations = new int[12];

			for (Location location : analysis.getAllImageLocations())
			{
				toReturn = toReturn + String.format("%-28s", location.getName());

				List<ImageEntry> byYearLocation = new PredicateBuilder().locationOnly(location).query(byYear);

				ImageEntry first = byYearLocation.get(0);
				ImageEntry last = byYearLocation.get(byYearLocation.size() - 1);
				Calendar firstCal = DateUtils.toCalendar(first.getDateTaken());
				Calendar lastCal = DateUtils.toCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				Calendar calendar = Calendar.getInstance();
				Integer totalDaysForLoc = 0;
				if (firstMonth == lastMonth)
					totalDaysForLoc = totalDaysForLoc + lastDay - firstDay + 1;
				else
				{
					totalDaysForLoc = totalDaysForLoc + firstDaysInMonth - (firstDay - 1);
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						totalDaysForLoc = totalDaysForLoc + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					totalDaysForLoc = totalDaysForLoc + lastDay;
				}

				totalDays = totalDays + totalDaysForLoc;

				toReturn = toReturn + String.format("  %3d %7d    %7.2f    ", totalDaysForLoc, byYearLocation.size(), 100D * (double) byYearLocation.size() / totalDaysForLoc);

				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> byYearLocMonth = new PredicateBuilder().monthOnly(i).query(byYearLocation);
					toReturn = toReturn + String.format(" %5.2f  ", (double) byYearLocMonth.size() / totalDaysForLoc);
					if (!byYearLocMonth.isEmpty())
					{
						averageRate[i] = averageRate[i] + (double) byYearLocMonth.size() / totalDaysForLoc;
						numberLocations[i]++;
					}
				}

				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "Total days pics; Avg rate   ";

			toReturn = toReturn + String.format("  %3d %7d    %7.2f    ", totalDays, totalPics, 100D * (double) totalPics / totalDays);

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format(" %5.2f  ", numberLocations[i] == 0 ? 0 : averageRate[i] / numberLocations[i]);
			}

			toReturn = toReturn + "\n\n";
		}

		return toReturn;
	}

	public String printDetectionRateLocationMonthSummary()
	{
		String toReturn = "";

		toReturn = toReturn + "DETECTION RATE SUMMARY FOR EACH LOCATION BY MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "                            Total   Total       Pics          Months \n";
		toReturn = toReturn + "Location                     days    pics       /prd       Jan     Feb     Mar     Apr     May     Jun     Jul     Aug     Sep     Oct     Nov     Dec\n";

		Integer totalPics = analysis.getImagesSortedByDate().size();
		Integer totalDays = 0;
		double[] averageRate = new double[12];
		int[] numberLocations = new int[12];

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s", location.getName());

			List<ImageEntry> byLocation = new PredicateBuilder().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer totalDaysLoc = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(byLocation);

				ImageEntry first = yearsPics.get(0);
				ImageEntry last = yearsPics.get(yearsPics.size() - 1);
				Calendar firstCal = DateUtils.toCalendar(first.getDateTaken());
				Calendar lastCal = DateUtils.toCalendar(last.getDateTaken());
				Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
				Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
				Integer firstMonth = firstCal.get(Calendar.MONTH);
				Integer lastMonth = lastCal.get(Calendar.MONTH);
				Calendar calendar = Calendar.getInstance();
				if (firstMonth == lastMonth)
					totalDaysLoc = totalDaysLoc + (lastDay - firstDay + 1);
				else
				{
					totalDaysLoc = totalDaysLoc + (firstDaysInMonth - (firstDay - 1));
					firstMonth++;
					while (firstMonth < lastMonth)
					{
						calendar.set(year, firstMonth, 1);
						totalDaysLoc = totalDaysLoc + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
						firstMonth++;
					}
					totalDaysLoc = totalDaysLoc + lastDay;
				}
			}

			totalDays = totalDays + totalDaysLoc;

			toReturn = toReturn + String.format("  %3d %7d    %7.2f    ", totalDaysLoc, byLocation.size(), 100D * (double) byLocation.size() / totalDaysLoc);

			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> byLocMonth = new PredicateBuilder().monthOnly(i).query(byLocation);
				toReturn = toReturn + String.format(" %5.2f  ", (double) byLocMonth.size() / totalDaysLoc);
				if (!byLocMonth.isEmpty())
				{
					averageRate[i] = averageRate[i] + (double) byLocMonth.size() / totalDaysLoc;
					numberLocations[i]++;
				}
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total days pics; Avg rate   ";

		toReturn = toReturn + String.format("  %3d %7d    %7.2f    ", totalDays, totalPics, 100D * (double) totalPics / totalDays);

		for (int i = 0; i < 12; i++)
		{
			toReturn = toReturn + String.format(" %5.2f  ", numberLocations[i] == 0 ? 0 : averageRate[i] / numberLocations[i]);
		}

		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printDetectionRateTrend()
	{
		String toReturn = "";

		toReturn = toReturn + "MONTHLY DETECTION RATE TREND\n";
		toReturn = toReturn + "   Use independent records from only those locations that ever recorded species\n";

		//		for (Integer year : analysis.getAllImageYears())
		//		{
		//			toReturn = toReturn + "  " + year + " ";
		//
		//			for (Species species : analysis.getAllImageSpecies())
		//				toReturn = toReturn + String.format("%5s ", StringUtils.left(species.getName(), 5));
		//
		//			toReturn = toReturn + "\n";
		//
		//			for (int month = 0; month < 12; month++)
		//			{
		//				Integer monthTotal = 0;
		//
		//				String forMonth = "";
		//
		//				forMonth = forMonth + year + "-" + String.format("%02d", month) + " ";
		//				
		//				for (Species species : analysis.getAllImageSpecies())
		//				{
		//					toReturn = toReturn + String.format("%4.2f ");
		//				}
		//
		//				if (monthTotal != 0)
		//					toReturn = toReturn + forMonth;
		//			}
		//		}

		toReturn = toReturn + "No idea what these numbers are\n\n";

		return toReturn;
	}
}
