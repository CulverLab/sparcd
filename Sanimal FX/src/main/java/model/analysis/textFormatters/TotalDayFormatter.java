package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The text formatter for any calculations containing "total days"
 * 
 * @author David Slovikosky
 */
public class TotalDayFormatter extends TextFormatter
{
	public TotalDayFormatter(List<ImageEntry> images, DataAnalyzer analysis)
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
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("PICTURES FOR EACH LOCATION BY MONTH AND YEAR\n");
		toReturn.append("  Number of independent pictures per location\n");
		for (Integer year : analysis.getAllImageYears())
		{
			toReturn.append(year).append("\n");
			toReturn.append("Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesByLocationAndYear = new ImageQuery().yearOnly(year).locationOnly(location).query(analysis.getImagesSortedByDate());
				if (!imagesByLocationAndYear.isEmpty())
				{
					toReturn.append(String.format("%-28s", location.getName()));
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesByLocationYearAndMonth = new ImageQuery().monthOnly(i).query(imagesByLocationAndYear);
						Integer period = analysis.periodForImageList(imagesByLocationYearAndMonth);
						total = total + period;
						toReturn.append(String.format("%5d ", period));
					}
					toReturn.append(String.format("%7d", total));
					toReturn.append("\n");
				}
			}
			toReturn.append("Total pictures              ");

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				Integer totalPeriod = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> imagesByLocationYearAndMonth = new ImageQuery().locationOnly(location).monthOnly(i).yearOnly(year).query(analysis.getImagesSortedByDate());
					Integer period = analysis.periodForImageList(imagesByLocationYearAndMonth);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
				toReturn.append(String.format("%5d ", totalPeriod));
			}
			toReturn.append(String.format("%7d", totalPic));
			toReturn.append("\n");

			toReturn.append("Total days                     ");

			List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

			int[] daysUsed = new int[12];
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth() - 1;
					Integer lastDay = lastCal.getDayOfMonth() - 1;
					Integer firstMonth = firstCal.getMonthValue() - 1;
					Integer lastMonth = lastCal.getMonthValue() - 1;
					if (firstMonth == lastMonth)
						daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
					else
					{
						daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
							firstMonth++;
						}
						daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
					}
				}
			}
			Integer totalDays = 0;
			for (Integer month : daysUsed)
			{
				toReturn.append(String.format("%2d    ", month));
				totalDays = totalDays + month;
			}

			toReturn.append(String.format(" %3d", totalDays));

			toReturn.append("\n");

			toReturn.append("Pictures/day               ");

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : ((double) totalPics[i] / daysUsed[i])));
			}
			toReturn.append(String.format("  %6.2f", totalDays == 0 ? 0 : ((double) totalPic / totalDays)));

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
	public String printPicturesByMonthLoc()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("PICTURES FOR EACH LOCATION BY MONTH AND YEAR SUMMARY\n");
		toReturn.append("  Number of independent pictures per location\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("Location                      Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");
		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-28s", location.getName()));

			List<ImageEntry> imagesAtLoc = new ImageQuery().locationOnly(location).query(analysis.getImagesSortedByDate());

			Integer picsInYear = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesAtLocWithMonth = new ImageQuery().monthOnly(i).query(imagesAtLoc);
				Integer period = analysis.periodForImageList(imagesAtLocWithMonth);
				picsInYear = picsInYear + period;
				toReturn.append(String.format("%5d ", period));
			}

			toReturn.append(String.format("  %5d", picsInYear));

			toReturn.append("\n");
		}

		toReturn.append("Total pictures              ");

		Integer totalPic = 0;
		int[] totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			Integer totalPeriod = 0;
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesByMonthAndLoc = new ImageQuery().monthOnly(i).locationOnly(location).query(analysis.getImagesSortedByDate());
				Integer period = analysis.periodForImageList(imagesByMonthAndLoc);
				totalPic = totalPic + period;
				totalPeriod = totalPeriod + period;
				totalPics[i] = totalPics[i] + period;
			}
			toReturn.append(String.format("%5d ", totalPeriod));
		}
		toReturn.append(String.format("%7d", totalPic));
		toReturn.append("\n");

		toReturn.append("Total days                     ");

		int[] daysUsed = new int[12];
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth() - 1;
					Integer lastDay = lastCal.getDayOfMonth() - 1;
					Integer firstMonth = firstCal.getMonthValue() - 1;
					Integer lastMonth = lastCal.getMonthValue() - 1;
					if (firstMonth == lastMonth)
						daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
					else
					{
						daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
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
			toReturn.append(String.format("%2d    ", month));
			totalDays = totalDays + month;
		}

		toReturn.append(String.format(" %3d", totalDays));

		toReturn.append("\n");

		toReturn.append("Pictures/day               ");

		for (int i = 0; i < 12; i++)
		{
			toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : ((double) totalPics[i] / daysUsed[i])));
		}
		toReturn.append(String.format("  %6.2f", totalDays == 0 ? 0 : ((double) totalPic / totalDays)));

		toReturn.append("\n\n");

		return toReturn.toString();
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
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES AND SPECIES RICHNESS BY YEAR AND MONTH\n");
		toReturn.append("  One record of each species per location per PERIOD\n");

		for (Integer year : analysis.getAllImageYears())
		{
			toReturn.append(year).append("\n");
			toReturn.append("Species                       Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

			int[] totalRichness = new int[12];
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesBySpeciesAndYear = new ImageQuery().yearOnly(year).speciesOnly(species).query(analysis.getImagesSortedByDate());
				if (!imagesBySpeciesAndYear.isEmpty())
				{
					toReturn.append(String.format("%-28s", species.getName()));
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesBySpeciesYearAndMonth = new ImageQuery().monthOnly(i).query(imagesBySpeciesAndYear);
						Integer period = analysis.periodForImageList(imagesBySpeciesYearAndMonth);
						total = total + period;
						toReturn.append(String.format("%5d ", period));
						totalRichness[i] = totalRichness[i] + (period == 0 ? 0 : 1);
					}
					toReturn.append(String.format("%7d", total));
					toReturn.append("\n");
				}
			}
			toReturn.append("Total pictures              ");

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByYearAndMonth = new ImageQuery().monthOnly(i).yearOnly(year).query(analysis.getImagesSortedByDate());
				Integer totalPeriod = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> imagesByYearMonthAndLocation = new ImageQuery().locationOnly(location).query(imagesByYearAndMonth);
					Integer period = analysis.periodForImageList(imagesByYearMonthAndLocation);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
				toReturn.append(String.format("%5d ", totalPeriod));
			}
			toReturn.append(String.format("%7d", totalPic));
			toReturn.append("\n");

			toReturn.append("Total days                     ");
			List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

			int[] daysUsed = new int[12];
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{
					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth() - 1;
					Integer lastDay = lastCal.getDayOfMonth() - 1;
					Integer firstMonth = firstCal.getMonthValue() - 1;
					Integer lastMonth = lastCal.getMonthValue() - 1;
					if (firstMonth == lastMonth)
						daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
					else
					{
						daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
							firstMonth++;
						}
						daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
					}
				}
			}
			Integer totalDays = 0;
			for (Integer month : daysUsed)
			{
				toReturn.append(String.format("%2d    ", month));
				totalDays = totalDays + month;
			}

			toReturn.append(String.format(" %3d", totalDays));

			toReturn.append("\n");

			toReturn.append("10*Pic/effort              ");

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i])));
			}

			toReturn.append("\n");

			toReturn.append("Species richness            ");

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%5d ", totalRichness[i]));
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
	public String printPicturesByMonthSpeciesRichness()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES ALL YEARS BY MONTH\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("Species                       Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");
		int[] totalRichness = new int[12];
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(String.format("%-28s", species.getName()));

			List<ImageEntry> imagesBySpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer total = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesBySpeciesAndMonth = new ImageQuery().monthOnly(i).query(imagesBySpecies);
				Integer period = analysis.periodForImageList(imagesBySpeciesAndMonth);
				total = total + period;
				toReturn.append(String.format("%5d ", period));
				totalRichness[i] = totalRichness[i] + (period == 0 ? 0 : 1);
			}
			toReturn.append(String.format("%7d", total));

			toReturn.append("\n");
		}

		toReturn.append("Total pictures              ");

		Integer totalPic = 0;
		int[] totalPics = new int[12];
		for (int i = 0; i < 12; i++)
		{
			List<ImageEntry> imagesByMonth = new ImageQuery().monthOnly(i).query(analysis.getImagesSortedByDate());
			Integer totalPeriod = 0;
			for (Location location : analysis.getAllImageLocations())
			{
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> imagesByYearMonthAndLocation = new ImageQuery().yearOnly(year).locationOnly(location).query(imagesByMonth);
					Integer period = analysis.periodForImageList(imagesByYearMonthAndLocation);
					totalPic = totalPic + period;
					totalPeriod = totalPeriod + period;
					totalPics[i] = totalPics[i] + period;
				}
			}
			toReturn.append(String.format("%5d ", totalPeriod));
		}
		toReturn.append(String.format("%7d", totalPic));
		toReturn.append("\n");

		toReturn.append("Total days                     ");

		int[] daysUsed = new int[12];
		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);
				if (!yearLocPics.isEmpty())
				{

					ImageEntry first = yearLocPics.get(0);
					ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
					LocalDateTime firstCal = first.getDateTaken();
					LocalDateTime lastCal = last.getDateTaken();
					Integer firstDaysInMonth = 31;
					Integer firstDay = firstCal.getDayOfMonth() - 1;
					Integer lastDay = lastCal.getDayOfMonth() - 1;
					Integer firstMonth = firstCal.getMonthValue() - 1;
					Integer lastMonth = lastCal.getMonthValue() - 1;
					if (firstMonth == lastMonth)
						daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
					else
					{
						daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
						firstMonth++;
						while (firstMonth < lastMonth)
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
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
			toReturn.append(String.format("%2d    ", month));
			totalDays = totalDays + month;
		}

		toReturn.append(String.format(" %3d", totalDays));

		toReturn.append("\n");

		toReturn.append("10*Pic/effort              ");

		for (int i = 0; i < 12; i++)
		{
			toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i])));
		}

		toReturn.append("\n");

		toReturn.append("Species richness            ");

		for (int i = 0; i < 12; i++)
		{
			toReturn.append(String.format("%5d ", totalRichness[i]));
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
	public String printPicturesByMonthSpeciesLocElevation()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n");
		toReturn.append("  One record of each species per location per PERIOD\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(species.getName()).append("\n");

			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> imagesBySpeciesAndYear = new ImageQuery().speciesOnly(species).yearOnly(year).query(analysis.getImagesSortedByDate());

				if (!imagesBySpeciesAndYear.isEmpty())
				{
					toReturn.append(year).append("\n");

					toReturn.append("Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> imagesBySpeciesLocationAndYear = new ImageQuery().locationOnly(location).query(imagesBySpeciesAndYear);
						if (!imagesBySpeciesLocationAndYear.isEmpty())
						{
							toReturn.append(String.format("%-28s %6d", location.getName(), location.getElevation().intValue()));
							Integer total = 0;
							for (int i = 0; i < 12; i++)
							{
								List<ImageEntry> imagesBySpeciesYearLocationAndMonth = new ImageQuery().monthOnly(i).query(imagesBySpeciesLocationAndYear);
								Integer period = analysis.periodForImageList(imagesBySpeciesYearLocationAndMonth);
								total = total + period;
								toReturn.append(String.format("%5d ", period));
							}
							toReturn.append(String.format("%7d", total));
							toReturn.append("\n");
						}
					}
					toReturn.append("Total pictures                     ");

					Integer totalPic = 0;
					int[] totalPics = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesByYearSpeciesAndMonth = new ImageQuery().speciesOnly(species).monthOnly(i).yearOnly(year).query(images);
						Integer totalPeriod = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> imagesByYearSpeciesMonthLocation = new ImageQuery().locationOnly(location).query(imagesByYearSpeciesAndMonth);
							Integer period = analysis.periodForImageList(imagesByYearSpeciesMonthLocation);
							totalPic = totalPic + period;
							totalPeriod = totalPeriod + period;
							totalPics[i] = totalPics[i] + period;
						}
						toReturn.append(String.format("%5d ", totalPeriod));
					}
					toReturn.append(String.format("%7d", totalPic));
					toReturn.append("\n");

					toReturn.append("Total days                            ");

					int[] daysUsed = new int[12];
					List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);

						if (!yearLocPics.isEmpty())
						{
							ImageEntry first = yearLocPics.get(0);
							ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
							LocalDateTime firstCal = first.getDateTaken();
							LocalDateTime lastCal = last.getDateTaken();
							Integer firstDaysInMonth = 31;
							Integer firstDay = firstCal.getDayOfMonth() - 1;
							Integer lastDay = lastCal.getDayOfMonth() - 1;
							Integer firstMonth = firstCal.getMonthValue() - 1;
							Integer lastMonth = lastCal.getMonthValue() - 1;
							if (firstMonth == lastMonth)
								daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
							else
							{
								daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
								firstMonth++;
								while (firstMonth < lastMonth)
								{
									daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
									firstMonth++;
								}
								daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
							}
						}
					}
					Integer totalDays = 0;
					for (Integer month : daysUsed)
					{
						toReturn.append(String.format("%2d    ", month));
						totalDays = totalDays + month;
					}

					toReturn.append(String.format(" %3d", totalDays));

					toReturn.append("\n");

					toReturn.append("10*Pic/effort                     ");

					for (int i = 0; i < 12; i++)
					{
						toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i])));
					}

					toReturn.append("\n\n");
				}
			}

			toReturn.append("SUMMARY ALL YEARS\n");

			Integer numYears = analysis.getAllImageYears().size();
			if (!analysis.getAllImageYears().isEmpty())
				toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

			toReturn.append("Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> imagesBySpeciesLocation = new ImageQuery().locationOnly(location).speciesOnly(species).query(analysis.getImagesSortedByDate());
				if (!imagesBySpeciesLocation.isEmpty())
				{
					toReturn.append(String.format("%-28s %6d", location.getName(), location.getElevation().intValue()));
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> imagesBySpeciesLocationAndMonth = new ImageQuery().monthOnly(i).query(imagesBySpeciesLocation);
						Integer totalPeriod = 0;
						for (Integer year : analysis.getAllImageYears())
						{
							List<ImageEntry> imagesBySpeciesLocationMonthAndYear = new ImageQuery().yearOnly(year).query(imagesBySpeciesLocationAndMonth);
							Integer period = analysis.periodForImageList(imagesBySpeciesLocationMonthAndYear);
							totalPeriod = totalPeriod + period;
							total = total + period;
						}
						toReturn.append(String.format("%5d ", totalPeriod));
					}
					toReturn.append(String.format("%7d", total));
					toReturn.append("\n");
				}
			}

			toReturn.append("Total pictures                     ");

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesByMonthSpecies = new ImageQuery().speciesOnly(species).monthOnly(i).query(analysis.getImagesSortedByDate());
				Integer totalPeriod = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> imagesByYearSpeciesMonthLocation = new ImageQuery().yearOnly(year).locationOnly(location).query(imagesByMonthSpecies);
						Integer period = analysis.periodForImageList(imagesByYearSpeciesMonthLocation);
						totalPic = totalPic + period;
						totalPeriod = totalPeriod + period;
						totalPics[i] = totalPics[i] + period;
					}
				}
				toReturn.append(String.format("%5d ", totalPeriod));
			}
			toReturn.append(String.format("%7d", totalPic));
			toReturn.append("\n");

			toReturn.append("Total days                            ");

			int[] daysUsed = new int[12];
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);

					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
						LocalDateTime firstCal = first.getDateTaken();
						LocalDateTime lastCal = last.getDateTaken();
						Integer firstDaysInMonth = 31;
						Integer firstDay = firstCal.getDayOfMonth() - 1;
						Integer lastDay = lastCal.getDayOfMonth() - 1;
						Integer firstMonth = firstCal.getMonthValue() - 1;
						Integer lastMonth = lastCal.getMonthValue() - 1;
						if (firstMonth == lastMonth)
							daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
						else
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
							firstMonth++;
							while (firstMonth < lastMonth)
							{
								daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
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
				toReturn.append(String.format("%2d    ", month));
				totalDays = totalDays + month;
			}

			toReturn.append(String.format(" %3d", totalDays));

			toReturn.append("\n");

			toReturn.append("10*Pic/effort                     ");

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i])));
			}
			toReturn.append("\n");

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
	public String printAbundanceByMonthSpeciesLocElevation()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES ABUNDANCE BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("  Use maximum number of individuals per PERIOD\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(species.getName()).append("\n");

			List<ImageEntry> withSpecices = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());

			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new ImageQuery().yearOnly(year).query(withSpecices);

				if (!withSpeciesYear.isEmpty())
				{
					toReturn.append(year).append("\n");

					toReturn.append("Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> withSpeciesYearLocation = new ImageQuery().locationOnly(location).query(withSpeciesYear);

						if (!withSpeciesYearLocation.isEmpty())
						{
							toReturn.append(String.format("%-28s %6d", location.getName(), location.getElevation().intValue()));
							Integer total = 0;
							for (int i = 0; i < 12; i++)
							{
								List<ImageEntry> withSpeciesYearLocationMonth = new ImageQuery().monthOnly(i).query(withSpeciesYearLocation);
								Integer abundance = analysis.abundanceForImageList(withSpeciesYearLocationMonth, species);
								total = total + abundance;
								toReturn.append(String.format("%5d ", abundance));
							}
							toReturn.append(String.format("%7d", total));
							toReturn.append("\n");
						}
					}

					toReturn.append("Total pictures                     ");

					Integer totalPic = 0;
					int[] totalPics = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesYearMonth = new ImageQuery().monthOnly(i).query(withSpeciesYear);
						Integer totalPeriod = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> withSpeciesYearMonthLocation = new ImageQuery().locationOnly(location).query(withSpeciesYearMonth);
							Integer period = analysis.periodForImageList(withSpeciesYearMonthLocation);
							totalPic = totalPic + period;
							totalPeriod = totalPeriod + period;
							totalPics[i] = totalPics[i] + period;
						}
						toReturn.append(String.format("%5d ", totalPeriod));
					}
					toReturn.append(String.format("%7d", totalPic));
					toReturn.append("\n");

					toReturn.append("Total abundance                    ");
					Integer totalAbundancePics = 0;
					int[] totalAbundances = new int[12];
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesYearMonth = new ImageQuery().monthOnly(i).query(withSpeciesYear);
						Integer totalAbundance = 0;
						for (Location location : analysis.getAllImageLocations())
						{
							List<ImageEntry> withSpeciesYearMonthLocation = new ImageQuery().locationOnly(location).query(withSpeciesYearMonth);
							Integer abundance = analysis.abundanceForImageList(withSpeciesYearMonthLocation, species);
							totalAbundancePics = totalAbundancePics + abundance;
							totalAbundance = totalAbundance + abundance;
						}
						totalAbundances[i] = totalAbundances[i] + totalAbundance;
						toReturn.append(String.format("%5d ", totalAbundance));
					}
					toReturn.append(String.format("%7d", totalAbundancePics));
					toReturn.append("\n");

					toReturn.append("Avg abundance                      ");
					for (int i = 0; i < 12; i++)
					{
						toReturn.append(String.format("%5.2f ", totalPics[i] == 0 ? 0 : (double) totalAbundances[i] / totalPics[i]));
					}
					toReturn.append(String.format("%7.2f", totalPic == 0 ? 0 : (double) totalAbundancePics / totalPic));
					toReturn.append("\n");

					toReturn.append("Total days                            ");
					int[] daysUsed = new int[12];
					List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

					for (Location location : analysis.getAllImageLocations())
					{
						List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);

						if (!yearLocPics.isEmpty())
						{
							ImageEntry first = yearLocPics.get(0);
							ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
							LocalDateTime firstCal = first.getDateTaken();
							LocalDateTime lastCal = last.getDateTaken();
							Integer firstDaysInMonth = 31;
							Integer firstDay = firstCal.getDayOfMonth() - 1;
							Integer lastDay = lastCal.getDayOfMonth() - 1;
							Integer firstMonth = firstCal.getMonthValue() - 1;
							Integer lastMonth = lastCal.getMonthValue() - 1;
							if (firstMonth == lastMonth)
								daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
							else
							{
								daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
								firstMonth++;
								while (firstMonth < lastMonth)
								{
									daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
									firstMonth++;
								}
								daysUsed[lastMonth] = daysUsed[lastMonth] + lastDay;
							}
						}
					}
					Integer totalDays = 0;
					for (Integer month : daysUsed)
					{
						toReturn.append(String.format("%2d    ", month));
						totalDays = totalDays + month;
					}

					toReturn.append(String.format(" %3d", totalDays));

					toReturn.append("\n");

					toReturn.append("10*Pic/effort                     ");

					for (int i = 0; i < 12; i++)
					{
						toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalAbundances[i] / daysUsed[i])));
					}

					toReturn.append("\n\n");
				}
			}

			toReturn.append("SUMMARY ALL YEARS\n");

			Integer numYears = analysis.getAllImageYears().size();
			if (!analysis.getAllImageYears().isEmpty())
				toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

			toReturn.append("Location                  Elevation  Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n");

			List<ImageEntry> withSpecies = new ImageQuery().query(withSpecices);
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLocation = new ImageQuery().locationOnly(location).query(withSpecies);

				if (!withSpeciesLocation.isEmpty())
				{
					toReturn.append(String.format("%-28s %6d", location.getName(), (int) location.getElevation().intValue()));
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesLocationMonth = new ImageQuery().monthOnly(i).query(withSpeciesLocation);
						Integer abundance = 0;
						for (Integer year : analysis.getAllImageYears())
						{
							List<ImageEntry> withSpeciesLocationMonthYear = new ImageQuery().yearOnly(year).query(withSpeciesLocationMonth);
							abundance = abundance + analysis.abundanceForImageList(withSpeciesLocationMonthYear, species);
							total = total + abundance;
						}
						toReturn.append(String.format("%5d ", abundance));
					}
					toReturn.append(String.format("%7d", total));
					toReturn.append("\n");
				}
			}

			toReturn.append("Total pictures                     ");

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> withSpeciesYearMonth = new ImageQuery().monthOnly(i).query(withSpecices);
				Integer period = analysis.periodForImageList(withSpeciesYearMonth);
				totalPic = totalPic + period;
				totalPics[i] = period;
				toReturn.append(String.format("%5d ", period));
			}
			toReturn.append(String.format("%7d", totalPic));
			toReturn.append("\n");

			toReturn.append("Total abundance                    ");
			Integer totalAbundancePics = 0;
			int[] totalAbundances = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> withSpeciesMonth = new ImageQuery().monthOnly(i).query(withSpecies);
				Integer totalAbundance = 0;
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withSpeciesMonthLocation = new ImageQuery().locationOnly(location).query(withSpeciesMonth);
					Integer abundance = 0;
					for (Integer year : analysis.getAllImageYears())
					{
						List<ImageEntry> withSpeciesMonthLocationYear = new ImageQuery().yearOnly(year).query(withSpeciesMonthLocation);
						abundance = abundance + analysis.abundanceForImageList(withSpeciesMonthLocationYear, species);
					}
					totalAbundancePics = totalAbundancePics + abundance;
					totalAbundance = totalAbundance + abundance;
				}
				toReturn.append(String.format("%5d ", totalAbundance));
				totalAbundances[i] = totalAbundances[i] + totalAbundance;
			}
			toReturn.append(String.format("%7d", totalAbundancePics));
			toReturn.append("\n");

			toReturn.append("Avg abundance                      ");
			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%5.2f ", totalPics[i] == 0 ? 0 : (double) totalAbundances[i] / totalPics[i]));
			}
			toReturn.append(String.format("%7.2f", totalPic == 0 ? 0 : (double) totalAbundancePics / totalPic));
			toReturn.append("\n");

			toReturn.append("Total days                            ");

			int[] daysUsed = new int[12];
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());

				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);
					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
						LocalDateTime firstCal = first.getDateTaken();
						LocalDateTime lastCal = last.getDateTaken();
						Integer firstDaysInMonth = 31;
						Integer firstDay = firstCal.getDayOfMonth() - 1;
						Integer lastDay = lastCal.getDayOfMonth() - 1;
						Integer firstMonth = firstCal.getMonthValue() - 1;
						Integer lastMonth = lastCal.getMonthValue() - 1;
						if (firstMonth == lastMonth)
							daysUsed[firstMonth] = daysUsed[firstMonth] + (lastDay - firstDay + 1);
						else
						{
							daysUsed[firstMonth] = daysUsed[firstMonth] + (firstDaysInMonth - (firstDay - 1));
							firstMonth++;
							while (firstMonth < lastMonth)
							{
								daysUsed[firstMonth] = daysUsed[firstMonth] + 31;
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
				toReturn.append(String.format("%2d    ", month));
				totalDays = totalDays + month;
			}

			toReturn.append(String.format(" %3d", totalDays));

			toReturn.append("\n");

			toReturn.append("10*Pic/effort                     ");

			for (int i = 0; i < 12; i++)
			{
				toReturn.append(String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalAbundances[i] / daysUsed[i])));
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
	public String printSpeciesByLocElevationAndEffort()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("\n");
		toReturn.append("SUMMARY ALL YEARS\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			toReturn.append("Location                  Elevation   # pics/Effort   Percent\n");
			toReturn.append(species.getName()).append("\n");
			double[] picsOverEffortTotals = new double[analysis.getAllImageLocations().size()];
			Double picsOverEffortTotal = 0D;
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new ImageQuery().locationOnly(location).query(withSpecies);

				Integer periodTotal = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> withSpeciesLocYear = new ImageQuery().yearOnly(year).query(withSpeciesLoc);
					if (!withSpeciesLocYear.isEmpty())
					{
						periodTotal = periodTotal + analysis.periodForImageList(withSpeciesLocYear);
					}
				}

				Integer effortTotal = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> yearsPics = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
					List<ImageEntry> yearLocPics = new ImageQuery().locationOnly(location).query(yearsPics);

					if (!yearLocPics.isEmpty())
					{
						ImageEntry first = yearLocPics.get(0);
						ImageEntry last = yearLocPics.get(yearLocPics.size() - 1);
						LocalDateTime firstCal = first.getDateTaken();
						LocalDateTime lastCal = last.getDateTaken();
						Integer firstDaysInMonth = 31;
						Integer firstDay = firstCal.getDayOfMonth();
						Integer lastDay = lastCal.getDayOfMonth();
						Integer firstMonth = firstCal.getMonthValue();
						Integer lastMonth = lastCal.getMonthValue();
						if (firstMonth == lastMonth)
							effortTotal = effortTotal + (lastDay - firstDay + 1);
						else
						{
							effortTotal = effortTotal + (firstDaysInMonth - (firstDay - 1));
							firstMonth++;
							while (firstMonth < lastMonth)
							{
								effortTotal = effortTotal + 31;
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
					toReturn.append(String.format("%-28s %6.0f        %5.3f       %5.2f\n", location.getName(), location.getElevation(), picsOverEffortTotals[index], picsOverEffortTotals[index] / picsOverEffortTotal * 100.0D));
			}
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
	public String printSpeciesByLocElevationAndEffortTable()
	{
		StringBuilder toReturn = new StringBuilder("\n");

		toReturn.append("SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT TABLE\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("  Table shows frequency of all pictures normalized by effort for each species\n");

		toReturn.append("\n");

		toReturn.append("SUMMARY ALL YEARS\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("Location                  Elevation ");

		for (Species species : analysis.getAllImageSpecies())
			toReturn.append(String.format("%6s ", StringUtils.left(species.getName(), 6)));

		toReturn.append("\n");

		for (Location location : analysis.getAllImageLocations())
		{
			toReturn.append(String.format("%-28s %5.0f  ", location.getName(), location.getElevation()));

			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> bySpecies = new ImageQuery().speciesOnly(species).query(images);
				List<ImageEntry> bySpeciesAndLoc = new ImageQuery().locationOnly(location).query(bySpecies);
				Integer bySpeciesPeriod = 0;
				Integer bySpeciesAndLocPeriod = 0;

				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> bySpeciesYear = new ImageQuery().yearOnly(year).query(bySpecies);
					for (Location location2 : analysis.getAllImageLocations())
						bySpeciesPeriod = bySpeciesPeriod + analysis.periodForImageList(new ImageQuery().locationOnly(location2).query(bySpeciesYear));

					List<ImageEntry> bySpeciesLocYear = new ImageQuery().yearOnly(year).query(bySpeciesAndLoc);
					bySpeciesAndLocPeriod = bySpeciesAndLocPeriod + analysis.periodForImageList(bySpeciesLocYear);
				}

				toReturn.append(String.format("%6.2f ", bySpeciesPeriod == 0 ? 0 : 100.0D * (double) bySpeciesAndLocPeriod / bySpeciesPeriod));
			}

			toReturn.append("\n");
		}

		toReturn.append("\n");

		return toReturn.toString();
	}

}
