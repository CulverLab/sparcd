package model.analysis.textFormatters;

import java.util.List;

import model.analysis.DataAnalysis;
import model.image.ImageEntry;

/**
 * The text formatter base class used for all text analysis
 * 
 * @author David Slovikosky
 */
public abstract class TextFormatter
{
	protected final List<ImageEntry> images;
	protected final DataAnalysis analysis;

	public TextFormatter(List<ImageEntry> images, DataAnalysis analysis)
	{
		this.images = images;
		this.analysis = analysis;
	}
}
