package controller;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.SanimalData;
import org.fxmisc.easybind.EasyBind;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the main program
 */
public class SanimalHomeController implements Initializable
{
	///
	/// FXML bound fields start
	///

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
		this.lblUsername.textProperty().bind(EasyBind.monadic(SanimalData.getInstance().usernameProperty()).map(username -> "Welcome " + username + "!").orElse(""));

		// Grab the logged in property
		ReadOnlyBooleanProperty loggedIn = SanimalData.getInstance().loggedInProperty();

		// Hide the logout button and text when not logged in
		this.btnLogout.visibleProperty().bind(loggedIn);
		this.lblUsername.visibleProperty().bind(loggedIn);
		this.btnExit.visibleProperty().bind(loggedIn);
	}

	/**
	 * When exit is pressed close the program
	 *
	 * @param actionEvent ignored
	 */
	@FXML
	public void exitPressed(ActionEvent actionEvent)
	{
		System.exit(0);
	}

	/**
	 * When the logout button is pressed
	 *
	 * @param actionEvent ignored
	 */
	public void logoutPressed(ActionEvent actionEvent)
	{
		System.exit(0);
	}

	/**
	 * When the user clicks the cyverse logo
	 *
	 * @param mouseEvent consumed
	 */
	public void showCyverseWebsite(MouseEvent mouseEvent)
	{
		try
		{
			Desktop.getDesktop().browse(new URI("http://www.cyverse.org"));
		}
		catch (IOException | URISyntaxException ignored)
		{
		}
		mouseEvent.consume();
	}

	/**
	 * When the user clicks the UA CS logo
	 *
	 * @param mouseEvent consumed
	 */
	public void showCSWebsite(MouseEvent mouseEvent)
	{
		try
		{
			Desktop.getDesktop().browse(new URI("https://www.cs.arizona.edu"));
		}
		catch (IOException | URISyntaxException ignored)
		{
		}
		mouseEvent.consume();
	}

	/**
	 * When the user clicks the UA SNRE logo
	 *
	 * @param mouseEvent consumed
	 */
	public void showSNREWebsite(MouseEvent mouseEvent)
	{
		try
		{
			Desktop.getDesktop().browse(new URI("https://snre.arizona.edu/"));
		}
		catch (IOException | URISyntaxException ignored)
		{
		}
		mouseEvent.consume();
	}
}
