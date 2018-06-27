package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import model.query.IQueryCondition;
import model.query.conditions.SpeciesFilterCondition;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Species filter" UI component
 */
public class SpeciesConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The list view of all species used in analysis
	@FXML
	public ListView<Species> speciesFilterListView;
	// The species search box
	@FXML
	public TextField txtSpeciesSearch;

	///
	/// FXML Bound Fields End
	///

	// The data model reference
	private SpeciesFilterCondition speciesFilterCondition;

	/**
	 * Initialize does nothing for this specific filter
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	/**
	 * Initializes the controller with a data model to bind to
	 *
	 * @param iQueryCondition The data model to bind to this filter, must be a species filter type
	 */
	public void initializeData(IQueryCondition iQueryCondition)
	{
		// The species filter controller must take a species filter condition
		if (iQueryCondition instanceof SpeciesFilterCondition)
		{
			this.speciesFilterCondition = (SpeciesFilterCondition) iQueryCondition;

			// Grab the global species list
			SortedList<Species> speciesSorted = new SortedList<>(this.speciesFilterCondition.getSpeciesList());
			// We set the comparator to be the name of the species
			speciesSorted.setComparator(Comparator.comparing(Species::getCommonName));
			// We create a local wrapper of the species list to filter
			FilteredList<Species> speciesFilteredList = new FilteredList<>(speciesSorted);
			// Set the filter to update whenever the species search text changes
			this.txtSpeciesSearch.textProperty().addListener(observable -> {
				speciesFilteredList.setPredicate(speciesToFilter ->
						// Allow any species with a name or scientific name containing the species search text
						(StringUtils.containsIgnoreCase(speciesToFilter.getCommonName(), this.txtSpeciesSearch.getCharacters()) ||
								StringUtils.containsIgnoreCase(speciesToFilter.getScientificName(), this.txtSpeciesSearch.getCharacters())));
			});
			// Set the items of the species list view to the newly sorted list
			this.speciesFilterListView.setItems(speciesFilteredList);
			this.speciesFilterListView.setCellFactory(CheckBoxListCell.forListView(species -> speciesFilterCondition.speciesSelectedProperty(species)));
			this.speciesFilterListView.setEditable(true);
		}
	}

	/**
	 * Button used to select all species for analysis use
	 *
	 * @param actionEvent consumed
	 */
	public void selectAllSpecies(ActionEvent actionEvent)
	{
		if (this.speciesFilterCondition != null)
			this.speciesFilterCondition.selectAll();
		actionEvent.consume();
	}

	/**
	 * Button used to select no species to be part of the analysis
	 *
	 * @param actionEvent consumed
	 */
	public void selectNoSpecies(ActionEvent actionEvent)
	{
		if (this.speciesFilterCondition != null)
			this.speciesFilterCondition.selectNone();
		actionEvent.consume();
	}

	/**
	 * Button used to clear the species search box
	 *
	 * @param actionEvent consumed
	 */
	public void clearSpeciesSearch(ActionEvent actionEvent)
	{
		this.txtSpeciesSearch.clear();
		actionEvent.consume();
	}
}
