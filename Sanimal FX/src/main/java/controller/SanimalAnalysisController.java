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

	@FXML
	public ListView<IQueryCondition> lvwQueryConditions;
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
		this.lvwQueryConditions.setItems(SanimalData.getInstance().getQueryEngine().getQueryConditions());
		this.lvwQueryConditions.setCellFactory(x -> FXMLLoaderUtils.loadFXML("analysisView/QueryConditionsListCell.fxml").getController());
	}

	/**
	 * Called when the refresh button is pressed
	 *
	 * @param actionEvent ignored
	 */
	public void query(ActionEvent actionEvent)
	{
		// Default 60s event interval
		Integer eventInterval = 60;
		try
		{
			eventInterval = Integer.parseInt(this.txtEventInterval.getText());
		}
		catch (NumberFormatException ignored) {}

		CyVerseQuery query = new CyVerseQuery();
		for (IQueryCondition queryCondition : SanimalData.getInstance().getQueryEngine().getQueryConditions())
			queryCondition.appendConditionToQuery(query);

		List<ImageEntry> queryResult = SanimalData.getInstance().getConnectionManager().performQuery(query);
		DataAnalyzer dataAnalyzer = new DataAnalyzer(queryResult, eventInterval);

		// Hand the analysis over to the visualizations to graph
		visDrSandersonController.visualize(dataAnalyzer);
		visCSVController.visualize(dataAnalyzer);
	}
}
