package controller.importView;

import javafx.animation.FadeTransition;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
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

    // Button used to preview the species
    @FXML
    public ImageView imagePreview;

    ///
    /// FXML bound fields end
    ///

    // This is set to true when we are awaiting a new keybind
    private BooleanProperty awaitingKeybind = new SimpleBooleanProperty(false);

    // This is the Current Image Preview which we set if the preview button is pressed
    private ObjectProperty<Image> currentImagePreview = null;

    // Used to cache species icons to avoid reloading them over and over again
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    // Used to create a fade animation for the loop image
    private FadeTransition previewFadeIn;
    private FadeTransition previewFadeOut;

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

        // When the keybind loses focus, and no key is pressed, we're not awaiting a keybind anymore so reset the label
        this.btnKeybind.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && this.awaitingKeybind.getValue())
            {
                this.awaitingKeybind.setValue(false);
                this.btnKeybind.setText("Keybind");
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

        // First create a fade-in transition for the image preview icon
        this.previewFadeIn = new FadeTransition(Duration.millis(100), this.imagePreview);
        this.previewFadeIn.setFromValue(0);
        this.previewFadeIn.setToValue(1);
        this.previewFadeIn.setCycleCount(1);

        // Then create a fade-out transition for the image preview icon
        this.previewFadeOut = new FadeTransition(Duration.millis(100), this.imagePreview);
        this.previewFadeOut.setFromValue(1);
        this.previewFadeOut.setToValue(0);
        this.previewFadeOut.setCycleCount(1);

        this.previewFadeOut.play();
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
     *
     * @param currentImagePreview The window to write the image to
     */
    public void setCurrentImagePreview(ObjectProperty<Image> currentImagePreview)
    {
        this.currentImagePreview = currentImagePreview;
    }

    /**
     * When the user clicks the preview button show a preview of the image
     *
     * @param mouseEvent consumed
     */
    public void previewImage(MouseEvent mouseEvent)
    {
        // When we click preview, set the image preview
        if (this.currentImagePreview != null)
            this.currentImagePreview.setValue(IMAGE_CACHE.get(this.getItem().getSpeciesIcon()));
        mouseEvent.consume();
    }

    /**
     * When the mouse enters the preview, play the fade in animation
     *
     * @param mouseEvent consumed
     */
    public void mouseEnteredPreview(MouseEvent mouseEvent)
    {
        this.previewFadeIn.play();
        mouseEvent.consume();
    }

    /**
     * When the mouse exits the preview, play the fade out animation
     *
     * @param mouseEvent consumed
     */
    public void mouseExitedPreview(MouseEvent mouseEvent)
    {
        this.previewFadeOut.play();
        mouseEvent.consume();
    }
}
