package controller.analysisView;

import javafx.fxml.Initializable;
import model.analysis.CloudDataAnalysis;

/**
 * Interface used in all visualization tab controllers
 */
public interface VisControllerBase extends Initializable
{
	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param cloudDataStatistics The cloud data set to visualize
	 */
	void visualize(CloudDataAnalysis cloudDataStatistics);
}
