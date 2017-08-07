package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

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

	///
	/// FXML bound fields end
	///

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}
}
