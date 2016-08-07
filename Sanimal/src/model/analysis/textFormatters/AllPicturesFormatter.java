/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.List;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

public class AllPicturesFormatter extends TextFormatter
{
	public AllPicturesFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String createAllPictures()
	{
		String toReturn = "";

		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new PredicateBuilder().locationOnly(location).query(images);
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withLocationSpecies = new PredicateBuilder().speciesOnly(species).query(withLocation);
				for (ImageEntry imageEntry : withLocationSpecies)
				{
					toReturn = toReturn + String.format("%-28s %-28s %-28s\n", location.getName(), species.getName(), imageEntry.getImageFile().getName());
				}
			}
		}

		if (!toReturn.isEmpty())
			toReturn = toReturn.substring(0, toReturn.length() - 1);

		return toReturn;
	}
}
