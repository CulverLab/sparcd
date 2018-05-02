package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import model.location.Location;
import model.query.IQueryCondition;
import model.query.conditions.LocationFilterCondition;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Location filter" UI component
 */
public class LocationConditionController implements IConditionController
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

	// The data model reference
	private LocationFilterCondition locationFilterCondition;

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
	 * @param iQueryCondition The data model which should be a location filter condition
	 */
	public void initializeData(IQueryCondition iQueryCondition)
	{
		// We must get a location filter condition for the location filter
		if (iQueryCondition instanceof LocationFilterCondition)
		{
			this.locationFilterCondition = (LocationFilterCondition) iQueryCondition;

			// Grab the global location list
			SortedList<Location> locations = new SortedList<>(this.locationFilterCondition.getLocationList());
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
			this.locationFilterListView.setCellFactory(CheckBoxListCell.forListView(location -> this.locationFilterCondition.locationSelectedProperty(location)));
			this.locationFilterListView.setEditable(true);
		}
	}

	/**
	 * Button used to select all locations for analysis use
	 *
	 * @param actionEvent consumed
	 */
	public void selectAllLocations(ActionEvent actionEvent)
	{
		if (this.locationFilterCondition != null)
			this.locationFilterCondition.selectAll();
		actionEvent.consume();
	}

	/**
	 * Button used to select no locations to be part of the analysis
	 *
	 * @param actionEvent consumed
	 */
	public void selectNoLocations(ActionEvent actionEvent)
	{
		if (this.locationFilterCondition != null)
			this.locationFilterCondition.selectNone();
		actionEvent.consume();
	}

	/**
	 * Button used to clear the location search box
	 *
	 * @param actionEvent consumed
	 */
	public void clearLocationSearch(ActionEvent actionEvent)
	{
		this.txtLocationSearch.clear();
		actionEvent.consume();
	}
}
