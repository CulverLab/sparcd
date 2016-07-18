/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TextOutputFormatter
{
	public String format(List<ImageEntry> images, Integer eventInterval)
	{
		String toReturn = "";

		if (images.isEmpty())
			return "No images found under directory";

		// location

		List<Location> locationsInList = new ArrayList<Location>();
		boolean nullLocationFound = false;
		for (ImageEntry imageEntry : images)
			if (imageEntry.getLocationTaken() != null)
			{
				if (!locationsInList.contains(imageEntry.getLocationTaken()))
					locationsInList.add(imageEntry.getLocationTaken());
			}
			else
				nullLocationFound = true;

		toReturn = toReturn + "LOCATIONS " + locationsInList.size() + "\n";
		for (Location location : locationsInList)
			toReturn = toReturn + location.getName() + " ";
		if (nullLocationFound)
			toReturn = toReturn + "Unknown ";
		toReturn = toReturn + "\n\n";

		// species

		List<Species> speciesInList = new ArrayList<Species>();
		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
				if (!speciesInList.contains(speciesEntry.getSpecies()))
					speciesInList.add(speciesEntry.getSpecies());

		toReturn = toReturn + "SPECIES " + speciesInList.size() + "\n";
		for (Species species : speciesInList)
			toReturn = toReturn + species.getName() + " ";
		toReturn = toReturn + "\n";

		// Image analysis header

		toReturn = toReturn + "FOR ALL SPECIES AT ALL LOCATIONS\n";
		toReturn = toReturn + "Number of pictures processed = " + images.size() + "\n";
		toReturn = toReturn + "Number of pictures used in activity calculation = \n";
		toReturn = toReturn + "Number of independent pictures used in analysis = \n";
		toReturn = toReturn + "Number of sequential pictures of same species at same location within a PERIOD = \n";
		toReturn = toReturn + "\n";

		// First/Last pic difference

		Date firstImageDate = null;
		Date lastImageDate = null;

		if (!images.isEmpty())
		{
			firstImageDate = images.get(0).getDateTaken();
			lastImageDate = images.get(0).getDateTaken();
		}
		for (ImageEntry imageEntry : images)
		{
			Date current = imageEntry.getDateTaken();
			if (current.before(firstImageDate))
				firstImageDate = current;
			else if (current.after(lastImageDate))
				lastImageDate = current;
		}

		toReturn = toReturn + "NUMBER OF DAYS IN CAMERA TRAP PROGRAM = " + daysBetween(firstImageDate, lastImageDate) + "\n";
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstImageDate);
		toReturn = toReturn + "First picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		calendar.setTime(lastImageDate);
		toReturn = toReturn + "Last picture: Year = " + calendar.get(Calendar.YEAR) + " Month = " + calendar.get(Calendar.MONTH) + " Day = " + calendar.get(Calendar.DAY_OF_MONTH) + "\n";
		toReturn = toReturn + "\n";

		// First pic of each species

		Map<Species, ImageEntry> speciesToDateFirst = new HashMap<Species, ImageEntry>();
		Map<Species, ImageEntry> speciesToDateLast = new HashMap<Species, ImageEntry>();
		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
			{
				Species currentSpeices = speciesEntry.getSpecies();
				// Check first date
				if (!speciesToDateFirst.containsKey(currentSpeices))
					speciesToDateFirst.put(currentSpeices, imageEntry);
				else
				{
					Date currentFirst = speciesToDateFirst.get(currentSpeices).getDateTaken();
					if (imageEntry.getDateTaken().before(currentFirst))
					{
						speciesToDateFirst.remove(currentSpeices);
						speciesToDateFirst.put(currentSpeices, imageEntry);
					}
				}
				// Check last date
				if (!speciesToDateLast.containsKey(currentSpeices))
					speciesToDateLast.put(currentSpeices, imageEntry);
				else
				{
					Date currentLast = speciesToDateLast.get(currentSpeices).getDateTaken();
					if (imageEntry.getDateTaken().after(currentLast))
					{
						speciesToDateLast.remove(currentSpeices);
						speciesToDateLast.put(currentSpeices, imageEntry);
					}
				}
			}

		toReturn = toReturn + "FIRST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToDateFirst.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = Calendar.getInstance();
			dateToPrint.setTime(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s\n", speciesToPrint, daysBetween(firstImageDate, dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH), dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(Calendar.HOUR),
					dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()));
		}
		toReturn = toReturn + "\n";
		toReturn = toReturn + "LAST PICTURE OF EACH SPECIES\n";
		toReturn = toReturn + "Species                      Days  Year Month Day Hour Minute Second Location                   Duration\n";
		for (Map.Entry<Species, ImageEntry> entry : speciesToDateLast.entrySet())
		{
			Species speciesToPrint = entry.getKey();
			ImageEntry imageToPrint = entry.getValue();
			Calendar dateToPrint = Calendar.getInstance();
			dateToPrint.setTime(imageToPrint.getDateTaken());
			toReturn = toReturn + String.format("%-28s %4d  %4d %4d %4d %3d %5d %6d   %-28s %4d\n", speciesToPrint, daysBetween(firstImageDate, dateToPrint.getTime()), dateToPrint.get(Calendar.YEAR), dateToPrint.get(Calendar.MONTH), dateToPrint.get(Calendar.DAY_OF_MONTH), dateToPrint.get(
					Calendar.HOUR), dateToPrint.get(Calendar.MINUTE), dateToPrint.get(Calendar.SECOND), (imageToPrint.getLocationTaken() == null ? "Unknown" : imageToPrint.getLocationTaken().getName()), daysBetween(speciesToDateFirst.get(speciesToPrint).getDateTaken(), dateToPrint.getTime()));
		}

		toReturn = toReturn + "\n";

		// ACCUMULATION CURVE

		List<Map.Entry<Species, ImageEntry>> firstImageEntriesSorted = new ArrayList<Map.Entry<Species, ImageEntry>>(speciesToDateFirst.entrySet());
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
			toReturn = toReturn + String.format("%5d     %3d      %s\n", daysBetween(firstImageDate, entry.getValue().getDateTaken()), ++number, entry.getKey().getName());

		toReturn = toReturn + "\n";

		// NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR
		// Jim's program added 1 to counts greater than 1 in activity count, fixed the issue
		// Jim's program added 1 to counts greater than 1 in period count, fixed the issue
		// Jim's program calculations for Abundance were completely wrong and made no sense
		// When running DataAnalyze, the last period can have X elements. The last period is being added to "Abundance" X times instead of once.
		// ALL = number of images containing the species
		// ACTIVITY = Number of periods containing at least one image in a single hour (ex. 1-1:59, 2-2:59, etc) 
		// PERIOD = Consecutive images that are less than "period" apart where period comes from user input
		// ABUNDANCE = Maximum number of animals photographed in a single image in each period

		Map<Integer, Integer> yearToNumberImages = new HashMap<Integer, Integer>();
		Map<Integer, Integer> yearToActivity = new HashMap<Integer, Integer>();
		Map<Integer, Integer> yearToPeriod = new HashMap<Integer, Integer>();
		Map<Integer, Integer> yearToAbundance = new HashMap<Integer, Integer>();
		Map<Integer, List<Location>> yearToLocations = new HashMap<Integer, List<Location>>();

		toReturn = toReturn + "NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR\n";
		toReturn = toReturn + "        Year       All Activity   Period Abundance Locations\n";

		for (ImageEntry entry : images)
		{
			calendar.setTime(entry.getDateTaken());
			int year = calendar.get(Calendar.YEAR);
			yearToNumberImages.put(year, yearToNumberImages.getOrDefault(year, 0) + 1);
		}

		Collections.sort(images, new Comparator<ImageEntry>()
		{
			@Override
			public int compare(ImageEntry entry1, ImageEntry entry2)
			{
				return entry1.getDateTaken().compareTo(entry2.getDateTaken());
			}
		});

		int oldHour = -1;
		int oldDay = -1;
		int oldYear = -1;
		for (ImageEntry entry : images)
		{
			calendar.setTime(entry.getDateTaken());
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int day = calendar.get(Calendar.DAY_OF_YEAR);
			int year = calendar.get(Calendar.YEAR);
			if ((hour != oldHour) || (oldDay != day) || (oldYear != year))
			{
				yearToActivity.put(year, yearToActivity.getOrDefault(year, 0) + 1);
				oldHour = hour;
				oldDay = day;
				oldYear = year;
			}
		}

		long lastImageTimeMillis = 0;
		for (ImageEntry entry : images)
		{
			long imageTimeMillis = entry.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;
			if (differenceMinutes >= eventInterval)
			{
				calendar.setTime(entry.getDateTaken());
				int year = calendar.get(Calendar.YEAR);
				yearToPeriod.put(year, yearToPeriod.getOrDefault(year, 0) + 1);
			}
			lastImageTimeMillis = imageTimeMillis;
		}

		//yearToAbundance
		lastImageTimeMillis = 0;
		Integer maxAnimalsInEvent = 0;
		for (ImageEntry entry : images)
		{
			long imageTimeMillis = entry.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;
			if (differenceMinutes >= eventInterval)
			{
				for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
					maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.getAmount());
				calendar.setTime(entry.getDateTaken());
				int year = calendar.get(Calendar.YEAR);
				yearToAbundance.put(year, yearToAbundance.getOrDefault(year, 0) + maxAnimalsInEvent);
				maxAnimalsInEvent = 0;
			}
			lastImageTimeMillis = imageTimeMillis;
		}

		for (ImageEntry entry : images)
		{
			calendar.setTime(entry.getDateTaken());
			int year = calendar.get(Calendar.YEAR);
			List<Location> current = yearToLocations.getOrDefault(year, new ArrayList<Location>());
			if (!current.contains(entry.getLocationTaken()))
			{
				current.add(entry.getLocationTaken());
				yearToLocations.put(year, current);
			}
		}

		int imageTotal = 0;
		int activityTotal = 0;
		int periodTotal = 0;
		int abundanceTotal = 0;
		int locationTotal = 0;
		for (Integer year : yearToNumberImages.keySet())
		{
			imageTotal = imageTotal + yearToNumberImages.getOrDefault(year, -1);
			activityTotal = activityTotal + yearToActivity.getOrDefault(year, -1);
			periodTotal = periodTotal + yearToPeriod.getOrDefault(year, -1);
			abundanceTotal = abundanceTotal + yearToAbundance.getOrDefault(year, -1);
			locationTotal = locationTotal + yearToLocations.getOrDefault(year, new ArrayList<Location>()).size();
			toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, yearToNumberImages.getOrDefault(year, -1), yearToActivity.getOrDefault(year, -1), yearToPeriod.getOrDefault(year, -1), yearToAbundance.getOrDefault(year, -1), yearToLocations.getOrDefault(year,
					new ArrayList<Location>()).size());
		}

		toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", imageTotal, activityTotal, periodTotal, abundanceTotal, locationTotal);
		toReturn = toReturn + "\n";

		toReturn = toReturn + "NUMBER OF PICTURES BY SPECIES BY YEAR\n";

		for (Species species : speciesInList)
		{
			yearToNumberImages.clear();
			yearToActivity.clear();
			yearToPeriod.clear();
			yearToAbundance.clear();
			yearToLocations.clear();

			List<ImageEntry> imagesWithSpecies = images.stream().filter(new Predicate<ImageEntry>()
			{
				@Override
				public boolean test(ImageEntry entry)
				{
					for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
						if (speciesEntry.getSpecies().equals(species))
							return true;
					return false;
				}
			}).collect(Collectors.<ImageEntry> toList());

			toReturn = toReturn + "  " + species.getName() + "\n";
			toReturn = toReturn + "        Year       All Activity   Period Abundance Locations\n";

			// COPY + PASTE BEGIN

			for (ImageEntry entry : imagesWithSpecies)
			{
				calendar.setTime(entry.getDateTaken());
				int year = calendar.get(Calendar.YEAR);
				yearToNumberImages.put(year, yearToNumberImages.getOrDefault(year, 0) + 1);
			}

			oldHour = -1;
			oldDay = -1;
			oldYear = -1;
			for (ImageEntry entry : imagesWithSpecies)
			{
				calendar.setTime(entry.getDateTaken());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int day = calendar.get(Calendar.DAY_OF_YEAR);
				int year = calendar.get(Calendar.YEAR);
				if ((hour != oldHour) || (oldDay != day) || (oldYear != year))
				{
					yearToActivity.put(year, yearToActivity.getOrDefault(year, 0) + 1);
					oldHour = hour;
					oldDay = day;
					oldYear = year;
				}
			}

			lastImageTimeMillis = 0;
			for (ImageEntry entry : imagesWithSpecies)
			{
				long imageTimeMillis = entry.getDateTaken().getTime();
				long differenceMillis = imageTimeMillis - lastImageTimeMillis;
				long differenceMinutes = differenceMillis / 1000 / 60;
				if (differenceMinutes >= eventInterval)
				{
					calendar.setTime(entry.getDateTaken());
					int year = calendar.get(Calendar.YEAR);
					yearToPeriod.put(year, yearToPeriod.getOrDefault(year, 0) + 1);
				}
				lastImageTimeMillis = imageTimeMillis;
			}

			lastImageTimeMillis = 0;
			maxAnimalsInEvent = 0;
			for (ImageEntry entry : imagesWithSpecies)
			{
				long imageTimeMillis = entry.getDateTaken().getTime();
				long differenceMillis = imageTimeMillis - lastImageTimeMillis;
				long differenceMinutes = differenceMillis / 1000 / 60;
				if (differenceMinutes >= eventInterval)
				{
					for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
						maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.getAmount());
					calendar.setTime(entry.getDateTaken());
					int year = calendar.get(Calendar.YEAR);
					yearToAbundance.put(year, yearToAbundance.getOrDefault(year, 0) + maxAnimalsInEvent);
					maxAnimalsInEvent = 0;
				}
				lastImageTimeMillis = imageTimeMillis;
			}

			for (ImageEntry entry : imagesWithSpecies)
			{
				calendar.setTime(entry.getDateTaken());
				int year = calendar.get(Calendar.YEAR);
				List<Location> current = yearToLocations.getOrDefault(year, new ArrayList<Location>());
				if (!current.contains(entry.getLocationTaken()))
				{
					current.add(entry.getLocationTaken());
					yearToLocations.put(year, current);
				}
			}

			imageTotal = 0;
			activityTotal = 0;
			periodTotal = 0;
			abundanceTotal = 0;
			locationTotal = 0;
			for (Integer year : yearToNumberImages.keySet())
			{
				imageTotal = imageTotal + yearToNumberImages.getOrDefault(year, -1);
				activityTotal = activityTotal + yearToActivity.getOrDefault(year, -1);
				periodTotal = periodTotal + yearToPeriod.getOrDefault(year, -1);
				abundanceTotal = abundanceTotal + yearToAbundance.getOrDefault(year, -1);
				locationTotal = locationTotal + yearToLocations.getOrDefault(year, new ArrayList<Location>()).size();
				toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, yearToNumberImages.getOrDefault(year, -1), yearToActivity.getOrDefault(year, -1), yearToPeriod.getOrDefault(year, -1), yearToAbundance.getOrDefault(year, -1), yearToLocations.getOrDefault(year,
						new ArrayList<Location>()).size());
			}

			toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", imageTotal, activityTotal, periodTotal, abundanceTotal, locationTotal);
			toReturn = toReturn + "\n";

			// COPY + PASTE END
		}

		//		Map<Integer, Map<Species, Integer>> yearToSpeciesToNumberPhotographed = new HashMap<Integer, Map<Species, Integer>>();
		//		for (ImageEntry entry : images)
		//			for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
		//			{
		//				Species currentSpecies = speciesEntry.getSpecies();
		//				calendar.setTime(entry.getDateTaken());
		//				// Get the map for the current year
		//				Map<Species, Integer> speciesToNumberPhotographed = yearToSpeciesToNumberPhotographed.getOrDefault(calendar.get(Calendar.YEAR), new HashMap<Species, Integer>());
		//				// Put the species -> number mapping into the map
		//				speciesToNumberPhotographed.put(currentSpecies, speciesToNumberPhotographed.getOrDefault(currentSpecies, 0) + 1);
		//				// Put the map for the year back into the mapping from year -> species -> number of species
		//				yearToSpeciesToNumberPhotographed.put(calendar.get(Calendar.YEAR), speciesToNumberPhotographed);
		//			}

		//		for (Map.Entry<Integer, Map<Species, Integer>> year : yearToSpeciesToNumberPhotographed.entrySet())
		//		{
		//			int imageTotalInYear = 0;
		//			for (Map.Entry<Species, Integer> entryInYear : year.getValue().entrySet())
		//			{
		//				imageTotalInYear = imageTotalInYear + entryInYear.getValue();
		//			}
		//			toReturn = toReturn + String.format("        %4d   %7d\n", year.getKey(), imageTotalInYear);
		//		}

		return toReturn;
	}

	private long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}
}
