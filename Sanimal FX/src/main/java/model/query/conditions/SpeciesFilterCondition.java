package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.SanimalData;
import model.query.ElasticSearchQuery;
import model.query.IQueryCondition;
import model.species.Species;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model used by the "Species filter" query condition
 */
public class SpeciesFilterCondition implements IQueryCondition
{
	// A map of species -> if the species is selected to be filtered
	private Map<Species, BooleanProperty> speciesToSelected = new HashMap<>();

	/**
	 * Constructor ensures that each species maps to a boolean property
	 */
	public SpeciesFilterCondition()
	{
		// Make sure each species maps to a boolean property, this is important for later, since our view will use this to populate checkboxes
		for (Species species : this.getSpeciesList())
			if (!this.speciesToSelected.containsKey(species))
				this.speciesToSelected.put(species, new SimpleBooleanProperty(true));
		// If the species list changes, we add a boolean property for the new added species
		this.getSpeciesList().addListener((ListChangeListener<Species>) c ->
		{
			while (c.next())
				if (c.wasAdded())
					for (Species species : c.getAddedSubList())
						if (!this.speciesToSelected.containsKey(species))
							this.speciesToSelected.put(species, new SimpleBooleanProperty(true));
		});
	}

	/**
	 * This query condition ensures only selected species are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(ElasticSearchQuery query)
	{
		for (Species species : this.getSpeciesList())
			if (speciesToSelected.containsKey(species) && speciesToSelected.get(species).getValue())
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
		return "SpeciesCondition.fxml";
	}

	/**
	 * Returns a list of species to be filtered
	 *
	 * @return The filtered species
	 */
	public ObservableList<Species> getSpeciesList()
	{
		return SanimalData.getInstance().getSpeciesList();
	}

	/**
	 * Gets the property defining if a species is selected
	 *
	 * @param species The species to test if it's selected
	 * @return The property representing if the species is selected
	 */
	public BooleanProperty speciesSelectedProperty(Species species)
	{
		if (!this.speciesToSelected.containsKey(species))
			this.speciesToSelected.put(species, new SimpleBooleanProperty(true));
		return this.speciesToSelected.get(species);
	}

	/**
	 * Selects all species
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.speciesToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all species
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.speciesToSelected.values())
			selected.set(false);
	}
}
