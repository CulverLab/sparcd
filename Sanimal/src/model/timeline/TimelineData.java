package model.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;

/**
 * Stores a list of current sourceImages to be displayed on the map timeline
 * 
 * @author David Slovikosky
 */
public class TimelineData extends Observable
{
	// The sourceImages to be displayed
	private List<ImageEntry> sourceImages = new ArrayList<ImageEntry>();
	// The days that the source images cover
	private Long daysCovered = 0L;
	// The list of images that are currently being displayed
	private List<ImageEntry> imagesToDisplay = new ArrayList<ImageEntry>();

	/**
	 * Update the current list of sourceImages displayed on the timeline/map
	 * 
	 * @param sourceImages
	 *            The image list of new sourceImages
	 */
	public void updateSourceImageList(List<ImageEntry> sourceImages)
	{
		this.sourceImages = sourceImages;
		Collections.sort(this.sourceImages, (entry1, entry2) ->
		{
			return entry1.getDateTaken().compareTo(entry2.getDateTaken());
		});
		if (!this.sourceImages.isEmpty())
			this.daysCovered = SanimalAnalysisUtils.daysBetween(this.sourceImages.get(0).getDateTaken(), this.sourceImages.get(this.sourceImages.size() - 1).getDateTaken());
		this.setChanged();
		this.notifyObservers(TimelineUpdate.NewSourceImageList);
	}

	/**
	 * Returns a subset of sourceImages within a certain threshhold that are "percent" the way into the list "sourceImages"
	 * 
	 * @param percent
	 *            The percent into the image list to center the returned list on
	 * @param milliThreshold
	 *            The threshhold +/- to the centered image to add
	 * @return A subset of sourceImages
	 */
	public void imageListByPercent(Double percent, Long milliThreshold)
	{
		if (!this.sourceImages.isEmpty())
		{
			// The date to center around 
			Long dayToCenterAround = this.sourceImages.get(0).getDateTaken().getTime() + (long) (DateUtils.MILLIS_PER_DAY * daysCovered * percent);
			// Filter the list and return it
			this.imagesToDisplay = sourceImages.stream().filter(entry ->
			{
				Long imageTime = entry.getDateTaken().getTime();
				Long difference = Math.abs(dayToCenterAround - imageTime);
				if (difference <= milliThreshold)
					return true;
				return false;
			}).collect(Collectors.<ImageEntry> toList());
		}
		else
			this.imagesToDisplay = Collections.EMPTY_LIST;
		this.setChanged();
		this.notifyObservers(TimelineUpdate.NewImageListToDisplay);
	}

	public List<ImageEntry> getImagesToDisplay()
	{
		return imagesToDisplay;
	}
}
