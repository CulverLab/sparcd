package controller;

import controller.analysisView.VisCSVController;
import controller.analysisView.VisDrSandersonController;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import model.SanimalData;
import model.analysis.DataAnalyzer;
import model.image.ImageEntry;
import model.query.ElasticSearchQuery;
import model.query.IQueryCondition;
import model.query.QueryEngine;
import model.threading.ErrorTask;
import model.util.FXMLLoaderUtils;
import org.controlsfx.control.MaskerPane;

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

	// The detachable tab containing the Dr. Sanderson output
	@FXML
	public Tab tabDrSanderson;

	// Tab pane full of visualizations
	@FXML
	public TabPane tpnVisualizations;

	// The list of possible filters
	@FXML
	public ListView<QueryEngine.QueryFilters> lvwFilters;

	// The Vbox with the query parameters, used to hide the event interval
	@FXML
	public VBox vbxQuery;

	// The imageview with the arrow divider
	@FXML
	public ImageView imgArrow;

	// If the query is happening
	@FXML
	public MaskerPane mpnQuerying;

	///
	/// FXML bound fields end
	///

	private Integer eventIntervalIndex = 0;

	private Image standardArrow = new Image("/images/analysisWindow/arrowDivider.png");
	private Image highlightedArrow = new Image("/images/analysisWindow/arrowDividerSelected.png");

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

		// Hide the Dr. Sanderson tab and the event interval if we don't have Dr. Sanderson's compatibility
		if (!SanimalData.getInstance().getSettings().getDrSandersonOutput())
		{
			this.tpnVisualizations.getTabs().remove(this.tabDrSanderson);
			this.eventIntervalIndex = this.vbxQuery.getChildren().indexOf(this.txtEventInterval);
			this.vbxQuery.getChildren().remove(this.txtEventInterval);
		}

		// Hide the Dr. Sanderson tab if compatibility is not enabled
		SanimalData.getInstance().getSettings().drSandersonOutputProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue)
			{
				if (!this.tpnVisualizations.getTabs().contains(this.tabDrSanderson))
					this.tpnVisualizations.getTabs().add(0, this.tabDrSanderson);
				if (!this.vbxQuery.getChildren().contains(this.txtEventInterval))
					this.vbxQuery.getChildren().add(this.eventIntervalIndex, this.txtEventInterval);
			}
			else
			{
				this.tpnVisualizations.getTabs().remove(this.tabDrSanderson);
				this.eventIntervalIndex = this.vbxQuery.getChildren().indexOf(this.txtEventInterval);
				this.vbxQuery.getChildren().remove(this.txtEventInterval);
			}
		});

		// Set the items in the list to be the list of possible query filters
		this.lvwFilters.setItems(SanimalData.getInstance().getQueryEngine().getQueryFilters());

		this.mpnQuerying.setVisible(false);
	}

	/**
	 * Called when the refresh button is pressed
	 *
	 * @param actionEvent consumed
	 */
	public void query(ActionEvent actionEvent)
	{
		this.mpnQuerying.setVisible(true);

		// Default 60s event interval
		Integer eventInterval = 60;
		try
		{
			// Check if a different interval was given
			eventInterval = Integer.parseInt(this.txtEventInterval.getText());
		}
		catch (NumberFormatException ignored) {}

		// Create a query
		ElasticSearchQuery query = new ElasticSearchQuery();
		// For each condition listed in the listview, apply that to the overall query
		for (IQueryCondition queryCondition : SanimalData.getInstance().getQueryEngine().getQueryConditions())
			queryCondition.appendConditionToQuery(query);

		Task<List<ImageEntry>> queryTask = new ErrorTask<List<ImageEntry>>()
		{
			@Override
			protected List<ImageEntry> call()
			{
				this.updateMessage("Performing query...");
				// Grab the result of the query
				return SanimalData.getInstance().getEsConnectionManager().performQuery(query);
			}
		};
		Integer finalEventInterval = eventInterval;

		// Once finished with the task, we test if the user wants to continue
		queryTask.setOnSucceeded(event ->
		{
			this.mpnQuerying.setVisible(false);

			// Analyze the result of the query
			DataAnalyzer dataAnalyzer = new DataAnalyzer(queryTask.getValue(), finalEventInterval);

			// Hand the analysis over to the visualizations to graph
			visDrSandersonController.visualize(dataAnalyzer);
			visCSVController.visualize(dataAnalyzer);
		});
		SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().addTask(queryTask);

		actionEvent.consume();
	}

	/**
	 * Called to add athe current filter to the analysis
	 *
	 * @param mouseEvent consumed
	 */
	public void clickedAdd(MouseEvent mouseEvent)
	{
		// If a filter was clicked, we instantiate it and append it to the end of the list (-1 so that the + is at the end)
		ObservableList<IQueryCondition> queryConditions = SanimalData.getInstance().getQueryEngine().getQueryConditions();
		if (this.lvwFilters.getSelectionModel().selectedItemProperty().getValue() != null)
			queryConditions.add(this.lvwFilters.getSelectionModel().selectedItemProperty().getValue().createInstance());
		mouseEvent.consume();
	}

	/**
	 * Called when the mouse enters the arrow image
	 *
	 * @param mouseEvent consumed
	 */
	public void mouseEnteredArrow(MouseEvent mouseEvent)
	{
		imgArrow.setImage(highlightedArrow);
		mouseEvent.consume();
	}

	/**
	 * Called when the mouse exits the arrow image
	 *
	 * @param mouseEvent consumed
	 */
	public void mouseExitedArrow(MouseEvent mouseEvent)
	{
		imgArrow.setImage(standardArrow);
		mouseEvent.consume();
	}

	/**
	 * Called whenever a filter is clicked on the filters list view
	 *
	 * @param mouseEvent consumed
	 */
	public void clickedFilters(MouseEvent mouseEvent)
	{
		if (mouseEvent.getClickCount() == 2)
			this.clickedAdd(mouseEvent);
		mouseEvent.consume();
	}
}
