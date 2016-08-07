/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

public class PredicateBuilder
{
	private Predicate<ImageEntry> predicate = entry ->
	{
		return true;
	};

	public PredicateBuilder()
	{
	}

	public PredicateBuilder yearOnly(Integer year)
	{
		this.predicate = this.predicate.and(image -> DateUtils.toCalendar(image.getDateTaken()).get(Calendar.YEAR) == year);
		return this;
	}

	public PredicateBuilder monthOnly(int... month)
	{
		this.predicate = this.predicate.and(image -> ArrayUtils.contains(month, DateUtils.toCalendar(image.getDateTaken()).get(Calendar.MONTH)));
		return this;
	}

	public PredicateBuilder timeFrame(Integer startTimeHour, Integer endTimeHour)
	{
		this.predicate = this.predicate.and(image -> (DateUtils.toCalendar(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) >= startTimeHour && DateUtils.toCalendar(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) < endTimeHour));
		return this;
	}

	public PredicateBuilder removeMonthlyDuplicates()
	{
		Set<Integer> usedDays = new HashSet<Integer>();
		this.predicate = this.predicate.and(imageEntry ->
		{
			int day = DateUtils.toCalendar(imageEntry.getDateTaken()).get(Calendar.DAY_OF_YEAR);
			if (!usedDays.contains(day))
			{
				usedDays.add(day);
				return true;
			}
			return false;
		});
		return this;
	}

	public PredicateBuilder speciesOnly(Species species)
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

	public PredicateBuilder anyValidSpecies()
	{
		this.predicate = this.predicate.and(entry ->
		{
			if (!entry.getSpeciesPresent().isEmpty())
				return true;
			return false;
		});
		return this;
	}

	public PredicateBuilder locationOnly(Location location)
	{
		this.predicate = this.predicate.and(image -> image.getLocationTaken() == location);
		return this;
	}

	public PredicateBuilder newMoonOnly(List<Date> newMoons)
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

	public PredicateBuilder fullMoonOnly(List<Date> fullMoons)
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

	public List<ImageEntry> query(List<ImageEntry> images)
	{
		return images.stream().filter(predicate).collect(Collectors.toList());
	}

	public List<ImageEntry> query(List<ImageEntry> images, Comparator<ImageEntry> sorter)
	{
		List<ImageEntry> result = query(images);
		Collections.sort(result, sorter);
		return result;
	}
}
