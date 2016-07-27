/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;

public class PredicateBuilder
{
	private Predicate<ImageEntry> predicate = new Predicate<ImageEntry>()
	{
		@Override
		public boolean test(ImageEntry t)
		{
			return true;
		}
	};

	public PredicateBuilder()
	{
	}

	public PredicateBuilder yearOnly(Integer year)
	{
		//this.images.removeIf(image -> getCalendarByDate(image.getDateTaken()).get(Calendar.YEAR) != year);
		this.predicate = this.predicate.and(image -> getCalendarByDate(image.getDateTaken()).get(Calendar.YEAR) == year);
		return this;
	}

	public PredicateBuilder monthOnly(int... month)
	{
		//this.images.removeIf(image -> !ArrayUtils.contains(month, getCalendarByDate(image.getDateTaken()).get(Calendar.MONTH)));
		this.predicate = this.predicate.and(image -> ArrayUtils.contains(month, getCalendarByDate(image.getDateTaken()).get(Calendar.MONTH)));
		return this;
	}

	public PredicateBuilder timeFrame(Integer startTimeHour, Integer endTimeHour)
	{
		//this.images = this.images.stream().filter(image -> (getCalendarByDate(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) >= startTimeHour && getCalendarByDate(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) < endTimeHour)).collect(Collectors.toList());
		this.predicate = this.predicate.and(image -> (getCalendarByDate(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) >= startTimeHour && getCalendarByDate(image.getDateTaken()).get(Calendar.HOUR_OF_DAY) < endTimeHour));
		return this;
	}

	public PredicateBuilder removeMonthlyDuplicates()
	{
		Set<Integer> usedDays = new HashSet<Integer>();
		this.predicate = this.predicate.and(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry imageEntry)
			{
				int day = getCalendarByDate(imageEntry.getDateTaken()).get(Calendar.DAY_OF_YEAR);
				if (!usedDays.contains(day))
				{
					usedDays.add(day);
					return true;
				}
				return false;
			}
		});
		return this;
	}

	public PredicateBuilder speciesOnly(Species species)
	{
		this.predicate = this.predicate.and(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
					if (speciesEntry.getSpecies() == species)
						return true;
				return false;
			}
		});
		return this;
	}

	public PredicateBuilder anyValidSpecies()
	{
		this.predicate = this.predicate.and(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				if (!entry.getSpeciesPresent().isEmpty())
					return true;
				return false;
			}
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
		this.predicate = this.predicate.and(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				long currentTime = entry.getDateTaken().getTime();
				for (Date date : newMoons)
				{
					long moonDate = date.getTime();
					if (Math.abs(moonDate - currentTime) < 5 * 1000 * 60 * 60 * 24)
						return true;
				}
				return false;
			}
		});
		return this;
	}

	public PredicateBuilder fullMoonOnly(List<Date> fullMoons)
	{
		//		this.images.removeIf(new Predicate<ImageEntry>()
		//		{
		//			@Override
		//			public boolean test(ImageEntry entry)
		//			{
		//				long currentTime = entry.getDateTaken().getTime();
		//				for (Date date : fullMoons)
		//				{
		//					long moonDate = date.getTime();
		//					if (Math.abs(moonDate - currentTime) < 5 * 1000 * 60 * 60 * 24)
		//						return false;
		//				}
		//				return true;
		//			}
		//		});
		this.predicate = this.predicate.and(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				long currentTime = entry.getDateTaken().getTime();
				for (Date date : fullMoons)
				{
					long moonDate = date.getTime();
					if (Math.abs(moonDate - currentTime) < 5 * 1000 * 60 * 60 * 24)
						return true;
				}
				return false;
			}
		});
		return this;
	}

	private Calendar getCalendarByDate(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public List<ImageEntry> query(List<ImageEntry> images)
	{
		return images.stream().filter(predicate).collect(Collectors.toList());
	}
}
