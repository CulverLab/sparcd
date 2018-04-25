package controller;

import com.panemu.tiwulfx.control.DetachableTabPane;
import controller.analysisView.VisCSVController;
import controller.analysisView.VisDrSandersonController;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import jfxtras.scene.control.LocalDateTimePicker;
import model.SanimalData;
import model.analysis.DataAnalyzer;
import model.cyverse.CyVerseQuery;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller class for the analysis page
 */
public class SanimalAnalysisController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The list view of all species used in analysis
	@FXML
	public ListView<Species> speciesFilterListView;
	// The species search box
	@FXML
	public TextField txtSpeciesSearch;

	// The list view of all locations used in analysis
	@FXML
	public ListView<Location> locationFilterListView;
	// The location search box
	@FXML
	public TextField txtLocationSearch;

	// References to all controllers in the tabs of the analysis page
	@FXML
	public VisDrSandersonController visDrSandersonController;
	@FXML
	public VisCSVController visCSVController;

	// Date picker used to select start and end dates
	@FXML
	public LocalDateTimePicker dateTimeStart;
	@FXML
	public LocalDateTimePicker dateTimeEnd;

	// The tab pane containing all visualizations
	@FXML
	public DetachableTabPane tbnVisualizations;


	///
	/// FXML bound fields end
	///

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// First we setup the species list

		// Grab the global species list
		SortedList<Species> species = new SortedList<>(SanimalData.getInstance().getSpeciesList());
		// We set the comparator to be the name of the species
		species.setComparator(Comparator.comparing(Species::getName));
		// We create a local wrapper of the species list to filter
		FilteredList<Species> speciesFilteredList = new FilteredList<>(species);
		// Set the filter to update whenever the species search text changes
		this.txtSpeciesSearch.textProperty().addListener(observable -> {
			speciesFilteredList.setPredicate(speciesToFilter ->
					// Allow any species with a name or scientific name containing the species search text
					(StringUtils.containsIgnoreCase(speciesToFilter.getName(), this.txtSpeciesSearch.getCharacters()) ||
							StringUtils.containsIgnoreCase(speciesToFilter.getScientificName(), this.txtSpeciesSearch.getCharacters())));
		});
		// Set the items of the species list view to the newly sorted list
		this.speciesFilterListView.setItems(speciesFilteredList);
		this.speciesFilterListView.setCellFactory(CheckBoxListCell.forListView(Species::shouldBePartOfAnalysisProperty));
		this.speciesFilterListView.setEditable(true);

		// Next we setup the location list

		// Grab the global location list
		SortedList<Location> locations = new SortedList<>(SanimalData.getInstance().getLocationList());
		// We set the comparator to be the name of the location
		locations.setComparator(Comparator.comparing(Location::getName));
		// We create a local wrapper of the location list to filter
		FilteredList<Location> locationsFilteredList = new FilteredList<>(locations);
		// Set the filter to update whenever the location search text changes
		this.txtLocationSearch.textProperty().addListener(observable -> {
			locationsFilteredList.setPredicate(locationToFilter ->
					// Allow any location with a name or id containing the location search text
					(StringUtils.containsIgnoreCase(locationToFilter.getName(), this.txtLocationSearch.getCharacters()) ||
							StringUtils.containsIgnoreCase(locationToFilter.getId(), this.txtLocationSearch.getCharacters())));
		});
		// Set the items of the location list view to the newly sorted list
		this.locationFilterListView.setItems(locationsFilteredList);
		this.locationFilterListView.setCellFactory(CheckBoxListCell.forListView(Location::shouldBePartOfAnalysisProperty));
		this.locationFilterListView.setEditable(true);

		// Setup our time filters
		this.dateTimeStart.setLocalDateTime(LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0));
		this.dateTimeEnd.setLocalDateTime(LocalDateTime.now());

		this.dateTimeStart.setAllowNull(false);
		this.dateTimeEnd.setAllowNull(false);
	}

	/**
	 * Called when the refresh button is pressed
	 *
	 * @param actionEvent ignored
	 */
	public void query(ActionEvent actionEvent)
	{
		// Default 60s event interval
		Integer eventInterval = 60;

		// Grab the start and end date
		LocalDateTime startDate = this.dateTimeStart.getLocalDateTime() == null ? LocalDateTime.MIN : this.dateTimeStart.getLocalDateTime();
		LocalDateTime endDate = this.dateTimeEnd.getLocalDateTime() == null ? LocalDateTime.MAX : this.dateTimeEnd.getLocalDateTime();

		CyVerseQuery query = new CyVerseQuery().setStartDate(startDate).setEndDate(endDate);
		for (Location location : SanimalData.getInstance().getLocationList())
			if (location.shouldBePartOfAnalysis())
				query = query.addLocation(location);
		for (Species species : SanimalData.getInstance().getSpeciesList())
			if (species.shouldBePartOfAnalysis())
				query = query.addSpecies(species);
		List<ImageEntry> queryResult = SanimalData.getInstance().getConnectionManager().performQuery(query);
		DataAnalyzer dataAnalyzer = new DataAnalyzer(queryResult, eventInterval);

		// Hand the analysis over to the visualizations to graph
		visDrSandersonController.visualize(dataAnalyzer);
		visCSVController.visualize(dataAnalyzer);
	}

	/**
	 * Button used to select all species for analysis use
	 *
	 * @param actionEvent ignored
	 */
	public void selectAllSpecies(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getSpeciesList().forEach(species -> species.setShouldBePartOfAnalysis(true));
	}

	/**
	 * Button used to select no species to be part of the analysis
	 *
	 * @param actionEvent ignored
	 */
	public void selectNoSpecies(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getSpeciesList().forEach(species -> species.setShouldBePartOfAnalysis(false));
	}

	/**
	 * Button used to clear the species search box
	 *
	 * @param actionEvent ignored
	 */
	public void clearSpeciesSearch(ActionEvent actionEvent)
	{
		this.txtSpeciesSearch.clear();
	}

	/**
	 * Button used to select all locations for analysis use
	 *
	 * @param actionEvent ignored
	 */
	public void selectAllLocations(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getLocationList().forEach(location -> location.setShouldBePartOfAnalysis(true));
	}

	/**
	 * Button used to select no locations to be part of the analysis
	 *
	 * @param actionEvent ignored
	 */
	public void selectNoLocations(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getLocationList().forEach(location -> location.setShouldBePartOfAnalysis(false));
	}

	/**
	 * Button used to clear the location search box
	 *
	 * @param actionEvent ignored
	 */
	public void clearLocationSearch(ActionEvent actionEvent)
	{
		this.txtLocationSearch.clear();
	}
}
