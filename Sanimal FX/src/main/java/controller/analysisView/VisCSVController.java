package controller.analysisView;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import model.analysis.DataAnalysis;
import model.location.Location;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VisCSVController implements VisControllerBase
{
	///
	/// FXML bound fields start
	///

	@FXML
	public TextArea txtCSV;

	///
	/// FXML bound fields end
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.txtCSV.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}

	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		String csv = dataStatistics.getImagesSortedByDate().stream().map(imageEntry -> {
			String imageCSV = "";
			Location locationTaken = imageEntry.getLocationTaken();
			imageCSV = imageCSV +
					imageEntry.getFile().getName() + "," +
					imageEntry.getDateTaken().toString() + "," +
					imageEntry.getSpeciesPresent() + "," +
					"{" + locationTaken.getName() + "," + locationTaken.getId() + "," + locationTaken.getLat() + "," + locationTaken.getLng() + "}";
			return imageCSV;
		}).collect(Collectors.joining("\n"));

		this.txtCSV.setText(csv);
	}
}
