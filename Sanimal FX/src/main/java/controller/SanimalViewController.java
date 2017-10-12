package controller;

import com.panemu.tiwulfx.control.DetachableTabPane;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.SanimalData;
import model.cyverse.CyVerseConnectionManager;
import model.cyverse.ImageCollection;
import model.location.Location;
import model.species.Species;
import model.util.FinishableTask;
import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller class for the program main view
 */
public class SanimalViewController implements Initializable
{
	///
	/// FXML Bound fields start
	///

	// The username and password text fields
	@FXML
	public TextField txtUsername;
	@FXML
	public PasswordField txtPassword;

	// Check box to remember username
	@FXML
	public CheckBox cbxRememberUsername;

	// The hyperlink label with the register and forgot password options
	@FXML
	public HyperlinkLabel hypRegisterPassword;


	// The pane containing the login information
	@FXML
	public StackPane loginPane;
	// The background rectangle of the login window
	@FXML
	public Rectangle rctLoginBackground;

	// The login button to connect to CyVerse
	@FXML
	public Button btnLogin;

	@FXML
	public DetachableTabPane tabPane;

	// A reference to the home screen
	@FXML
	public AnchorPane homePane;

	// The primary background pane
	@FXML
	public StackPane primaryPane;

	///
	/// FXML Bound fields end
	///

	// The preference key which will just be "Username"
	private static final String USERNAME_PREF = "username";

	// Guassian blur is used to hide the other buttons before logging in
	private final GaussianBlur backgroundBlur = new GaussianBlur();
	// The validator used to validate the username and password (aka ensure they're not empty!)
	private final ValidationSupport USER_PASS_VALIDATOR = new ValidationSupport();

	// Property used to detect if we are logging in or not
	private BooleanProperty loggingIn = new SimpleBooleanProperty(false);

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Grab the logged in property
		ReadOnlyBooleanProperty loggedIn = SanimalData.getInstance().loggedInProperty();

		// Disable the main pane when not logged in
		this.tabPane.disableProperty().bind(loggedIn.not());

		// Blur the main pane when not logged in
		this.tabPane.effectProperty().bind(Bindings.when(loggedIn.not()).then(backgroundBlur).otherwise((GaussianBlur) null));

		// Bind the rectangles width and height to the login pane's width and height
		this.rctLoginBackground.widthProperty().bind(this.loginPane.widthProperty());
		this.rctLoginBackground.heightProperty().bind(this.loginPane.heightProperty());

		// Disable the login property if the username and password are empty
		this.btnLogin.disableProperty().bind(this.USER_PASS_VALIDATOR.invalidProperty().or(loggingIn));

		// Hide the login pane when logged in
		this.loginPane.visibleProperty().bind(loggedIn.not());

		// Register validators for username and password. This simply makes sure that they're both not empty
		this.USER_PASS_VALIDATOR.registerValidator(this.txtUsername, Validator.createEmptyValidator("Username cannot be empty!"));
		this.USER_PASS_VALIDATOR.registerValidator(this.txtPassword, Validator.createEmptyValidator("Password cannot be empty!"));

		// Ensure that the tabs always cover the top of the screen
		tabPane.tabMinWidthProperty().bind(tabPane.widthProperty().divide(tabPane.getTabs().size()).subtract(25));

		// Grab the stored username if the user had 'remember username' selected
		String storedUsername = SanimalData.getInstance().getSanimalPreferences().get(USERNAME_PREF, "");

		// Load default username if it was stored
		if (!storedUsername.isEmpty())
		{
			this.txtUsername.setText(storedUsername);
			this.cbxRememberUsername.setSelected(true);
		}

