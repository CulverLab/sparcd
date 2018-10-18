package model.query.conditions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model used by the "Collection filter" query condition
 */
public class CollectionCondition implements IQueryCondition
{
	// A map of collection -> if the collection is selected to be filtered
	private Map<ImageCollection, BooleanProperty> imageCollectionToSelected = new HashMap<>();

	/**
	 * Constructor ensures that each collection maps to a boolean property
	 */
	public CollectionCondition()
	{
		// Make sure each hour maps to a boolean property, this is important for later, since our view will use this to populate checkboxes
		for (ImageCollection imageCollection : this.getImageCollections())
			if (!this.imageCollectionToSelected.containsKey(imageCollection))
				this.imageCollectionToSelected.put(imageCollection, new SimpleBooleanProperty(true));
		// If the collections list changes, we add a boolean property for the new added image collection
		this.getImageCollections().addListener((ListChangeListener<ImageCollection>) c ->
		{
			while (c.next())
				if (c.wasAdded())
					for (ImageCollection imageCollection : c.getAddedSubList())
						if (!this.imageCollectionToSelected.containsKey(imageCollection))
							this.imageCollectionToSelected.put(imageCollection, new SimpleBooleanProperty(true));
		});
	}

	/**
	 * This query condition ensures only selected collections are queried for
	 *
	 * @param query The current state of the query before the appending
	 */
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (ImageCollection imageCollection : this.getImageCollections())
			if (imageCollectionToSelected.containsKey(imageCollection) && imageCollectionToSelected.get(imageCollection).getValue())
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
	public ObservableList<ImageCollection> getImageCollections()
	{
		return SanimalData.getInstance().getCollectionList();
	}

	/**
	 * Gets the property defining if a image collection is selected
	 *
	 * @param imageCollection The image collection to test if it's selected
	 * @return The property representing if the image collection is selected
	 */
	public BooleanProperty imageCollectionSelectedProperty(ImageCollection imageCollection)
	{
		if (!this.imageCollectionToSelected.containsKey(imageCollection))
			this.imageCollectionToSelected.put(imageCollection, new SimpleBooleanProperty(true));
		return this.imageCollectionToSelected.get(imageCollection);
	}

	/**
	 * Selects all image collections
	 */
	public void selectAll()
	{
		for (BooleanProperty selected : this.imageCollectionToSelected.values())
			selected.set(true);
	}

	/**
	 * De-selects all image collections
	 */
	public void selectNone()
	{
		for (BooleanProperty selected : this.imageCollectionToSelected.values())
			selected.set(false);
	}
}
