package model.query.conditions;

import javafx.collections.ObservableList;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.species.Species;

public class CollectionCondition implements IQueryCondition
{
	@Override
	public void appendConditionToQuery(CyVerseQuery query)
	{
		for (ImageCollection imageCollection : SanimalData.getInstance().getCollectionList())
			if (imageCollection.shouldBePartOfAnalysis())
				query.addImageCollection(imageCollection);
	}

	@Override
	public String getFXMLConditionEditor()
	{
		return "CollectionCondition.fxml";
	}

	public ObservableList<ImageCollection> imageCollectionListProperty()
	{
		return SanimalData.getInstance().getCollectionList();
	}
}
