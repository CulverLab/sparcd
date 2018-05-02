package controller.analysisView;

import javafx.fxml.Initializable;
import model.query.IQueryCondition;

/**
 * Interface to be used by any filter's UI controller
 */
public interface IConditionController extends Initializable
{
	/**
	 * Given a query condition this function initializes the controller's fields with the data object
	 *
	 * @param queryCondition The query condition data model to bind to this controller
	 */
	void initializeData(IQueryCondition queryCondition);
}
