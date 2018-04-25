package model.analysis;

import model.cyverse.CyVerseQueryResult;
import model.image.AnalysisImageEntry;
import model.image.CloudImageEntry;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CloudDataAnalysis
{
	private List<CyVerseQueryResult> rawCloudQueryResult;
	private DataAnalyzer dataAnalyzer;

	public CloudDataAnalysis(List<CyVerseQueryResult> rawCloudQueryResult, Integer eventInterval)
	{
		this.rawCloudQueryResult = rawCloudQueryResult;

		List<Location> uniqueLocations = new LinkedList<>();
		List<Species> uniqueSpecies = new LinkedList<>();
		List<ImageEntry> managableData = new LinkedList<>();
		for (CyVerseQueryResult result : rawCloudQueryResult)
		{
			Boolean locationForImagePresent = uniqueLocations.stream().anyMatch(location -> location.getId().equals(result.getLocationID()));
			if (!locationForImagePresent)
				uniqueLocations.add(new Location(result.getLocationName(), result.getLocationID(), result.getLocationLatitude(), result.getLocationLongitude(), result.getLocationElevation()));
			Boolean speciesForImagePresent = uniqueSpecies.stream().anyMatch(species -> species.getScientificName().equalsIgnoreCase(result.getSpeciesScientificName()));
			if (!speciesForImagePresent)
				uniqueSpecies.add(new Species(result.getSpeciesName(), result.getSpeciesScientificName(), ""));

			Location correctLocation = uniqueLocations.stream().filter(location -> location.getId().equals(result.getLocationID())).findFirst().get();
			Species correctSpecies = uniqueSpecies.stream().filter(species -> species.getScientificName().equals(result.getSpeciesScientificName())).findFirst().get();
			ImageEntry entry = new AnalysisImageEntry(result.getIrodsFileAbsolutePath());
			entry.setLocationTaken(correctLocation);
			entry.setDateTaken(result.getDateTimeTaken());
			entry.addSpecies(correctSpecies, result.getSpeciesCount());
			managableData.add(entry);
		}

		dataAnalyzer = new DataAnalyzer(managableData, eventInterval);
	}

	public List<CyVerseQueryResult> getRawCloudQueryResult()
	{
		return this.rawCloudQueryResult;
	}

	public DataAnalyzer getDataAnalyzer()
	{
		return this.dataAnalyzer;
	}
}
