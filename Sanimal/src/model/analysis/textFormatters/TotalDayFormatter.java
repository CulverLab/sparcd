/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;
import model.analysis.SanimalAnalysisUtils;

public class TotalDayFormatter extends TextFormatter
{
	public TotalDayFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

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
			Calendar firstCal = DateUtils.toCalendar(first.getDateTaken());
			Calendar lastCal = DateUtils.toCalendar(last.getDateTaken());
			Integer firstDaysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer lastDaysInMonth = lastCal.getActualMaximum(Calendar.DAY_OF_MONTH);
			Integer firstDay = firstCal.get(Calendar.DAY_OF_MONTH);
			Integer lastDay = lastCal.get(Calendar.DAY_OF_MONTH);
			Integer firstMonth = firstCal.get(Calendar.MONTH);
			Integer lastMonth = lastCal.get(Calendar.MONTH);
			Calendar calendar = Calendar.getInstance();
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

		return toReturn;
	}

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
				List<ImageEntry> imagesBySpeciesAndYear = new PredicateBuilder().yearOnly(year).speciesOnly(species).query(images);
				toReturn = toReturn + String.format("%-28s", species.getName());
				Integer total = 0;
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesBySpeciesYearAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpeciesAndYear);
					Integer period = analysis.periodForImageList(imagesBySpeciesYearAndMonth, eventInterval);
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
				List<ImageEntry> imagesByYearAndMonth = new PredicateBuilder().monthOnly(i).yearOnly(year).query(images);
				Integer period = analysis.periodForImageList(imagesByYearAndMonth, eventInterval);
				totalPic = totalPic + period;
				totalPics[i] = period;
				toReturn = toReturn + String.format("%5d ", period);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                     ";
			List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

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

		return toReturn;
	}

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

			List<ImageEntry> imagesBySpecies = new PredicateBuilder().speciesOnly(species).query(images);
			Integer total = 0;
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> imagesBySpeciesAndMonth = new PredicateBuilder().monthOnly(i).query(imagesBySpecies);
				Integer period = analysis.periodForImageList(imagesBySpeciesAndMonth, eventInterval);
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
			List<ImageEntry> imagesByMonth = new PredicateBuilder().monthOnly(i).query(images);
			Integer period = analysis.periodForImageList(imagesByMonth, eventInterval);
			totalPic = totalPic + period;
			totalPics[i] = period;
			toReturn = toReturn + String.format("%5d ", period);
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
						Integer period = analysis.periodForImageList(imagesBySpeciesYearLocationAndMonth, eventInterval);
						total = total + period;
						toReturn = toReturn + String.format("%5d ", period);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}
				toReturn = toReturn + "Total pictures                     ";

				Integer totalPic = 0;
				int[] totalPics = new int[12];
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> imagesByYearSpeciesAndMonth = new PredicateBuilder().speciesOnly(species).monthOnly(i).yearOnly(year).query(images);
					Integer period = analysis.periodForImageList(imagesByYearSpeciesAndMonth, eventInterval);
					totalPic = totalPic + period;
					totalPics[i] = period;
					toReturn = toReturn + String.format("%5d ", period);
				}
				toReturn = toReturn + String.format("%7d", totalPic);
				toReturn = toReturn + "\n";

				toReturn = toReturn + "Total days                            ";
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

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

				toReturn = toReturn + "10*Pic/effort                     ";

				for (int i = 0; i < 12; i++)
				{
					toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
				}

			}
			toReturn = toReturn + "\n\n";

			toReturn = toReturn + "SUMMARY ALL YEARS\n";

			Integer numYears = analysis.getAllImageYears().size();
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

			Integer totalPic = 0;
			int[] totalPics = new int[12];
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

			int[] daysUsed = new int[]
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

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

	public String printAbundanceByMonthSpeciesLocElevation()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES ABUNDANCE BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "  Use maximum number of individuals per PERIOD\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + species.getName() + "\n";

			List<ImageEntry> withSpecices = new PredicateBuilder().speciesOnly(species).query(images);

			for (Integer year : analysis.getAllImageYears())
			{
				toReturn = toReturn + year + "\n";

				toReturn = toReturn + "Location                 Elevation   Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

				List<ImageEntry> withSpeciesYear = new PredicateBuilder().yearOnly(year).query(withSpecices);

				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withSpeciesYearLocation = new PredicateBuilder().locationOnly(location).query(withSpeciesYear);

					toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
					Integer total = 0;
					for (int i = 0; i < 12; i++)
					{
						List<ImageEntry> withSpeciesYearLocationMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesYearLocation);
						Integer abundance = analysis.abundanceForImageList(withSpeciesYearLocationMonth, eventInterval);
						total = total + abundance;
						toReturn = toReturn + String.format("%5d ", abundance);
					}
					toReturn = toReturn + String.format("%7d", total);
					toReturn = toReturn + "\n";
				}

				toReturn = toReturn + "Total pictures                     ";

				Integer totalPic = 0;
				int[] totalPics = new int[12];
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> withSpeciesYearMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesYear);
					Integer abundance = analysis.abundanceForImageList(withSpeciesYearMonth, eventInterval);
					totalPic = totalPic + abundance;
					totalPics[i] = abundance;
					toReturn = toReturn + String.format("%5d ", abundance);
				}
				toReturn = toReturn + String.format("%7d", totalPic);
				toReturn = toReturn + "\n";

				toReturn = toReturn + "Total days                            ";
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

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

				toReturn = toReturn + "10*Pic/effort                     ";

				for (int i = 0; i < 12; i++)
				{
					toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
				}

				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "\n";

			toReturn = toReturn + "SUMMARY ALL YEARS\n";

			Integer numYears = analysis.getAllImageYears().size();
			if (!analysis.getAllImageYears().isEmpty())
				toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

			toReturn = toReturn + "Location                 Elevation   Jan   Feb   Mar   Apr   May   Jun   Jul   Aug   Sep   Oct   Nov   Dec   Total\n";

			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLocation = new PredicateBuilder().locationOnly(location).query(withSpecices);

				toReturn = toReturn + String.format("%-28s %6d", location.getName(), (int) location.getElevation());
				Integer total = 0;
				for (int i = 0; i < 12; i++)
				{
					List<ImageEntry> withSpeciesLocationMonth = new PredicateBuilder().monthOnly(i).query(withSpeciesLocation);
					Integer abundance = analysis.abundanceForImageList(withSpeciesLocationMonth, eventInterval);
					total = total + abundance;
					toReturn = toReturn + String.format("%5d ", abundance);
				}
				toReturn = toReturn + String.format("%7d", total);
				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "Total pictures                     ";

			Integer totalPic = 0;
			int[] totalPics = new int[12];
			for (int i = 0; i < 12; i++)
			{
				List<ImageEntry> withSpeciesYearMonth = new PredicateBuilder().monthOnly(i).query(withSpecices);
				Integer abundance = analysis.abundanceForImageList(withSpeciesYearMonth, eventInterval);
				totalPic = totalPic + abundance;
				totalPics[i] = abundance;
				toReturn = toReturn + String.format("%5d ", abundance);
			}
			toReturn = toReturn + String.format("%7d", totalPic);
			toReturn = toReturn + "\n";

			toReturn = toReturn + "Total days                            ";

			int[] daysUsed = new int[]
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> yearsPics = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());

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

			toReturn = toReturn + "10*Pic/effort                     ";

			for (int i = 0; i < 12; i++)
			{
				toReturn = toReturn + String.format("%6.2f", daysUsed[i] == 0 ? 0D : 10D * ((double) totalPics[i] / daysUsed[i]));
			}

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

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
				Integer numPics = withSpeciesLoc.size();

				Integer effort = 0;
				for (Integer year : analysis.getAllImageYears())
				{
					List<ImageEntry> withSpeciesLocYear = new PredicateBuilder().yearOnly(year).query(withSpeciesLoc);
					if (!withSpeciesLocYear.isEmpty())
					{
						Date first = withSpeciesLocYear.get(0).getDateTaken();
						Date last = withSpeciesLocYear.get(withSpeciesLocYear.size() - 1).getDateTaken();
						effort = effort + Math.toIntExact(SanimalAnalysisUtils.daysBetween(first, last)) + 1;
					}
				}

				Double picsOverEffort = (double) numPics / effort;

				picsOverEffortTotal = picsOverEffortTotal + picsOverEffort;
				picsOverEffortTotals[analysis.getAllImageLocations().indexOf(location)] = picsOverEffort;
			}

			for (Location location : analysis.getAllImageLocations())
			{
				Integer index = analysis.getAllImageLocations().indexOf(location);
				toReturn = toReturn + String.format("%-28s %6.0f        %5.3f       %5.2f\n", location.getName(), location.getElevation(), picsOverEffortTotals[index], picsOverEffortTotals[index] / picsOverEffortTotal * 100.0D);
			}

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

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

				toReturn = toReturn + String.format("%6.2f ", bySpecies.size() == 0 ? 0 : 100.0D * (double) bySpeciesAndLoc.size() / bySpecies.size());
			}

			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

}
