package controller;

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
        // Put the scene on the stage
        primaryStage.setScene(scene);
        Image icon = new Image("images/mainMenu/paw.png");
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("Scientific Animal Image Analysis (SANIMAL)");
        // When we exit the window exit the program
        //primaryStage.setOnCloseRequest(ignored -> root.<SanimalViewController> getController().exitPressed(null));
        // Show it
        primaryStage.show();
    }
}
