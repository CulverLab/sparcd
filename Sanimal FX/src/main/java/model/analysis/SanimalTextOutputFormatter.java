package model.analysis;

import model.analysis.textFormatters.*;
import model.image.ImageEntry;

import java.util.List;

/**
 * The formatter that simulates the creation of Jim Sanderson's "Output.txt"
 * 
 * @author David Slovikosky
 */
public class SanimalTextOutputFormatter
{
	/**
	 * Returns a massive string that is perfectly formatted to look like "Output.txt"
	 * 
	 * @param dataStatistics
	 *            A set of statistics computed from the images
	 * @return A formatted string
	 */
	public String format(DataAnalyzer dataStatistics)
	{
		// The string to return
		String toReturn = "";

		List<ImageEntry> images = dataStatistics.getOriginalImageList();

		// If there are no images, return an appropriate string
		if (images.isEmpty())
			return "No images found under directory";

		// Elapsed time in the calculation
		long elapsedTime = System.currentTimeMillis();

		// Create all required formatters
		HeaderFormatter headerFormatter = new HeaderFormatter(images, dataStatistics);
		FirstLastSpeciesFormatter firstLastSpeciesFormatter = new FirstLastSpeciesFormatter(images, dataStatistics);
		ActPerAbuLocFormatter actPerAbuLocFormatter = new ActPerAbuLocFormatter(images, dataStatistics);
		TrapDaysAndEffortFormatter trapDaysAndEffortFormatter = new TrapDaysAndEffortFormatter(images, dataStatistics);
		LocationStatFormatter locationStatFormatter = new LocationStatFormatter(images, dataStatistics);
		ActivityPatternFormatter activityPatternFormatter = new ActivityPatternFormatter(images, dataStatistics);
		LunarActivityFormatter lunarActivityFormatter = new LunarActivityFormatter(images, dataStatistics);
		RichnessFormatter richnessFormatter = new RichnessFormatter(images, dataStatistics);
		SpeciesLocCoordFormatter speciesLocCoordFormatter = new SpeciesLocCoordFormatter(images, dataStatistics);
		TotalDayFormatter totalDayFormatter = new TotalDayFormatter(images, dataStatistics);
		OccouranceFormatter occouranceFormatter = new OccouranceFormatter(images, dataStatistics);
		DetectionRateFormatter detectionRateFormatter = new DetectionRateFormatter(images, dataStatistics);

		// Proceed to print each header:

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

	/**
	 * Instead of printing "output.txt" like above, this prints "Allpictures.txt"
	 * 
	 * @param dataStatistics
	 *            A set of statistics computed from the images
	 * @return a formatted string
	 */
	public String createAllPictures(DataAnalyzer dataStatistics)
	{
		String toReturn = "";

		// Create an analysis object, then create the formatter, and create the text
		DataAnalyzer analysis = new DataAnalyzer(dataStatistics.getOriginalImageList(), dataStatistics.getEventInterval());
		AllPicturesFormatter allPicturesFormatter = new AllPicturesFormatter(dataStatistics.getOriginalImageList(), analysis);
		toReturn = toReturn + allPicturesFormatter.createAllPictures();

		return toReturn;
	}
}
