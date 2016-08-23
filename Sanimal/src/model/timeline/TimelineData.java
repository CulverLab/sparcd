package model.timeline;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

import javax.swing.Timer;

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
	// Millisecond amount representing the time +/- that an image must be taken to count 
	private static final Long MILLI_THRESHOLD = DateUtils.MILLIS_PER_DAY;
	// Base clock tick speed
	private static final Integer BASE_CLOCK_SPEED = 1000;

	// The sourceImages to be displayed
	private List<ImageEntry> sourceImages = new ArrayList<ImageEntry>();
	// The days that the source images cover
	private Long daysCovered = 0L;
	// The list of images that are currently being displayed
	private List<ImageEntry> imagesToDisplay = new ArrayList<ImageEntry>();
	// The current day to "center" around
	private Long dayToCenterAround = 0L;
	// Play forward or in reverse
	private Boolean playForward = true;
	// Timer to do the continuous play forward/backward
	private Timer clock;

	public TimelineData()
	{
		this.clock = new Timer(BASE_CLOCK_SPEED, event ->
		{
			if (TimelineData.this.playForward)
				TimelineData.this.advanceBySingleDay();
			else
				TimelineData.this.rewindBySingleDay();
		});
		this.clock.setRepeats(true);
	}

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
		{
			this.daysCovered = SanimalAnalysisUtils.daysBetween(this.sourceImages.get(0).getDateTaken(), this.sourceImages.get(this.sourceImages.size() - 1).getDateTaken());
			this.dayToCenterAround = this.sourceImages.get(0).getDateTaken().getTime();
		}
		this.setChanged();
		this.notifyObservers(TimelineUpdate.NewSourceImageList);
	}

	/**
	 * Updates the center time of the image list by a given percent through the images
	 * 
	 * @param percent
	 *            The percent into the image list to center the returned list on
	 */
	public void centerOnDayByPercent(Double percent)
	{
		// The date to center around 
		if (!this.sourceImages.isEmpty())
			dayToCenterAround = this.sourceImages.get(0).getDateTaken().getTime() + (long) (DateUtils.MILLIS_PER_DAY * daysCovered * percent);
		// Filter the list and return it
		this.updateDisplayImageListByCenter();
	}

	public void advanceBySingleDay()
	{
		Long previousDayToCenterAround = this.dayToCenterAround;
		this.dayToCenterAround = this.dayToCenterAround + DateUtils.MILLIS_PER_DAY;
		if (this.sourceImages.size() > 0)
			this.dayToCenterAround = Math.min(this.dayToCenterAround, (this.sourceImages.get(0).getDateTaken().getTime() + daysCovered * DateUtils.MILLIS_PER_DAY));
		if (!previousDayToCenterAround.equals(this.dayToCenterAround))
			this.updateDisplayImageListByCenter();
	}

	public void rewindBySingleDay()
	{
		Long previousDayToCenterAround = this.dayToCenterAround;
		this.dayToCenterAround = this.dayToCenterAround - DateUtils.MILLIS_PER_DAY;
		if (this.sourceImages.size() > 0)
			this.dayToCenterAround = Math.max(this.dayToCenterAround, this.sourceImages.get(0).getDateTaken().getTime());
		if (!previousDayToCenterAround.equals(this.dayToCenterAround))
			this.updateDisplayImageListByCenter();
	}

	public void goToFirst()
	{
		this.dayToCenterAround = sourceImages.size() == 0 ? 0 : sourceImages.get(0).getDateTaken().getTime();
		this.updateDisplayImageListByCenter();
	}

	public void goToLast()
	{
		this.dayToCenterAround = sourceImages.size() == 0 ? 0 : (sourceImages.get(0).getDateTaken().getTime() + daysCovered * DateUtils.MILLIS_PER_DAY);
		this.updateDisplayImageListByCenter();
	}

	public void beginForwardPlay()
	{
		this.playForward = true;
		if (!this.clock.isRunning())
			this.clock.start();
	}

	public void beginReversePlay()
	{
		this.playForward = false;
		if (!this.clock.isRunning())
			this.clock.start();
	}

	public void stopPlay()
	{
		this.clock.stop();
	}

	public void setClockSpeedMultiplier(Double multiplier)
	{
		Integer currentDelay = this.clock.getDelay();
		Integer newDelay = Integer.MAX_VALUE;
		if (multiplier != 0)
			newDelay = (int) Math.round(BASE_CLOCK_SPEED / multiplier);
		this.clock.setDelay(newDelay);
		this.clock.setInitialDelay(newDelay);
		if (this.clock.isRunning())
			this.clock.restart();
	}

	/**
	 * Easy function to update the current image display list by the center day
	 */
	private void updateDisplayImageListByCenter()
	{
		this.imagesToDisplay = sourceImages.stream().filter(entry ->
		{
			Long imageTime = entry.getDateTaken().getTime();
			Long difference = Math.abs(dayToCenterAround - imageTime);
			if (difference <= MILLI_THRESHOLD)
				return true;
			return false;
		}).collect(Collectors.<ImageEntry> toList());
		this.setChanged();
		this.notifyObservers(TimelineUpdate.NewImageListToDisplay);
	}

	public Double getPercentageAcrossDisplayedImages()
	{
		if (sourceImages.isEmpty())
			return 0D;
		else
			return (double) (dayToCenterAround - sourceImages.get(0).getDateTaken().getTime()) / (double) (daysCovered * DateUtils.MILLIS_PER_DAY);
	}

	public Date getCenterDayAsDate()
	{
		return new Date(this.dayToCenterAround);
	}

	public List<ImageEntry> getImagesToDisplay()
	{
		return imagesToDisplay;
	}
}
