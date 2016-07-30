/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.List;

import model.ImageEntry;
import model.analysis.DataAnalysis;

public abstract class TextFormatter
{
	protected final List<ImageEntry> images;
	protected final DataAnalysis analysis;
	protected final Integer eventInterval;

	public TextFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		this.images = images;
		this.analysis = analysis;
		this.eventInterval = eventInterval;
	}
}
