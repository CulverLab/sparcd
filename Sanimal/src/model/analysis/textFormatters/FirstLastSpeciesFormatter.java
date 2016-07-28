/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.SanimalAnalysisUtils;

public class FirstLastSpeciesFormatter extends TextFormatter
{
	public FirstLastSpeciesFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printDaysInCameraTrap()
	{
		String toReturn = "";

		Date firstImageDate = analysis.getImagesSortedByDate().get(0).getDateTaken();
		Date lastImageDate = analysis.getImagesSortedByDate().get(analysis.getImagesSortedByDate().size() - 1).getDateTaken();

		toReturn = toReturn + "NUMBER OF DAYS IN CAMERA TRAP PROGRAM = " + SanimalAnalysisUtils.daysBetween(firstImageDate, lastImageDate) + "\n";
		Calendar calendar = DateUtils.toCalendar(firstImageDate);
		toReturn = toReturn + "First picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		calendar.setTime(lastImageDate);
		toReturn = toReturn + "Last picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printFirstPicOfEachSpecies()
	{
		String toReturn = "";

		Map<Species, ImageEntry> speciesToFirstImage = analysis.getSpeciesToFirstImage();

		toReturn = toReturn + "FIRST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToFirstImage.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = DateUtils.toCalendar(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s\n", speciesToPrint, SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH), dateToPrint
					.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()));
		}
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printLastPicOfEachSpecies()
	{
		String toReturn = "";

		Map<Species, ImageEntry> speciesToLastImage = analysis.getSpeciesToLastImage();

		toReturn = toReturn + "LAST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location                   Duration\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToLastImage.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = DateUtils.toCalendar(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s %4d\n", speciesToPrint, SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH),
					dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()), SanimalAnalysisUtils.daysBetween(analysis
							.getSpeciesToFirstImage().get(speciesToPrint).getDateTaken(), dateToPrint.getTime()));
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printSpeciesAccumulationCurve()
	{
		String toReturn = "";

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
			toReturn = toReturn + String.format("%5d     %3d      %s\n", SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), entry.getValue().getDateTaken()) + 1, ++number, entry.getKey().getName());

		toReturn = toReturn + "\n";

		return toReturn;
	}
}
