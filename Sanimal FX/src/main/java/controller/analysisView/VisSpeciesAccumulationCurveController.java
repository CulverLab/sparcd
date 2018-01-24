package controller.analysisView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import model.analysis.DataAnalysis;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.species.Species;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class VisSpeciesAccumulationCurveController implements VisControllerBase
{
	///
	/// FXML bound fields start
	///

	@FXML
	public LineChart<Long, String> lineChart;
	@FXML
	public CategoryAxis yAxis;

	///
	/// FXML bound fields end
	///

	private ObservableList<XYChart.Data<Long, String>> chartData = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		XYChart.Series<Long, String> chartSeries = new XYChart.Series<>();
		chartSeries.setName("First day seen");
		lineChart.getData().add(chartSeries);
		chartSeries.setData(new SortedList<>(chartData, Comparator.comparingLong(XYChart.Data::getXValue)));
	}

	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		chartData.clear();

		if (dataStatistics.getImagesSortedByDate().size() > 0)
		{
			Date firstDate = dataStatistics.getImagesSortedByDate().get(0).getDateTaken();

			for (Species species : dataStatistics.getAllImageSpecies())
			{
				List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(dataStatistics.getImagesSortedByDate());
				if (!imagesWithSpecies.isEmpty())
				{
					Long newX = SanimalAnalysisUtils.daysBetween(firstDate, imagesWithSpecies.get(0).getDateTaken()) + 1;
					chartData.add(new XYChart.Data<>(newX, species.getName()));
				}
			}
		}
	}
}
