/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis.textFormatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			for (Species species : analysis.getAllImageSpecies())
			{
				yearImageTotal = yearImageTotal + analysis.getYearToNumberImages().get(species).getOrDefault(year, -1);
				yearActivityTotal = yearActivityTotal + analysis.getYearToActivity().get(species).getOrDefault(year, -1);
				yearPeriodTotal = yearPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, -1);
				yearAbundanceTotal = yearAbundanceTotal + analysis.getYearToAbundance().get(species).getOrDefault(year, -1);
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
			for (Integer year : analysis.getYearToNumberImages().get(species).keySet())
			{
				speciesImageTotal = speciesImageTotal + analysis.getYearToNumberImages().get(species).getOrDefault(year, -1);
				speciesActivityTotal = speciesActivityTotal + analysis.getYearToActivity().get(species).getOrDefault(year, -1);
				speciesPeriodTotal = speciesPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, -1);
				speciesAbundanceTotal = speciesAbundanceTotal + analysis.getYearToAbundance().get(species).getOrDefault(year, -1);
				speciesLocationTotal = speciesLocationTotal + analysis.getYearToLocations().get(species).getOrDefault(year, new ArrayList<Location>()).size();
				toReturn = toReturn + String.format("        %4d   %7d  %7d  %7d  %7d  %7d\n", year, analysis.getYearToNumberImages().get(species).getOrDefault(year, -1), analysis.getYearToActivity().get(species).getOrDefault(year, -1), analysis.getYearToPeriod().get(species).getOrDefault(year, -1),
						analysis.getYearToAbundance().get(species).getOrDefault(year, -1), analysis.getYearToLocations().get(species).getOrDefault(year, new ArrayList<Location>()).size());
			}

			toReturn = toReturn + String.format("        Total  %7d  %7d  %7d  %7d  %7d\n", speciesImageTotal, speciesActivityTotal, speciesPeriodTotal, speciesAbundanceTotal, speciesLocationTotal);
		}
		toReturn = toReturn + "\n";

		return toReturn;
	}

	public String printNumberOfPicturesByPercentTotal()
	{
		String toReturn = "";

		toReturn = toReturn + "SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL\n";

		toReturn = toReturn + "     Species                   Total  Percent\n";

		int periodTotal = 0;
		for (Integer year : analysis.getAllImageYears())
		{
			int yearPeriodTotal = 0;
			for (Species species : analysis.getAllImageSpecies())
				yearPeriodTotal = yearPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, -1);
			periodTotal = periodTotal + yearPeriodTotal;
		}

		for (Species species : analysis.getAllImageSpecies())
		{
			Integer speciesPeriodTotal = 0;
			for (Integer year : analysis.getAllImageYears())
				speciesPeriodTotal = speciesPeriodTotal + analysis.getYearToPeriod().get(species).getOrDefault(year, 0);
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
		for (Map<Integer, Integer> map : analysis.getYearToActivity().values())
			for (Integer integer : map.values())
				periodOverAllSpecies = periodOverAllSpecies + integer;
		for (Species species : analysis.getAllImageSpecies())
			for (Integer abundance : analysis.getYearToAbundance().get(species).values())
				numAnimalsPhotographed = numAnimalsPhotographed + abundance;
		for (Species species : analysis.getAllImageSpecies())
		{
			List<ImageEntry> picsWithSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			Map<Integer, Integer> yearToAbundance = analysis.getYearToAbundance().get(species);
			Map<Integer, Integer> yearToPeriod = analysis.getYearToPeriod().get(species);
			Integer abundanceTotal = 0;
			Integer periodTotal = 0;
			for (Integer abundance : yearToAbundance.values())
				abundanceTotal = abundanceTotal + abundance;
			for (Integer period : yearToPeriod.values())
				periodTotal = periodTotal + period;
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
		toReturn = toReturn + "\n";

		for (Integer year : analysis.getAllImageYears())
		{
			List<ImageEntry> withYear = new PredicateBuilder().yearOnly(year).query(images);
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
					Integer abundance = analysis.abundanceForImageList(withYearSpeciesLoc, eventInterval);
					Integer period = analysis.periodForImageList(new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images), eventInterval);
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
			List<ImageEntry> withSpecies = new PredicateBuilder().speciesOnly(species).query(images);
			toReturn = toReturn + String.format("%-28s", species.getName());
			for (Location location : analysis.getAllImageLocations())
			{
				List<ImageEntry> withSpeciesLoc = new PredicateBuilder().locationOnly(location).query(withSpecies);
				Integer abundance = analysis.abundanceForImageList(withSpeciesLoc, eventInterval);
				Integer period = analysis.periodForImageList(new PredicateBuilder().locationOnly(location).speciesOnly(species).query(images), eventInterval);
				toReturn = toReturn + String.format("%5.2f ", period == 0 ? 0 : (double) abundance / period);
			}
			toReturn = toReturn + "\n";
		}

		toReturn = toReturn + "\n";

		return toReturn;
	}
}
