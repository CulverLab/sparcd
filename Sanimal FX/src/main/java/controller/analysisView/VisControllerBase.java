package controller.analysisView;

import javafx.fxml.Initializable;
import model.analysis.DataAnalyzer;

/**
 * Interface used in all visualization tab controllers
 */
public interface VisControllerBase extends Initializable
{
	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataAnalyzer The cloud data set to visualize
	 */
	void visualize(DataAnalyzer dataAnalyzer);
}
