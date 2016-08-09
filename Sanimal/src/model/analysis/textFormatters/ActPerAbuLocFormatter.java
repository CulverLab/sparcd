/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import model.ImageEntry;
import model.Location;
import model.Species;
import model.analysis.DataAnalysis;
import model.analysis.PredicateBuilder;

public class ActPerAbuLocFormatter extends TextFormatter
{
	// Jim's program added 1 to counts greater than 1 in activity count, fixed the issue
	// Jim's program added 1 to counts greater than 1 in period count, fixed the issue
	// Jim's program calculations for Abundance were completely wrong and made no sense
	// When running DataAnalyze, the last period can have X elements. The last period is being added to "Abundance" X times instead of once.
	// ALL = number of images containing the species
	// ACTIVITY = Number of periods containing at least one image in a single hour (ex. 1-1:59, 2-2:59, etc) 
	// PERIOD = Consecutive images that are less than "period" apart where period comes from user input
	// ABUNDANCE = Maximum number of animals photographed in a single image in each period

	public ActPerAbuLocFormatter(List<ImageEntry> images, DataAnalysis analysis, Integer eventInterval)
	{
		super(images, analysis, eventInterval);
	}

	public String printNumberOfPicturesByYear()
	{
		String toReturn = "";

		toReturn = toReturn + "NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR\n";
		toReturn = toReturn + "        Year       All Activity   Period Abundance\n";

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
			List<ImageEntry> withYear = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withYearSpecies = new PredicateBuilder().speciesOnly(species).query(withYear);
				yearImageTotal = yearImageTotal + withYearSpecies.size();
				yearActivityTotal = yearActivityTotal + analysis.activityForImageList(withYearSpecies);
				yearPeriodTotal = yearPeriodTotal + analysis.periodForImageList(withYearSpecies);
				yearAbundanceTotal = yearAbundanceTotal + analysis.abundanceForImageList(withYearSpecies, species);
			}
			imageTotal = imageTotal + yearImageTotal;
			activityTotal = activityTotal + yearActivityTotal;
			periodTotal = periodTotal + yearPeriodTotal;
			abundanceTotal = abundanceTotal + yearAbundanceTotal;
			toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d\n", year, yearImageTotal, yearActivityTotal, yearPeriodTotal, yearAbundanceTotal);
		}
		toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d\n", imageTotal, activityTotal, periodTotal, abundanceTotal);

		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printNumberOfPicturesBySpeciesByYear()
	{
		String toReturn = "";

		toReturn = toReturn + "NUMBER OF PICTURES BY SPECIES BY YEAR\n";
		for (Species species : analysis.getAllImageSpecies())
		{
			toReturn = toReturn + "  " + species.getName() + "\n";
			toReturn = toReturn + "        Year       All Activity   Period Abundance Locations\n";
			int speciesImageTotal = 0;
			int speciesActivityTotal = 0;
			int speciesPeriodTotal = 0;
			int speciesAbundanceTotal = 0;
			int speciesLocationTotal = 0;
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new PredicateBuilder().yearOnly(year).query(withSpecies);
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
				toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, speciesImage, speciesActivity, speciesPeriod, speciesAbundance, speciesLocation);
			}

			toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", speciesImageTotal, speciesActivityTotal, speciesPeriodTotal, speciesAbundanceTotal, speciesLocationTotal);

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

	public String printNumberOfPicturesByPercentTotal()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL\n";

		toReturn = toReturn + "     Species                   Total  Percent\n";

		Integer periodTotal = 0;
		for (Integer year : analysis.getAllImageYears())
		{
			Integer yearPeriodTotal = 0;
			List<ImageEntry> withYear = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
			for (Species species : analysis.getAllImageSpecies())
				yearPeriodTotal = yearPeriodTotal + analysis.periodForImageList(new PredicateBuilder().speciesOnly(species).query(withYear));
			periodTotal = periodTotal + yearPeriodTotal;
		}

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer speciesPeriodTotal = 0;
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
				speciesPeriodTotal = speciesPeriodTotal + analysis.periodForImageList(new PredicateBuilder().yearOnly(year).query(withSpecies));
			toReturn = toReturn + String.format("  %-28s %5d  %7.2f\n", species.getName(), speciesPeriodTotal, (speciesPeriodTotal.doubleValue() / periodTotal) * 100.0);
		}
		toReturn = toReturn + String.format("  Total pictures               %5d   100.00\n", periodTotal);
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printSpeciesAbundance()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES ABUNDANCE\n";
		toReturn = toReturn + "  One record of each species per location per PERIOD\n";
		toReturn = toReturn + "SPECIES                      NUMBER PICS      RELATIVE ABUNDANCE     AVG NUM INDIVS     ABUNDANCE OF INDIVS\n";
		Integer periodOverAllSpecies = 0;
		Integer numAnimalsPhotographed = 0;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new PredicateBuilder().yearOnly(year).query(withSpecies);
				periodOverAllSpecies = periodOverAllSpecies + analysis.periodForImageList(withSpeciesYear);
				numAnimalsPhotographed = numAnimalsPhotographed + analysis.abundanceForImageList(withSpeciesYear, species);
			}
		}
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			Integer abundanceTotal = 0;
			Integer periodTotal = 0;
			for (Integer year : analysis.getAllImageYears())
			{
				List<ImageEntry> withSpeciesYear = new PredicateBuilder().yearOnly(year).query(withSpecies);
				abundanceTotal = abundanceTotal + analysis.abundanceForImageList(withSpeciesYear, species);
				periodTotal = periodTotal + analysis.periodForImageList(withSpeciesYear);
			}
			toReturn = toReturn + String.format("%-28s %7d               %7.2f             %7.2f             %7.2f\n", species.getName(), periodTotal, 100.0D * (double) periodTotal / periodOverAllSpecies, (double) abundanceTotal / periodTotal, (double) abundanceTotal / numAnimalsPhotographed * 100);
		}
		toReturn = toReturn + String.format("Total                        %7d                100.00", periodOverAllSpecies);

		toReturn = toReturn + "\n\n";

		return toReturn;
	}

	public String printSpeciesAbundanceYearSite()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES AVERAGE ABUNDANCE BY YEAR AND SITE\n";
		toReturn = toReturn + "One record of each species per location per PERIOD\n";
		toReturn = toReturn + "               Use maximum number of individuals per PERIOD\n";

		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> withYear = new PredicateBuilder().yearOnly(year).query(analysis.getImagesSortedByDate());
			toReturn = toReturn + year + "\n";
			toReturn = toReturn + "Species                     ";
			for (Location location : analysis.getAllImageLocations())
				toReturn = toReturn + String.format("%-5s ", StringUtils.left(location.getName(), 5));
			toReturn = toReturn + "\n";
			for (Species species : analysis.getAllImageSpecies())
			{
				List<ImageEntry> withYearSpecies = new PredicateBuilder().speciesOnly(species).query(withYear);
				toReturn = toReturn + String.format("%-28s", species.getName());
				for (Location location : analysis.getAllImageLocations())
				{
					List<ImageEntry> withYearSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withYearSpecies);
					Integer abundance = analysis.abundanceForImageList(withYearSpeciesLoc, species);
					Integer period = analysis.periodForImageList(new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images));
					toReturn = toReturn + String.format("%5.2f ", period == 0 ? 0 : (double) abundance / period);
				}
				toReturn = toReturn + "\n";
			}

			toReturn = toReturn + "\n";
		}

		return toReturn;
	}

	public String printSpeciesAbundanceSite()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES AVERAGE ABUNDANCE BY SITE ALL YEARS\n";

		Integer numYears = analysis.getAllImageYears().size();
		if (!analysis.getAllImageYears().isEmpty())
			toReturn = toReturn + "Years " + analysis.getAllImageYears().get(0) + " to " + analysis.getAllImageYears().get(numYears - 1) + "\n";

		toReturn = toReturn + "Species                     ";
		for (Location location : analysis.getAllImageLocations())
			toReturn = toReturn + String.format("%-5s ", StringUtils.left(location.getName(), 5));
		toReturn = toReturn + "\n";

		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(analysis.getImagesSortedByDate());
			toReturn = toReturn + String.format("%-28s", species.getName());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				Integer abundance = analysis.abundanceForImageList(withSpeciesLoc, species);
				Integer period = analysis.periodForImageList(new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images));
				toReturn = toReturn + String.format("%5.2f ", period == 0 ? 0 : (double) abundance / period);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}
}
