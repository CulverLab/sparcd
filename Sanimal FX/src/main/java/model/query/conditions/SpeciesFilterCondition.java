package model.query.conditions;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.SanimalData;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

public class SpeciesFilterCondition implements IQueryCondition
{
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (Species species : SanimalData.getInstance().getSpeciesList())
			if (species.shouldBePartOfAnalysis())
				query.addSpecies(species);
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "SpeciesFilterCondition.fxml";
	}

	public ObservableList<Species> speciesListProperty()
	{
		return SanimalData.getInstance().getSpeciesList();
	}
}
