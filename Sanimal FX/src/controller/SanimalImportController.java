package controller;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import library.ImageViewPane;
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

    @FXML
    public TreeView imageTree;

    @FXML
    private ListView<Species> speciesListView;

    private ObservableList<Species> speciesList;

    public SanimalImportController() {
        this.speciesList = FXCollections.<Species>observableArrayList(species -> new Observable[]{species.getNameProperty(), species.getScientificNameProperty(), species.getSpeciesIconURLProperty()});
        this.speciesList.add(new Species("Tiger", "Panthera tigris", "http://kids.nationalgeographic.com/content/dam/kids/photos/articles/Other%20Explore%20Photos/R-Z/Wacky%20Weekend/Wild%20Cats/ww-wild-cats-tiger.adapt.945.1.jpg"));
        this.speciesList.add(new Species("Lion", "Panthera leo", "https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg"));
        this.speciesList.add(new Species("Unicorn", "Fakus Imaginus", "https://s-media-cache-ak0.pinimg.com/736x/3b/ca/b6/3bcab6f591ac1d61b1e6abded3ea06a7.jpg"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SortedList<Species> species = new SortedList<Species>(this.speciesList);
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
        //this.imagePreview.fitWidthProperty().bind(this.imageAnchor.widthProperty());
        //this.imagePreview.fitHeightProperty().bind(this.imageAnchor.heightProperty());
    }

    public void addNewSpecies(ActionEvent actionEvent) {
        Species newSpecies = new Species();
        requestEdit(newSpecies);
        if (!newSpecies.isUninitialized())
            this.speciesList.add(newSpecies);
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
        this.speciesList.remove(selected);
    }

    public void importImages(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder with Images");
        directoryChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
        File file = directoryChooser.showDialog(this.imagePreview.getScene().getWindow());
        if (file != null && file.isDirectory())
        {
            //this.imageTree.
        }
    }

    public void deleteImages(ActionEvent actionEvent) {

    }
}
