package controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SanimalViewController implements Initializable
{
    private Stage importStage = null;

    @FXML
    public Button btnImport;
    @FXML
    public Button btnAnalyze;
    @FXML
    public Button btnMap;
    @FXML
    public Button btnExit;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Ignored?
    }

    @FXML
    public void importPressed(ActionEvent actionEvent)
    {
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
                System.err.println("Could not load the SanimalView FXML file. This is an error.");
                e.printStackTrace();
                return;
            }
        }

        if (!importStage.isShowing())
            importStage.show();
    }

    @FXML
    public void analyzePressed(ActionEvent actionEvent)
    {
        System.out.println("Analyze");
    }

    @FXML
    public void mapPressed(ActionEvent actionEvent)
    {
        System.out.println("Map");
    }

    @FXML
    public void exitPressed(ActionEvent actionEvent)
    {
        System.exit(0);
    }
}
