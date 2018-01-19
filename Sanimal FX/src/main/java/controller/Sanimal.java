package controller;

import com.panemu.tiwulfx.form.BaseControl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.util.FXMLLoaderUtils;

/**
 * Main class entry point
 *
 * @author David Slovikosky
 * @version 1.0
 */
public class Sanimal extends Application
{
    // Main just launches the application
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Application.setUserAgentStylesheet(STYLESHEET_MODENA);

        // Load the FXML document
        FXMLLoader root = FXMLLoaderUtils.loadFXML("SanimalView.fxml");
        // Create the scene
        Scene scene = new Scene(root.getRoot());
        // We need this to ensure that the tiwulfx library correctly renders the detachable tabs
        scene.getStylesheets().add(BaseControl.class.getResource("/com/panemu/tiwulfx/res/tiwulfx.css").toExternalForm());
        // Put the scene on the stage
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("images/mainMenu/paw.png"));
        primaryStage.setTitle("Scientific Animal Image Analysis (SANIMAL)");
        primaryStage.setOnCloseRequest(x -> System.exit(0));
        primaryStage.setMaximized(true);
        // When we exit the window exit the program
        // Show it
        primaryStage.show();
    }
}
