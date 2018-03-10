package controller.analysisView;

import javafx.fxml.Initializable;
import model.analysis.DataAnalysis;

public interface VisControllerBase extends Initializable
{
	void visualize(DataAnalysis dataStatistics);
}
