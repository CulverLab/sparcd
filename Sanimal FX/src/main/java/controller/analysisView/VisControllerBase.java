package controller.analysisView;

import javafx.fxml.Initializable;
import model.analysis.DataAnalysis;

/**
 * Interface used in all visualization tab controllers
 */
public interface VisControllerBase extends Initializable
{
	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataStatistics The data set to visualize
	 */
	void visualize(DataAnalysis dataStatistics);
}
