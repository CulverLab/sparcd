package controller;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;

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
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // Load the URL
        URL rootLoc = getClass().getResource("/view/SanimalView.fxml");
        // Load the FXML document
        Parent root = FXMLLoader.load(rootLoc);
        // Create the scene
        Scene scene = new Scene(root);
        // Put the scene on the stage
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        Image icon = new Image("images/mainMenu/paw.png");
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Scientific Animal Image Analysis (SANIMAL)");
        // Show it
        primaryStage.show();
    }
}
