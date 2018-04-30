package model.query.conditions;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.SanimalData;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

/**
 * Data model used by the "Species filter" query condition
 */
public class SpeciesFilterCondition implements IQueryCondition
{
	/**
	 * This query condition ensures only selected species are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Species species : this.speciesListProperty())
			if (species.shouldBePartOfAnalysis())
				query.addSpecies(species);
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "SpeciesFilterCondition.fxml";
	}

	/**
	 * Returns a list of species to be filtered
	 *
	 * @return The filtered species
	 */
	public ObservableList<Species> speciesListProperty()
	{
		return SanimalData.getInstance().getSpeciesList();
	}
}
