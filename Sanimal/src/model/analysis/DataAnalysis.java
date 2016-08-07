/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import library.MoonCalculator;
import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

public class DataAnalysis
{
	private boolean nullLocationsFound = false;
	private List<Location> allImageLocations = new ArrayList<Location>();
	private List<Species> allImageSpecies = new ArrayList<Species>();
	private List<Integer> allImageYears = new ArrayList<Integer>();
	private List<ImageEntry> imagesSortedByDate;
	private Integer eventInterval = 60;
	private List<Date> fullMoons = new ArrayList<Date>();
	private List<Date> newMoons = new ArrayList<Date>();

	public DataAnalysis(List<ImageEntry> images, Integer eventInterval)
	{
		this.eventInterval = eventInterval;
		for (ImageEntry entry : images)
			if (entry.getLocationTaken() != null)
			{
				if (!allImageLocations.contains(entry.getLocationTaken()))
					allImageLocations.add(entry.getLocationTaken());
			}
			else
				nullLocationsFound = true;

		Collections.sort(allImageLocations, new Comparator<Location>()
		{
			@Override
			public int compare(Location loc1, Location loc2)
			{
				return loc1.getName().compareTo(loc2.getName());
			}
		});

		for (ImageEntry imageEntry : images)
			for (SpeciesEntry speciesEntry : imageEntry.getSpeciesPresent())
				if (!allImageSpecies.contains(speciesEntry.getSpecies()))
					allImageSpecies.add(speciesEntry.getSpecies());

		Collections.sort(allImageSpecies, new Comparator<Species>()
		{
			@Override
			public int compare(Species species1, Species species2)
			{
				return species1.getName().compareTo(species2.getName());
			}
		});

		for (ImageEntry imageEntry : images)
		{
			Integer year = DateUtils.toCalendar(imageEntry.getDateTaken()).get(Calendar.YEAR);
			if (!allImageYears.contains(year))
				allImageYears.add(year);
		}

		Collections.sort(allImageYears);

		imagesSortedByDate = new ArrayList<ImageEntry>(images);
		Collections.sort(imagesSortedByDate, new Comparator<ImageEntry>()
		{
			@Override
			public int compare(ImageEntry entry1, ImageEntry entry2)
			{
				return entry1.getDateTaken().compareTo(entry2.getDateTaken());
			}
		});

		if (imagesSortedByDate.size() > 0)
		{
			Date first = imagesSortedByDate.get(0).getDateTaken();
			Date last = imagesSortedByDate.get(imagesSortedByDate.size() - 1).getDateTaken();
			while (first.before(last))
			{
				double julianDate = MoonCalculator.getJulian(first);
				double[] phases = MoonCalculator.getPhase(julianDate);
				double fullMoon = MoonCalculator.getLunation(julianDate, phases[MoonCalculator.MOONPHASE], 180);
				long fullMillis = MoonCalculator.toMillisFromJulian(fullMoon);
				Date nextFullMoonDate = new Date(fullMillis);
				fullMoons.add(nextFullMoonDate);
				first = new Date(nextFullMoonDate.getTime() + 20 * 1000 * 60 * 60 * 24);
			}

			Date first2 = imagesSortedByDate.get(0).getDateTaken();
			Date last2 = imagesSortedByDate.get(imagesSortedByDate.size() - 1).getDateTaken();
			while (first2.before(last2))
			{
				double julianDate = MoonCalculator.getJulian(first2);
				double[] phases = MoonCalculator.getPhase(julianDate);
				double newMoon = MoonCalculator.getLunation(julianDate, phases[MoonCalculator.MOONPHASE], 0);
				long newMillis = MoonCalculator.toMillisFromJulian(newMoon);
				Date nextNewMoonDate = new Date(newMillis);
				newMoons.add(nextNewMoonDate);
				first2 = new Date(nextNewMoonDate.getTime() + 20 * 1000 * 60 * 60 * 24);
			}
		}
	}

	public ImageEntry getFirstImageInList(List<ImageEntry> images)
	{
		ImageEntry first = images.size() == 0 ? null : images.get(0);
		for (ImageEntry imageEntry : images)
			if (imageEntry.getDateTaken().before(first.getDateTaken()))
				first = imageEntry;
		return first;
	}

	public ImageEntry getLastImageInList(List<ImageEntry> images)
	{
		ImageEntry last = images.size() == 0 ? null : images.get(0);
		for (ImageEntry imageEntry : images)
			if (imageEntry.getDateTaken().after(last.getDateTaken()))
				last = imageEntry;
		return last;
	}

	public List<Location> locationsForImageList(List<ImageEntry> images)
	{
		List<Location> locations = new ArrayList<Location>();

		for (ImageEntry image : images)
			if (image.getLocationTaken() != null)
				if (!locations.contains(image.getLocationTaken()))
					locations.add(image.getLocationTaken());

		Collections.sort(locations, new Comparator<Location>()
		{
			@Override
			public int compare(Location loc1, Location loc2)
			{
				return loc1.getName().compareTo(loc2.getName());
			}
		});

		return locations;
	}

	public Integer activityForImageList(List<ImageEntry> images)
	{
		Integer activity = 0;

		int oldHour = -1;
		int oldDay = -1;
		int oldYear = -1;
		for (ImageEntry image : images)
		{
			Calendar calendar = DateUtils.toCalendar(image.getDateTaken());
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int day = calendar.get(Calendar.DAY_OF_YEAR);
			int year = calendar.get(Calendar.YEAR);
			if ((hour != oldHour) || (oldDay != day) || (oldYear != year))
			{
				activity = activity + 1;
				oldHour = hour;
				oldDay = day;
				oldYear = year;
			}
		}

		return activity;
	}

	public Integer periodForImageList(List<ImageEntry> images)
	{
		Integer period = 0;

		long lastImageTimeMillis = 0;
		for (ImageEntry image : images)
		{
			long imageTimeMillis = image.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;
			if (differenceMinutes >= eventInterval)
			{
				period++;
			}
			lastImageTimeMillis = imageTimeMillis;
		}

		return period;
	}

	/**
	 * Get the abundance value for a list of images
	 * 
	 * @param images
	 *            The list of images to search through (must be sorted)
	 * @param speciesFilter
	 *            The species for which to look for. May be null for any species
	 * @return The abundance value for "images"
	 */
	public Integer abundanceForImageList(List<ImageEntry> images, Species speciesFilter)
	{
		Integer abundance = 0;

		long lastImageTimeMillis = 0;
		Integer maxAnimalsInEvent = 0;
		for (ImageEntry image : images)
		{
			long imageTimeMillis = image.getDateTaken().getTime();
			long differenceMillis = imageTimeMillis - lastImageTimeMillis;
			long differenceMinutes = differenceMillis / 1000 / 60;

			if (differenceMinutes >= eventInterval)
			{
				abundance = abundance + maxAnimalsInEvent;
				maxAnimalsInEvent = 0;
			}

			for (SpeciesEntry speciesEntry : image.getSpeciesPresent())
				if (speciesFilter == null || speciesEntry.getSpecies() == speciesFilter)
					maxAnimalsInEvent = Math.max(maxAnimalsInEvent, speciesEntry.getAmount());

			lastImageTimeMillis = imageTimeMillis;
		}

		abundance = abundance + maxAnimalsInEvent;

		return abundance;
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

	public List<Date> getFullMoons()
	{
		return fullMoons;
	}

	public List<Date> getNewMoons()
	{
		return newMoons;
	}
}
