package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

/**
 * The text formatter for detection rates of species at locations
 * 
 * @author David Slovikosky
 */
public class DetectionRateFormatter extends TextFormatter
{
	public DetectionRateFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printDetectionRateSpeciesYear()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("DETECTION RATE FOR EACH SPECIES PER YEAR\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("  Number of pictures/prd multiplied by 100\n");

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn.append("Year ").append(year).append("\n");
			toReturn.append("                            Total   Total       Pics          Species\n");
			toReturn.append("Location                     days    pics       /prd    ");

			for (Species species : analysis.getAllImageSpecies())
				toReturn.append(String.format("%5s ", StringUtils.left(species.getName(), 5)));

			toReturn.append("\n");

			List<ImageEntry> byYear = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

			Integer totalPics = 0;
			Integer totalDays = 0;
			double[] averageRate = new double[analysis.getAllImageSpecies().size()];
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> byYearLocation = new ImageQuery().locationOnly(location).query(byYear);
				if (!byYearLocation.isEmpty())
				{
					toReturn.append(String.format("%-28s", location.getName()));

					Integer totalDaysForLoc = 0;
					ImageEntry first = byYearLocation.get(0);
					ImageEntry last = byYearLocation.get(byYearLocation.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					Calendar calendar = Calendar.getInstance();
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

					Integer periodTotal = 0;

					for (Species species : analysis.getAllImageSpecies())
					{
						periodTotal = periodTotal + analysis.periodForImageList(new ImageQuery().speciesOnly(species).query(byYearLocation));
					}

					totalPics = totalPics + periodTotal;

					toReturn.append(String.format("  %3d %7d    %7.2f   ", totalDaysForLoc, periodTotal, (totalDaysForLoc == 0 ? 0 : 100D * (double) periodTotal / totalDaysForLoc)));

					for (Species species : analysis.getAllImageSpecies())
					{
						Integer period = analysis.periodForImageList(new ImageQuery().speciesOnly(species).query(byYearLocation));
						toReturn.append(String.format(" %5.2f", 100D * (double) period / totalDaysForLoc));
						Integer index = analysis.getAllImageSpecies().indexOf(species);
						averageRate[index] = averageRate[index] + (double) period;
					}

					toReturn.append("\n");
				}
			}

			toReturn.append("Total days pics; Avg rate   ");

			toReturn.append(String.format("  %3d %7d    %7.2f   ", totalDays, totalPics, 100D * (double) totalPics / totalDays));

			for (Integer species = 0; species < analysis.getAllImageSpecies().size(); species++)
			{
				toReturn.append(String.format(" %5.2f", totalDays == 0 ? 0 : (100D * averageRate[species] / totalDays)));
			}

			toReturn.append("\n\n");
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
	public String printDetectionRateSummary()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("DETECTION RATE SUMMARY FOR EACH SPECIES\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("  Number of pictures/PERIOD multiplied by 100\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("                            Total   Total     Pics          Species\n");
		toReturn.append("Location                     days    pics     /prd   ");

		for (Species species : analysis.getAllImageSpecies())
			toReturn.append(String.format("%5s ", StringUtils.left(species.getName(), 5)));

		toReturn.append("\n");

		Integer totalDays = 0;
		Integer totalPics = 0;

		double[] averageRate = new double[analysis.getAllImageSpecies().size()];

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-28s", location.getName()));
			List<ImageEntry> byLocation = new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer totalDaysLoc = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(byLocation);

				if (!yearsPics.isEmpty())
				{
					ImageEntry first = yearsPics.get(0);
					ImageEntry last = yearsPics.get(yearsPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					if (firstMonth == lastMonth)
						totalDaysLoc = totalDaysLoc + (lastDay - firstDay + 1);
					else
					{
						totalDaysLoc = totalDaysLoc + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							totalDaysLoc = totalDaysLoc + 31;
							firstMonth++;
						}
						totalDaysLoc = totalDaysLoc + lastDay;
					}
				}
			}

			totalDays = totalDays + totalDaysLoc;

			Integer periodTotal = 0;

			for (Species species : analysis.getAllImageSpecies())
			{
				for (Integer year : analysis.getAllImageYears())
					periodTotal = periodTotal + analysis.periodForImageList(new ImageQuery().speciesOnly(species).yearOnly(year).query(byLocation));
			}

			totalPics = totalPics + periodTotal;

			toReturn.append(String.format("  %3d %7d  %7.2f  ", totalDaysLoc, periodTotal, totalDaysLoc == 0 ? 0 : 100D * (double) periodTotal / totalDaysLoc));

			for (Species species : analysis.getAllImageSpecies())
			{
				Integer period = 0;
				for (Integer year : analysis.getAllImageYears())
					period = period + analysis.periodForImageList(new ImageQuery().speciesOnly(species).yearOnly(year).query(byLocation));
				toReturn.append(String.format(" %5.2f", (double) period / totalDaysLoc));
				Integer index = analysis.getAllImageSpecies().indexOf(species);
				averageRate[index] = averageRate[index] + (double) period;
			}

			toReturn.append("\n");
		}

		toReturn.append("Total days pics; Avg rate   ");

		toReturn.append(String.format("  %3d %7d  %7.2f  ", totalDays, totalPics, 100D * (double) totalPics / totalDays));

		for (Integer species = 0; species < analysis.getAllImageSpecies().size(); species++)
		{
			toReturn.append(String.format(" %5.2f", totalDays == 0 ? 0 : 100D * averageRate[species] / totalDays));
		}

