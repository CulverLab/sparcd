package controller.analysisView.conditions;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.query.IQueryCondition;
import model.query.conditions.CollectionCondition;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class CollectionConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<ImageCollection> collectionFilterListView;
	@FXML
	public TextField txtCollectionSearch;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	@Override
	public void initializeData(IQueryCondition queryCondition)
	{
		if (queryCondition instanceof CollectionCondition)
		{
			// Grab the global image collection list
			SortedList<ImageCollection> imageCollections = new SortedList<>(((CollectionCondition) queryCondition).imageCollectionListProperty());
			// We set the comparator to be the name of the collection
			imageCollections.setComparator(Comparator.comparing(ImageCollection::getName));
			// We create a local wrapper of the imageCollections list to filter
			FilteredList<ImageCollection> imageCollectionFilteredList = new FilteredList<>(imageCollections);
			// Set the filter to update whenever the imageCollections search text changes
			this.txtCollectionSearch.textProperty().addListener(observable -> {
				imageCollectionFilteredList.setPredicate(imageCollection ->
						// Allow any imageCollections with a name or scientific name containing the imageCollections search text
						(StringUtils.containsIgnoreCase(imageCollection.getName(), this.txtCollectionSearch.getCharacters())));
			});
			// Set the items of the imageCollections list view to the newly sorted list
			this.collectionFilterListView.setItems(imageCollectionFilteredList);
			this.collectionFilterListView.setCellFactory(CheckBoxListCell.forListView(ImageCollection::shouldBePartOfAnalysisProperty));
			this.collectionFilterListView.setEditable(true);
		}
	}

	public void clearCollectionSearch(ActionEvent actionEvent)
	{
		this.txtCollectionSearch.clear();
	}

	public void selectAllCollections(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getCollectionList().forEach(imageCollection -> imageCollection.setShouldBePartOfAnalysis(true));
	}

	public void selectNoCollections(ActionEvent actionEvent)
	{
		SanimalData.getInstance().getCollectionList().forEach(imageCollection -> imageCollection.setShouldBePartOfAnalysis(false));
	}
}
