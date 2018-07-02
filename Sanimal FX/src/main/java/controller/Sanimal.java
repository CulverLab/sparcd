package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.SanimalData;
import model.util.FXMLLoaderUtils;
import org.controlsfx.control.action.Action;

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
    public void start(Stage primaryStage)
    {
        Application.setUserAgentStylesheet(STYLESHEET_MODENA);

        // Load the FXML document
        FXMLLoader root = FXMLLoaderUtils.loadFXML("SanimalView.fxml");
        // Create the scene
        Scene scene = new Scene(root.getRoot());

        // Put the scene on the stage
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("images/mainMenu/paw.png"));
        primaryStage.setTitle("Scientific Animal Image Analysis (SANIMAL)");
        // When we click exit...
        primaryStage.setOnCloseRequest(event ->
        {
            // If a task is still running ask for confirmation to exit
            if (SanimalData.getInstance().getSanimalExecutor().anyTaskRunning())
            {
                SanimalData.getInstance().getErrorDisplay().notify("Sanimal is still cleaning up background tasks and exiting now may cause data corruption. Are you sure you want to exit?",
					new Action("Exit Anyway", actionEvent ->
					{
						System.exit(0);
					}));
				event.consume();
            }
            else
            {
                System.exit(0);
            }
        });
        primaryStage.setMaximized(true);
        // When we exit the window exit the program
        // Show it
        primaryStage.show();
    }
}
