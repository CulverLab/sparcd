package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import model.image.ImageEntry;
import model.species.Species;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller class for the species list entry
 */
public class SpeciesListEntryController extends ListCell<Species>
{
    ///
    /// FXML bound fields start
    ///

    // A reference to the main background pane
    @FXML
    private HBox mainPane;

    // A species icon
    @FXML
    private ImageView imageView;

    // The species name
    @FXML
    private Label lblName;
    // The species scientific name
    @FXML
    private Label lblScientificName;

    // The keybind button
    @FXML
    private Button btnKeybind;
    // The preview button
    @FXML
    private Button btnPreview;

    ///
    /// FXML bound fields end
    ///

    // This is set to true when we are awaiting a new keybind
    private BooleanProperty awaitingKeybind = new SimpleBooleanProperty(false);

    // This is the Current Image Preview which we set if the preview button is pressed
    private ObjectProperty<Image> currentImagePreview = null;

    // Used to cache species icons to avoid reloading them over and over again
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    /**
     * Called once the controller has been setup
     */
    @FXML
    public void initialize()
    {
        // When we are awaiting a keybind, and we get a key typed, then store the keybind into the newKeybind property
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (this.awaitingKeybind.getValue())
            {
                // Set the awaiting keybind flag to false
                this.awaitingKeybind.setValue(false);
                // Set the keybinding
                this.getItem().setKeyBinding(event.getCode());
                // Set the button text
                this.btnKeybind.setText(this.getItem().getKeyBinding() != null ? this.getItem().getKeyBinding().getName() : "Keybind");
                // Consume the event
                event.consume();
            }
        });

        this.btnKeybind.setOnAction(event -> {
            // If we have a keybinding already, clear the existing keybind
            if (this.getItem().getKeyBinding() != null)
            {
                this.getItem().setKeyBinding(null);
                this.btnKeybind.setText("Keybind");
            }
            // If we don't request a new one
            else
            {
                this.awaitingKeybind.setValue(true);
                this.btnKeybind.setText("Waiting...");
            }
        });

        this.btnPreview.setOnAction(event ->
        {
            if (this.currentImagePreview != null)
                this.currentImagePreview.setValue(IMAGE_CACHE.get(this.getItem().getSpeciesIcon()));
        });
    }

    /**
     * Update item is called when the list cell gets a new species
     *
     * @param species The new species
     * @param empty if the cell will be empty
     */
    @Override
    protected void updateItem(Species species, boolean empty)
    {
        // Update the cell internally
        super.updateItem(species, empty);

        // Set the text of the cell to nothing
        this.setText(null);

        // If no species was given and the cell was empty, clear the graphic
        if (empty && species == null)
        {
            this.setGraphic(null);
        }
        else
        {
            // Set the name to the species name
            this.lblName.setText(species.getName());
            // Set the scientific name to the species scientific name
            this.lblScientificName.setText(species.getScientificName());
            // Check if the image cache contains the species unique key. If it does not, load it in a thread.
            if (!IMAGE_CACHE.containsKey(species.getSpeciesIcon()))
            {
                IMAGE_CACHE.put(species.getSpeciesIcon(), new Image(species.getSpeciesIcon(), true));
            }
            // Set the image in the image view to the given species icon
            SpeciesListEntryController.this.imageView.setImage(IMAGE_CACHE.get(species.getSpeciesIcon()));
            // Set the keybind if we have one
            this.btnKeybind.setText(species.getKeyBinding() != null ? species.getKeyBinding().getName() : "Keybind");
            // Set the graphic to display
            this.setGraphic(mainPane);
        }
    }

    /**
     * Used to set the ImageView that the preview button should write the image to
     * @param currentImagePreview The window to write the image to
     */
    public void setCurrentImagePreview(ObjectProperty<Image> currentImagePreview)
    {
        this.currentImagePreview = currentImagePreview;
    }
}
