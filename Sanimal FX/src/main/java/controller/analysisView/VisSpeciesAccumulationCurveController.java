package controller.analysisView;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import model.analysis.DataAnalysis;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageEntry;
import model.species.Species;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the species accumulation curve tab
 */
public class VisSpeciesAccumulationCurveController implements VisControllerBase
{
	///
	/// FXML bound fields start
	///

	// The line chart used in the visualization
	@FXML
	public LineChart<Long, String> lineChart;
	// The number axis used for day
	@FXML
	public NumberAxis xAxis;
	// The category axis used for species
	@FXML
	public CategoryAxis yAxis;

	///
	/// FXML bound fields end
	///

	// List of chart data (TODO: maybe we need to pass args to FXCollections below?)
	private ObservableList<XYChart.Data<Long, String>> chartData = FXCollections.observableArrayList();

	/**
	 * Initializes the species accumulation curve controller by initializing the line chart
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// The only series is a line with x values that is the first day seen, and y values have species
		XYChart.Series<Long, String> chartSeries = new XYChart.Series<>();
		chartSeries.setName("First day seen");
		lineChart.getData().add(chartSeries);
		chartSeries.setData(new SortedList<>(chartData, Comparator.comparingLong(XYChart.Data::getXValue)));
	}

	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataStatistics The data set to visualize
	 */
	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		// Ensure we have images to visualize
		if (dataStatistics.getImagesSortedByDate().size() > 0)
		{
			// Grab the first date
			LocalDateTime firstDate = dataStatistics.getImagesSortedByDate().get(0).getDateTaken();

			// Add each species to the chart
			for (Species species : dataStatistics.getAllImageSpecies())
			{
				// Query for all images with that species
				List<ImageEntry> imagesWithSpecies = new ImageQuery().speciesOnly(species).query(dataStatistics.getImagesSortedByDate());
				// Make sure we have images with that species
				if (!imagesWithSpecies.isEmpty())
				{
					// Grab the new X value
					Long newX = SanimalAnalysisUtils.daysBetween(firstDate, imagesWithSpecies.get(0).getDateTaken()) + 1;
					// If the y axis does not have the species add the data value
					if (!yAxis.getCategories().contains(species.getName()))
						chartData.add(new XYChart.Data<>(newX, species.getName()));
					// If the y axis does have the species update the X value
					else
						chartData.filtered(data -> data.getYValue().equals(species.getName())).get(0).setXValue(newX);
				}
			}
		}
	}
}
