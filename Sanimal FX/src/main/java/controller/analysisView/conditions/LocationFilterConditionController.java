package controller.analysisView.conditions;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import model.SanimalData;
import model.location.Location;
import model.query.IQueryCondition;
import model.query.conditions.LocationFilterCondition;
import model.query.conditions.StartDateCondition;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class LocationFilterConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The list view of all locations used in analysis
	@FXML
	public ListView<Location> locationFilterListView;
	// The location search box
	@FXML
	public TextField txtLocationSearch;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	public void initializeData(IQueryCondition locationFilterCondition)
	{
		if (locationFilterCondition instanceof LocationFilterCondition)
		{
			// Grab the global location list
			SortedList<Location> locations = new SortedList<>(((LocationFilterCondition) locationFilterCondition).locationListProperty());
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
