package model.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	// A reference to the information image icon used in the notification bar
	private Node infoImage;
	// Pause transition is used to hide the notification bar after a few seconds
	private PauseTransition delay = new PauseTransition(Duration.seconds(5));

	/**
	 * Constructor takes in a reference to the global data model
	 *
	 * @param sanimalData The data model
	 */
	public ErrorDisplay(SanimalData sanimalData)
	{
		// Load in the image
		this.infoImage = new ImageView(new Image("images/generic/info64.png"));
		// Set the duration of the fade to be equal to what is in the settings. If the settings update, make sure to
		// update this value too
		this.delay.setDuration(Duration.seconds(sanimalData.getSettings().getPopupDelaySec()));
		sanimalData.getSettings().popupDelaySecProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
				this.delay.setDelay(Duration.seconds(newValue));
		});
		// Once the delay is over hide the notifications pane
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
		if (SanimalData.getInstance().getSettings().getNoPopups())
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
		else
		{
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			if (content.length() > 150)
			{
				TextArea area = new TextArea(content);
				area.textProperty().bind(alert.contentTextProperty());
				alert.getDialogPane().setContent(area);
			}
			alert.setContentText(content);
			alert.setHeaderText(null);
			if (actions.length > 0)
			{
				alert.getButtonTypes().clear();
				ButtonType[] buttonTypes = new ButtonType[actions.length];
				for (int i = 0; i < actions.length; i++)
					buttonTypes[i] = new ButtonType(actions[i].getText());
				alert.getButtonTypes().setAll(buttonTypes);
				alert.getButtonTypes().add(ButtonType.CANCEL);
				Optional<ButtonType> result = alert.showAndWait();
				result.ifPresent(buttonType ->
				{
					for (int i = 0; i < buttonTypes.length; i++)
						if (buttonTypes[i] == buttonType)
							actions[i].handle(new ActionEvent());
				});
			}
			else
			{
				alert.show();
			}
		}
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
