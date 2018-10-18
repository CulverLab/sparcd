package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The text formatter for species activity patterns
 * 
 * @author David Slovikosky
 */
public class ActivityPatternFormatter extends TextFormatter
{
	public ActivityPatternFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species daily activity patterns are given for all species by one hour segments. The species is listed and in parentheses (the number
	 * of reords used in the activity calculation / the total number of records some of which might be sequentil). The first column, labeled Hour,
	 * shows the hour segments starting and ending at midnight. Activity pattern is given by the number of records collected from all locations
	 * analyzed for all years, and in frequency for all years and months, and for all years and each month (since activity can vary by month). The
	 * total number of records for all years that was used is also given. The number of records matches the number of pictures listed under NUMBER OF
	 * PICTURES AND FILTERED PICTURES PER YEAR above.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printActivityPatterns()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("ACTIVITY PATTERNS\n");
		toReturn.append(" Activity in one-hour segments - Species (Number of pictures in one hour segments/Total number of pics)\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			StringBuilder toAdd = new StringBuilder();
			List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer totalImages = imagesWithSpecies.size();
			// Activity / All
			toAdd.append("                   All months         Jan              Feb              Mar              Apr              May              Jun              Jul              Aug              Sep              Oct              Nov              Dec\n");
			toAdd.append("    Hour        Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency Number Frequency\n");

			int[] totals = new int[13];
			int[] totalActivities = new int[13];

			// 12 months + all months
			for (int i = -1; i < 12; i++)
			{
				Integer activity;
				// -1 = all months
				if (i == -1)
					activity = analysis.activityForImageList(imagesWithSpecies);
				else
					activity = analysis.activityForImageList(new ImageQuery().monthOnly(i).query(imagesWithSpecies));
				totalActivities[i + 1] = activity;
			}

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpecies);
				toAdd.append(String.format("%02d:00-%02d:00   ", i, i + 1));
				// 12 months
				for (int j = -1; j < 12; j++)
				{
					Integer activity;
					// -1 = all months
					if (j == -1)
						activity = analysis.activityForImageList(imagesWithSpeciesAtTime);
					else
						activity = analysis.activityForImageList(new ImageQuery().monthOnly(j).query(imagesWithSpeciesAtTime));

					if (activity != 0)
						toAdd.append(String.format("%6d %10.3f", activity, (double) activity / totalActivities[j + 1]));
					else
						toAdd.append("                 ");
					totals[j + 1] = totals[j + 1] + activity;
				}
				toAdd.append("\n");
			}

			toAdd.append("Total         ");

			for (int total : totals) toAdd.append(String.format("%6d    100.000", total));

			toAdd.append("\n");

			// Print the header first
			toReturn.append(String.format("%-28s (%6d/ %6d)\n", species.getName(), totals[0], totalImages));

			toReturn.append(toAdd);

			toReturn.append("\n");
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A table showing the similarity comparison of activity patterns using hourly frequency is given. The number in the table shows the squart root
	 * of the sum of the squared differencs by hour for each species pair. Freqency is used because the number of records used to calcluate activity
	 * patterns generally differs for each species. If a pair of species has similar activity patterns then the value in the table will be low. If two
	 * species have very different activity patterns, one being diurnal, the other nocturnal for instance, the value in the table will be high.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesPairsActivitySimilarity()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES PAIRS ACTIVITY SIMILARITY (LOWER IS MORE SIMILAR)\n");

		toReturn.append("                            ");
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(String.format("%-8s ", StringUtils.left(species.getName(), 8)));
		}

		toReturn.append("\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(String.format("%-27s", species.getName()));
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
				List<ImageEntry> imagesWithSpeciesOther = new ImageQuery().speciesOnly(other).query(analysis.getImagesSortedByDate());
				int totalActivity = analysis.activityForImageList(imagesWithSpecies);
				int totalActivityOther = analysis.activityForImageList(imagesWithSpeciesOther);

				double activitySimilarity = 0;

				// 24 hrs
				for (int i = 0; i < 24; i++)
				{
					List<ImageEntry> imagesWithSpeciesAtTime = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpecies);
					List<ImageEntry> imagesWithSpeciesAtTimeOther = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
					double activity = analysis.activityForImageList(imagesWithSpeciesAtTime);
					double activityOther = analysis.activityForImageList(imagesWithSpeciesAtTimeOther);
					double frequency = activity / totalActivity;
					double frequencyOther = activityOther / totalActivityOther;
					double difference = frequency - frequencyOther;
					// Frequency squared
					activitySimilarity = activitySimilarity + difference * difference;
				}

				toReturn.append(String.format("%6.3f   ", activitySimilarity));
			}
			toReturn.append("\n");
		}

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * The species pair that has the most similar activity pattern is compared. Only those species with 25 or more pictures are used.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciePairMostSimilar()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES PAIR MOST SIMILAR IN ACTIVITY (FREQUENCY)\n");
		toReturn.append("  Consider those species with 25 or more pictures\n");

		Species lowest = null;
		Species lowestOther = null;
		double lowestFrequency = Double.MAX_VALUE;

		for (Species species : analysis.getAllImageSpecies())
		{
			for (Species other : analysis.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(images);
				List<ImageEntry> imagesWithSpeciesOther = new ImageQuery().speciesOnly(other).query(images);
				int totalImages = imagesWithSpecies.size();
				int totalImagesOther = imagesWithSpeciesOther.size();
				double activitySimilarity = 0;

				if (totalImages >= 25 && totalImagesOther >= 25 && !species.equals(other))
				{
					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					activitySimilarity = Math.sqrt(activitySimilarity);

					if (lowestFrequency >= activitySimilarity)
					{
						lowestFrequency = activitySimilarity;
						lowest = species;
						lowestOther = other;
					}
				}
			}
		}

		if (lowest != null)
		{
			toReturn.append(String.format("Hour            %-28s %-28s\n", lowest.getName(), lowestOther.getName()));

			List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(lowest).query(images);
			List<ImageEntry> imagesWithSpeciesOther = new ImageQuery().speciesOnly(lowestOther).query(images);
			int totalImages = imagesWithSpecies.size();
			int totalImagesOther = imagesWithSpeciesOther.size();
			double activitySimilarity = 0;

			// 24 hrs
			for (int i = 0; i < 24; i++)
			{
				List<ImageEntry> imagesWithSpeciesAtTime = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpecies);
				List<ImageEntry> imagesWithSpeciesAtTimeOther = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
				double numImages = imagesWithSpeciesAtTime.size();
				double numImagesOther = imagesWithSpeciesAtTimeOther.size();
				double frequency = numImages / totalImages;
				double frequencyOther = numImagesOther / totalImagesOther;
				double difference = frequency - frequencyOther;
				// Frequency squared
				activitySimilarity = activitySimilarity + difference * difference;

				toReturn.append(String.format("%02d:00-%02d:00     %5.3f                        %5.3f\n", i, i + 1, frequency, frequencyOther));
			}
		}

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Using the Ch-squared statistic activity patterns of paired species are analyzed and results presented in species x species table. The null
	 * hypothesis H0: Species A and B have similar activity patterns at 95% is tested. If the pattern is significantly similar then a "+" is entered
	 * for A x B, otherwise the pattern is not significcantly similar and is indicated by a"0" in the table. Only those species that have 25 or more
	 * records are considered.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printChiSquareAnalysisPairedActivity()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("CHI-SQUARE ANALYSIS OF PAIRED ACTIVITY PATTERNS\n");
		toReturn.append("  H0: Species A and B have similar activity patterns at 95%\n");
		toReturn.append("  Significant = X, Not significant = Blank\n");
		toReturn.append("  Consider only species with >= 25 pictures\n");

		toReturn.append("                            ");
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append(String.format("%-8s ", StringUtils.left(species.getName(), 8)));
		}

		toReturn.append("\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(images);
			int totalImages = imagesWithSpecies.size();
			if (totalImages >= 25)
			{
				toReturn.append(String.format("%-28s", species.getName()));
				for (Species other : analysis.getAllImageSpecies())
				{
					List<ImageEntry> imagesWithSpeciesOther = new ImageQuery().speciesOnly(other).query(images);
					int totalImagesOther = imagesWithSpeciesOther.size();
					double activitySimilarity = 0;

					// 24 hrs
					for (int i = 0; i < 24; i++)
					{
						List<ImageEntry> imagesWithSpeciesAtTime = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpecies);
						List<ImageEntry> imagesWithSpeciesAtTimeOther = new ImageQuery().timeFrame(i, i + 1).query(imagesWithSpeciesOther);
						double numImages = imagesWithSpeciesAtTime.size();
						double numImagesOther = imagesWithSpeciesAtTimeOther.size();
						double frequency = numImages / totalImages;
						double frequencyOther = numImagesOther / totalImagesOther;
						double difference = frequency - frequencyOther;
						// Frequency squared
						activitySimilarity = activitySimilarity + difference * difference;
					}

					double chiSquare = (1 - activitySimilarity) / 1.0;

					if (chiSquare >= 0.95 && imagesWithSpeciesOther.size() >= 25)
						toReturn.append("   X     ");
					else
						toReturn.append("         ");
				}
				toReturn.append("\n");
			}
		}

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * Using Northern hemisphere seasons of winter (Dec-Jan-Feb), spring (Mar-Apr-May), summer (Jun-Jul-Aug), and fall (Sep-Oct-Nov) activity patterns
	 * for each species are presented in a table. The table shows the number of records used in the actvity calculation and the frequency for each
	 * sason. To compare the seasonal activity patterns requires knowning the number of independent pictures recorded in each season normalied by the
	 * number of camera trap days (Pictures/Effort) for the season, and the proportion of the number of records divided by the total number of records
	 * for the all four seasons (Visitation proportion). That is, Visitation proportion is computed by summing Picture/Effort for all seasons, then
	 * dividing each season by the sum. This gives the proportion of records (based on indepdenent pictures, not the number of pictures used to create
	 * activity). Note that more records likely result from greater effort, hence the number of records must be normalizedby effort. The total number
	 * of records for each season is given.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printActivityPatternsSeason()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("ACTIVITY PATTERNS BY SEASON\n");
		toReturn.append("  Activity in one-hour segments by season\n");

		int[][] seasons = new int[][]
		{
				{ 11, 0, 1 }, // 1
				{ 2, 3, 4 }, // 2
				{ 5, 6, 7 }, // 3
				{ 8, 9, 10 } }; // 4

		int[] lengthPerSeason = new int[4];
		int[] monthlyTotals = new int[12];
		for (Location location : analysis.getAllImageLocations())
		{
			List<ImageEntry> withLocation = new ImageQuery().locationOnly(location).query(images);
			LocalDateTime firstDate = analysis.getFirstImageInList(withLocation).getDateTaken();
			LocalDateTime lastDate = analysis.getLastImageInList(withLocation).getDateTaken();
			Integer firstMonth = firstDate.getMonthValue();
			Integer lastMonth = lastDate.getMonthValue();
			Integer firstDay = firstDate.getDayOfMonth();
			Integer lastDay = lastDate.getDayOfMonth();
			toReturn.append(String.format("%-28s", location.getName()));
			int monthTotal = 0;
			for (int i = 0; i < 12; i++)
			{
				int monthValue = 0;
				if (firstMonth == lastMonth && firstMonth == i)
					monthValue = lastDay - firstDay + 1;
				else if (firstMonth == i)
					monthValue = 31 - firstDay + 1;
				else if (lastMonth == i)
					monthValue = lastDay;
				else if (firstMonth < i && lastMonth > i)
					monthValue = 31;

				toReturn.append(String.format(" %2d    ", monthValue));
				monthTotal = monthTotal + monthValue;
				monthlyTotals[i] = monthlyTotals[i] + monthValue;
			}
			toReturn.append(monthTotal).append("\n");
		}

		for (int[] season : seasons)
			for (int month : season)
				lengthPerSeason[ArrayUtils.indexOf(seasons, season)] = lengthPerSeason[ArrayUtils.indexOf(seasons, season)] + monthlyTotals[month];

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());

			toReturn.append(species.getName()).append("\n");
			toReturn.append("                     Dec-Jan-Feb           Mar-Apr-May           Jun-Jul-Aug           Sep-Oct-Nov\n");
			toReturn.append("Camera trap days    ");

			for (Integer length : lengthPerSeason)
				toReturn.append(String.format("%7d               ", length));
			toReturn.append("\n");

			toReturn.append("Number of pictures  ");
			int[] imagesPerSeason = new int[4];
			for (int i = 0; i < 4; i++)
			{
				List<ImageEntry> seasonWithSpecies = new ImageQuery().monthOnly(seasons[i]).query(withSpecies);
				Integer activity = analysis.activityForImageList(seasonWithSpecies);
				toReturn.append(String.format("%7d               ", activity));
				imagesPerSeason[i] = activity;
			}
			toReturn.append("\n");
			toReturn.append("Pictures/Effort        ");
			double total = 0;
			double ratios[] = new double[4];
			for (int i = 0; i < 4; i++)
			{
				double currentRatio = 0;
				if (lengthPerSeason[i] != 0)
					currentRatio = (double) imagesPerSeason[i] / lengthPerSeason[i];
				toReturn.append(String.format("%5.4f                ", currentRatio));
				ratios[i] = currentRatio;
				total = total + currentRatio;
			}
			toReturn.append("\n");
			toReturn.append("Visitation proportion  ");
			for (int i = 0; i < 4; i++)
			{
				if (total != 0)
					toReturn.append(String.format("%5.4f                ", ratios[i] / total));
				else
					toReturn.append(String.format("%5.4f                ", 0f));
			}

			toReturn.append("\n");

			StringBuilder toAdd = new StringBuilder();

			toAdd.append("           Hour        Number      Freq      Number      Freq      Number      Freq      Number      Freq\n");

			int[] hourlyTotals = new int[4];

			// 24 hrs
			for (int j = 0; j < 24; j++)
			{
				List<ImageEntry> withSpeciesAtTime = new ImageQuery().timeFrame(j, j + 1).query(withSpecies);

				toAdd.append(String.format("       %02d:00-%02d:00    ", j, j + 1));

				// 4 seasons
				for (int i = 0; i < 4; i++)
				{
					List<ImageEntry> withSpeciesAtTimeInSeason = new ImageQuery().monthOnly(seasons[i]).query(withSpeciesAtTime);
					List<ImageEntry> withSpeciesInSeason = new ImageQuery().monthOnly(seasons[i]).query(withSpecies);
					Integer numPics = analysis.activityForImageList(withSpeciesAtTimeInSeason);
					Integer totalPics = analysis.activityForImageList(withSpeciesInSeason);
					double frequency;
					if (totalPics != 0)
						frequency = (double) numPics / totalPics;
					else
						frequency = 0;

					hourlyTotals[i] = hourlyTotals[i] + numPics;

					toAdd.append(String.format("%5d        %5.3f    ", numPics, frequency));
				}

				toAdd.append("\n");
			}

			toAdd.append("       Hourly pics  ");
			for (int hourlyTotal : hourlyTotals) toAdd.append(String.format("%7d               ", hourlyTotal));

			toAdd.append("\n");

			toReturn.append(toAdd).append("\n");
		}

		return toReturn.toString();
	}
}
