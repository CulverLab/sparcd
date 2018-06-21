package model.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.util.Duration;
import model.SanimalData;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A class used to display errors in the sanimal program
 */
public class ErrorDisplay
{
	// A reference to the notification pane used to display errors
	private NotificationPane notificationPane = null;
	private Node infoImage;
	private PauseTransition delay = new PauseTransition(Duration.seconds(5));

	public ErrorDisplay(SanimalData sanimalData)
	{
		this.infoImage = new ImageView(new Image("images/generic/info64.png"));
		sanimalData.getSettings().popupDelaySecProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
				this.delay.setDelay(Duration.seconds(newValue));
		});
		this.delay.setOnFinished(event -> this.notificationPane.hide());
	}

	/**
	 * Shows a popup to the user given a set of parameters. Can be called on any thread
	 *
	 * @param content The content of the window
	 * @param actions The actions to display as options to the popup
	 */
	public void notify(String content, Action... actions)
	{
		if (Platform.isFxApplicationThread())
			notifyOnFX(content, actions);
		else
			Platform.runLater(() -> notifyOnFX(content, actions));
	}

	/**
	 * Shows a popup to the user given a set of parameters, must be called on the FX thread
	 *
	 * @param content The content of the window
	 * @param actions The actions to display as options to the popup
	 */
	private void notifyOnFX(String content, Action... actions)
	{
		notificationPane.getActions().clear();
		// When any action is pressed, we hide the notification. This makes sure that each action is mapped to a new action that hides
		// the pane and then calls the action
		notificationPane.show(content, this.infoImage, Stream.of(actions).map(action -> new Action(action.getText(), actionEvent ->
		{
			this.notificationPane.hide();
			action.handle(actionEvent);
		})).toArray(Action[]::new));
		this.delay.playFromStart();
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

	@FXML
	public void setNotificationPane(NotificationPane notificationPane)
	{
		this.notificationPane = notificationPane;
	}
}
