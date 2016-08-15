package model;

import model.analysis.ExcelFormatter;
import model.analysis.SanimalTextOutputFormatter;
import model.image.ImageImporterData;
import model.location.LocationData;
import model.species.SpeciesData;
import model.timeline.TimelineData;

/**
 * A container class for all data Sanimal will need to store
 * 
 * @author David Slovikosky
 */
public class SanimalData
{
	// Image data
	private ImageImporterData imageData = new ImageImporterData();
	// Location data
	private LocationData locationData = new LocationData();
	// Species data
	private SpeciesData speciesData = new SpeciesData();
	// Output formatter
	private SanimalTextOutputFormatter outputFormatter = new SanimalTextOutputFormatter();
	// Excel output formatter
	private ExcelFormatter excelFormatter = new ExcelFormatter();
	// Timeline data
	private TimelineData timelineData = new TimelineData();

	/**
	 * @return The image data
	 */
	public ImageImporterData getImageData()
	{
		return imageData;
	}

	/**
	 * @return The location data
	 */
	public LocationData getLocationData()
	{
		return locationData;
	}

	/**
	 * @return The species data
	 */
	public SpeciesData getSpeciesData()
	{
		return speciesData;
	}

	/**
	 * @return The text outputter
	 */
	public SanimalTextOutputFormatter getOutputFormatter()
	{
		return outputFormatter;
	}

	/**
	 * @return The excel outputter
	 */
	public ExcelFormatter getExcelFormatter()
	{
		return excelFormatter;
	}

	/**
	 * @return The timeline data
	 */
	public TimelineData getTimelineData()
	{
		return timelineData;
	}
}
