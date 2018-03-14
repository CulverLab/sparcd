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
	public TextArea txtRawCSV;
	@FXML
	public TextArea txtLocationCSV;
	@FXML
	public TextArea txtSpeciesCSV;

	///
	/// FXML bound fields end
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.txtRawCSV.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}

	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		String rawCSV = dataStatistics.getImagesSortedByDate().stream().map(imageEntry -> {
			Location locationTaken = imageEntry.getLocationTaken();
			return
				imageEntry.getFile().getName() + "," +
				imageEntry.getDateTaken().toString() + "," +
				imageEntry.getSpeciesPresent() + "," +
				"{" + locationTaken.getName() + "," + locationTaken.getId() + "," + locationTaken.getLat() + "," + locationTaken.getLng() + "}";
		}).collect(Collectors.joining("\n"));
		this.txtRawCSV.setText(rawCSV);

		String locationCSV = dataStatistics.getAllImageLocations().stream().map(location ->
			location.getName() + "," +
			location.getId() + "," +
			location.getLat() + "," +
			location.getLng() + "," +
			location.getElevation())
		.collect(Collectors.joining("\n"));
		this.txtLocationCSV.setText(locationCSV);

		String speciesCSV = dataStatistics.getAllImageSpecies().stream().map(species ->
			species.getName() + "," +
			species.getScientificName() + "," +
			species.getKeyBinding()
		).collect(Collectors.joining("\n"));
		this.txtSpeciesCSV.setText(speciesCSV);
	}
}
