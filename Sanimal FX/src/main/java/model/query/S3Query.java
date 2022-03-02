package model.query;

import model.constant.SanimalMetadataFields;
import model.s3.ImageCollection;
import model.location.Location;
import model.species.Species;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class representing a query
 */
public class S3Query
{
	// A list of species to query for
	private Set<Species> speciesQuery = new HashSet<>();
	// A list of locations to query for
	private Set<Location> locationQuery = new HashSet<>();
	// A list of collections to query for
	private Set<ImageCollection> collectionQuery = new HashSet<>();
	// A list of months to query for
	private Set<Integer> monthQuery = new HashSet<>();
	// A list of hours to query for
	private Set<Integer> hourQuery = new HashSet<>();
	// A list of days of week to query for
	private Set<Integer> dayOfWeekQuery = new HashSet<>();

	// The IRODS query builder to append to
	private S3QueryBuilder queryBuilder;

	/**
	 * Constructor initializes base query fields
	 */
	public S3Query()
	{
		// We want distinct results
		this.queryBuilder = new S3QueryBuilder(true, false);
	}

	/**
	 * Adds a given species to the query
	 *
	 * @param species The species to 'and' into the query
	 */
	public void addSpecies(Species species)
	{
		this.speciesQuery.add(species);
	}

	/**
	 * Adds a given location to the query
	 *
	 * @param location The location to 'and' into the query
	 */
	public void addLocation(Location location)
	{
		this.locationQuery.add(location);
	}

	/**
	 * Adds a given image collection to the query
	 *
	 * @param imageCollection The image collection to 'and' into the query
	 */
	public void addImageCollection(ImageCollection imageCollection)
	{
		this.collectionQuery.add(imageCollection);
	}

	/**
	 * Adds a given startYear to the query
	 *
	 * @param startYear The start year to 'and' into the query
	 * @param startYear The end year to 'and' into the query
	 */
	public void setStartAndEndYear(Integer startYear, Integer endYear)
	{
		appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_YEAR_TAKEN);
		appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.NUMERIC_GREATER_THAN_OR_EQUAL_TO, startYear);
		appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_YEAR_TAKEN);
		appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.NUMERIC_LESS_THAN_OR_EQUAL_TO, endYear);
	}

	/**
	 * Adds a given month to the query
	 *
	 * @param month The month to 'and' into the query
	 */
	public void addMonth(Integer month)
	{
		this.monthQuery.add(month);
	}

	/**
	 * Adds a given hour to the query
	 *
	 * @param hour The hour to 'and' into the query
	 */
	public void addHour(Integer hour)
	{
		this.hourQuery.add(hour);
	}

	/**
	 * Adds a given day of week to the query
	 *
	 * @param dayOfWeek The day of week to 'and' into the query
	 */
	public void addDayOfWeek(Integer dayOfWeek)
	{
		this.dayOfWeekQuery.add(dayOfWeek);
	}

	/**
	 * Sets the start date that all images must be taken after
	 *
	 * @param startDate The start date
	 */
	public void setStartDate(LocalDateTime startDate)
	{
		appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_TIME_TAKEN);
		appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.NUMERIC_GREATER_THAN, startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	/**
	 * Sets the end date that all images must be taken after
	 *
	 * @param endDate The end date
	 */
	public void setEndDate(LocalDateTime endDate)
	{
		appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_TIME_TAKEN);
		appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.NUMERIC_LESS_THAN, endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	public void addElevationCondition(Double elevation, S3QueryConditionOperators operator)
	{
		appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_LOCATION_ELEVATION);
		appendQueryElement(S3QueryPart.VALUE, operator, elevation.toString());
	}

	/**
	 * Finalizes the query and returns it as an S3 query builder objects
	 *
	 * @return The S3 query ready to be executed
	 */
	public S3QueryBuilder build()
	{
		// To test if a species is in a list, we is the "IN" operator. We need to create a formatted string like: ('spec1','spec2')
		String speciesInStr = "(" + this.speciesQuery.stream().map(species -> "'" + species.getScientificName() + "'").collect(Collectors.joining(",")) + ")";
		if (!speciesQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, speciesInStr);
		}

		// To test if a location is in a list, we is the "IN" operator. We need to create a formatted string like: ('loc1','loc2')
		String locationInStr = "(" + this.locationQuery.stream().map(location -> "'" + location.getId() + "'").collect(Collectors.joining(",")) + ")";
		if (!locationQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_LOCATION_ID);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, locationInStr);
		}

		// To test if a collection is in a list, we is the "IN" operator. We need to create a formatted string like: ('col1','col2')
		String imageCollectionInStr = "(" + this.collectionQuery.stream().map(imageCollection -> "'" + imageCollection.getID().toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!collectionQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_COLLECTION_ID);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, imageCollectionInStr);
		}

		// To test if a month is in a list, we is the "IN" operator. We need to create a formatted string like: ('mon1','mon2')
		String monthInStr = "(" + this.monthQuery.stream().map(month -> "'" + month.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!monthQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_MONTH_TAKEN);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, monthInStr);
		}

		// To test if a hour is in a list, we is the "IN" operator. We need to create a formatted string like: ('hr1','hr2')
		String hourInStr = "(" + this.hourQuery.stream().map(hour -> "'" + hour.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!hourQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_HOUR_TAKEN);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, hourInStr);
		}

		// To test if a day of week is in a list, we is the "IN" operator. We need to create a formatted string like: ('doy1','doy2')
		String dayOfWeekInStr = "(" + this.dayOfWeekQuery.stream().map(dayOfWeek -> "'" + dayOfWeek.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!dayOfWeekQuery.isEmpty())
		{
			appendQueryElement(S3QueryPart.ATTRIBUTE, S3QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_DAY_OF_WEEK_TAKEN);
			appendQueryElement(S3QueryPart.VALUE, S3QueryConditionOperators.IN, dayOfWeekInStr);
		}

		return this.queryBuilder;
	}

	/**
	 * Appends a query element given ATTRIBUTE, VALUE, or UNIT
	 *
	 * @param part The part to translate into iRODS column
	 * @param operator The operator which is unchanged
	 * @param value The value to query for which is unchanged
	 */
	private void appendQueryElement(S3QueryPart part, S3QueryConditionOperators operator, String value)
	{
		this.queryBuilder.addConditionAsQueryField(part, operator, value);
	}

	/**
	 * Appends a query element given ATTRIBUTE, VALUE, or UNIT
	 *
	 * @param part The part to translate into iRODS column
	 * @param operator The operator which is unchanged
	 * @param value The value to query for which is unchanged
	 */
	private void appendQueryElement(S3QueryPart part, S3QueryConditionOperators operator, int value)
	{
		this.queryBuilder.addConditionAsQueryField(part, operator, value);
	}

	/**
	 * Appends a query element given ATTRIBUTE, VALUE, or UNIT
	 *
	 * @param part The part to translate into iRODS column
	 * @param operator The operator which is unchanged
	 * @param value The value to query for which is unchanged
	 */
	private void appendQueryElement(S3QueryPart part, S3QueryConditionOperators operator, long value)
	{
		this.queryBuilder.addConditionAsQueryField(part, operator, value);
	}
}
