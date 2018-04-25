package model.cyverse;


import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.location.Location;
import model.species.Species;
import org.irods.jargon.core.query.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CyVerseQuery
{
	private List<Species> speciesQuery = new LinkedList<>();
	private List<Location> locationQuery = new LinkedList<>();

	private IRODSGenQueryBuilder queryBuilder;

	public CyVerseQuery()
	{
		this.queryBuilder = new IRODSGenQueryBuilder(true, false, null);
		try
		{
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME); // Path to the collection containing this data item
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME); // Name of this data object
			/*
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_D_DATA_ID); // ID of the data item
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_ID); // ID of the metadata
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME); // Attribute
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE); // Value
			queryBuilder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS); // Units
			*/
		}
		catch (GenQueryBuilderException e)
		{
			e.printStackTrace();
		}
		appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_SANIMAL);
		appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.EQUAL, "true");
	}

	public CyVerseQuery addSpecies(Species species)
	{
		speciesQuery.add(species);
		return this;
	}

	public CyVerseQuery setSpecies(Species species)
	{
		speciesQuery.clear();
		speciesQuery.add(species);
		return this;
	}

	public CyVerseQuery addLocation(Location location)
	{
		locationQuery.add(location);
		return this;
	}

	public CyVerseQuery setLocation(Location location)
	{
		locationQuery.clear();
		locationQuery.add(location);
		return this;
	}

	public CyVerseQuery setStartDate(LocalDateTime startDate)
	{
		appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_TIME_TAKEN);
		appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.NUMERIC_GREATER_THAN, startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		return this;
	}

	public CyVerseQuery setEndDate(LocalDateTime endDate)
	{
		appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_DATE_TIME_TAKEN);
		appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.NUMERIC_LESS_THAN, endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		return this;
	}

	public IRODSGenQueryBuilder build()
	{
		String speciesInStr = "(" + this.speciesQuery.stream().map(species -> "'" + species.getScientificName() + "'").collect(Collectors.joining(",")) + ")";
		if (!speciesQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, speciesInStr);
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME);
		}

		String locationInStr = "(" + this.locationQuery.stream().map(location -> "'" + location.getId() + "'").collect(Collectors.joining(",")) + ")";
		if (!locationQuery.isEmpty())
		{
			appendQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, QueryConditionOperators.EQUAL, SanimalMetadataFields.A_LOCATION_ID);
			appendQueryElement(AVUQueryElement.AVUQueryPart.VALUE, QueryConditionOperators.IN, locationInStr);
		}
		return this.queryBuilder;
	}

	private void appendQueryElement(AVUQueryElement.AVUQueryPart part, QueryConditionOperators operator, String value)
	{
		switch(part)
		{
			case ATTRIBUTE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, operator, value);
				break;
			case VALUE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE, operator, value);
				break;
			case UNITS:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS, operator, value);
				break;
			default:
				break;
		}
	}

	private void appendQueryElement(AVUQueryElement.AVUQueryPart part, QueryConditionOperators operator, int value)
	{
		switch(part)
		{
			case ATTRIBUTE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, operator, value);
				break;
			case VALUE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE, operator, value);
				break;
			case UNITS:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS, operator, value);
				break;
			default:
				break;
		}
	}

	private void appendQueryElement(AVUQueryElement.AVUQueryPart part, QueryConditionOperators operator, long value)
	{
		switch(part)
		{
			case ATTRIBUTE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, operator, value);
				break;
			case VALUE:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE, operator, value);
				break;
			case UNITS:
				appendQueryElement(RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS, operator, value);
				break;
			default:
				break;
		}
	}

	private void appendQueryElement(RodsGenQueryEnum column, QueryConditionOperators operator, String value)
	{
		this.queryBuilder.addConditionAsGenQueryField(column, operator, value);
	}

	private void appendQueryElement(RodsGenQueryEnum column, QueryConditionOperators operator, int value)
	{
		this.queryBuilder.addConditionAsGenQueryField(column, operator, value);
	}

	private void appendQueryElement(RodsGenQueryEnum column, QueryConditionOperators operator, long value)
	{
		this.queryBuilder.addConditionAsGenQueryField(column, operator, value);
	}
}
