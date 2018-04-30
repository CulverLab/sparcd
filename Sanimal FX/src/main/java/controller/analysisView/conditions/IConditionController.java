package controller.analysisView.conditions;

import javafx.fxml.Initializable;
import model.query.IQueryCondition;

public interface IConditionController extends Initializable
{
	void initializeData(IQueryCondition queryCondition);
}
