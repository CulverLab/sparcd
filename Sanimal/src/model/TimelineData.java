/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import model.analysis.SanimalAnalysisUtils;

public class TimelineData
{
	private List<ImageEntry> images = new ArrayList<ImageEntry>();
	private Long daysCovered = 0L;

	public void updateList(List<ImageEntry> images)
	{
		this.images = images;
		Collections.sort(this.images, new Comparator<ImageEntry>()
		{
			@Override
			public int compare(ImageEntry entry1, ImageEntry entry2)
			{
				return entry1.getDateTaken().compareTo(entry2.getDateTaken());
			}
		});
		if (!this.images.isEmpty())
			this.daysCovered = SanimalAnalysisUtils.daysBetween(this.images.get(0).getDateTaken(), this.images.get(this.images.size() - 1).getDateTaken());
	}

	public List<ImageEntry> imageListByPercent(Double percent, Long milliThreshold)
	{
		if (!this.images.isEmpty())
		{
			Long dayToCenterAround = this.images.get(0).getDateTaken().getTime() + (long) (DateUtils.MILLIS_PER_DAY * daysCovered * percent);
			return images.stream().filter(new Predicate<ImageEntry>()
			{
				@Override
				public boolean test(ImageEntry entry)
				{
					Long imageTime = entry.getDateTaken().getTime();
					Long difference = Math.abs(dayToCenterAround - imageTime);
					if (difference <= milliThreshold)
						return true;
					return false;
				}
			}).collect(Collectors.<ImageEntry> toList());
		}
		else
			return new ArrayList<ImageEntry>();
	}
}