		toReturn.append("\n\n");

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
	public String printDetectionRateLocationMonth()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("DETECTION RATE FOR EACH LOCATION BY MONTH\n");
		toReturn.append("  One record of each species per location per PERIOD\n");

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn.append("Year ").append(year).append("\n");
			toReturn.append("                            Total   Total       Pics          Months \n");
			toReturn.append("Location                     days    pics       /prd       Jan     Feb     Mar     Apr     May     Jun     Jul     Aug     Sep     Oct     Nov     Dec\n");

			List<ImageEntry> byYear = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

			Integer totalPics = 0;
			Integer totalDays = 0;
			double[] averageRate = new double[12];

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> byYearLocation = new ImageQuery().locationOnly(location).query(byYear);
				if (!byYearLocation.isEmpty())
				{
					Integer totalDaysForLoc = 0;
					toReturn.append(String.format("%-28s", location.getName()));

					ImageEntry first = byYearLocation.get(0);
					ImageEntry last = byYearLocation.get(byYearLocation.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					if (firstMonth == lastMonth)
						totalDaysForLoc = totalDaysForLoc + lastDay - firstDay + 1;
					else
					{
						totalDaysForLoc = totalDaysForLoc + firstDaysInMonth - (firstDay - 1);
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							totalDaysForLoc = totalDaysForLoc + 31;
							firstMonth++;
						}
						totalDaysForLoc = totalDaysForLoc + lastDay;
					}

					totalDays = totalDays + totalDaysForLoc;

					Integer periodTotal = 0;

					for (Species species : analysis.getAllImageSpecies())
					{
						periodTotal = periodTotal + analysis.periodForImageList(new ImageQuery().speciesOnly(species).query(byYearLocation));
					}

					totalPics = totalPics + periodTotal;

					toReturn.append(String.format("  %3d %7d    %7.2f    ", totalDaysForLoc, periodTotal, (double) periodTotal / totalDaysForLoc));

					for (int i = 0; i < 12; i++)
					{
						// Go through species here?
						Integer period = 0;
						for (Species species : analysis.getAllImageSpecies())
							period = period + analysis.periodForImageList(new ImageQuery().monthOnly(i).speciesOnly(species).query(byYearLocation));
						toReturn.append(String.format(" %5.2f  ", (double) period / totalDaysForLoc));

						averageRate[i] = averageRate[i] + (double) period;
					}

					toReturn.append("\n");
				}
			}

			toReturn.append("Total days pics; Avg rate   ");

			toReturn.append(String.format("  %3d %7d    %7.2f    ", totalDays, totalPics, (double) totalPics / totalDays));

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format(" %5.2f  ", totalDays == 0 ? 0 : averageRate[i] / totalDays));
			}

			toReturn.append("\n\n");
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
	public String printDetectionRateLocationMonthSummary()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("DETECTION RATE SUMMARY FOR EACH LOCATION BY MONTH\n");
		toReturn.append("  One record of each species per location per PERIOD\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("                            Total   Total       Pics          Months \n");
		toReturn.append("Location                     days    pics       /prd       Jan     Feb     Mar     Apr     May     Jun     Jul     Aug     Sep     Oct     Nov     Dec\n");

		Integer totalPics = 0;
		Integer totalDays = 0;
		double[] averageRate = new double[12];

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-28s", location.getName()));

			List<ImageEntry> byLocation = new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer totalDaysLoc = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(byLocation);

				if (!yearsPics.isEmpty())
				{
					ImageEntry first = yearsPics.get(0);
					ImageEntry last = yearsPics.get(yearsPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth();
					Integer lastDay = lastCal.getDayOfMonth();
					Integer firstMonth = firstCal.getMonthValue();
					Integer lastMonth = lastCal.getMonthValue();
					if (firstMonth == lastMonth)
						totalDaysLoc = totalDaysLoc + (lastDay - firstDay + 1);
					else
					{
						totalDaysLoc = totalDaysLoc + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							totalDaysLoc = totalDaysLoc + 31;
							firstMonth++;
						}
						totalDaysLoc = totalDaysLoc + lastDay;
					}
				}
			}

			totalDays = totalDays + totalDaysLoc;

			Integer periodTotal = 0;

			for (Species species : analysis.getAllImageSpecies())
			{
				for (Integer year : analysis.getAllImageYears())
					periodTotal = periodTotal + analysis.periodForImageList(new ImageQuery().speciesOnly(species).yearOnly(year).query(byLocation));
			}

			totalPics = totalPics + periodTotal;

			toReturn.append(String.format("  %3d %7d    %7.2f    ", totalDaysLoc, periodTotal, (double) periodTotal / totalDaysLoc));

			for (int i = 0; i < 12; i++)
			{
				Integer period = 0;
				for (Species species : analysis.getAllImageSpecies())
					for (Integer year : analysis.getAllImageYears())
						period = period + analysis.periodForImageList(new ImageQuery().monthOnly(i).speciesOnly(species).yearOnly(year).query(byLocation));
				toReturn.append(String.format(" %5.2f  ", (double) period / totalDaysLoc));

				averageRate[i] = averageRate[i] + (double) period;
			}

			toReturn.append("\n");
		}

		toReturn.append("Total days pics; Avg rate   ");

		toReturn.append(String.format("  %3d %7d    %7.2f    ", totalDays, totalPics, (double) totalPics / totalDays));

		for (int i = 0; i < 12; i++)
		{
			toReturn.append(String.format(" %5.2f  ", totalDays == 0 ? 0 : averageRate[i] / totalDays));
		}

		toReturn.append("\n\n");

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
