package controller;

import controller.analysisView.VisCSVController;
import controller.analysisView.VisDrSandersonController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.SanimalData;
import model.analysis.DataAnalyzer;
import model.image.ImageEntry;
import model.query.CyVerseQuery;
import model.query.IQueryCondition;
import model.util.FXMLLoaderUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller class for the analysis page
 */
public class SanimalAnalysisController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// References to all controllers in the tabs of the analysis page
	@FXML
	public VisDrSandersonController visDrSandersonController;
	@FXML
	public VisCSVController visCSVController;

	// The list of query conditions
	@FXML
	public ListView<IQueryCondition> lvwQueryConditions;
	// The event interval used for Dr. Sanderson's output
	@FXML
	public TextField txtEventInterval;

	///
	/// FXML bound fields end
	///

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Set the query conditions to be specified by the data model
		this.lvwQueryConditions.setItems(SanimalData.getInstance().getQueryEngine().getQueryConditions());
		// Set the cell factory to be our custom query condition cell which adapts itself to the specific condition
		this.lvwQueryConditions.setCellFactory(x -> FXMLLoaderUtils.loadFXML("analysisView/QueryConditionsListCell.fxml").getController());
	}

	/**
	 * Called when the refresh button is pressed
	 *
	 * @param actionEvent consumed
	 */
	public void query(ActionEvent actionEvent)
	{
		// Default 60s event interval
		Integer eventInterval = 60;
		try
		{
			// Check if a different interval was given
			eventInterval = Integer.parseInt(this.txtEventInterval.getText());
		}
		catch (NumberFormatException ignored) {}

		// Create a query
		CyVerseQuery query = new CyVerseQuery();
		// For each condition listed in the listview, apply that to the overall query
		for (IQueryCondition queryCondition : SanimalData.getInstance().getQueryEngine().getQueryConditions())
			queryCondition.appendConditionToQuery(query);

		// Grab the result of the query
		List<ImageEntry> queryResult = SanimalData.getInstance().getConnectionManager().performQuery(query);

		// Analyze the result of the query
		DataAnalyzer dataAnalyzer = new DataAnalyzer(queryResult, eventInterval);

		// Hand the analysis over to the visualizations to graph
		visDrSandersonController.visualize(dataAnalyzer);
		visCSVController.visualize(dataAnalyzer);

		actionEvent.consume();
	}
}
