package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;

/**
 * The text formatter for any calculations containing "total days"
 * 
 * @author David Slovikosky
 */
public class TotalDayFormatter extends TextFormatter
{
	public TotalDayFormatter(List<ImageEntry> images, DataAnalysis analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each year and for each month a table of the number of independent pictures per month for each location. The last column shows the Total
	 * number of independent pictures at a location for all months. Total pictures for each month and then year is also given. For all locations Total
	 * days is the number of camera trap days (or effort) for each month, with the total of all months in the last column. The last row, Pictures/day,
	 * is Total pictures normalized (divided) by total effort for each month, and for all 12 months.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPicturesByMonthYearLoc()
	{
		String toReturn = "";

		toReturn = toReturn + "PICTURES FOR EACH LOCATION BY MONTH AND YEAR\n";
		toReturn = toReturn + "  Number of independent pictures per location\n";
		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + year + "\n";
			toReturn = toReturn + "Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesByLocationAndYear = new PredicateBuilder().yearOnly(year).locationOnly(location).query(analysis.getImagesSortedByDate());
				if (!imagesByLocationAndYear.isEmpty())
				{
					toReturn = toReturn + String.format("%-28s", location.getName());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesByLocationYearAndMonth = new PredicateBuilder().monthOnly(i).query(imagesByLocationAndYear);
						Integer period = analysis.periodForImageList(imagesByLocationYearAndMonth);
						total = total + period;
						toReturn = toReturn + String.format("%5d ", period);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
			}
			toReturn = toReturn + "Total pictures              ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				Integer totalPeriod = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> imagesByLocationYearAndMonth = new PredicateBuilder().locationOnly(location).monthOnly(i).yearOnly(year).query(analysis.getImagesSortedByDate());
					Integer period = analysis.periodForImageList(imagesByLocationYearAndMonth);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
				toReturn = toReturn + String.format("%5d ", totalPeriod);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                     ";

			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			int[] daysUsed = new int[12];
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPicturesByMonthLoc()
	{
		String toReturn = "";

		toReturn = toReturn + "PICTURES FOR EACH LOCATION BY MONTH AND YEAR SUMMARY\n";
		toReturn = toReturn + "  Number of independent pictures per location\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s", location.getName());

			List<ImageEntry> imagesAtLoc = new PredicateBuilder().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer picsInYear = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesAtLocWithMonth = new PredicateBuilder().monthOnly(i).query(imagesAtLoc);
				Integer period = analysis.periodForImageList(imagesAtLocWithMonth);
				picsInYear = picsInYear + period;
				toReturn = toReturn + String.format("%5d ", period);
			}

			toReturn = toReturn + String.format("  %5d", picsInYear);

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures              ";

		Integer totalPic = 0;
		int[] totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			Integer totalPeriod = 0;
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesByMonthAndLoc = new PredicateBuilder().monthOnly(i).locationOnly(location).query(analysis.getImagesSortedByDate());
				Integer period = analysis.periodForImageList(imagesByMonthAndLoc);
				totalPic = totalPic + period;
				totalPeriod = totalPeriod + period;
				totalPics[i] = totalPics[i] + period;
			}
			toReturn = toReturn + String.format("%5d ", totalPeriod);
		}
		toReturn = toReturn + String.format("%7d", totalPic);
		toReturn = toReturn + "\n";

		toReturn = toReturn + "Total days                     ";

		int[] daysUsed = new int[12];
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each year a table of species records for each month, and the total number of each species for the year. For all speies, for each month
	 * Total pictures, Total days (effort), 10*(number of pictures divived by total effort), and species richness is given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPicturesByMonthYearSpeciesRichness()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES AND SPECIES RICHNESS BY YEAR AND MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn = toReturn + year + "\n";
			toReturn = toReturn + "Species                       Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			int[] totalRichness = new int[12];
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesBySpeciesAndYear = new PredicateBuilder().yearOnly(year).speciesOnly(species).query(analysis.getImagesSortedByDate());
				if (!imagesBySpeciesAndYear.isEmpty())
				{
					toReturn = toReturn + String.format("%-28s", species.getName());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesBySpeciesYearAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesAndYear);
						Integer period = analysis.periodForImageList(imagesBySpeciesYearAndMonth);
						total = total + period;
						toReturn = toReturn + String.format("%5d ", period);
						totalRichness[i] = totalRichness[i] + (period == 0 ? 0 : 1);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
			}
			toReturn = toReturn + "Total pictures              ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByYearAndMonth = new PredicateBuilder().monthOnly(i).yearOnly(year).query(analysis.getImagesSortedByDate());
				Integer totalPeriod = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> imagesByYearMonthAndLocation = new PredicateBuilder().locationOnly(location).query(imagesByYearAndMonth);
					Integer period = analysis.periodForImageList(imagesByYearMonthAndLocation);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
				toReturn = toReturn + String.format("%5d ", totalPeriod);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                     ";
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

			int[] daysUsed = new int[12];
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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
			}
			Integer totalDays = 0;
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
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPicturesByMonthSpeciesRichness()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES ALL YEARS BY MONTH\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "Species                       Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";
		int[] totalRichness = new int[12];
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + String.format("%-28s", species.getName());

			List<ImageEntry> imagesBySpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer total = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesBySpeciesAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpecies);
				Integer period = analysis.periodForImageList(imagesBySpeciesAndMonth);
				total = total + period;
				toReturn = toReturn + String.format("%5d ", period);
				totalRichness[i] = totalRichness[i] + (period == 0 ? 0 : 1);
			}
			toReturn = toReturn + String.format("%7d", total);

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "Total pictures              ";

		Integer totalPic = 0;
		int[] totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			List<ImageEntry> imagesByMonth = new PredicateBuilder().monthOnly(i).query(analysis.getImagesSortedByDate());
			Integer totalPeriod = 0;
			for (Location location : analysis.getAllImageLocations())
			{
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> imagesByYearMonthAndLocation = new PredicateBuilder().yearOnly(year).locationOnly(location).query(imagesByMonth);
					Integer period = analysis.periodForImageList(imagesByYearMonthAndLocation);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
			}
			toReturn = toReturn + String.format("%5d ", totalPeriod);
		}
		toReturn = toReturn + String.format("%7d", totalPic);
		toReturn = toReturn + "\n";

		toReturn = toReturn + "Total days                     ";

		int[] daysUsed = new int[12];
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{

					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printPicturesByMonthSpeciesLocElevation()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";

			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> imagesBySpeciesAndYear = new PredicateBuilder().speciesOnly(species).yearOnly(year).query(analysis.getImagesSortedByDate());

				if (!imagesBySpeciesAndYear.isEmpty())
				{
					toReturn = toReturn + year + "\n";

					toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> imagesBySpeciesLocationAndYear = new PredicateBuilder().locationOnly(location).query(imagesBySpeciesAndYear);
						if (!imagesBySpeciesLocationAndYear.isEmpty())
						{
							toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
							Integer total = 0;
							for (int i = 0; i < 12; i++)
							{
								List<ImageEntry> imagesBySpeciesYearLocationAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesLocationAndYear);
								Integer period = analysis.periodForImageList(imagesBySpeciesYearLocationAndMonth);
								total = total + period;
								toReturn = toReturn + String.format("%5d ", period);
							}
							toReturn = toReturn + String.format("%7d", total);
							toReturn = toReturn + "\n";
						}
					}
					toReturn = toReturn + "Total pictures                     ";

					Integer totalPic = 0;
					int[] totalPics = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesByYearSpeciesAndMonth = new PredicateBuilder().speciesOnly(species).monthOnly(i).yearOnly(year).query(images);
						Integer totalPeriod = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> imagesByYearSpeciesMonthLocation = new PredicateBuilder().locationOnly(location).query(imagesByYearSpeciesAndMonth);
							Integer period = analysis.periodForImageList(imagesByYearSpeciesMonthLocation);
							totalPic = totalPic + period;
							totalPeriod = totalPeriod + period;
							totalPics[i] = totalPics[i] + period;
						}
						toReturn = toReturn + String.format("%5d ", totalPeriod);
					}
					toReturn = toReturn + String.format("%7d", totalPic);
					toReturn = toReturn + "\n";

					toReturn = toReturn + "Total days                            ";

					int[] daysUsed = new int[12];
					List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);

						if (!yearLocPics.isEmpty())
						{
							ImageEntry first = yearLocPics.get(0);
							ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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
					}
					Integer totalDays = 0;
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

					toReturn = toReturn + "\n\n";
				}
			}

			toReturn = toReturn + "SUMMARY ALL YEARS\n";

			Integer numYears = analysis.getAllImageYears().size();
			if (!analysis.getAllImageYears().isEmpty())
				toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

			toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesBySpeciesLocation = new PredicateBuilder().locationOnly(location).speciesOnly(species).query(analysis.getImagesSortedByDate());
				if (!imagesBySpeciesLocation.isEmpty())
				{
					toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesBySpeciesLocationAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesLocation);
						Integer totalPeriod = 0;
						for (Integer year : analysis.getAllImageYears())
						{
							List<ImageEntry> imagesBySpeciesLocationMonthAndYear = new PredicateBuilder().yearOnly(year).query(imagesBySpeciesLocationAndMonth);
							Integer period = analysis.periodForImageList(imagesBySpeciesLocationMonthAndYear);
							totalPeriod = totalPeriod + period;
							total = total + period;
						}
						toReturn = toReturn + String.format("%5d ", totalPeriod);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
			}

			toReturn = toReturn + "Total pictures                     ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByMonthSpecies = new PredicateBuilder().speciesOnly(species).monthOnly(i).query(analysis.getImagesSortedByDate());
				Integer totalPeriod = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> imagesByYearSpeciesMonthLocation = new PredicateBuilder().yearOnly(year).locationOnly(location).query(imagesByMonthSpecies);
						Integer period = analysis.periodForImageList(imagesByYearSpeciesMonthLocation);
						totalPic = totalPic + period;
						totalPeriod = totalPeriod + period;
						totalPics[i] = totalPics[i] + period;
					}
				}
				toReturn = toReturn + String.format("%5d ", totalPeriod);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                            ";

			int[] daysUsed = new int[12];
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);

					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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

			toReturn = toReturn + "10*Pic/effort                     ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
			}
			toReturn = toReturn + "\n";

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printAbundanceByMonthSpeciesLocElevation()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES ABUNDANCE BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "  Use maximum number of individuals per PERIOD\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";

			List<ImageEntry> withSpecices = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());

			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new PredicateBuilder().yearOnly(year).query(withSpecices);

				if (!withSpeciesYear.isEmpty())
				{
					toReturn = toReturn + year + "\n";

					toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> withSpeciesYearLocation = new PredicateBuilder().locationOnly(location).query(withSpeciesYear);

						if (!withSpeciesYearLocation.isEmpty())
						{
							toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
							Integer total = 0;
							for (int i = 0; i < 12; i++)
							{
								List<ImageEntry> withSpeciesYearLocationMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesYearLocation);
								Integer abundance = analysis.abundanceForImageList(withSpeciesYearLocationMonth, species);
								total = total + abundance;
								toReturn = toReturn + String.format("%5d ", abundance);
							}
							toReturn = toReturn + String.format("%7d", total);
							toReturn = toReturn + "\n";
						}
					}

					toReturn = toReturn + "Total pictures                     ";

					Integer totalPic = 0;
					int[] totalPics = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesYearMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesYear);
						Integer totalPeriod = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> withSpeciesYearMonthLocation = new PredicateBuilder().locationOnly(location).query(withSpeciesYearMonth);
							Integer period = analysis.periodForImageList(withSpeciesYearMonthLocation);
							totalPic = totalPic + period;
							totalPeriod = totalPeriod + period;
							totalPics[i] = totalPics[i] + period;
						}
						toReturn = toReturn + String.format("%5d ", totalPeriod);
					}
					toReturn = toReturn + String.format("%7d", totalPic);
					toReturn = toReturn + "\n";

					toReturn = toReturn + "Total abundance                    ";
					Integer totalAbundancePics = 0;
					int[] totalAbundances = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesYearMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesYear);
						Integer totalAbundance = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> withSpeciesYearMonthLocation = new PredicateBuilder().locationOnly(location).query(withSpeciesYearMonth);
							Integer abundance = analysis.abundanceForImageList(withSpeciesYearMonthLocation, species);
							totalAbundancePics = totalAbundancePics + abundance;
							totalAbundance = totalAbundance + abundance;
						}
						totalAbundances[i] = totalAbundances[i] + totalAbundance;
						toReturn = toReturn + String.format("%5d ", totalAbundance);
					}
					toReturn = toReturn + String.format("%7d", totalAbundancePics);
					toReturn = toReturn + "\n";

					toReturn = toReturn + "Avg abundance                      ";
					for (int i = 0; i < 12; i++)
					{
						toReturn = toReturn + String.format("%5.2f ", totalPics[i] == 0 ? 0 : (double) totalAbundances[i] / totalPics[i]);
					}
					toReturn = toReturn + String.format("%7.2f", totalPic == 0 ? 0 : (double) totalAbundancePics / totalPic);
					toReturn = toReturn + "\n";

					toReturn = toReturn + "Total days                            ";
					int[] daysUsed = new int[12];
					List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);

						if (!yearLocPics.isEmpty())
						{
							ImageEntry first = yearLocPics.get(0);
							ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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
					}
					Integer totalDays = 0;
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
						toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalAbundances[i] / daysUsed[i]));
					}

					toReturn = toReturn + "\n\n";
				}
			}

			toReturn = toReturn + "SUMMARY ALL YEARS\n";

			Integer numYears = analysis.getAllImageYears().size();
			if (!analysis.getAllImageYears().isEmpty())
				toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

			toReturn = toReturn + "Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			List<ImageEntry> withSpecies = new PredicateBuilder().query(withSpecices);
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLocation = new PredicateBuilder().locationOnly(location).query(withSpecies);

				if (!withSpeciesLocation.isEmpty())
				{
					toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesLocationMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesLocation);
						Integer abundance = 0;
						for (Integer year : analysis.getAllImageYears())
						{
							List<ImageEntry> withSpeciesLocationMonthYear = new PredicateBuilder().yearOnly(year).query(withSpeciesLocationMonth);
							abundance = abundance + analysis.abundanceForImageList(withSpeciesLocationMonthYear, species);
							total = total + abundance;
						}
						toReturn = toReturn + String.format("%5d ", abundance);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
			}

			toReturn = toReturn + "Total pictures                     ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> withSpeciesYearMonth = new PredicateBuilder().monthOnly(i).query(withSpecices);
				Integer period = analysis.periodForImageList(withSpeciesYearMonth);
				totalPic = totalPic + period;
				totalPics[i] = period;
				toReturn = toReturn + String.format("%5d ", period);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total abundance                    ";
			Integer totalAbundancePics = 0;
			int[] totalAbundances = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> withSpeciesMonth = new PredicateBuilder().monthOnly(i).query(withSpecies);
				Integer totalAbundance = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withSpeciesMonthLocation = new PredicateBuilder().locationOnly(location).query(withSpeciesMonth);
					Integer abundance = 0;
					for (Integer year : analysis.getAllImageYears())
					{
						List<ImageEntry> withSpeciesMonthLocationYear = new PredicateBuilder().yearOnly(year).query(withSpeciesMonthLocation);
						abundance = abundance + analysis.abundanceForImageList(withSpeciesMonthLocationYear, species);
					}
					totalAbundancePics = totalAbundancePics + abundance;
					totalAbundance = totalAbundance + abundance;
				}
				toReturn = toReturn + String.format("%5d ", totalAbundance);
				totalAbundances[i] = totalAbundances[i] + totalAbundance;
			}
			toReturn = toReturn + String.format("%7d", totalAbundancePics);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Avg abundance                      ";
			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%5.2f ", totalPics[i] == 0 ? 0 : (double) totalAbundances[i] / totalPics[i]);
			}
			toReturn = toReturn + String.format("%7.2f", totalPic == 0 ? 0 : (double) totalAbundancePics / totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                            ";

			int[] daysUsed = new int[12];
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);
					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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

			toReturn = toReturn + "10*Pic/effort                     ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalAbundances[i] / daysUsed[i]));
			}

			toReturn = toReturn + "\n\n";
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesByLocElevationAndEffort()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "\n";
		toReturn = toReturn + "SUMMARY ALL YEARS\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			toReturn = toReturn + "Location                  Elevation   # pics/Effort   Percent\n";
			toReturn = toReturn + species.getName() + "\n";
			double[] picsOverEffortTotals = new double[analysis.getAllImageLocations().size()];
			Double picsOverEffortTotal = 0D;
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);

				Integer periodTotal = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> withSpeciesLocYear = new PredicateBuilder().yearOnly(year).query(withSpeciesLoc);
					if (!withSpeciesLocYear.isEmpty())
					{
						periodTotal = periodTotal + analysis.periodForImageList(withSpeciesLocYear);
					}
				}

				Integer effortTotal = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
					List<ImageEntry> yearLocPics = new PredicateBuilder().locationOnly(location).query(yearsPics);

					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
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
							effortTotal = effortTotal + (lastDay - firstDay + 1);
						else
						{
							effortTotal = effortTotal + (firstDaysInMonth - (firstDay - 1));
							firstMonth++;
							while (firstMonth < lastMonth)
							{
								calendar.set(year, firstMonth, 1);
								effortTotal = effortTotal + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
								firstMonth++;
							}
							effortTotal = effortTotal + lastDay;
						}
					}
				}

				Double picsOverEffort = (effortTotal == 0 ? 0 : (double) periodTotal / effortTotal);

				picsOverEffortTotal = picsOverEffortTotal + picsOverEffort;
				picsOverEffortTotals[analysis.getAllImageLocations().indexOf(location)] = picsOverEffort;
			}

			for (Location location : analysis.getAllImageLocations())
			{
				Integer index = analysis.getAllImageLocations().indexOf(location);
				if (picsOverEffortTotals[index] != 0)
					toReturn = toReturn + String.format("%-28s %6.0f        %5.3f       %5.2f\n", location.getName(), location.getElevation(), picsOverEffortTotals[index], picsOverEffortTotals[index] / picsOverEffortTotal * 100.0D);
			}
		}

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesByLocElevationAndEffortTable()
	{
		String toReturn = "\n";

		toReturn = toReturn + "SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT TABLE\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "  Table shows frequency of all pictures normalized by effort for each species\n";

		toReturn = toReturn + "\n";

		toReturn = toReturn + "SUMMARY ALL YEARS\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "Location                  Elevation ";

		for (Species species : analysis.getAllImageSpecies())
			toReturn = toReturn + String.format("%6s ", StringUtils.left(species.getName(), 6));

		toReturn = toReturn + "\n";

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn = toReturn + String.format("%-28s %5.0f  ", location.getName(), location.getElevation());

			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> bySpecies = new PredicateBuilder().speciesOnly(species).query(images);
				List<ImageEntry> bySpeciesAndLoc = new PredicateBuilder().locationOnly(location).query(bySpecies);
				Integer bySpeciesPeriod = 0;
				Integer bySpeciesAndLocPeriod = 0;

				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> bySpeciesYear = new PredicateBuilder().yearOnly(year).query(bySpecies);
					for (Location location2 : analysis.getAllImageLocations())
						bySpeciesPeriod = bySpeciesPeriod + analysis.periodForImageList(new PredicateBuilder().locationOnly(location2).query(bySpeciesYear));

					List<ImageEntry> bySpeciesLocYear = new PredicateBuilder().yearOnly(year).query(bySpeciesAndLoc);
					bySpeciesAndLocPeriod = bySpeciesAndLocPeriod + analysis.periodForImageList(bySpeciesLocYear);
				}

				toReturn = toReturn + String.format("%6.2f ", bySpeciesPeriod == 0 ? 0 : 100.0D * (double) bySpeciesAndLocPeriod / bySpeciesPeriod);
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

}
