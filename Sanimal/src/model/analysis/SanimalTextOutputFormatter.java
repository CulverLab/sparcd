/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.util.List;

import model.ImageEntry;
import model.analysis.textFormatters.ActPerAbuLocFormatter;
import model.analysis.textFormatters.ActivityPatternFormatter;
import model.analysis.textFormatters.FirstLastSpeciesFormatter;
import model.analysis.textFormatters.HeaderFormatter;
import model.analysis.textFormatters.LocationStatFormatter;
import model.analysis.textFormatters.LunarActivityFormatter;
import model.analysis.textFormatters.RichnessFormatter;
import model.analysis.textFormatters.SpeciesLocCoordFormatter;
import model.analysis.textFormatters.TotalDayFormatter;
import model.analysis.textFormatters.TrapDaysAndEffortFormatter;

public class SanimalTextOutputFormatter
{
	public String format(List<ImageEntry> images, Integer eventInterval)
	{
		String toReturn = "";

		if (images.isEmpty())
			return "No images found under directory";

		DataAnalysis analysis = new DataAnalysis(images, eventInterval);
		HeaderFormatter headerFormatter = new HeaderFormatter(images, analysis, eventInterval);
		FirstLastSpeciesFormatter firstLastSpeciesFormatter = new FirstLastSpeciesFormatter(images, analysis, eventInterval);
		ActPerAbuLocFormatter actPerAbuLocFormatter = new ActPerAbuLocFormatter(images, analysis, eventInterval);
		TrapDaysAndEffortFormatter trapDaysAndEffortFormatter = new TrapDaysAndEffortFormatter(images, analysis, eventInterval);
		LocationStatFormatter locationStatFormatter = new LocationStatFormatter(images, analysis, eventInterval);
		ActivityPatternFormatter activityPatternFormatter = new ActivityPatternFormatter(images, analysis, eventInterval);
		LunarActivityFormatter lunarActivityFormatter = new LunarActivityFormatter(images, analysis, eventInterval);
		RichnessFormatter richnessFormatter = new RichnessFormatter(images, analysis, eventInterval);
		SpeciesLocCoordFormatter speciesLocCoordFormatter = new SpeciesLocCoordFormatter(images, analysis, eventInterval);
		TotalDayFormatter totalDayFormatter = new TotalDayFormatter(images, analysis, eventInterval);

		// LOCATIONS 

		toReturn = toReturn + headerFormatter.printLocations();

		// SPECIES

		toReturn = toReturn + headerFormatter.printSpecies();

		// FOR ALL SPECIES AT ALL LOCATIONS

		toReturn = toReturn + headerFormatter.printImageAnalysisHeader();

		// NUMBER OF DAYS IN CAMERA TRAP PROGRAM

		toReturn = toReturn + firstLastSpeciesFormatter.printDaysInCameraTrap();

		// FIRST PICTURE OF EACH SPECIES

		toReturn = toReturn + firstLastSpeciesFormatter.printFirstPicOfEachSpecies();

		// LAST PICTURE OF EACH SPECIES

		toReturn = toReturn + firstLastSpeciesFormatter.printLastPicOfEachSpecies();

		// SPECIES ACCUMULATION CURVE

		toReturn = toReturn + firstLastSpeciesFormatter.printSpeciesAccumulationCurve();

		// NUMBER OF PICTURES AND FILTERED PICTURES PER YEAR

		toReturn = toReturn + actPerAbuLocFormatter.printNumberOfPicturesByYear();

		// NUMBER OF PICTURES BY SPECIES BY YEAR

		toReturn = toReturn + actPerAbuLocFormatter.printNumberOfPicturesBySpeciesByYear();

		// SPECIES RANKED BY NUMBER OF INDEPENDENT PICTURES AND PERCENT OF TOTAL

		toReturn = toReturn + actPerAbuLocFormatter.printNumberOfPicturesByPercentTotal();

		// CAMERA TRAP DAYS

		toReturn = toReturn + trapDaysAndEffortFormatter.printCameraTrapDays();

		//CAMERA TRAP EFFORT

		toReturn = toReturn + trapDaysAndEffortFormatter.printCameraTrapEffort();

		// CAMERA TRAP EFFORT SUMMARY

		toReturn = toReturn + trapDaysAndEffortFormatter.printCameraTrapEffortSummary();

		// FOR EACH LOCATION TOTAL NUMBER AND PERCENT OF EACH SPECIES

		toReturn = toReturn + locationStatFormatter.printPercentOfSpeciesInLoc();

		// FOR EACH LOCATION AND MONTH TOTAL NUMBER EACH SPECIES

		toReturn = toReturn + locationStatFormatter.printSpeciesByMonthByLocByYear();

		// ALL LOCATIONS ALL SPECIES FOR EACH MONTH FOR ALL YEARS

		toReturn = toReturn + locationStatFormatter.printSpeciesByMonthByLoc();

		// DISTANCE (km) BETWEEN LOCATIONS

		toReturn = toReturn + locationStatFormatter.printDistanceBetweenLocations();

		// ACTIVITY PATTERNS

		toReturn = toReturn + activityPatternFormatter.printActivityPatterns();

		// SPECIES PAIRS ACTIVITY SIMILARITY (LOWER IS MORE SIMILAR)

		toReturn = toReturn + activityPatternFormatter.printSpeciesPairsActivitySimilarity();

		// SPECIES PAIR MOST SIMILAR IN ACTIVITY (FREQUENCY)

		toReturn = toReturn + activityPatternFormatter.printSpeciePairMostSimilar();

		// CHI-SQUARE ANALYSIS OF PAIRED ACTIVITY PATTERNS

		toReturn = toReturn + activityPatternFormatter.printChiSquareAnalysisPairedActivity();

		// LUNAR ACTIVITY PATTERN

		toReturn = toReturn + lunarActivityFormatter.printLunarActivity();

		//SPECIES LUNAR ACTIVITY MOST DIFFERENT: 

		toReturn = toReturn + lunarActivityFormatter.printLunarActivityMostDifferent();

		// ACTIVITY PATTERNS BY SEASON

		toReturn = toReturn + activityPatternFormatter.printActivityPatternsSeason();

		// SPECIES ABUNDANCE

		toReturn = toReturn + actPerAbuLocFormatter.printSpeciesAbundance();

		// LOCATIONS BY SPECIES AND LOCATION AND SPECIES RICHNESS

		toReturn = toReturn + richnessFormatter.printLocationSpeciesRichness();

		// LOCATION SPECIES FREQUENCY SIMILARITY (LOWER IS MORE SIMILAR)
		//		toReturn = toReturn + "LOCATION SPECIES FREQUENCY SIMILARITY (LOWER IS MORE SIMILAR)\n";
		//		toReturn = toReturn + "   One picture of each species per camera per PERIOD\n";
		//		toReturn = toReturn + "   Square root of sums of squared difference in frequency\n\n";
		//		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES FREQUENCY\n\n";
		//		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES FREQUENCY\n\n";
		//      ???

		// LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)
		//		toReturn = toReturn + "LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)\n";
		//		toReturn = toReturn + "  Is species present at this location? yes=1, no=0\n";
		//		toReturn = toReturn + "  1.00 means locations are identical; 0.00 means locations have no species in common\n";
		//		toReturn = toReturn + "  Location, location, JSI, number of species at each location, and number of species in common\n\n";
		//		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST SIMILAR IN SPECIES COMPOSITION\n\n";
		//		toReturn = toReturn + "  TOP 10 LOCATION PAIRS MOST DIFFERENT IN SPECIES COMPOSITION\n\n";
		//      ???

		// SPECIES BY LOCATION WITH UTM AND ELEVATION

		toReturn = toReturn + speciesLocCoordFormatter.printSpeciesByLocWithUTM();

		// SPECIES OVERLAP AT LOCATIONS

		toReturn = toReturn + locationStatFormatter.printSpeciesOverlapAtLoc();

		// CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES
		toReturn = toReturn + "CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES\n";
		toReturn = toReturn + "  H0: Species frequencies are independent of site\n";
		toReturn = toReturn + "  Reject null hypothesis = R, Accept null hypothesis = -\n";
		toReturn = toReturn + "Sites                      ";
		//		for (Location location : analysis.getAllImageLocations())
		//			toReturn = toReturn + String.format("%-8s", StringUtils.left(location.getName(), 8));
		//		toReturn = toReturn + "\n";
		//		for (Location location : analysis.getAllImageLocations())
		//		{
		//			toReturn = toReturn + String.format("%-28s", location.getName());
		//
		//			toReturn = toReturn + "\n";
		//		}
		// ???
		toReturn = toReturn + "\n";

		// PICTURES FOR EACH LOCATION BY MONTH AND YEAR

		toReturn = toReturn + totalDayFormatter.printPicturesByMonthYearLoc();

		// PICTURES FOR EACH LOCATION BY MONTH AND YEAR SUMMARY

		toReturn = toReturn + totalDayFormatter.printPicturesByMonthLoc();

		// SPECIES AND SPECIES RICHNESS BY YEAR AND MONTH

		toReturn = toReturn + totalDayFormatter.printPicturesByMonthYearSpeciesRichness();

		// SPECIES ALL YEARS BY MONTH

		toReturn = toReturn + totalDayFormatter.printPicturesByMonthSpeciesRichness();

		// SPECIES BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION

		toReturn = toReturn + totalDayFormatter.printPicturesByMonthSpeciesLocElevation();

		// SPECIES ABUNDANCE BY LOCATION BY YEAR BY MONTH SORTED BY ELEVATION

		toReturn = toReturn + totalDayFormatter.printAbundanceByMonthSpeciesLocElevation();

		return toReturn;

	}
}
