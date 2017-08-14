package controller;

import ch.qos.logback.classic.Level;
import javafx.animation.Animation;
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
import org.apache.commons.validator.util.ValidatorUtils;
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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controller class for the main program
 */
public class SanimalViewController implements Initializable
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
    // The login button to connect to CyVerse
    @FXML
    public Button btnLogin;
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
    // The pane containing the login information
    @FXML
    public StackPane loginPane;
    // The background rectangle of the login window
    @FXML
    public Rectangle rctLoginBackground;

    // The hyperlink label with the register and forgot password options
    @FXML
    public HyperlinkLabel hypRegisterPassword;

    // The username and password text fields
    @FXML
    public TextField txtUsername;
    @FXML
    public PasswordField txtPassword;

    ///
    /// FXML bound fields end
    ///

    // Store the stages to re-open them if they get closed
    private Stage importStage = null;
    private Stage mapStage = null;
    private Stage analysisStage = null;

    // Guassian blur is used to hide the other buttons before logging in
    private final GaussianBlur backgroundBlur = new GaussianBlur();
    // The validator used to validate the username and password (aka ensure they're not empty!)
    private final ValidationSupport USER_PASS_VALIDATOR = new ValidationSupport();

    /**
     * Initialize sets up the analysis window and bindings
     *
     * @param location ignored
     * @param resources ignored
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Grab the logged in property
        ReadOnlyBooleanProperty loggedIn = SanimalData.getInstance().getConnectionManager().loggedInProperty();
        // If we're logged in show the logged in person's username
        this.lblUsername.textProperty().bind(EasyBind.monadic(SanimalData.getInstance().getConnectionManager().usernameProperty()).map(username -> "Welcome " + username + "!").orElse(""));
        // Disable the main pane when not logged in
        this.mainPane.disableProperty().bind(loggedIn.not());

        // Blur the main pane when not logged in
        this.mainPane.effectProperty().bind(Bindings.when(loggedIn.not()).then(backgroundBlur).otherwise((GaussianBlur) null));

        // Bind the rectangles width and height to the login pane's width and height
        this.rctLoginBackground.widthProperty().bind(this.loginPane.widthProperty());
        this.rctLoginBackground.heightProperty().bind(this.loginPane.heightProperty());

        // Disable the login property if the username and password are empty
        this.btnLogin.disableProperty().bind(this.USER_PASS_VALIDATOR.invalidProperty());

        // Hide the login pane when logged in
        this.loginPane.visibleProperty().bind(loggedIn.not());

        // Register validators for username and password. This simply makes sure that they're both not empty
        this.USER_PASS_VALIDATOR.registerValidator(this.txtUsername, Validator.createEmptyValidator("Username cannot be empty!"));
        this.USER_PASS_VALIDATOR.registerValidator(this.txtPassword, Validator.createEmptyValidator("Password cannot be empty!"));
    }

    /**
     * When the import button is pressed either re-open the import stage or create it and open it
     *
     * @param actionEvent Action is consumed
     */
    @FXML
    public void importPressed(ActionEvent actionEvent)
    {
        // If the stage has not yet been initialized
        if (importStage == null)
        {
            try
            {
                // Initialize it
                importStage = new Stage();
                // Load the FXML document
                URL document = getClass().getResource("/view/SanimalImport.fxml");
                Parent importRoot = FXMLLoader.load(document);
                // Create the scene
                Scene scene = new Scene(importRoot);
                // Put the scene on the stage
                Image icon = new Image("images/mainMenu/paw.png");
                importStage.getIcons().add(icon);
                importStage.setTitle("SANIMAL Image Importer");
                importStage.setScene(scene);
            }
            catch (IOException e)
            {
                System.err.println("Could not load the Sanimal Import FXML file. This is an error.");
                e.printStackTrace();
                return;
            }
        }

        // Show the stage
        if (!importStage.isShowing())
            importStage.show();

        // Consume the event
        actionEvent.consume();
    }

    /**
     * When the analyze button is pressed either re-open the analyze stage or create it and open it
     *
     * @param actionEvent Action is consumed
     */
    @FXML
    public void analyzePressed(ActionEvent actionEvent)
    {
        /*
        SanimalData.getInstance().addTask(new Task<Void>() {
            @Override
            protected Void call() throws Exception
            {
                SanimalData.getInstance().getConnectionManager().test();
                return null;
            }
        });
        */
        // If the stage has not yet been initialized
        if (analysisStage == null)
        {
            try
            {
                // Initialize it
                analysisStage = new Stage();
                // Load the FXML document
                URL document = getClass().getResource("/view/SanimalAnalysis.fxml");
                Parent analysisRoot = FXMLLoader.load(document);
                // Create the scene
                Scene scene = new Scene(analysisRoot);
                // Put the scene on the stage
                Image icon = new Image("images/mainMenu/paw.png");
                analysisStage.getIcons().add(icon);
                analysisStage.setTitle("SANIMAL Analysis");
                analysisStage.setScene(scene);
            }
            catch (IOException e)
            {
                System.err.println("Could not load the Sanimal Analysis FXML file. This is an error.");
                e.printStackTrace();
                return;
            }
        }

        // Show the stage
        if (!analysisStage.isShowing())
            analysisStage.show();

        // Consume the event
        actionEvent.consume();
    }

    /**
     * When the map button is pressed either re-open the map stage or create it and open it
     *
     * @param actionEvent Action is consumed
     */
    @FXML
    public void mapPressed(ActionEvent actionEvent)
    {
        // If the stage has not yet been initialized
        if (mapStage == null)
        {
            try
            {
                // Initialize it
                mapStage = new Stage();
                // Load the FXML document
                URL document = getClass().getResource("/view/SanimalMap.fxml");
                Parent mapRoot = FXMLLoader.load(document);
                // Create the scene
                Scene scene = new Scene(mapRoot);
                // Put the scene on the stage
                Image icon = new Image("images/mainMenu/paw.png");
                mapStage.getIcons().add(icon);
                mapStage.setTitle("SANIMAL Map");
                mapStage.setScene(scene);
            }
            catch (IOException e)
            {
                System.err.println("Could not load the Sanimal Map FXML file. This is an error.");
                e.printStackTrace();
                return;
            }
        }

        // Show the stage
        if (!mapStage.isShowing())
            mapStage.show();

        // Consume the event
        actionEvent.consume();
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
        // Only login if we're not logged in
        if (!SanimalData.getInstance().getConnectionManager().loggedInProperty().getValue())
        {
            // Show the loading icon graphic
            this.btnLogin.setGraphic(new ImageView(new Image("/images/mainMenu/loading.gif", 26, 26, true, true)));
            // Grab our connection manager
            CyVerseConnectionManager connectionManager = SanimalData.getInstance().getConnectionManager();
            // Grab the username and password
            String username = this.txtUsername.getText();
            String password = this.txtPassword.getText();
            // Thread off logging in...
            Task<Boolean> loginAttempt = new Task<Boolean>()
            {
                @Override
                protected Boolean call() throws Exception
                {
                    // Just login
                    return connectionManager.login(username, password);
                }
            };
            // Once the task succeeds
            loginAttempt.setOnSucceeded(event -> {
                Boolean loginSucceeded = loginAttempt.getValue();
                // If we did not succeed, notify the user
                if (!loginSucceeded)
                {
                    // Show an alert to the user that the sign in did not complete
                    Alert invalidAlert = new Alert(Alert.AlertType.INFORMATION);
                    invalidAlert.setTitle("Invalid Credentials");
                    invalidAlert.setHeaderText("");
                    invalidAlert.setContentText("Invalid Username or Password");
                    invalidAlert.initOwner(this.mainPane.getScene().getWindow());
                    invalidAlert.showAndWait();
                }
                // Hide the loading graphic
                this.btnLogin.setGraphic(null);
            });
            // Perform the task
            SanimalData.getInstance().addTask(loginAttempt);
        }
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
            Task<Void> logoutAttempt = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    // Logout
                    SanimalData.getInstance().getConnectionManager().logout();
                    return null;
                }
            };
            // Once the thread completes, we clear the username and password
            logoutAttempt.setOnSucceeded(event -> {
                this.txtUsername.clear();
                this.txtPassword.clear();
            });
            // Perform the task
            SanimalData.getInstance().addTask(logoutAttempt);
        }
        actionEvent.consume();
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
        catch (URISyntaxException | IOException ignored) {}
    }
}
