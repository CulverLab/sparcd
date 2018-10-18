package controller;

import controller.importView.SpeciesCreatorController;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.SanimalData;
import model.util.FXMLLoaderUtils;
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

	// The credits button
	@FXML
	public Button btnCredits;
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
		this.lblUsername.visibleProperty().bind(loggedIn);
		this.btnCredits.visibleProperty().bind(loggedIn);
		this.btnLogout.visibleProperty().bind(loggedIn);
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

	/**
	 * When the user clicks the UA Wildcat Research logo
	 *
	 * @param mouseEvent consumed
	 */
	public void showWildcatResearchWebsite(MouseEvent mouseEvent)
	{
		try
		{
			Desktop.getDesktop().browse(new URI("https://wildcatresearch.arizona.edu/"));
		}
		catch (IOException | URISyntaxException ignored)
		{
		}
		mouseEvent.consume();
	}

	/**
	 * When the user clicks the credits button
	 *
	 * @param actionEvent consumed
	 */
	public void creditsPressed(ActionEvent actionEvent)
	{
		// Load the FXML file of the editor window
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("homeView/Credits.fxml");

		// Create the stage that will have the species creator/editor
		Stage dialogStage = new Stage();
		// Set the title
		dialogStage.setTitle("Credits");
		// Set the modality and initialize the owner to be this current window
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.mainPane.getScene().getWindow());
		// Set the scene to the root of the FXML file
		Scene scene = new Scene(loader.getRoot());
		// Set the scene of the stage, and show it!
		dialogStage.setScene(scene);
		dialogStage.setResizable(false);
		dialogStage.showAndWait();

		actionEvent.consume();
	}
}
