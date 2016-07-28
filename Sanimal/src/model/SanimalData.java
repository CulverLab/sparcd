/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import model.analysis.ExcelFormatter;
import model.analysis.SanimalTextOutputFormatter;

public class SanimalData
{
	private ImageImporterData imageData = new ImageImporterData();
	private LocationData locationData = new LocationData();
	private SpeciesData speciesData = new SpeciesData();
	private SanimalTextOutputFormatter outputFormatter = new SanimalTextOutputFormatter();
	private ExcelFormatter excelFormatter = new ExcelFormatter();

	public ImageImporterData getImageData()
	{
		return imageData;
	}

	public LocationData getLocationData()
	{
		return locationData;
	}

	public SpeciesData getSpeciesData()
	{
		return speciesData;
	}

	public SanimalTextOutputFormatter getOutputFormatter()
	{
		return outputFormatter;
	}

	public ExcelFormatter getExcelFormatter()
	{
		return excelFormatter;
	}
}
