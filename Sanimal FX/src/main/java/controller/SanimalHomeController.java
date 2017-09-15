package controller;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import model.SanimalData;
import model.cyverse.CyVerseConnectionManager;
import model.cyverse.ImageCollection;
import model.location.Location;
import model.species.Species;
import model.util.FinishableTask;
import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.dialog.LoginDialog;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.fxmisc.easybind.EasyBind;
import org.irods.jargon.core.connection.*;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller class for the main program
 */
public class SanimalHomeController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The import button to open the import window
	@FXML
	public Button btnImport;
	// The analyze button to open the analyze window
	@FXML
	public Button btnAnalyze;
	// The map button to open the map window
	@FXML
	public Button btnMap;
	// The button to open the upload manager
	@FXML
	public Button btnUpload;
	// The logout button to disconnect from CyVerse
	@FXML
	public Button btnLogout;
	// The exit button to close the program
	@FXML
	public Button btnExit;
	// After logging in this shows the username of the logged in person
	@FXML
	public Label lblUsername;

	// The main anchor pane in the background
	@FXML
	public AnchorPane mainPane;

	// The background image containing the camera trap image
	@FXML
	public ImageView backgroundImage;

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
		// If we're logged in show the logged in person's username
		this.lblUsername.textProperty().bind(EasyBind.monadic(SanimalData.getInstance().getConnectionManager().usernameProperty()).map(username -> "Welcome " + username + "!").orElse(""));

		// Grab the logged in property
		ReadOnlyBooleanProperty loggedIn = SanimalData.getInstance().getConnectionManager().loggedInProperty();

		// Hide the logout button and text when not logged in
		this.btnLogout.visibleProperty().bind(loggedIn);
		this.lblUsername.visibleProperty().bind(loggedIn);
	}

	/**
	 * When exit is pressed close the program
	 *
	 * @param actionEvent ignored
	 */
	@FXML
	public void exitPressed(ActionEvent actionEvent)
	{
		// Ensure to log out before exiting
		SanimalData.getInstance().getConnectionManager().logout();
		System.exit(0);
	}

	/**
	 * When the logout button is pressed
	 *
	 * @param actionEvent consumed
	 */
	public void logoutPressed(ActionEvent actionEvent)
	{
		// Ensure we're logged in first
		if (SanimalData.getInstance().getConnectionManager().loggedInProperty().getValue())
		{
			// Thread off logging out
			FinishableTask<Void> logoutAttempt = new FinishableTask<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					Platform.runLater(() -> {
						// Clear locations, species, and images
						SanimalData.getInstance().fullReset();
					});

					// Logout from CyVerse
					SanimalData.getInstance().getConnectionManager().logout();
					return null;
				}
			};
			// Clear all currently running tasks, then perform the rest of the logout
			SanimalData.getInstance().getSanimalExecutor().clearTasks();
			// Perform the task
			SanimalData.getInstance().getSanimalExecutor().addTask(logoutAttempt);
		}
		actionEvent.consume();
	}
}
