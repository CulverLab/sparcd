/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

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

public class DataAnalysis
{
	private List<Location> allImageLocations = new ArrayList<Location>();
	private boolean nullLocationsFound = false;
	private List<Species> allImageSpecies = new ArrayList<Species>();
	private List<Integer> allImageYears = new ArrayList<Integer>();
	private List<ImageEntry> imagesSortedByDate;
	private Map<Species, ImageEntry> speciesToFirstImage = new HashMap<Species, ImageEntry>();
	private Map<Species, ImageEntry> speciesToLastImage = new HashMap<Species, ImageEntry>();
	private Map<Species, List<ImageEntry>> speciesToImageList = new HashMap<Species, List<ImageEntry>>();

	private Map<Species, Map<Integer, Integer>> yearToNumberImages = new HashMap<Species, Map<Integer, Integer>>();
	private Map<Species, Map<Integer, Integer>> yearToActivity = new HashMap<Species, Map<Integer, Integer>>();
	private Map<Species, Map<Integer, Integer>> yearToPeriod = new HashMap<Species, Map<Integer, Integer>>();
	private Map<Species, Map<Integer, Integer>> yearToAbundance = new HashMap<Species, Map<Integer, Integer>>();
	private Map<Species, Map<Integer, List<Location>>> yearToLocations = new HashMap<Species, Map<Integer, List<Location>>>();

