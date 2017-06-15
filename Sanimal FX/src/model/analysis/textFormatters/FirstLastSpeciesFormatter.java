package model.analysis.textFormatters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import model.analysis.DataAnalysis;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.species.Species;

/**
 * The text formatter for first and last images taken of a species
 * 
 * @author David Slovikosky
 */
public class FirstLastSpeciesFormatter extends TextFormatter
{
	public FirstLastSpeciesFormatter(List<ImageEntry> images, DataAnalysis analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Self explanatory
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printDaysInCameraTrap()
	{
		String toReturn = "";

		Date firstImageDate = analysis.getImagesSortedByDate().get(0).getDateTaken();
		Date lastImageDate = analysis.getImagesSortedByDate().get(analysis.getImagesSortedByDate().size() - 1).getDateTaken();

		toReturn = toReturn + "NUMBER OF DAYS IN CAMERA TRAP PROGRAM = " + (SanimalAnalysisUtils.daysBetween(firstImageDate, lastImageDate) + 1) + "\n";
		Calendar calendar = DateUtils.toCalendar(firstImageDate);
		toReturn = toReturn + "First picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + (calendar.get(Calendar.MONTH) + 1) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		calendar.setTime(lastImageDate);
		toReturn = toReturn + "Last picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + (calendar.get(Calendar.MONTH) + 1) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		toReturn = toReturn + "\n";

		return toReturn;
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species to be analyzed, the day of the study, and the year, month, day, hour, minute, and location where the species was first
	 * recorded.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printFirstPicOfEachSpecies()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("FIRST PICTURE OF EACH SPECIES\n");
		toReturn.append("Species                      Days  Year Month Day Hour Minute Second Location\n");

		for (Species speciesToPrint : analysis.getAllImageSpecies())
		{
			ImageEntry imageToPrint = analysis.getFirstImageInList(new ImageQuery().speciesOnly(speciesToPrint).query(images));
			Calendar dateToPrint = DateUtils.toCalendar(imageToPrint.getDateTaken());
			toReturn.append(String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s\n", speciesToPrint, SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), dateToPrint.getTime()) + 1, dateToPrint.get(Calendar.YEAR), (dateToPrint.get(Calendar.MONTH) + 1),
					dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR_OF_DAY), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName())));
		}
		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species to be analyzed, the day of the study, and the year, month, day, hour, minute, and location where the species was last
	 * recorded.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLastPicOfEachSpecies()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("LAST PICTURE OF EACH SPECIES\n");
		toReturn.append("Species                      Days  Year Month Day Hour Minute Second Location                   Duration\n");
		for (Species speciesToPrint : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(speciesToPrint).query(images);
			ImageEntry imageToPrint = analysis.getLastImageInList(withSpecies);
			ImageEntry firstImage = analysis.getFirstImageInList(withSpecies);
			Calendar dateToPrint = DateUtils.toCalendar(imageToPrint.getDateTaken());
			toReturn.append(String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s %4d\n", speciesToPrint, SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), dateToPrint.getTime()) + 1, dateToPrint.get(Calendar.YEAR), (dateToPrint.get(Calendar.MONTH)
					+ 1), dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR_OF_DAY), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()), SanimalAnalysisUtils
					.daysBetween(firstImage.getDateTaken(), imageToPrint.getDateTaken()) + 1));
		}

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The day of the study that a new species was recorded, the total number of new species records, and the name of the species that was (were)
	 * recorded.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesAccumulationCurve()
	{
		StringBuilder toReturn = new StringBuilder();

		List<Pair<Species, ImageEntry>> speciesFirstImage = new ArrayList<Pair<Species, ImageEntry>>();

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			if (!imagesWithSpecies.isEmpty())
				speciesFirstImage.add(Pair.of(species, imagesWithSpecies.get(0)));
		}

		speciesFirstImage.sort(Comparator.comparing(pair -> pair.getRight().getDateTaken()));

		toReturn.append("SPECIES ACCUMULATION CURVE\n");
		toReturn.append("  DAY    NUMBER    SPECIES\n");
		int number = 0;
		for (Pair<Species, ImageEntry> entry : speciesFirstImage)
			toReturn.append(String.format("%5d     %3d      %s\n", SanimalAnalysisUtils.daysBetween(analysis.getImagesSortedByDate().get(0).getDateTaken(), entry.getValue().getDateTaken()) + 1, ++number, entry.getKey().getName()));

		toReturn.append("\n");

		return toReturn.toString();
	}
}
