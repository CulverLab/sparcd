package controller.analysisView;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import model.analysis.DataAnalysis;
import model.analysis.SanimalTextOutputFormatter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the tab containing Dr. Sanderson's analysis
 */
public class VisDrSandersonController implements VisControllerBase
{
	///
	/// FXML Bound fields start
	///

	// The text area to output the "output.txt" replica to
	@FXML
	public TextArea txtOutput;
	// The text area to output the "allPictures.txt" replica to
	@FXML
	public TextArea txtAllPictures;

	///
	/// FXML Bound fields end
	///

	// Formatter used to format our data into Dr. Sanderson's format
	private SanimalTextOutputFormatter outputFormatter = new SanimalTextOutputFormatter();

	/**
	 * Initializes the Dr. Sanderson output controller by setting the text area fonts
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Use monospaced fonts
		this.txtOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
		this.txtAllPictures.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}

	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataStatistics The data set to visualize
	 */
	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		// Have our output formatter do all the hard work and just stick the result into the two text areas
		this.txtOutput.setText(outputFormatter.format(dataStatistics));
		this.txtAllPictures.setText(outputFormatter.createAllPictures(dataStatistics));
	}
}