	public DataAnalysis(List<ImageEntry> images, Integer eventInterval)
	{
		for (ImageEntry entry : images)
			if (entry.getLocationTaken() != null)
			{
				if (!allImageLocations.contains(entry.getLocationTaken()))
					allImageLocations.add(entry.getLocationTaken());
			}
			else
				nullLocationsFound = true;

		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
				if (!allImageSpecies.contains(speciesEntry.getSpecies()))
					allImageSpecies.add(speciesEntry.getSpecies());

		for (ImageEntry imageEntry : images)
		{
			Integer year = this.getCalendar(imageEntry.getDateTaken()).get(Calendar.YEAR);
			if (!allImageYears.contains(year))
				allImageYears.add(year);
		}

		imagesSortedByDate = new ArrayList<ImageEntry>(images);
		Collections.sort(imagesSortedByDate, new Comparator<ImageEntry>()
		{
			@Override
			public int compare(ImageEntry entry1, ImageEntry entry2)
			{
				return entry1.getDateTaken().compareTo(entry2.getDateTaken());
			}
		});

		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
			{
				Species currentSpeices = speciesEntry.getSpecies();
				// Check first date				
				Date currentFirst = speciesToFirstImage.getOrDefault(currentSpeices, imageEntry).getDateTaken();
				if (imageEntry.getDateTaken().before(currentFirst) || imageEntry.getDateTaken().equals(currentFirst))
					speciesToFirstImage.put(currentSpeices, imageEntry);

				// Check last date
				Date currentLast = speciesToLastImage.getOrDefault(currentSpeices, imageEntry).getDateTaken();
				if (imageEntry.getDateTaken().after(currentLast) || imageEntry.getDateTaken().equals(currentLast))
					speciesToLastImage.put(currentSpeices, imageEntry);
			}

		for (Species species : this.allImageSpecies)
		{
			this.speciesToImageList.put(species, images.stream().filter(new Predicate<ImageEntry>()
			{
				@Override
				public boolean test(ImageEntry entry)
				{
					for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
						if (speciesEntry.getSpecies().equals(species))
							return true;
					return false;
				}
			}).collect(Collectors.<ImageEntry> toList()));
		}

		for (Map.Entry<Species, List<ImageEntry>> entry : speciesToImageList.entrySet())
		{
			// ALL images
			Map<Integer, Integer> currentMap = yearToNumberImages.getOrDefault(entry.getKey(), new HashMap<Integer, Integer>());
			for (ImageEntry image : entry.getValue())
			{
				int year = this.getCalendar(image.getDateTaken()).get(Calendar.YEAR);
				currentMap.put(year, currentMap.getOrDefault(year, 0) + 1);
			}
			yearToNumberImages.put(entry.getKey(), currentMap);

			// Activity
			currentMap = yearToActivity.getOrDefault(entry.getKey(), new HashMap<Integer, Integer>());
			int oldHour = -1;
			int oldDay = -1;
			int oldYear = -1;
			for (ImageEntry image : entry.getValue())
			{
				Calendar calendar = this.getCalendar(image.getDateTaken());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int day = calendar.get(Calendar.DAY_OF_YEAR);
				int year = calendar.get(Calendar.YEAR);
				if ((hour != oldHour) || (oldDay != day) || (oldYear != year))
				{
					currentMap.put(year, currentMap.getOrDefault(year, 0) + 1);
					oldHour = hour;
					oldDay = day;
					oldYear = year;
				}
			}
			yearToActivity.put(entry.getKey(), currentMap);

			// Period
			currentMap = yearToPeriod.getOrDefault(entry.getKey(), new HashMap<Integer, Integer>());
			long lastImageTimeMillis = 0;
			for (ImageEntry image : entry.getValue())
			{
				long imageTimeMillis = image.getDateTaken().getTime();
				long differenceMillis = imageTimeMillis - lastImageTimeMillis;
				long differenceMinutes = differenceMillis / 1000 / 60;
				if (differenceMinutes >= eventInterval)
				{
					int year = this.getCalendar(image.getDateTaken()).get(Calendar.YEAR);
					currentMap.put(year, currentMap.getOrDefault(year, 0) + 1);
				}
				lastImageTimeMillis = imageTimeMillis;
			}
			yearToPeriod.put(entry.getKey(), currentMap);

			// Abundance
			currentMap = yearToAbundance.getOrDefault(entry.getKey(), new HashMap<Integer, Integer>());
			lastImageTimeMillis = 0;
			Integer maxAnimalsInEvent = 0;
			for (ImageEntry image : entry.getValue())
			{
				long imageTimeMillis = image.getDateTaken().getTime();
				long differenceMillis = imageTimeMillis - lastImageTimeMillis;
				long differenceMinutes = differenceMillis / 1000 / 60;
				if (differenceMinutes >= eventInterval)
				{
					for (SpeciesEntry speciesEntry : image.getSpeciesPresent())
						maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.getAmount());
					int year = this.getCalendar(image.getDateTaken()).get(Calendar.YEAR);
					currentMap.put(year, currentMap.getOrDefault(year, 0) + maxAnimalsInEvent);
					maxAnimalsInEvent = 0;
				}
				lastImageTimeMillis = imageTimeMillis;
			}
			yearToAbundance.put(entry.getKey(), currentMap);

			// Location
			Map<Integer, List<Location>> currentMapLocation = yearToLocations.getOrDefault(entry.getKey(), new HashMap<Integer, List<Location>>());
			for (ImageEntry image : entry.getValue())
			{
				int year = this.getCalendar(image.getDateTaken()).get(Calendar.YEAR);
				List<Location> currentLocations = currentMapLocation.getOrDefault(year, new ArrayList<Location>());
				if (!currentLocations.contains(image.getLocationTaken()))
				{
					currentLocations.add(image.getLocationTaken());
					currentMapLocation.put(year, currentLocations);
				}
			}
			yearToLocations.put(entry.getKey(), currentMapLocation);
		}
	}

	public List<Location> getAllImageLocations()
	{
		return allImageLocations;
	}

	public boolean nullLocationsFound()
	{
		return nullLocationsFound;
	}

	public List<Species> getAllImageSpecies()
	{
		return allImageSpecies;
	}

	public List<Integer> getAllImageYears()
	{
		return allImageYears;
	}

	public List<ImageEntry> getImagesSortedByDate()
	{
		return imagesSortedByDate;
	}

	public Map<Species, ImageEntry> getSpeciesToFirstImage()
	{
		return speciesToFirstImage;
	}

	public Map<Species, ImageEntry> getSpeciesToLastImage()
	{
		return speciesToLastImage;
	}

	public Calendar getCalendar(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public Map<Species, List<ImageEntry>> getSpeciesToImageList()
	{
		return speciesToImageList;
	}

	public Map<Species, Map<Integer, Integer>> getYearToAbundance()
	{
		return yearToAbundance;
	}

	public Map<Species, Map<Integer, Integer>> getYearToActivity()
	{
		return yearToActivity;
	}

	public Map<Species, Map<Integer, List<Location>>> getYearToLocations()
	{
		return yearToLocations;
	}

	public Map<Species, Map<Integer, Integer>> getYearToNumberImages()
	{
		return yearToNumberImages;
	}

	public Map<Species, Map<Integer, Integer>> getYearToPeriod()
	{
		return yearToPeriod;
	}
}
