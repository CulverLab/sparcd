package controller.analysisView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import model.analysis.SanimalTextOutputFormatter;
import model.image.ImageEntry;

import java.net.URL;
import java.util.List;
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
		// Nothing yet
	}

	@Override
	public void visualize(List<ImageEntry> images, Integer eventInterval)
	{
		this.txtOutput.setText(outputFormatter.format(images, eventInterval));
		this.txtOutput.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));

		this.txtAllPictures.setText(outputFormatter.createAllPictures(images, eventInterval));
		this.txtAllPictures.setFont(Font.font(java.awt.Font.MONOSPACED, 12f));
	}
}
