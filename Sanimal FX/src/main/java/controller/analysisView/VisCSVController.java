package controller.analysisView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Font;
import model.SanimalData;
import model.analysis.CloudDataAnalysis;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the CSV visualization tab
 */
public class VisCSVController implements VisControllerBase
{
	///
	/// FXML bound fields start
	///

	// The text area containing raw CSV with a list of image data
	@FXML
	public TextArea txtRawCSV;
	// The text area containing location CSV with a location list
	@FXML
	public TextArea txtLocationCSV;
	// The text area containing species CSV with a species list
	@FXML
	public TextArea txtSpeciesCSV;

	///
	/// FXML bound fields end
	///

	/**
	 * Initializes the CSV controller by setting the text area fonts
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Use a monospaced font with size 14
		this.txtRawCSV.setFont(Font.font(java.awt.Font.MONOSPACED, 14f));
		this.txtLocationCSV.setFont(Font.font(java.awt.Font.MONOSPACED, 14f));
		this.txtSpeciesCSV.setFont(Font.font(java.awt.Font.MONOSPACED, 14f));
	}

	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param cloudDataStatistics The cloud data set to visualize
	 */
	@Override
	public void visualize(CloudDataAnalysis cloudDataStatistics)
	{

		// The raw CSV for each image is made up of 1 line per image in the format of:
		// File Name,Date Taken, Species in image, Species count, Location name, Location ID, Location latitude, Location longitude, Location elevation
		// If multiple species are in each image, the single entry is broken into multiple lines, one per species
		String rawCSV = cloudDataStatistics.getRawCloudQueryResult().stream().map(cyVerseQueryResult ->
		{
			return cyVerseQueryResult.getIrodsFileAbsolutePath() + "," +
					SanimalData.getInstance().getSettings().formatDateTime(cyVerseQueryResult.getDateTimeTaken(), " ") + "," +
					cyVerseQueryResult.getSpeciesName() + "," +
					cyVerseQueryResult.getSpeciesScientificName() + "," +
					cyVerseQueryResult.getSpeciesCount().toString() + "," +
					cyVerseQueryResult.getLocationName() + "," +
					cyVerseQueryResult.getLocationID() + "," +
					cyVerseQueryResult.getLocationLatitude().toString() + "," +
					cyVerseQueryResult.getLocationLongitude().toString() + "," +
					cyVerseQueryResult.getLocationElevation().toString();
		}).collect(Collectors.joining("\n"));
		if (rawCSV.isEmpty())
			rawCSV = "No query results found.";
		this.txtRawCSV.setText(rawCSV);

		// The location CSV contains each location, one per line, in the form:
		// Name, ID, Latitude, Longitude, Elevation
		String locationCSV = cloudDataStatistics.getDataAnalyzer().getAllImageLocations().stream().map(location ->
			location.getName() + "," +
			location.getId() + "," +
			location.getLat() + "," +
			location.getLng() + "," +
			location.getElevation())
		.collect(Collectors.joining("\n"));
		this.txtLocationCSV.setText(locationCSV);

		// The species CSV contains each species, one per line, in the form:
		// Name, Scientific Name, Key bound (or null if none)
		String speciesCSV = cloudDataStatistics.getDataAnalyzer().getAllImageSpecies().stream().map(species ->
			species.getName() + "," +
			species.getScientificName()
		).collect(Collectors.joining("\n"));
		this.txtSpeciesCSV.setText(speciesCSV);
	}

	public void copyCSV(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtRawCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
	}

	public void copyLocations(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtLocationCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
	}

	public void copySpecies(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtSpeciesCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
	}
}
