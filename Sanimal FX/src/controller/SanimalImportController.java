package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.species.Species;

import java.net.URL;
import java.util.ResourceBundle;

public class SanimalImportController implements Initializable
{
    @FXML
    private ListView<Species> speciesListView;

    private ObservableList<Species> speciesList;

    public SanimalImportController()
    {
        this.speciesList = FXCollections.<Species> observableArrayList();
        this.speciesList.addAll(
            new Species("Tiger", "Panthera tigris", "http://kids.nationalgeographic.com/content/dam/kids/photos/articles/Other%20Explore%20Photos/R-Z/Wacky%20Weekend/Wild%20Cats/ww-wild-cats-tiger.adapt.945.1.jpg"),
            new Species("Lion", "Panthera leo", "https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg"),
            new Species("Unicorn", "Fakus Imaginus", "https://s-media-cache-ak0.pinimg.com/736x/3b/ca/b6/3bcab6f591ac1d61b1e6abded3ea06a7.jpg")
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        this.speciesListView.setItems(this.speciesList);
        this.speciesListView.setCellFactory(x -> new SpeciesListEntryController());
    }
}
