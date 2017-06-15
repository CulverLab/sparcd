package controller;

import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.ImageViewPane;
import library.TreeViewAutomatic;
import model.SanimalData;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageImporter;
import model.species.Species;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class SanimalImportController implements Initializable {
    @FXML
    public ImageViewPane imagePreviewPane;

    @FXML
    public ImageView imagePreview;

    // Will contain ImageEntries and ImageDirectories
    @FXML
    public TreeViewAutomatic<ImageContainer> imageTree;

    @FXML
    private ListView<Species> speciesListView;

    private ImageImporter importedData = new ImageImporter();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SortedList<Species> species = new SortedList<Species>(SanimalData.getInstance().getSpeciesList());
        species.setComparator(Comparator.comparing(Species::getName));
        this.speciesListView.setItems(species);
        this.speciesListView.setCellFactory(x ->
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SpeciesListEntry.fxml"));

                    try {
                        loader.load();
                    } catch (IOException exception) {
                        System.err.println("Could not load the FXML file for the species list entry!");
                        exception.printStackTrace();
                        return null;
                    }

                    return loader.getController();
                }
        );
        this.imagePreview.setImage(new Image(new File("./src/images/importWindow/testImg.JPG").toURI().toString()));

        final TreeItem<ImageContainer> ROOT = new TreeItem<ImageContainer>(SanimalData.getInstance().getImageTree());
        this.imageTree.setShowRoot(false);
        this.imageTree.setRoot(ROOT);
        this.imageTree.setItems(SanimalData.getInstance().getImageTree().getChildren());
    }

    public void addNewSpecies(ActionEvent actionEvent) {
        Species newSpecies = new Species();
        requestEdit(newSpecies);
        if (!newSpecies.isUninitialized())
            SanimalData.getInstance().getSpeciesList().add(newSpecies);
    }

    public void editCurrentSpecies(ActionEvent actionEvent) {
        Species selected = speciesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            requestEdit(selected);
            //this.speciesList.remove(selected);
            //this.speciesList.add(selected);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(this.imagePreview.getScene().getWindow());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Species Selected");
            alert.setContentText("Please select a species from the species list to edit.");
            alert.showAndWait();
        }
    }

    private void requestEdit(Species species) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SpeciesCreator.fxml"));
        GridPane windowHead;
        try {
            windowHead = loader.<GridPane>load();
        } catch (IOException exception) {
            System.err.println("Could not load the FXML file for the species list entry!");
            exception.printStackTrace();
            return;
        }
        SpeciesCreatorController controller = loader.<SpeciesCreatorController>getController();
        controller.setSpecies(species);

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Species Creator/Editor");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(this.imagePreview.getScene().getWindow());
        Scene scene = new Scene(windowHead);
        dialogStage.setScene(scene);

        dialogStage.showAndWait();
    }

    public void deleteCurrentSpecies(ActionEvent actionEvent) {
        Species selected = speciesListView.getSelectionModel().getSelectedItem();
        SanimalData.getInstance().getSpeciesList().remove(selected);
        actionEvent.consume();
    }

    public void importImages(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder with Images");
        directoryChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
        File file = directoryChooser.showDialog(this.imagePreview.getScene().getWindow());
        if (file != null && file.isDirectory())
        {
            ImageDirectory directory = ImageImporter.loadDirectory(file);
            ImageImporter.removeEmptyDirectories(directory);
            SanimalData.getInstance().getImageTree().addSubDirectory(directory);
        }
        actionEvent.consume();
    }

    public void deleteImages(ActionEvent actionEvent) {
        TreeItem<ImageContainer> item = this.imageTree.getSelectionModel().getSelectedItem();
        item.getParent().getChildren().remove(item);
        actionEvent.consume();
    }
}
