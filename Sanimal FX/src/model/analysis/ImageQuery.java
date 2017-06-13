package model.analysis;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;

/**
 * A class that utilizes Java 8 predicates to filter a list of images
 * 
 * @author David Slovikosky
 */
public class ImageQuery
{
	// A base predicate to add to
	private Predicate<ImageEntry> predicate = entry ->
	{
		return true;
	};

	public ImageQuery()
	{
	}

	/**
	 * Filter images by year only
	 * 
	 * @param year
	 *            The year to filter by
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery yearOnly(int... year)
	{
		this.predicate = this.predicate.and(image -> ArrayUtils.contains(year, DateUtils.toCalendar(image.getDateTaken()).get(Calendar.YEAR)));
		return this;
	}

	/**
	 * Filter images by month(s)
	 * 
	 * @param month
	 *            The month(s) to filter by
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery monthOnly(int... month)
	{
		this.predicate = this.predicate.and(image -> ArrayUtils.contains(month, DateUtils.toCalendar(image.getDateTaken()).get(Calendar.MONTH)));
		return this;
	}

	/**
	 * Filter images by time frame
	 * 
	 * @param startTimeHour
	 *            The hour to begin filtering at
	 * @param endTimeHour
	 *            The hour to end filtering at
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery timeFrame(Integer startTimeHour, Integer endTimeHour)
	{
		this.predicate = this.predicate.and(image -> (DateUtils.toCalendar(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) >= startTimeHour && DateUtils.toCalendar(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) < endTimeHour));
		return this;
	}

	/**
	 * Filter images by species type
	 * 
	 * @param species
	 *            The species to filter by
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery speciesOnly(Species species)
	{
		this.predicate = this.predicate.and(entry ->
		{
			for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
				if (speciesEntry.getSpecies() == species)
					return true;
			return false;
		});
		return this;
	}

	/**
	 * Filter images with any species
	 * 
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery anyValidSpecies()
	{
		this.predicate = this.predicate.and(entry ->
		{
			if (!entry.getSpeciesPresent().isEmpty())
				return true;
			return false;
		});
		return this;
	}

	/**
	 * Filter images by location
	 * 
	 * @param location
	 *            The location to filter by
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery locationOnly(Location location)
	{
		this.predicate = this.predicate.and(image -> image.getLocationTaken() == location);
		return this;
	}

	/**
	 * Filter images by new moon only
	 * 
	 * @param newMoons
	 *            The list of new moon dates
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery newMoonOnly(List<Date> newMoons)
	{
		this.predicate = this.predicate.and(entry ->
		{
			long currentTime = entry.getDateTaken().getTime();
			for (Date date : newMoons)
			{
				long moonDate = date.getTime();
				if (Math.abs(moonDate - currentTime) < 5 * 1000 * 60 * 60 * 24)
					return true;
			}
			return false;
		});
		return this;
	}

	/**
	 * Filter images by full moon only
	 * 
	 * @param fullMoons
	 *            The list of ful moon dates
	 * @return The predicate builder instance to chain filters
	 */
	public ImageQuery fullMoonOnly(List<Date> fullMoons)
	{
		this.predicate = this.predicate.and(entry ->
		{
			long currentTime = entry.getDateTaken().getTime();
			for (Date date : fullMoons)
			{
				long moonDate = date.getTime();
				if (Math.abs(moonDate - currentTime) < 5 * 1000 * 60 * 60 * 24)
					return true;
			}
			return false;
		});
		return this;
	}

	/**
	 * Finalize the predicate and apply it to a list of images.
	 * 
	 * @param images
	 *            The list of images to query
	 * @return The filtered list
	 */
	public List<ImageEntry> query(List<ImageEntry> images)
	{
		return images.stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * Finalize the predicate and apply it to a list of images. Also sorts the images by the sorter
	 * 
	 * @param images
	 *            The list of images to query
	 * @param sorter
	 *            The sorter to sort the filtered images by
	 * @return The filtered list
	 */
	public List<ImageEntry> query(List<ImageEntry> images, Comparator<ImageEntry> sorter)
	{
		List<ImageEntry> result = query(images);
		Collections.sort(result, sorter);
		return result;
	}
}
