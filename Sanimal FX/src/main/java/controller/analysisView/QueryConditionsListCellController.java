package controller.analysisView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import model.query.IQueryCondition;
import model.util.FXMLLoaderUtils;

/**
 * Generic class used as a list cell of type IQueryCondition. This cell is empty, and gets UI content from the IQueryCondition
 */
public class QueryConditionsListCellController extends ListCell<IQueryCondition>
{
	///
	/// FXML Bound Fields Start
	///

	// The main pane used as the background of the cell
	@FXML
	public StackPane mainPane;
	// The content pane which contains the current IQueryCondition UI instance
	@FXML
	public BorderPane contentPane;
	// Button to remove the current IQueryCondition UI instance
	@FXML
	public Button btnRemoveCondition;

	///
	/// FXML Bound Fields End
	///

	/**
	 * There's nothing to initialize, everything is done once we get data
	 */
	@FXML
	public void initialize()
	{
	}

	/**
	 * Called when we get data in the form of a query condition
	 *
	 * @param iQueryCondition The filter to be displayed in this cell
	 * @param empty If the cell is empty
	 */
	@Override
	protected void updateItem(IQueryCondition iQueryCondition, boolean empty)
	{
		// Update the cell first
		super.updateItem(iQueryCondition, empty);

		// Set the text to null
		this.setText(null);

		// If the cell is empty we have no graphic
		if (empty && iQueryCondition == null)
		{
			this.setGraphic(null);
		}
		// if the cell is not empty, set the field's values and set the graphic
		else
		{
			// Load the FXML of the given data model UI
			FXMLLoader fxml = FXMLLoaderUtils.loadFXML("analysisView/conditions/" + iQueryCondition.getFXMLConditionEditor());
			// Initialize the IQueryConditionController that controls the UI for the data
			fxml.<IConditionController> getController().initializeData(iQueryCondition);
			// Set our cell to display the FXML data
			contentPane.setCenter(fxml.getRoot());

			// Show the data
			this.setGraphic(mainPane);
		}
	}

	/**
	 * Clears the current filter if the X is pressed
	 *
	 * @param actionEvent consumed
	 */
	public void clearCondition(ActionEvent actionEvent)
	{
		this.getListView().getItems().remove(this.getItem());
		actionEvent.consume();
	}
}
