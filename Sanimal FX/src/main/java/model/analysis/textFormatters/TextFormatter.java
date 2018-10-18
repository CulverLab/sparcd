package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.image.ImageEntry;

import java.util.List;

/**
 * The text formatter base class used for all text analysis
 * 
 * @author David Slovikosky
 */
public abstract class TextFormatter
{
	protected final List<ImageEntry> images;
	protected final DataAnalyzer analysis;

	public TextFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		this.images = images;
		this.analysis = analysis;
	}
}
