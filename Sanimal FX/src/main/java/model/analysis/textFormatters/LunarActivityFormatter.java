package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.analysis.LunarActivityEntry;
import model.image.ImageEntry;
import model.species.Species;

import java.util.ArrayList;
import java.util.List;

/**
 * The text formatter for statistics about lunar activity
 * 
 * @author David Slovikosky
 */
public class LunarActivityFormatter extends TextFormatter
{
	private List<LunarActivityEntry> lunarActivities = null;

	public LunarActivityFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For all species the activity pattern for 11 days centered around a Full moon and New moon is given. The table shows the hour of the day,the
	 * number of records,and the frequency of total records for both a New moon and a Full moon. The moon completes one orbit around earth each 29.55
	 * days. The Difference is the square root of the sum of the squared differences in frequency. The greater the difference, the more a species is
	 * active during one phase of the moon compared to the other phase. Note that birds are likely more active during a Full moon than a New moon, and
	 * nocturnal rodents might show the opposite pattern.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLunarActivity()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("LUNAR ACTIVITY PATTERN\n");
		toReturn.append("  New and full moon +/- 5 days activity patterns\n");
		toReturn.append("  Difference (large is greater difference)\n");

		List<ImageEntry> imagesFull = new ImageQuery().fullMoonOnly(analysis.getFullMoons()).query(images);
		List<ImageEntry> imagesNew = new ImageQuery().newMoonOnly(analysis.getNewMoons()).query(images);

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(species.getCommonName()).append("\n");
			toReturn.append("                 Full moon activity    New moon activity\n");
			toReturn.append("    Hour        Number    Frequency   Number    Frequency\n");

			List<ImageEntry> imagesWithSpeciesFull = new ImageQuery().speciesOnly(species).query(imagesFull);
			int totalImagesFull = imagesWithSpeciesFull.size();

			List<ImageEntry> imagesWithSpeciesNew = new ImageQuery().speciesOnly(species).query(imagesNew);
			int totalImagesNew = imagesWithSpeciesNew.size();

			int numImagesTotalFull = 0;
			int numImagesTotalNew = 0;

			double totalDifference = 0;

			StringBuilder toAdd = new StringBuilder();

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTimeFull = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesFull);
				int numImagesFull = imagesWithSpeciesAtTimeFull.size();
				double frequencyFull = 0;
				if (totalImagesFull != 0)
					frequencyFull = (double) numImagesFull / totalImagesFull;
				numImagesTotalFull = numImagesTotalFull + numImagesFull;

				List<ImageEntry> imagesWithSpeciesAtTimeNew = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesNew);
				int numImagesNew = imagesWithSpeciesAtTimeNew.size();
				double frequencyNew = 0;
				if (totalImagesNew != 0)
					frequencyNew = (double) numImagesNew / totalImagesNew;
				numImagesTotalNew = numImagesTotalNew + numImagesNew;

				double difference = frequencyFull - frequencyNew;
				totalDifference = totalDifference + difference * difference;

				toAdd.append(String.format("%02d:00-%02d:00      %5d      %5.3f      %5d      %5.3f\n", i, i + 1, numImagesFull, frequencyFull, numImagesNew, frequencyNew));
			}

			totalDifference = Math.sqrt(totalDifference);

			toAdd.append(String.format("Total            %5d                 %5d\n", numImagesTotalFull, numImagesTotalNew));
			toAdd.append(String.format("Difference       %5.2f\n", totalDifference));

			toAdd.append("\n");

			if (totalDifference != 0)
				toReturn.append(toAdd);
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The species whose lunar activity pattern is most different is given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printLunarActivityMostDifferent()
	{
		StringBuilder toReturn = new StringBuilder();

		if (lunarActivities == null)
			this.createLunarActivityTable();

		if (!lunarActivities.isEmpty())
		{
			lunarActivities.sort((entry1, entry2) ->
					entry2.getDifference().compareTo(entry1.getDifference()));

			toReturn.append("SPECIES LUNAR ACTIVITY MOST DIFFERENT: ");

			toReturn.append(lunarActivities.get(0).getSpecies().getCommonName()).append("\n");

			toReturn.append("\nSpecies                   Difference Number of records\n");
			for (LunarActivityEntry entry : lunarActivities)
				toReturn.append(String.format("%-28s %4.2f      %7d\n", entry.getSpecies(), entry.getDifference(), entry.getNumRecords()));
			toReturn.append("\n");
		}

		return toReturn.toString();
	}

	// Algorithm copied from "public String printLunarActivity()"
	private void createLunarActivityTable()
	{
		lunarActivities = new ArrayList<LunarActivityEntry>();

		List<ImageEntry> imagesFull = new ImageQuery().fullMoonOnly(analysis.getFullMoons()).query(images);
		List<ImageEntry> imagesNew = new ImageQuery().newMoonOnly(analysis.getNewMoons()).query(images);

		for (Species species : analysis.getAllImageSpecies())
		{
			int numImagesTotalFull = 0;
			int numImagesTotalNew = 0;

			double totalDifference = 0;

			List<ImageEntry> imagesWithSpeciesFull = new ImageQuery().speciesOnly(species).query(imagesFull);
			int totalImagesFull = imagesWithSpeciesFull.size();

			List<ImageEntry> imagesWithSpeciesNew = new ImageQuery().speciesOnly(species).query(imagesNew);
			int totalImagesNew = imagesWithSpeciesNew.size();

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTimeFull = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesFull);
				int numImagesFull = imagesWithSpeciesAtTimeFull.size();
				double frequencyFull = 0;
				if (totalImagesFull != 0)
					frequencyFull = (double) numImagesFull / totalImagesFull;
				numImagesTotalFull = numImagesTotalFull + numImagesFull;

				List<ImageEntry> imagesWithSpeciesAtTimeNew = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesNew);
				int numImagesNew = imagesWithSpeciesAtTimeNew.size();
				double frequencyNew = 0;
				if (totalImagesNew != 0)
					frequencyNew = (double) numImagesNew / totalImagesNew;
				numImagesTotalNew = numImagesTotalNew + numImagesNew;

				double difference = frequencyFull - frequencyNew;
				totalDifference = totalDifference + difference * difference;

			}

			totalDifference = Math.sqrt(totalDifference);

			lunarActivities.add(new LunarActivityEntry(species, totalDifference, numImagesTotalFull + numImagesTotalNew));
		}
	}
}
