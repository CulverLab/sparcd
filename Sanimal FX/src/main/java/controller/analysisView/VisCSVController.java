package controller.analysisView;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Font;
import model.SanimalData;
import model.analysis.DataAnalyzer;
import model.analysis.SanimalAnalysisUtils;
import model.location.Location;
import model.location.UTMCoord;
import model.util.RoundingUtils;
import model.util.SettingsData;
import org.apache.poi.ss.formula.functions.T;

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

	// 3 buttons to control showing or hiding of lat/long fields
	@FXML
	public ToggleButton tbnShowLatLon;
	@FXML
	public ToggleButton tbnRoundLatLon;
	@FXML
	public ToggleButton tbnHideLatLon;
	// A spinner to control how many decimal places we round to
	@FXML
	public Spinner<Integer> spnDecimalPlaces;

	// 2 buttons used to control if we show or hide elevation
	@FXML
	public ToggleButton tbnShowElevation;
	@FXML
	public ToggleButton tbnHideElevation;

	// Two buttons to show or hide the site code
	@FXML
	public ToggleButton tbnShowCode;
	@FXML
	public ToggleButton tbnHideCode;

	// Show or hide the site name
	@FXML
	public ToggleButton tbnShowName;
	@FXML
	public ToggleButton tbnHideName;

	///
	/// FXML bound fields end
	///

	private DataAnalyzer dataAnalyzer;

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
		// When we select the round toggle button we show the rounding field
		this.spnDecimalPlaces.disableProperty().bind(this.tbnRoundLatLon.selectedProperty().not());
		// When we edit our toggle buttons update the CSV
		ChangeListener<Boolean> tbnChangeListener = (observable, oldValue, newValue) -> { if (newValue) this.refreshCSVs(); };
		this.tbnHideLatLon.selectedProperty().addListener(tbnChangeListener);
		this.tbnRoundLatLon.selectedProperty().addListener(tbnChangeListener);
		this.tbnShowLatLon.selectedProperty().addListener(tbnChangeListener);
		this.spnDecimalPlaces.valueProperty().addListener((observable, oldValue, newValue) -> this.refreshCSVs());
		this.tbnShowElevation.selectedProperty().addListener(tbnChangeListener);
		this.tbnHideElevation.selectedProperty().addListener(tbnChangeListener);
		this.tbnShowCode.selectedProperty().addListener(tbnChangeListener);
		this.tbnHideCode.selectedProperty().addListener(tbnChangeListener);
		this.tbnShowName.selectedProperty().addListener(tbnChangeListener);
		this.tbnHideName.selectedProperty().addListener(tbnChangeListener);
		SanimalData.getInstance().getSettings().locationFormatProperty().addListener((observable, oldValue, newValue) -> this.refreshCSVs());
	}

	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataAnalyzer The cloud data set to visualize
	 */
	@Override
	public void visualize(DataAnalyzer dataAnalyzer)
	{
		this.dataAnalyzer = dataAnalyzer;
		this.refreshCSVs();
	}

	/**
	 * Refreshes all the CSV fields
	 */
	private void refreshCSVs()
	{
		if (dataAnalyzer == null)
			return;

		// The raw CSV for each image is made up of 1 line per image in the format of:
		// File Name,Date Taken, Species in image, Species count, Location name, Location ID, Location latitude, Location longitude, Location elevation
		// If multiple species are in each image, the single entry is broken into multiple lines, one per species
		String rawCSV = dataAnalyzer.getImagesSortedByDate().stream().map(imageEntry ->
		{
			Location location = imageEntry.getLocationTaken();
			// Start with location name and id
			String locationString =
					(this.tbnShowName.isSelected() ? location.getName() : "Omitted") + "," +
					(this.tbnShowCode.isSelected() ? location.getId() : "Omitted") + ",";

			locationString = locationString + this.formatLatLong(location.getLat(), location.getLng()) + ",";

			// Add elevation
			SettingsData.DistanceUnits distanceUnits = SanimalData.getInstance().getSettings().getDistanceUnits();
			locationString = locationString + (this.tbnShowElevation.isSelected() ? RoundingUtils.round(distanceUnits.formatToMeters(location.getElevation()), 2) + distanceUnits.getSymbol() : "Omitted");
			return imageEntry.getFile().getName() + "," +
				SanimalData.getInstance().getSettings().formatDateTime(imageEntry.getDateTaken(), " ") + "," +
				imageEntry.getSpeciesPresent().stream().map(speciesEntry ->
					speciesEntry.getSpecies().getName() + ";" +
					speciesEntry.getSpecies().getScientificName() + ";" +
					speciesEntry.getAmount().toString()
				).collect(Collectors.joining(";")) + "," + locationString;
		}).collect(Collectors.joining("\n"));
		if (rawCSV.isEmpty())
			rawCSV = "No query results found.";
		this.txtRawCSV.setText(rawCSV);

		// The location CSV contains each location, one per line, in the form:
		// Name, ID, Position, Elevation
		String locationCSV = dataAnalyzer.getAllImageLocations().stream().map(location ->
		{
			// Start with location name and id
			String locationString =
				(this.tbnShowName.isSelected() ? location.getName() : "Omitted") + "," +
				(this.tbnShowCode.isSelected() ? location.getId() : "Omitted") + ",";

			locationString = locationString + this.formatLatLong(location.getLat(), location.getLng()) + ",";

			// Distance units depend on feet or meters
			SettingsData.DistanceUnits distanceUnits = SanimalData.getInstance().getSettings().getDistanceUnits();
			locationString = locationString + (this.tbnShowElevation.isSelected() ? RoundingUtils.round(distanceUnits.formatToMeters(location.getElevation()), 2) + distanceUnits.getSymbol() : "Omitted");
			return locationString;
		}).collect(Collectors.joining("\n"));
		this.txtLocationCSV.setText(locationCSV);

		// The species CSV contains each species, one per line, in the form:
		// Name, Scientific Name, Key bound (or null if none)
		String speciesCSV = dataAnalyzer.getAllImageSpecies().stream().map(species ->
				species.getName() + "," +
				species.getScientificName()
		).collect(Collectors.joining("\n"));
		this.txtSpeciesCSV.setText(speciesCSV);
	}

	/**
	 * Given a latitude and longitude this returns either the UTM or lat/long depending on program settings and if rounding was specified
	 *
	 * @param latitude The latitude to format
	 * @param longitude The longitude to format
	 * @return A string in either UTM or Lat/Long or "Omitted" format
	 */
	private String formatLatLong(Double latitude, Double longitude)
	{
		String locationString = "";

		double locationLatitude = this.tbnRoundLatLon.isSelected() ? RoundingUtils.round(latitude, this.spnDecimalPlaces.getValue()) : latitude;
		double locationLongitude = this.tbnRoundLatLon.isSelected() ? RoundingUtils.round(longitude, this.spnDecimalPlaces.getValue()) : longitude;

		// If we're using Lat/Lng
		if (SanimalData.getInstance().getSettings().getLocationFormat() == SettingsData.LocationFormat.LatLong)
		{
			// Add lat/lng
			locationString = locationString +
				(this.tbnHideLatLon.isSelected() ? "Omitted" : locationLatitude) + "," +
				(this.tbnHideLatLon.isSelected() ? "Omitted" : locationLongitude);
		}
		// If we're using UTM
		else if (SanimalData.getInstance().getSettings().getLocationFormat() == SettingsData.LocationFormat.UTM)
		{
			// Convert to UTM, and print it
			UTMCoord utmCoord = SanimalAnalysisUtils.Deg2UTM(locationLatitude, locationLongitude);
			locationString = locationString +
				(this.tbnHideLatLon.isSelected() ? "Omitted" :
					utmCoord.getZone().toString() + utmCoord.getLetter().toString() + "," +
					utmCoord.getEasting() + "E," +
					utmCoord.getNorthing() + "N");
		}
		return locationString;
	}

	/**
	 * If copy CSV is pressed, we copy the content of the CSV clipboard
	 *
	 * @param actionEvent consumed
	 */
	public void copyRawCSV(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtRawCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
		actionEvent.consume();
	}

	/**
	 * If copy Locations CSV is pressed, we copy the content of the CSV clipboard
	 *
	 * @param actionEvent consumed
	 */
	public void copyLocationsCSV(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtLocationCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
		actionEvent.consume();
	}

	/**
	 * If copy Species CSV is pressed, we copy the content of the CSV clipboard
	 *
	 * @param actionEvent consumed
	 */
	public void copySpeciesCSV(ActionEvent actionEvent)
	{
		ClipboardContent content = new ClipboardContent();
		content.putString(this.txtSpeciesCSV.getText());
		Clipboard.getSystemClipboard().setContent(content);
		actionEvent.consume();
	}
}
