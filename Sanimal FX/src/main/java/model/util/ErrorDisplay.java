package model.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public class ErrorDisplay
{
	public void showPopup(Alert.AlertType type, Window window, String title, String header, String content, Boolean shouldWait)
	{
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

	public void printError(String errorMessage)
	{
		System.err.println(errorMessage);
	}
}
