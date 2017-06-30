package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
    // The exit button to close the program
    @FXML
    public Button btnExit;

    ///
    /// FXML bound fields end
    ///

    // Store the stages to re-open them if they get closed
    private Stage importStage = null;
    private Stage mapStage = null;
    private Stage analysisStage = null;

    /**
     * Initialize sets up the analysis window and bindings
     *
     * @param location ignored
     * @param resources ignored
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Ignored
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
        System.exit(0);
    }
}
