package controller.analysisView;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import model.analysis.DataAnalysis;
import model.analysis.SanimalTextOutputFormatter;

import java.net.URL;
import java.util.ResourceBundle;

public class VisDrSandersonController implements VisControllerBase
{
	///
	/// FXML Bound fields start
	///

	@FXML
	public TextArea txtOutput;
	@FXML
	public TextArea txtAllPictures;

	///
	/// FXML Bound fields end
	///

	private SanimalTextOutputFormatter outputFormatter = new SanimalTextOutputFormatter();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.txtOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
		this.txtAllPictures.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}

	@Override
	public void visualize(DataAnalysis dataStatistics)
	{
		this.txtOutput.setText(outputFormatter.format(dataStatistics));

		this.txtAllPictures.setText(outputFormatter.createAllPictures(dataStatistics));
	}
}
