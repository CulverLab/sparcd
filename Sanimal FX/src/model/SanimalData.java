package model;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.DataFormat;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.location.Location;
import model.species.Species;

import java.io.File;

public class SanimalData
{
    private static final SanimalData instance = new SanimalData();

    public static SanimalData getInstance()
    {
        return SanimalData.instance;
    }

    private ObservableList<Species> speciesList;

    private ObservableList<Location> locationList;

    private ImageDirectory imageTree;

    private SanimalData()
    {
        this.speciesList = FXCollections.<Species>observableArrayList(species -> new Observable[]{species.getNameProperty(), species.getScientificNameProperty(), species.getSpeciesIconURLProperty()});
        this.speciesList.add(new Species("Tiger", "Panthera tigris", "http://kids.nationalgeographic.com/content/dam/kids/photos/articles/Other%20Explore%20Photos/R-Z/Wacky%20Weekend/Wild%20Cats/ww-wild-cats-tiger.adapt.945.1.jpg"));
        this.speciesList.add(new Species("Lion", "Panthera leo", "https://upload.wikimedia.org/wikipedia/commons/7/73/Lion_waiting_in_Namibia.jpg"));
        this.speciesList.add(new Species("Unicorn", "Fakus Imaginus", "https://s-media-cache-ak0.pinimg.com/736x/3b/ca/b6/3bcab6f591ac1d61b1e6abded3ea06a7.jpg"));

        this.locationList = FXCollections.<Location> observableArrayList( location -> new Observable[] {location.getNameProperty(), location.getLatProperty(), location.getLngProperty(), location.getElevationProperty()});
        this.locationList.add(new Location("Tucson", 32D, 110D, 2388D));
        this.locationList.add(new Location("Munich", 48D, 11D, 520D));

        this.imageTree = new ImageDirectory(new File("./"));
    }

    public ObservableList<Species> getSpeciesList()
    {
        return speciesList;
    }

    public ObservableList<Location> getLocationList()
    {
        return locationList;
    }

    public ImageDirectory getImageTree()
    {
        return imageTree;
    }
}
