package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import model.SanimalData;
import model.analysis.SanimalTextOutputFormatter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the analysis page
 */
public class SanimalAnalysisController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The text area of the results from the analysis
	@FXML
	public TextArea txtResults;
	@FXML
	public TextField txtEventInterval;

	///
	/// FXML bound fields end
	///

	private SanimalTextOutputFormatter outputFormatter = new SanimalTextOutputFormatter();

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		txtResults.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}

	public void createJimsOutput(ActionEvent actionEvent)
	{
		try
		{
			txtResults.setText(outputFormatter.format(SanimalData.getInstance().getAllImages(), Integer.parseInt(txtEventInterval.getText())));
		}
		catch (NumberFormatException ignored)
		{
			txtResults.setText("Invalid event interval entered!");
		}
	}

	public void createJSONOutput(ActionEvent actionEvent)
	{
		txtResults.setText(SanimalData.getInstance().getGson().toJson(SanimalData.getInstance().getAllImages()));
	}
}
