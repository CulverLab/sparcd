package controller.analysisView.conditions;

import controller.analysisView.IConditionController;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import model.cyverse.ImageCollection;
import model.query.IQueryCondition;
import model.query.conditions.CollectionCondition;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Collection filter" UI component
 */
public class CollectionConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The listview of collections to filter
	@FXML
	public ListView<ImageCollection> collectionFilterListView;
	// The search bar for the collections
	@FXML
	public TextField txtCollectionSearch;

	///
	/// FXML Bound Fields End
	///

	// The data model reference
	private CollectionCondition collectionCondition;

	/**
	 * Initializes the UI, does nothing
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	@Override
	public void initializeData(IQueryCondition iQueryCondition)
	{
		// Make sure the data model we received matches our type
		if (iQueryCondition instanceof CollectionCondition)
		{
			this.collectionCondition = (CollectionCondition) iQueryCondition;

			// Grab the image collection list from our data model item
			SortedList<ImageCollection> imageCollections = new SortedList<>(this.collectionCondition.getImageCollections());
			// We set the comparator to be the name of the collection
			imageCollections.setComparator(Comparator.comparing(ImageCollection::getName));
			// We create a local wrapper of the imageCollections list to filter
			FilteredList<ImageCollection> imageCollectionFilteredList = new FilteredList<>(imageCollections);
			// Set the filter to update whenever the imageCollections search text changes
			this.txtCollectionSearch.textProperty().addListener(observable -> {
				imageCollectionFilteredList.setPredicate(imageCollection ->
						// Allow any imageCollections with a name containing the imageCollections search text
						(StringUtils.containsIgnoreCase(imageCollection.getName(), this.txtCollectionSearch.getCharacters())));
			});
			// Set the items of the imageCollections list view to the newly sorted list
			this.collectionFilterListView.setItems(imageCollectionFilteredList);
			// Each collection gets a checkbox
			this.collectionFilterListView.setCellFactory(CheckBoxListCell.forListView(imageCollection -> this.collectionCondition.imageCollectionSelectedProperty(imageCollection)));
			this.collectionFilterListView.setEditable(true);
		}
	}

	/**
	 * Clear the search bar when we click the clear search button
	 *
	 * @param actionEvent consumed
	 */
	public void clearCollectionSearch(ActionEvent actionEvent)
	{
		this.txtCollectionSearch.clear();
		actionEvent.consume();
	}

	/**
	 * Selects all collections for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectAllCollections(ActionEvent actionEvent)
	{
		if (collectionCondition != null)
			collectionCondition.selectAll();
		actionEvent.consume();
	}

	/**
	 * Selects no collections for the given data model
	 *
	 * @param actionEvent consumed
	 */
	public void selectNoCollections(ActionEvent actionEvent)
	{
		if (collectionCondition != null)
			collectionCondition.selectNone();
		actionEvent.consume();
	}
}
