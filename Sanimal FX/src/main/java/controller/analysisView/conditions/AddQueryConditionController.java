package controller.analysisView.conditions;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import model.SanimalData;
import model.query.IQueryCondition;
import model.query.QueryEngine;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class used as a controller for the "Add new filter" UI component
 */
public class AddQueryConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	// The large "plus" button
	@FXML
	public Button btnAdd;
	// The list of filters to select from
	@FXML
	public ListView<QueryEngine.QueryFilters> lvwFilters;
	// The button go back to the "plus" screen, doesn't functionally do anything but is useful to some users
	@FXML
	public Button btnReset;

	///
	/// FXML Bound Fields End
	///

	/**
	 * Initializes the add query condition controller
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Set the items in the list to be the list of possible query filters
		lvwFilters.setItems(SanimalData.getInstance().getQueryEngine().getQueryFilters());
		// Add a selection filter
		this.lvwFilters.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			// If a filter was clicked, we instantiate it and append it to the end of the list (-1 so that the + is at the end)
			ObservableList<IQueryCondition> queryConditions = SanimalData.getInstance().getQueryEngine().getQueryConditions();
			if (newValue != null)
				// Append at the end - 1 because the + condition is at the end
				queryConditions.add(queryConditions.size() - 1, newValue.createInstance());
			// Show the plus again, and hide the rest
			this.btnAdd.setVisible(true);
			this.btnReset.setVisible(false);
			this.lvwFilters.setVisible(false);
		});
	}

	/**
	 * Unused because the AddQueryCondition does not actually have a data model associated with it
	 *
	 * @param queryCondition ignored
	 */
	@Override
	public void initializeData(IQueryCondition queryCondition)
	{
	}

	/**
	 * Called when the new button is clicked, we clear the selection and show appropriate buttons
	 *
	 * @param actionEvent consumed
	 */
	public void addNewCondition(ActionEvent actionEvent)
	{
		this.btnAdd.setVisible(false);
		this.btnReset.setVisible(true);
		this.lvwFilters.getSelectionModel().clearSelection();
		this.lvwFilters.setVisible(true);
		actionEvent.consume();
	}

	/**
	 * Called when the clear button is clicked, we clear the selection and show appropriate buttons
	 *
	 * @param actionEvent consumed
	 */
	public void resetToAdd(ActionEvent actionEvent)
	{
		this.btnAdd.setVisible(true);
		this.btnReset.setVisible(false);
		this.lvwFilters.setVisible(false);
		actionEvent.consume();
	}
}