		// If the user deselects the remember username box, remove the stored username
		this.cbxRememberUsername.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				SanimalData.getInstance().getSanimalPreferences().put(USERNAME_PREF, "");
		});
	}

	/**
	 * When the login button is pressed
	 *
	 * @param actionEvent ignored
	 */
	@FXML
	public void loginPressed(ActionEvent actionEvent)
	{
		// Login
		this.performLogin();
		actionEvent.consume();
	}

	/**
	 * When the enter key gets pressed, also try and login
	 *
	 * @param keyEvent Used to test if the key was enter or not
	 */
	public void enterPressed(KeyEvent keyEvent)
	{
		// Ensure the username and password are valid, and the key pressed was enter
		if (keyEvent.getCode() == KeyCode.ENTER && !this.USER_PASS_VALIDATOR.isInvalid())
		{
			// Login! and comsume the event
			this.performLogin();
			keyEvent.consume();
		}
	}

	/**
	 * Login to the given cyverse account
	 */
	private void performLogin()
	{
		// Save username preference if the box is checked
		if (this.cbxRememberUsername.isSelected())
			SanimalData.getInstance().getSanimalPreferences().put(USERNAME_PREF, this.txtUsername.getText());

		// Only login if we're not logged in
		if (!SanimalData.getInstance().loggedInProperty().getValue())
		{
			this.loggingIn.setValue(true);

			// Show the loading icon graphic
			this.btnLogin.setGraphic(new ImageView(new Image("/images/mainMenu/loading.gif", 26, 26, true, true)));
			// Grab our connection manager
			CyVerseConnectionManager connectionManager = SanimalData.getInstance().getConnectionManager();
			// Grab the username and password
			String username = this.txtUsername.getText();
			String password = this.txtPassword.getText();
			// Thread off logging in...
			FinishableTask<Boolean> loginAttempt = new FinishableTask<Boolean>()
			{
				@Override
				protected Boolean call() throws Exception
				{
					// First login
					this.updateMessage("Logging in...");
					this.updateProgress(1, 6);
					Boolean loginSuccessful = connectionManager.login(username, password);

					if (loginSuccessful)
					{
						connectionManager.derp();
						System.exit(0);

						/*
						// Then initialize the remove sanimal directory
						this.updateMessage("Initializing Sanimal remote directory...");
						this.updateProgress(2, 6);
						connectionManager.initSanimalRemoteDirectory();

						// Pull any locations from the remote directory
						this.updateMessage("Pulling locations from remote directory...");
						this.updateProgress(3, 6);
						List<Location> locations = connectionManager.pullRemoteLocations();

						// Pull any species from the remote directory
						this.updateMessage("Pulling species from remote directory...");
						this.updateProgress(4, 6);
						List<Species> species = connectionManager.pullRemoteSpecies();

						// Pull any species from the remote directory
						this.updateMessage("Pulling collections from remote directory...");
						this.updateProgress(5, 6);
						List<ImageCollection> imageCollections = connectionManager.pullRemoteCollections();

						// Set the locations and species on the FXApplication thread
						Platform.runLater(() ->
						{
							// Set the location list to be these locations
							SanimalData.getInstance().getLocationList().addAll(locations);
							// Set the species list to be these species
							SanimalData.getInstance().getSpeciesList().addAll(species);
							// Set the image collection list to be these collections
							SanimalData.getInstance().getCollectionList().addAll(imageCollections);
						});
						*/

						this.updateProgress(6, 6);
					}

					return loginSuccessful;
				}
			};
			// Once the task succeeds
			loginAttempt.setOnFinished(event -> {
				Boolean loginSucceeded = loginAttempt.getValue();
				// If we did not succeed, notify the user
				if (!loginSucceeded)
				{
					// Show an alert to the user that the sign in did not complete
					Alert invalidAlert = new Alert(Alert.AlertType.INFORMATION);
					invalidAlert.setTitle("Invalid Credentials");
					invalidAlert.setHeaderText("");
					invalidAlert.setContentText("Invalid Username or Password");
					invalidAlert.initOwner(this.tabPane.getScene().getWindow());
					invalidAlert.showAndWait();
				}
				else
				{
					SanimalData.getInstance().setLoggedIn(true);
					SanimalData.getInstance().setUsername(username);
				}
				this.loggingIn.setValue(false);
				// Hide the loading graphic
				this.btnLogin.setGraphic(null);
			});
			// Perform the task
			SanimalData.getInstance().getSanimalExecutor().addTask(loginAttempt);
		}
	}

	/**
	 * When we click a hyperlink, this gets called
	 *
	 * @param actionEvent consumed
	 */
	public void linkPressed(ActionEvent actionEvent)
	{
		try
		{
			switch (((Hyperlink) actionEvent.getSource()).getText())
			{
				// Either open the register or password page on cyverse's website
				case "Register":
					Desktop.getDesktop().browse(new URI("https://user.cyverse.org/register"));
					actionEvent.consume();
					break;
				case "Password":
					Desktop.getDesktop().browse(new URI("https://user.cyverse.org/password/forgot"));
					actionEvent.consume();
					break;
			}
		}
		catch (URISyntaxException | IOException ignored)
		{
		}
	}
}