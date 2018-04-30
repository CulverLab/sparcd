package model.query.conditions;

import javafx.collections.ObservableList;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

/**
 * Data model used by the "Collection filter" query condition
 */
public class CollectionCondition implements IQueryCondition
{
	/**
	 * This query condition ensures only selected collections are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (ImageCollection imageCollection : this.imageCollectionListProperty())
			if (imageCollection.shouldBePartOfAnalysis())
				query.addImageCollection(imageCollection);
	}

	/**
	 * Returns the FXML document that can edit this data model
	 *
	 * @return An FXML UI document to edit this data model
	 */
	@Override
	public String getFXMLConditionEditor()
	{
		return "CollectionCondition.fxml";
	}

	/**
	 * Returns the list of image collections
	 *
	 * @return A list of image collections to filter
	 */
	public ObservableList<ImageCollection> imageCollectionListProperty()
	{
		return SanimalData.getInstance().getCollectionList();
	}
}
