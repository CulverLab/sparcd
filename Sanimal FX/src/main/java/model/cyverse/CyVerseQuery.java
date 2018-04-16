package model.cyverse;


import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.location.Location;
import model.species.Species;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;

import java.util.LinkedList;
import java.util.List;

public class CyVerseQuery
{
	private List<AVUQueryElement> baseQuery = new LinkedList<>();
	private List<AVUQueryElement> speciesQuery = new LinkedList<>();
	private List<AVUQueryElement> locationQuery = new LinkedList<>();

	public CyVerseQuery()
	{
		baseQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, SanimalMetadataFields.A_SANIMAL));
		baseQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, "true"));
	}

	public CyVerseQuery addSpecies(Species species)
	{
		speciesQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME));
		speciesQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, species.getScientificName()));
		return this;
	}

	public CyVerseQuery setSpecies(Species species)
	{
		speciesQuery.clear();
		speciesQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, SanimalMetadataFields.A_SPECIES_SCIENTIFIC_NAME));
		speciesQuery.add(createQueryElement(AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, species.getScientificName()));
		return this;
	}

	public CyVerseQuery addLocation(Location location)
	{
		createQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, SanimalMetadataFields.A_LOCATION_ID);
		createQueryElement(AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, location.getId());
		return this;
	}

	public CyVerseQuery setLocation(Location location)
	{
		locationQuery.clear();
		createQueryElement(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, SanimalMetadataFields.A_LOCATION_ID);
		createQueryElement(AVUQueryElement.AVUQueryPart.VALUE, AVUQueryOperatorEnum.EQUAL, location.getId());
		return this;
	}

	public List<AVUQueryElement> build()
	{
		List<AVUQueryElement> query = new LinkedList<>();
		query.addAll(baseQuery);
		query.addAll(speciesQuery);
		query.addAll(locationQuery);
		return query;
	}

	private AVUQueryElement createQueryElement(AVUQueryElement.AVUQueryPart part, AVUQueryOperatorEnum operator, String value)
	{
		try
		{
			return AVUQueryElement.instanceForValueQuery(part, operator, value);
		}
		catch (JargonQueryException e)
		{
			SanimalData.getInstance().getErrorDisplay().printError("Couldn't create query element?");
			e.printStackTrace();
			return null;
		}
	}
}
