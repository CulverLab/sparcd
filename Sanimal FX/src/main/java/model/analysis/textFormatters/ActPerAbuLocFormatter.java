package model.analysis.textFormatters;

import model.analysis.DataAnalyzer;
import model.analysis.ImageQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * The text formatter for species activities, abundance, and period at locations
 * 
 * @author David Slovikosky
 */
public class ActPerAbuLocFormatter extends TextFormatter
{
	// Jim's program added 1 to counts greater than 1 in activity count, fixed the issue
	// Jim's program added 1 to counts greater than 1 in period count, fixed the issue
	// Jim's program calculations for Abundance were completely wrong and made no sense
	// When running DataAnalyze, the last period can have X elements. The last period is being added to "Abundance" X times instead of once.
	// The above problems were fixed as of the latest version of DataAnalyze
	// ALL = number of images containing the species
	// ACTIVITY = Number of periods containing at least one image in a single hour (ex. 1-1:59, 2-2:59, etc) 
	// PERIOD = Consecutive images that are less than "period" apart where period comes from user input
	// ABUNDANCE = Maximum number of animals photographed in a single image in each period

	public ActPerAbuLocFormatter(List<ImageEntry> images, DataAnalyzer analysis)
	{
		super(images, analysis);
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each year the total number of pictures of all species (All), the number of pictures that are used to calculate the activity pattern
	 * (Activity), the number of independent pictures (Period), and the total number of individuals in all the independent pitures (Abundance). This
	 * is followed by the total (Total) for all years.
	 * 
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printNumberOfPicturesByYear()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR\n");
		toReturn.append("        Year       All Activity   Period Abundance\n");

		int imageTotal = 0;
		int activityTotal = 0;
		int periodTotal = 0;
		int abundanceTotal = 0;
		for (Integer year : analysis.getAllImageYears())
		{
			int yearImageTotal = 0;
			int yearActivityTotal = 0;
			int yearPeriodTotal = 0;
			int yearAbundanceTotal = 0;
			List<ImageEntry> withYear = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withYearSpecies = new ImageQuery().speciesOnly(species).query(withYear);
				yearImageTotal = yearImageTotal + withYearSpecies.size();
				yearActivityTotal = yearActivityTotal + analysis.activityForImageList(withYearSpecies);
				yearPeriodTotal = yearPeriodTotal + analysis.periodForImageList(withYearSpecies);
				yearAbundanceTotal = yearAbundanceTotal + analysis.abundanceForImageList(withYearSpecies, species);
			}
			imageTotal = imageTotal + yearImageTotal;
			activityTotal = activityTotal + yearActivityTotal;
			periodTotal = periodTotal + yearPeriodTotal;
			abundanceTotal = abundanceTotal + yearAbundanceTotal;
			toReturn.append(String.format("        %4d   %7d  %7d  %7d  %7d\n", year, yearImageTotal, yearActivityTotal, yearPeriodTotal, yearAbundanceTotal));
		}
		toReturn.append(String.format("        Total  %7d  %7d  %7d  %7d\n", imageTotal, activityTotal, periodTotal, abundanceTotal));

		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species to be analyzed and for each year, the total number of pictures (All), the number of pictures that are used to calculate the
	 * activity pattern (Activity), the number of independent pictures (Period), the total number of individuals in all the independent pitures
	 * (Abundance), and the number of locations where the species was recorded (Sites). This is followed by the total (Total) for all years.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printNumberOfPicturesBySpeciesByYear()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("NUMBER OF PICTURES BY SPECIES BY YEAR\n");
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn.append("  ").append(species.getName()).append("\n");
			toReturn.append("        Year       All Activity   Period Abundance Locations\n");
			int speciesImageTotal = 0;
			int speciesActivityTotal = 0;
			int speciesPeriodTotal = 0;
			int speciesAbundanceTotal = 0;
			int speciesLocationTotal = 0;
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new ImageQuery().yearOnly(year).query(withSpecies);
				Integer speciesImage = withSpeciesYear.size();
				Integer speciesActivity = analysis.activityForImageList(withSpeciesYear);
				Integer speciesPeriod = analysis.periodForImageList(withSpeciesYear);
				Integer speciesAbundance = analysis.abundanceForImageList(withSpeciesYear, species);
				Integer speciesLocation = analysis.locationsForImageList(withSpeciesYear).size();
				speciesImageTotal = speciesImageTotal + speciesImage;
				speciesActivityTotal = speciesActivityTotal + speciesActivity;
				speciesPeriodTotal = speciesPeriodTotal + speciesPeriod;
				speciesAbundanceTotal = speciesAbundanceTotal + speciesAbundance;
				speciesLocationTotal = speciesLocationTotal + speciesLocation;
				toReturn.append(String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, speciesImage, speciesActivity, speciesPeriod, speciesAbundance, speciesLocation));
			}

			toReturn.append(String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", speciesImageTotal, speciesActivityTotal, speciesPeriodTotal, speciesAbundanceTotal, speciesLocationTotal));

			toReturn.append("\n");
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * A list of the number of independent pictures of each species ranked by total from most to least, and the percent of the total number of all
	 * independent pictures. The total number of indepdenent pictures is given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printNumberOfPicturesByPercentTotal()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL\n");

		toReturn.append("     Species                   Total  Percent\n");

		Integer periodTotal = 0;
		for (Integer year : analysis.getAllImageYears())
		{
			Integer yearPeriodTotal = 0;
			List<ImageEntry> withYear = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Species species : analysis.getAllImageSpecies())
				yearPeriodTotal = yearPeriodTotal + analysis.periodForImageList(new ImageQuery().speciesOnly(species).query(withYear));
			periodTotal = periodTotal + yearPeriodTotal;
		}

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer speciesPeriodTotal = 0;
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
				speciesPeriodTotal = speciesPeriodTotal + analysis.periodForImageList(new ImageQuery().yearOnly(year).query(withSpecies));
			toReturn.append(String.format("  %-28s %5d  %7.2f\n", species.getName(), speciesPeriodTotal, (speciesPeriodTotal.doubleValue() / periodTotal) * 100.0));
		}
		toReturn.append(String.format("  Total pictures               %5d   100.00\n", periodTotal));
		toReturn.append("\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * For each species (SPECIES) a list of the number of independent pictures (NUMBER PICS), proportion of the total number of all independent
	 * pictures (PROPORTION), the average number of individuals in each picture (AVG NUM INDIVS), and the proportion of the total number of
	 * individuals for that species divided by the total number of all individuals (PROPORTION). The total number of all independent pictures (Total)
	 * is given in the last line.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesAbundance()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES ABUNDANCE\n");
		toReturn.append("  One record of each species per location per PERIOD\n");
		toReturn.append("SPECIES                      NUMBER PICS      RELATIVE ABUNDANCE     AVG NUM INDIVS     ABUNDANCE OF INDIVS\n");
		Integer periodOverAllSpecies = 0;
		Integer numAnimalsPhotographed = 0;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new ImageQuery().yearOnly(year).query(withSpecies);
				periodOverAllSpecies = periodOverAllSpecies + analysis.periodForImageList(withSpeciesYear);
				numAnimalsPhotographed = numAnimalsPhotographed + analysis.abundanceForImageList(withSpeciesYear, species);
			}
		}
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer abundanceTotal = 0;
			Integer periodTotal = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new ImageQuery().yearOnly(year).query(withSpecies);
				abundanceTotal = abundanceTotal + analysis.abundanceForImageList(withSpeciesYear, species);
				periodTotal = periodTotal + analysis.periodForImageList(withSpeciesYear);
			}
			toReturn.append(String.format("%-28s %7d               %7.2f             %7.2f             %7.2f\n", species.getName(), periodTotal, 100.0D * (double) periodTotal / periodOverAllSpecies, (double) abundanceTotal / periodTotal, (double) abundanceTotal / numAnimalsPhotographed * 100));
		}
		toReturn.append(String.format("Total                        %7d                100.00", periodOverAllSpecies));

		toReturn.append("\n\n");

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesAbundanceYearSite()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES AVERAGE ABUNDANCE BY YEAR AND SITE\n");
		toReturn.append("One record of each species per location per PERIOD\n");
		toReturn.append("               Use maximum number of individuals per PERIOD\n");

		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> withYear = new ImageQuery().yearOnly(year).query(analysis.getImagesSortedByDate());
			toReturn.append(year).append("\n");
			toReturn.append("Species                     ");
			for (Location location : analysis.getAllImageLocations())
				toReturn.append(String.format("%-5s ", StringUtils.left(location.getName(), 5)));
			toReturn.append("\n");
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withYearSpecies = new ImageQuery().speciesOnly(species).query(withYear);
				toReturn.append(String.format("%-28s", species.getName()));
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withYearSpeciesLoc = new ImageQuery().locationOnly(location).query(withYearSpecies);
					Integer abundance = analysis.abundanceForImageList(withYearSpeciesLoc, species);
					Integer period = analysis.periodForImageList(new ImageQuery().locationOnly(location).speciesOnly(species).query(images));
					toReturn.append(String.format("%5.2f ", period == 0 ? 0 : (double) abundance / period));
				}
				toReturn.append("\n");
			}

			toReturn.append("\n");
		}

		return toReturn.toString();
	}

	/**
	 * <p>
	 * Dr. Jim Sanderson's description:
	 * <p>
	 * No description given.
	 * 
	 * @return Returns a string representing the data in a clean form
	 */
	public String printSpeciesAbundanceSite()
	{
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("SPECIES AVERAGE ABUNDANCE BY SITE ALL YEARS\n");

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn.append("Years ").append(analysis.getAllImageYears().get(0)).append(" to ").append(analysis.getAllImageYears().get(numYears - 1)).append("\n");

		toReturn.append("Species                     ");
		for (Location location : analysis.getAllImageLocations())
			toReturn.append(String.format("%-5s ", StringUtils.left(location.getName(), 5)));
		toReturn.append("\n");

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new ImageQuery().speciesOnly(species).query(analysis.getImagesSortedByDate());
			toReturn.append(String.format("%-28s", species.getName()));
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new ImageQuery().locationOnly(location).query(withSpecies);
				Integer abundance = analysis.abundanceForImageList(withSpeciesLoc, species);
				Integer period = analysis.periodForImageList(new ImageQuery().locationOnly(location).speciesOnly(species).query(images));
				toReturn.append(String.format("%5.2f ", period == 0 ? 0 : (double) abundance / period));
			}
			toReturn.append("\n");
		}

		toReturn.append("\n");

		return toReturn.toString();
	}
}
