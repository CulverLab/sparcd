package controller;

import controller.analysisView.VisDrSandersonController;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import model.SanimalData;
import model.analysis.DataAnalysis;
import model.analysis.SanimalTextOutputFormatter;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller class for the analysis page
 */
public class SanimalAnalysisController implements Initializable
{
	///
	/// FXML bound fields start
	///

	@FXML
	public ListView<Species> speciesFilterListView;

	@FXML
	public TextField txtSpeciesSearch;

	@FXML
	public ListView<Location> locationFilterListView;

	@FXML
	public TextField txtLocationSearch;

	@FXML
	public TextField txtEventInterval;

	@FXML
	public VisDrSandersonController visDrSandersonController;

	@FXML
	public DatePicker dateStart;
	@FXML
	public DatePicker dateEnd;

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
	}

	public void refreshVisualizations(ActionEvent actionEvent)
	{
		// First parse all parameters, starting with event interval (default 30 min)
		Integer eventInterval = 30;
		try
		{
			eventInterval = Integer.parseInt(this.txtEventInterval.getText());
		}
		catch (NumberFormatException ignored) {}

		if (eventInterval <= 0)
			eventInterval = 30;

		Date startDate = this.dateStart.getValue() == null ? Date.valueOf(LocalDate.MAX) : Date.valueOf(this.dateStart.getValue());
		Date endDate = this.dateEnd.getValue() == null ? Date.valueOf(LocalDate.MIN) : Date.valueOf(this.dateEnd.getValue());

		// Now process the filters
		List<ImageEntry> imagesToAnalyze = SanimalData.getInstance().getAllImages().stream()
				// Test for checked location
				.filter(imageEntry -> imageEntry.getLocationTaken().shouldBePartOfAnalysis())
				// Test for checked species
				.filter(imageEntry -> imageEntry.getSpeciesPresent().stream().map(SpeciesEntry::getSpecies).anyMatch(Species::shouldBePartOfAnalysis))
				// Test for the date range
				.filter(imageEntry -> imageEntry.getDateTaken().after(startDate) && imageEntry.getDateTaken().before(endDate))
				.collect(Collectors.toList());

		visDrSandersonController.visualize(imagesToAnalyze, eventInterval);
	}

	public void selectAllSpecies(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getSpeciesList().forEach(species -> species.setShouldBePartOfAnalysis(true));
	}

	public void selectNoSpecies(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getSpeciesList().forEach(species -> species.setShouldBePartOfAnalysis(false));
	}

	public void clearSpeciesSearch(ActionEvent actionEvent)
	{
		this.txtSpeciesSearch.clear();
	}

	public void selectAllLocations(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getLocationList().forEach(location -> location.setShouldBePartOfAnalysis(true));
	}

	public void selectNoLocations(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getLocationList().forEach(location -> location.setShouldBePartOfAnalysis(false));
	}

	public void clearLocationSearch(ActionEvent actionEvent)
	{
		this.txtLocationSearch.clear();
	}
}
