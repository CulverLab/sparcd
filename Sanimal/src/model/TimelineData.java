package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import model.analysis.SanimalAnalysisUtils;

/**
 * Stores a list of current images to be displayed on the map timeline
 * 
 * @author David Slovikosky
 */
public class TimelineData
{
	// The images to be displayed
	private List<ImageEntry> images = new ArrayList<ImageEntry>();
	private Long daysCovered = 0L;

	/**
	 * Update the current list of images displayed on the timeline/map
	 * 
	 * @param images
	 *            The image list of new images
	 */
	public void updateList(List<ImageEntry> images)
	{
		this.images = images;
		Collections.sort(this.images, (entry1, entry2) ->
		{
			return entry1.getDateTaken().compareTo(entry2.getDateTaken());
		});
		if (!this.images.isEmpty())
			this.daysCovered = SanimalAnalysisUtils.daysBetween(this.images.get(0).getDateTaken(), this.images.get(this.images.size() - 1).getDateTaken());
	}

	/**
	 * Returns a subset of images within a certain threshhold that are "percent" the way into the list "images"
	 * 
	 * @param percent
	 *            The percent into the image list to center the returned list on
	 * @param milliThreshold
	 *            The threshhold +/- to the centered image to add
	 * @return A subset of images
	 */
	public List<ImageEntry> imageListByPercent(Double percent, Long milliThreshold)
	{
		if (!this.images.isEmpty())
		{
			// The date to center around 
			Long dayToCenterAround = this.images.get(0).getDateTaken().getTime() + (long) (DateUtils.MILLIS_PER_DAY * daysCovered * percent);
			// Filter the list and return it
			return images.stream().filter(entry ->
			{
				Long imageTime = entry.getDateTaken().getTime();
				Long difference = Math.abs(dayToCenterAround - imageTime);
				if (difference <= milliThreshold)
					return true;
				return false;
			}).collect(Collectors.<ImageEntry> toList());
		}
		else
			return new ArrayList<ImageEntry>();
	}
}
