package model.image;

import model.location.Location;
import model.species.Species;

import java.io.File;
import java.util.List;

public class AnalysisImageEntry extends ImageEntry
{
	public AnalysisImageEntry(String fileName)
	{
		super(null, null, null);
		this.getFileProperty().set(new File(fileName));
	}

	/**
	 * We don't initialize our default bindings the way an ImageEntry does it
	 */
	@Override
	void initIconBindings()
	{
	}

	/**
	 * We don't do anything because a cloud image does not get loaded right away.
	 *
	 * @param file ignored
	 */
	@Override
	void readFileMetadataIntoImage(File file, List<Location> knownLocations, List<Species> knownSpecies)
	{
	}
}
