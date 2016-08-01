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
import model.analysis.textFormatters.DetectionRateFormatter;
import model.analysis.textFormatters.FirstLastSpeciesFormatter;
import model.analysis.textFormatters.HeaderFormatter;
import model.analysis.textFormatters.LocationStatFormatter;
import model.analysis.textFormatters.LunarActivityFormatter;
import model.analysis.textFormatters.OccouranceFormatter;
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

		long elapsedTime = System.currentTimeMillis();

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
		OccouranceFormatter occouranceFormatter = new OccouranceFormatter(images, analysis, eventInterval);
		DetectionRateFormatter detectionRateFormatter = new DetectionRateFormatter(images, analysis, eventInterval);

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

		toReturn = toReturn + locationStatFormatter.printLocSpeciesFrequencySimiliarity();

		// LOCATION-SPECIES COMPOSITION SIMILARITY (Jaccard Similarity Index)

		toReturn = toReturn + locationStatFormatter.printLocSpeciesCompositionSimiliarity();

		// SPECIES BY LOCATION WITH UTM AND ELEVATION

		toReturn = toReturn + speciesLocCoordFormatter.printSpeciesByLocWithUTM();

		// SPECIES OVERLAP AT LOCATIONS

		toReturn = toReturn + locationStatFormatter.printSpeciesOverlapAtLoc();

		// CHI-SQUARE ANALYSIS OF PAIRED SITES SPECIES FREQUENCIES

		toReturn = toReturn + occouranceFormatter.printCHISqAnalysisOfPairedSpecieFreq();

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

		// SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT

		toReturn = toReturn + totalDayFormatter.printSpeciesByLocElevationAndEffort();

		// SPECIES BY LOCATION SORTED BY ELEVATION AND NORMALIZED BY EFFORT TABLE

		toReturn = toReturn + totalDayFormatter.printSpeciesByLocElevationAndEffortTable();

		// SPECIES AVERAGE ABUNDANCE BY YEAR AND SITE

		toReturn = toReturn + actPerAbuLocFormatter.printSpeciesAbundanceYearSite();

		// SPECIES AVERAGE ABUNDANCE BY SITE ALL YEARS

		toReturn = toReturn + actPerAbuLocFormatter.printSpeciesAbundanceSite();

		// SPECIES CO-OCCURRENCE MATRIX

		toReturn = toReturn + occouranceFormatter.printCoOccuranceMatrix();

		// ABSENCE-PRESENCE MATRIX

		toReturn = toReturn + occouranceFormatter.printAbsensePresenceMatrix();

		// SPECIES MIN AND MAX ELEVATION

		toReturn = toReturn + occouranceFormatter.printMaxMinSpeciesElevation();

		// DETECTION RATE FOR EACH SPECIES PER YEAR

		toReturn = toReturn + detectionRateFormatter.printDetectionRateSpeciesYear();

		// DETECTION RATE SUMMARY FOR EACH SPECIES

		toReturn = toReturn + detectionRateFormatter.printDetectionRateSummary();

		// DETECTION RATE FOR EACH LOCATION BY MONTH

		toReturn = toReturn + detectionRateFormatter.printDetectionRateLocationMonth();

		// DETECTION RATE SUMMARY FOR EACH LOCATION BY MONTH

		toReturn = toReturn + detectionRateFormatter.printDetectionRateLocationMonthSummary();

		// MONTHLY DETECTION RATE TREND

		toReturn = toReturn + detectionRateFormatter.printDetectionRateTrend();

		// NATIVE OCCUPANCY

		toReturn = toReturn + occouranceFormatter.printNativeOccupancy();

		// AREA COVERED BY CAMERA TRAPS

		toReturn = toReturn + locationStatFormatter.printAreaCoveredByTraps();

		// ELAPSED TIME

		toReturn = toReturn + "ELAPSED TIME " + String.format("%10.3f ", ((System.currentTimeMillis() - elapsedTime) / 1000D)) + "SECONDS";

		return toReturn;

	}
}
