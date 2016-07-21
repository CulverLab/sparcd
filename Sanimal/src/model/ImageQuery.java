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
import java.util.List;
import java.util.function.Predicate;

public class ImageQuery
{
	private final List<ImageEntry> images;

	public ImageQuery(List<ImageEntry> images)
	{
		this.images = new ArrayList<ImageEntry>(images);
	}

	public ImageQuery sort(Comparator<ImageEntry> comparator)
	{
		Collections.sort(images, comparator);
		return this;
	}

	public ImageQuery yearOnly(Integer year)
	{
		this.images.removeIf(image -> getCalendarByDate(image.getDateTaken()).get(Calendar.YEAR) != year);
		return this;
	}

	public ImageQuery speciesOnly(Species species)
	{
		this.images.removeIf(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				for (SpeciesEntry speciesEntry : entry.getSpeciesPresent())
					if (speciesEntry.getSpecies() == species)
						return false;
				return true;
			}
		});
		return this;
	}

	public ImageQuery anyValidSpecies()
	{
		this.images.removeIf(new Predicate<ImageEntry>()
		{
			@Override
			public boolean test(ImageEntry entry)
			{
				if (!entry.getSpeciesPresent().isEmpty())
					return false;
				return true;
			}
		});
		return this;
	}

	public ImageQuery locationOnly(Location location)
	{
		this.images.removeIf(image -> image.getLocationTaken() != location);
		return this;
	}

	private Calendar getCalendarByDate(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public List<ImageEntry> query()
	{
		return images;
	}
}
