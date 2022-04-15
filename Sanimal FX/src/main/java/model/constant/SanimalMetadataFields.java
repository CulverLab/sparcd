package model.constant;

import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShort;

public class SanimalMetadataFields
{
	// The metadata field representing a flag that tells us that the image has been tagged with sanimal data
	public static final TagInfoShort SANIMAL = new TagInfoShort("Sanimal Flag", 551, TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
	// The metadata field representing what species entries are added to an image
	public static final TagInfoAscii SPECIES_ENTRY = new TagInfoAscii("Species Entry", 552, -1, TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
	// The metadata field representing the location that a species was tagged at
	public static final TagInfoAscii LOCATION_ENTRY = new TagInfoAscii("Location Name", 553, -1, TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);

	// The meta key for SANIMAL data
	public static final String A_SANIMAL = "sanimal";

	// The meta key for date taken
	public static final String A_DATE_TIME_TAKEN = "dateTimeTaken";
	// The meta key for year taken
	public static final String A_DATE_YEAR_TAKEN = "dateYearTaken";
	// The meta key for month taken
	public static final String A_DATE_MONTH_TAKEN = "dateMonthTaken";
	// The meta key for hour taken
	public static final String A_DATE_HOUR_TAKEN = "dateHourTaken";
	// The meta key for day of year taken
	public static final String A_DATE_DAY_OF_YEAR_TAKEN = "dateDayOfYearTaken";
	// The meta key for day of week taken
	public static final String A_DATE_DAY_OF_WEEK_TAKEN = "dateDayOfWeekTaken";

	// The meta key for Location name
	public static final String A_LOCATION_NAME = "locationName";
	// The meta key for Location ID
	public static final String A_LOCATION_ID = "locationID";
	// The meta key for Location latitude
	public static final String A_LOCATION_LATITUDE = "locationLatitude";
	// The meta key for Location longitude
	public static final String A_LOCATION_LONGITUDE = "locationLongitude";
	// The meta key for location elevation
	public static final String A_LOCATION_ELEVATION = "locationElevation";

	// The meta key for species scientific name
	public static final String A_SPECIES_SCIENTIFIC_NAME = "speciesScientificName";
	// The meta key for species name
	public static final String A_SPECIES_COMMON_NAME = "metaSpeciesCommonName";
	// The meta key for species count
	public static final String A_SPECIES_COUNT = "metaSpeciesCount";

	// The meta key for collection id
	public static final String A_COLLECTION_ID = "collectionID";
}
