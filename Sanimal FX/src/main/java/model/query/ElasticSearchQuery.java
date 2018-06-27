package model.query;


import model.SanimalData;
import model.cyverse.ImageCollection;
import model.location.Location;
import model.query.conditions.ElevationCondition;
import model.species.Species;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a query to be sent to CyVerse
 */
public class ElasticSearchQuery
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

	private final BoolQueryBuilder queryBuilder;

	/**
	 * Constructor initializes base query fields
	 */
	public ElasticSearchQuery()
	{
		this.queryBuilder = QueryBuilders.boolQuery();
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
	 * @param endYear The end year to 'and' into the query
	 */
	public void setStartAndEndYear(Integer startYear, Integer endYear)
	{
		queryBuilder.must().add(QueryBuilders.rangeQuery("yearTaken").gte(startYear).lte(endYear));
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
		this.queryBuilder.must().add(QueryBuilders.rangeQuery("dateTaken").gte(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
	}

	/**
	 * Sets the end date that all images must be taken after
	 *
	 * @param endDate The end date
	 */
	public void setEndDate(LocalDateTime endDate)
	{
		this.queryBuilder.must().add(QueryBuilders.rangeQuery("dateTaken").lte(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
	}

	public void addElevationCondition(Double elevation, ElevationCondition.ElevationComparisonOperators operator)
	{
		switch (operator)
		{
			case Equal:
				this.queryBuilder.must().add(QueryBuilders.termQuery("location.elevation", elevation));
				break;
			case GreaterThan:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("location.elevation").gt(elevation));
				break;
			case GreaterThanOrEqual:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("location.elevation").gte(elevation));
				break;
			case LessThan:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("location.elevation").lt(elevation));
				break;
			case LessThanOrEqual:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("location.elevation").lte(elevation));
				break;
			default:
				SanimalData.getInstance().getErrorDisplay().printError("Got an impossible elevation condition");
				break;
		}
	}

	public QueryBuilder build()
	{
		if (!speciesQuery.isEmpty())
		{
			this.queryBuilder.must().add(QueryBuilders.termQuery("imageMetadata.species.scientificName", this.speciesQuery.stream().map(Species::getScientificName).toArray(Species[]::new)));
		}

		/*
		// To test if a location is in a list, we is the "IN" operator. We need to create a formatted string like: ('loc1','loc2')
		String locationInStr = "(" + this.locationQuery.stream().map(location -> "'" + location.getId() + "'").collect(Collectors.joining(",")) + ")";
		if (!locationQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_LOCATION_ID);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, locationInStr);
		}

		// To test if a collection is in a list, we is the "IN" operator. We need to create a formatted string like: ('col1','col2')
		String imageCollectionInStr = "(" + this.collectionQuery.stream().map(imageCollection -> "'" + imageCollection.getID().toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!collectionQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_COLLECTION_ID);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, imageCollectionInStr);
		}

		// To test if a month is in a list, we is the "IN" operator. We need to create a formatted string like: ('mon1','mon2')
		String monthInStr = "(" + this.monthQuery.stream().map(month -> "'" + month.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!monthQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_MONTH_TAKEN);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, monthInStr);
		}

		// To test if a hour is in a list, we is the "IN" operator. We need to create a formatted string like: ('hr1','hr2')
		String hourInStr = "(" + this.hourQuery.stream().map(hour -> "'" + hour.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!hourQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_HOUR_TAKEN);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, hourInStr);
		}

		// To test if a day of week is in a list, we is the "IN" operator. We need to create a formatted string like: ('doy1','doy2')
		String dayOfWeekInStr = "(" + this.dayOfWeekQuery.stream().map(dayOfWeek -> "'" + dayOfWeek.toString() + "'").collect(Collectors.joining(",")) + ")";
		if (!dayOfWeekQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_DAY_OF_WEEK_TAKEN);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, dayOfWeekInStr);
		}
		*/

		return this.queryBuilder;
	}
}
