package model.query;


import model.SanimalData;
import model.cyverse.ImageCollection;
import model.location.Location;
import model.query.conditions.ElevationCondition;
import model.species.Species;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
		queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.yearTaken").gte(startYear).lte(endYear));
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
		this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.dateTaken").gte(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
	}

	/**
	 * Sets the end date that all images must be taken after
	 *
	 * @param endDate The end date
	 */
	public void setEndDate(LocalDateTime endDate)
	{
		this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.dateTaken").lte(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
	}

	public void addElevationCondition(Double elevation, ElevationCondition.ElevationComparisonOperators operator)
	{
		switch (operator)
		{
			case Equal:
				this.queryBuilder.must().add(QueryBuilders.termQuery("imageMetadata.location.elevation", elevation));
				break;
			case GreaterThan:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.location.elevation").gt(elevation));
				break;
			case GreaterThanOrEqual:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.location.elevation").gte(elevation));
				break;
			case LessThan:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.location.elevation").lt(elevation));
				break;
			case LessThanOrEqual:
				this.queryBuilder.must().add(QueryBuilders.rangeQuery("imageMetadata.location.elevation").lte(elevation));
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
			TermsQueryBuilder innerQuery = QueryBuilders.termsQuery("imageMetadata.speciesEntries.species.scientificName", this.speciesQuery.stream().map(Species::getScientificName).collect(Collectors.toList()));
			this.queryBuilder.must(QueryBuilders.nestedQuery("imageMetadata.speciesEntries", innerQuery, ScoreMode.Max));
		}

		if (!locationQuery.isEmpty())
			this.queryBuilder.must().add(QueryBuilders.termsQuery("imageMetadata.location.id", this.locationQuery.stream().map(Location::getId).collect(Collectors.toList())));

		if (!collectionQuery.isEmpty())
			this.queryBuilder.must().add(QueryBuilders.termsQuery("collectionID", this.collectionQuery.stream().map(imageCollection -> imageCollection.getID().toString()).collect(Collectors.toList())));

		if (!monthQuery.isEmpty())
			this.queryBuilder.must().add(QueryBuilders.termsQuery("imageMetadata.monthTaken", this.monthQuery));

		if (!hourQuery.isEmpty())
			this.queryBuilder.must().add(QueryBuilders.termsQuery("imageMetadata.hourTaken", this.hourQuery));

		if (!dayOfWeekQuery.isEmpty())
			this.queryBuilder.must().add(QueryBuilders.termsQuery("imageMetadata.dayOfWeekTaken", this.dayOfWeekQuery));

		return this.queryBuilder;
	}
}
