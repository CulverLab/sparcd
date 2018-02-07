package controller.uploadView;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.image.ImageDirectory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.fxmisc.easybind.EasyBind;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUploadDownloadListEntryController extends ListCell<String>
{
	///
	/// FXML Bound fields start
	///

	@FXML
	public HBox mainPane;

	@FXML
	public Label lblDate;

	@FXML
	public Label lblUsername;

	///
	/// FXML Bound fields end
	///

	private static final SimpleDateFormat FOLDER_FORMAT = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm");
	private static final SimpleDateFormat ORIGINAL_FOLDER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

	/**
	 * Called when we get a new item to display
	 * @param directoryName The image directory to show
	 * @param empty If the cell is empty
	 */
	@Override
	public void updateItem(String directoryName, boolean empty)
	{
		// Update the underlying item
		super.updateItem(directoryName, empty);

		// Set the text of the cell to nothing
		this.setText(null);

		// If no image directory was given and the cell was empty, clear the graphic
		if (empty && directoryName == null)
		{
			this.setGraphic(null);
		}
		else
		{
			// Set the name to the image directory name
			String[] pieces = StringUtils.split(directoryName, " ");

			if (pieces.length == 3)
			{
				this.lblUsername.setText(pieces[2]);
				try
				{
					this.lblDate.setText(FOLDER_FORMAT.format(ORIGINAL_FOLDER_FORMAT.parse(pieces[0] + " " + pieces[1])));
				}
				catch (ParseException ignored)
				{
				}
			}
			else
			{
				this.lblUsername.setText(directoryName);
				this.lblDate.setText("");
			}

			// Set the graphic to display
			this.setGraphic(mainPane);
		}
	}
}
