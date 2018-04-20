package model.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * A class used to display errors in the sanimal program
 */
public class ErrorDisplay
{
	/**
	 * Shows a popup to the user given a set of parameters
	 *
	 * @param type The type of window to display
	 * @param window The owner of the window
	 * @param title The title to display in the window
	 * @param header The header to display in the window
	 * @param content The content of the window
	 * @param shouldWait If the program should pause execution to show the window
	 */
	public void showPopup(Alert.AlertType type, Window window, String title, String header, String content, Boolean shouldWait)
	{
		printError(content);
		Platform.runLater(() ->
		{
			Alert alert;
			if (type != null)
				alert = new Alert(type);
			else
				alert = new Alert(Alert.AlertType.ERROR);

			if (window != null)
				alert.initOwner(window);

			if (title != null)
				alert.setTitle(title);

			if (header != null)
				alert.setHeaderText(header);

			if (content != null)
				alert.setContentText(content);

			if (shouldWait)
				alert.showAndWait();
			else
				alert.show();
		});
	}

	/**
	 * Prints an error message to STDErr for internal errors
	 *
	 * @param errorMessage The error that occured
	 */
	public void printError(String errorMessage)
	{
		System.err.println(errorMessage);
	}
}
