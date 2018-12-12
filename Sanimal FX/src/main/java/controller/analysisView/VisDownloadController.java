package controller.analysisView;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import model.SanimalData;
import model.analysis.DataAnalyzer;
import model.threading.ErrorTask;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the query downloader
 */
public class VisDownloadController implements VisControllerBase
{
	///
	/// FXML bound fields start
	///

	// A list of image files that will be downloaded
	@FXML
	public ListView<String> lvwImageFiles;
	// The button to download images
	@FXML
	public Button btnDownload;

	///
	/// FXML bound fields end
	///

	private ObservableList<String> imageFilePaths = FXCollections.observableArrayList();

	/**
	 * Initializes the CSV controller by setting the text area fonts
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.lvwImageFiles.setItems(this.imageFilePaths);
	}

	/**
	 * Function called whenever we're given a new pre-analyzed data set to visualize
	 *
	 * @param dataAnalyzer The cloud data set to visualize
	 */
	@Override
	public void visualize(DataAnalyzer dataAnalyzer)
	{
		this.imageFilePaths.setAll(dataAnalyzer.getOriginalImageList().stream().map(imageEntry -> imageEntry.getFile().toString().replace('\\', '/')).collect(Collectors.toList()));
	}

	/**
	 * Called when the user presses download images to download the image files
	 *
	 * @param actionEvent consumed
	 */
	public void downloadImages(ActionEvent actionEvent)
	{
		// Create a directory chooser to pick which directory to download to
		DirectoryChooser directoryChooser = new DirectoryChooser();
		// Set the title of the window
		directoryChooser.setTitle("Pick a directory to download to");
		// Set the initial directory to just be documents folder
		directoryChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
		// Grab the directory to save to
		File dirToSaveTo = directoryChooser.showDialog(this.lvwImageFiles.getScene().getWindow());

		// Make sure we got a directory to save to
		if (dirToSaveTo != null)
		{
			// Make sure the directory is a directory, exists, and can be written to
			if (dirToSaveTo.exists() && dirToSaveTo.isDirectory() && dirToSaveTo.canWrite())
			{
				// Create a new task to perform the computation
				ErrorTask<Void> errorTask = new ErrorTask<Void>()
				{
					@Override
					protected Void call()
					{
						// Update the users on what the query is doing
						this.updateMessage("Performing query to figure out which images to download...");
						// Perform the query
						// Create a callback so we can easily update our task progress
						DoubleProperty progressCallback = new SimpleDoubleProperty(0);
						progressCallback.addListener((observable, oldValue, newValue) -> this.updateProgress(newValue.doubleValue(), 1.0));
						// Call the final function to download data to disk
						SanimalData.getInstance().getConnectionManager().downloadImages(imageFilePaths, dirToSaveTo, progressCallback);
						return null;
					}
				};
				errorTask.setOnSucceeded(event -> this.btnDownload.setDisable(false));
				// Disable the download images button for now
				this.btnDownload.setDisable(true);
				// Execute the task
				SanimalData.getInstance().getSanimalExecutor().getImmediateExecutor().addTask(errorTask);
			}
		}
		actionEvent.consume();
	}
}
