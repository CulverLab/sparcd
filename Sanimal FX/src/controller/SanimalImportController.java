package controller;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import library.FXMLLoaderUtils;
import library.ImageViewPane;
import library.TreeViewAutomatic;
import model.SanimalData;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.image.DirectoryManager;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import org.controlsfx.control.StatusBar;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller class for the main import window
 */
public class SanimalImportController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The pane containing the image preview
	@FXML
	public ImageViewPane imagePreviewPane;

	// The image view to be contained inside the image view pane
	@FXML
	public ImageView imagePreview;

	// The stack pane containing the image preview
	@FXML
	public StackPane imagePane;

	// The slider for image brightness
	@FXML
	public Slider sldBrightness;
	// The slider for image contrast
	@FXML
	public Slider sldContrast;
	// The slider for image hue
	@FXML
	public Slider sldHue;
	// The slider for saturation
	@FXML
	public Slider sldSaturation;

	// The date the image was taken
	@FXML
	public TextField txtDateTaken;

	// The list view containing all species entries
	@FXML
	public ListView<SpeciesEntry> speciesEntryListView;

	// The tree view containing all the images and folders
	@FXML
	public TreeViewAutomatic<ImageContainer> imageTree;

	// The list view containg all locations
	@FXML
	public ListView<Location> locationListView;

	// The button to reset the image effects
	@FXML
	public Button btnResetImage;

	// The region that hovers over the image which is used for its border
	@FXML
	public Region imageAddOverlay;

	// Status bar for showing how far we have completed metadata image tasks
	@FXML
	public StatusBar sbrTaskProgress;

	// Left and right arrow buttons to allow easy next and previous image selection
	@FXML
	public Button btnLeftArrow;
	@FXML
	public Button btnRightArrow;

	// The list view containing the species
	@FXML
	private ListView<Species> speciesListView;

	///
	/// FXML bound fields end
	///

	// The color adjust property is used to adjust the image preview's color FX
	private ObjectProperty<ColorAdjust> colorAdjust = new SimpleObjectProperty<>(new ColorAdjust());

	// Fields to hold the currently selected image entry and image directory
	private ObjectProperty<ImageEntry> currentlySelectedImage = new SimpleObjectProperty<>(null);
	private ObjectProperty<ImageDirectory> currentlySelectedDirectory = new SimpleObjectProperty<>(null);
	// Use fade transitions to fade the species list in and out
	private FadeTransition fadeSpeciesEntryListIn;
	private FadeTransition fadeSpeciesEntryListOut;
	private FadeTransition fadeAddPanelIn;
	private FadeTransition fadeAddPanelOut;
	private FadeTransition fadeLeftIn;
	private FadeTransition fadeLeftOut;
	private FadeTransition fadeRightIn;
	private FadeTransition fadeRightOut;
	// A property used to process the image scrolling
	private ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

	/**
	 * Initialize the sanimal import view and data bindings
	 *
	 * @param location  ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// First we setup the species list

		// Grab the global species list
		SortedList<Species> species = new SortedList<>(SanimalData.getInstance().getSpeciesList());
		// We set the comparator to be the name of the species
		species.setComparator(Comparator.comparing(Species::getName));
		// Set the items of the species list view to the newly sorted list
		this.speciesListView.setItems(species);
		// Set the cell factory to be our custom species list cell
		this.speciesListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("SpeciesListEntry.fxml").getController());
		// When we double click the species list view items, we want to edit the species
		this.speciesListView.setOnMouseClicked(event -> {
			if (event.getClickCount() >= 2 && this.speciesListView.getSelectionModel().getSelectedItem() != null)
				this.requestEdit(this.speciesListView.getSelectionModel().getSelectedItem());
		});

		// Then we setup the locations list in a similar manner

		// Grab the global location list
		SortedList<Location> locations = new SortedList<>(SanimalData.getInstance().getLocationList());
		// Set the comparator to be the name of the location
		locations.setComparator(Comparator.comparing(Location::getName));
		// Set the items of the location list view to the newly sorted list
		this.locationListView.setItems(locations);
		// Set the cell factory to be our custom location list cell
		this.locationListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("LocationListEntry.fxml").getController());
		// When we select a location, we need to set the location on the selected image or directory of images
		this.locationListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) ->
		{
			// Make sure we got a new value
			if (newValue != null)
				// Check if we have a selected image or directory to update!
				if (currentlySelectedImage.getValue() != null)
					currentlySelectedImage.getValue().setLocationTaken(newValue);
				else if (currentlySelectedDirectory.getValue() != null)
					this.setContainerLocation(currentlySelectedDirectory.getValue(), newValue);
		}));
		// When we double click the location list view items, we want to edit the location
		this.locationListView.setOnMouseClicked(event -> {
			if (event.getClickCount() >= 2 && this.locationListView.getSelectionModel().getSelectedItem() != null)
				this.requestEdit(this.locationListView.getSelectionModel().getSelectedItem());
		});

		// Setup the species entry list view

		// The species entry list view just needs to have a cell factory
		this.speciesEntryListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("SpeciesEntryListEntry.fxml").getController());

		// Setup the color adjustment property on the image

		// We bind the brightness, contrast, hue, and saturation from the sliders to the color adjust object
		colorAdjust.getValue().brightnessProperty().bind(this.sldBrightness.valueProperty());
		colorAdjust.getValue().contrastProperty().bind(this.sldContrast.valueProperty());
		colorAdjust.getValue().hueProperty().bind(this.sldHue.valueProperty());
		colorAdjust.getValue().saturationProperty().bind(this.sldSaturation.valueProperty());
		// Finally we bind the effect property to the color adjust so that the sliders are bound to the image adjustment
		this.imagePreview.effectProperty().bind(this.colorAdjust);

		// Initialize root of the right side directory/image tree and make the root invisible
		// This is because a treeview must have ONE root.

		// Create a fake invisible root node whos children
		final TreeItem<ImageContainer> ROOT = new TreeItem<>(SanimalData.getInstance().getImageTree());
		// Hide the fake invisible root
		this.imageTree.setShowRoot(false);
		// Set the fake invisible root
		this.imageTree.setRoot(ROOT);
		// Set the items of the tree to be the children of the fake invisible root
		this.imageTree.setItems(SanimalData.getInstance().getImageTree().getChildren());
		// When a new image is selected...
		this.imageTree.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				// If it's an image, update the currently selected image, otherwise update the currently selected directory
				ImageContainer newOne = newValue.getValue();
				if (newOne instanceof ImageEntry)
				{
					currentlySelectedImage.setValue((ImageEntry) newOne);
					currentlySelectedDirectory.setValue(null);
				}
				else if (newOne instanceof ImageDirectory)
				{
					currentlySelectedImage.setValue(null);
					currentlySelectedDirectory.setValue((ImageDirectory) newOne);
				}
			}
		}));

		// Create bindings in the GUI

		// First bind the 4 color adjustment sliders disable property to if an adjustable image is selected
		this.sldSaturation.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldHue.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldContrast.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldBrightness.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		// Also bind the date taken text field's disable property if an image is selected
		this.txtDateTaken.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		// Also bind the disable button's disable property if an adjustable image is selected
		this.btnResetImage.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		// Finally bind the date taken's disable property if an adjustable image is selected
		this.txtDateTaken.textProperty().bind(Bindings.createStringBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getDateTakenFormatted() : "", currentlySelectedImage));
		// Bind the image preview to the selected image from the right side tree view
		this.imagePreview.imageProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? new Image(currentlySelectedImage.getValue().getFile().toURI().toString()) : null, currentlySelectedImage));
		// Bind the species entry list view items to the selected image species present
		this.speciesEntryListView.itemsProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getSpeciesPresent() : null, currentlySelectedImage));
		// Hide the progress bar when progress == 1
		this.sbrTaskProgress.visibleProperty().bind(SanimalData.getInstance().getPendingTasksProperty().isEqualTo(0).not());
		// Bind the progress bar's text property to tasks remaining
		this.sbrTaskProgress.textProperty().bind(SanimalData.getInstance().getPendingTasksProperty().asString("Tasks remaining: %d"));
		// Bind the progress bar's progress property go back and forth if at least one task is present. We do this by setting it to -1
		this.sbrTaskProgress.progressProperty().bind(Bindings.when(SanimalData.getInstance().getPendingTasksProperty().greaterThan(0)).then(-1).otherwise(1));
		// Bind the left arrow's visibility property to if there is a previous item available
		this.btnLeftArrow.visibleProperty().bind(
				this.imageTree.getSelectionModel().selectedIndexProperty()
						// -1 Would mean that nothing is selected
						.isEqualTo(-1)
						// If 0 is selected, that means we're at the top element
						.or(this.imageTree.getSelectionModel().selectedIndexProperty()
								.isEqualTo(0))
						// Make sure to negate because we want to hide the arrow when the above things are true
						.not());
		// Bind the left arrow's visibility property to if there is a next item available
		this.btnRightArrow.visibleProperty().bind(
				this.imageTree.getSelectionModel().selectedIndexProperty()
						// Expanded item count property counts the total number of entries, so the last one is count - 1. If this is selected hide the right arrow
						.isEqualTo(this.imageTree.expandedItemCountProperty()
								.subtract(1))
						// -1 Would mean that nothing is selected
						.or(this.imageTree.getSelectionModel().selectedIndexProperty()
								.isEqualTo(-1))
						// Make sure to negate because we want to hide the arrow when the above things are true
						.not());

		// The listener we will apply to each species entry list
		final ListChangeListener<SpeciesEntry> listener = change ->
		{
			// Here we use a magic number of 75. This is the height of a list cell. Unfortunately I have no other way of getting the cell height.
			// Possibly this.speciesEntryListView.lookup(".list-cell")? Or new ListCell().getHeight()? These don't seem to work right now.
			this.speciesEntryListView.setMaxHeight(this.speciesEntryListView.getItems().size() * 75);
		};
		// Make the species entry list view dynamically resize using the above listener
		this.speciesEntryListView.itemsProperty().addListener((observable, oldValue, newValue) ->
		{
			if (oldValue != null)
				// Remove the old listener
				oldValue.removeListener(listener);
			if (newValue != null)
			{
				// Add the new listener
				newValue.addListener(listener);
				// Force an on changed event to trigger a resize
				listener.onChanged(null);
			}
		});

		// When we select a new image, reset the image viewport to center and zoomed out. We also check the location that the image has selected
		this.currentlySelectedImage.addListener((observable, oldValue, newValue) ->
		{
			this.resetImageView(null);
			if (this.currentlySelectedImage.getValue() != null && currentlySelectedImage.getValue().getLocationTaken() != null)
				locationListView.getSelectionModel().select(currentlySelectedImage.getValue().getLocationTaken());
			else
				locationListView.getSelectionModel().clearSelection();
		});

		// Initialize the fade transitions

		// First create a fade-in transition for the species entry list view
		this.fadeSpeciesEntryListIn = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeSpeciesEntryListIn.setFromValue(0.9);
		this.fadeSpeciesEntryListIn.setToValue(0.4);
		this.fadeSpeciesEntryListIn.setCycleCount(1);

		// First create a fade-out transition for the species entry list view
		this.fadeSpeciesEntryListOut = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeSpeciesEntryListOut.setFromValue(0.4);
		this.fadeSpeciesEntryListOut.setToValue(0.9);
		this.fadeSpeciesEntryListOut.setCycleCount(1);

		// First create a fade-in transition for the add a new species hover
		this.fadeAddPanelIn = new FadeTransition(Duration.millis(100), this.imageAddOverlay);
		this.fadeAddPanelIn.setFromValue(0.5);
		this.fadeAddPanelIn.setToValue(0);
		this.fadeAddPanelIn.setCycleCount(1);

		// First create a fade-out transition for the add a new species hover
		this.fadeAddPanelOut = new FadeTransition(Duration.millis(100), this.imageAddOverlay);
		this.fadeAddPanelOut.setFromValue(0);
		this.fadeAddPanelOut.setToValue(0.5);
		this.fadeAddPanelOut.setCycleCount(1);

		// Create a fade-in transition for the left and right arrow
		this.fadeLeftIn = new FadeTransition(Duration.millis(100), this.btnLeftArrow);
		this.fadeLeftIn.setFromValue(0);
		this.fadeLeftIn.setToValue(1);
		this.fadeLeftIn.setCycleCount(1);
		this.fadeRightIn = new FadeTransition(Duration.millis(100), this.btnRightArrow);
		this.fadeRightIn.setFromValue(0);
		this.fadeRightIn.setToValue(1);
		this.fadeRightIn.setCycleCount(1);

		// Create a fade-out transition for the left and right arrow
		this.fadeLeftOut = new FadeTransition(Duration.millis(100), this.btnLeftArrow);
		this.fadeLeftOut.setFromValue(1);
		this.fadeLeftOut.setToValue(0);
		this.fadeLeftOut.setCycleCount(1);
		this.fadeRightOut = new FadeTransition(Duration.millis(100), this.btnRightArrow);
		this.fadeRightOut.setFromValue(1);
		this.fadeRightOut.setToValue(0);
		this.fadeRightOut.setCycleCount(1);
	}

	/**
	 * Recursively set the location of all images in the given container
	 *
	 * @param container The container to set the location of, if it's a directory recursively set the location on all its children
	 * @param location  The location to recursively set
	 */
	private void setContainerLocation(ImageContainer container, Location location)
	{
		// If it's an image, set the location taken of the image and return
		if (container instanceof ImageEntry)
			((ImageEntry) container).setLocationTaken(location);
			// If it's a directory, recursively call this function on all children
		else
			container.getChildren().forEach(child -> setContainerLocation(child, location));
	}

	/**
	 * If the new species button is clicked...
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void addNewSpecies(ActionEvent actionEvent)
	{
		// Create a new species, and request edit of the species
		Species newSpecies = new Species();
		requestEdit(newSpecies);
		// After the edit is complete, check if it's uninitialized. If it isn't, add it to the global species list
		if (!newSpecies.isUninitialized())
			SanimalData.getInstance().getSpeciesList().add(newSpecies);
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * If the edit species button is clicked...
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void editCurrentSpecies(ActionEvent actionEvent)
	{
		// Grab the selected species
		Species selected = speciesListView.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), request the edit of the species
		if (selected != null)
		{
			requestEdit(selected);
		}
		// Otherwise show an alert that no species was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Species Selected");
			alert.setContentText("Please select a species from the species list to edit.");
			alert.showAndWait();
		}
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * Create a popup that requests that the player edits the species
	 *
	 * @param species The species to edit
	 */
	private void requestEdit(Species species)
	{
		// Load the FXML file of the editor window
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("SpeciesCreator.fxml");
		// Grab the controller and set the species of that controller
		SpeciesCreatorController controller = loader.getController();
		controller.setSpecies(species);

		// Create the stage that will have the species creator/editor
		Stage dialogStage = new Stage();
		// Set the title
		dialogStage.setTitle("Species Creator/Editor");
		// Set the modality and initialize the owner to be this current window
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.imagePreview.getScene().getWindow());
		// Set the scene to the root of the FXML file
		Scene scene = new Scene(loader.getRoot());
		// Set the scene of the stage, and show it!
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	/**
	 * When the delete species is clicked we delete the selected species
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void deleteCurrentSpecies(ActionEvent actionEvent)
	{
		Species selected = speciesListView.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), delete the species
		if (selected != null)
		{
			// Grab a list of all images registered in the program
			List<ImageEntry> imageList = SanimalData.getInstance().getAllImages();
			// Count the number of images that contain the species
			Long speciesUsages = imageList
					.stream()
					.flatMap(imageEntry -> imageEntry.getSpeciesPresent()
							.stream())
					.filter(speciesEntry -> speciesEntry.getSpecies() == selected).count();

			// If no images contain the species, we're good to delete
			if (speciesUsages == 0)
			{
				SanimalData.getInstance().getSpeciesList().remove(selected);
			}
			// Otherwise prompt the user if they want to untag all images with the species
			else
			{
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.initOwner(this.imagePreview.getScene().getWindow());
				alert.setTitle("Species in Use");
				alert.setHeaderText("Species is already in use");
				alert.setContentText("This species (" + selected.getName() + ") has already been tagged in " + speciesUsages + " images.\nYes will untag all images with the species and remove it.");
				Optional<ButtonType> responseOptional = alert.showAndWait();
				responseOptional.ifPresent(response ->
				{
					// If they clicked OK
					if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE)
					{
						// Remove the species and remove each species entry that has its species set to the selected species
						SanimalData.getInstance().getSpeciesList().remove(selected);
						imageList.forEach(imageEntry -> imageEntry.getSpeciesPresent().removeIf(speciesEntry -> speciesEntry.getSpecies() == selected));
					}
				});
			}
		}
		// Otherwise show an alert that no species was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Species Selected");
			alert.setContentText("Please select a species from the species list to remove.");
			alert.showAndWait();
		}
		actionEvent.consume();
	}

	/**
	 * If the new location button is clicked...
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void addNewLocation(ActionEvent actionEvent)
	{
		// Create a new location, and request edit of the location
		Location newLocation = new Location();
		// After the edit is complete, check if it's uninitialized. If it isn't, add it to the global location list
		requestEdit(newLocation);
		if (newLocation.locationValid())
			SanimalData.getInstance().getLocationList().add(newLocation);
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * If the edit location button is clicked...
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void editCurrentLocation(ActionEvent actionEvent)
	{
		// Grab the selected location
		Location selected = locationListView.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), request the edit of the location
		if (selected != null)
		{
			requestEdit(selected);
		}
		// Otherwise show an alert that no location was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Location Selected");
			alert.setContentText("Please select a location from the location list to edit.");
			alert.showAndWait();
		}
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * Create a popup that requests that the player edits the location
	 *
	 * @param location The location to edit
	 */
	private void requestEdit(Location location)
	{
		// Load the FXML file of the editor window
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("LocationCreator.fxml");
		// Grab the controller and set the location of that controller
		LocationCreatorController controller = loader.getController();
		controller.setLocation(location);

		// Create the stage that will have the species creator/editor
		Stage dialogStage = new Stage();
		// Set the title
		dialogStage.setTitle("Location Creator/Editor");
		// Set the modality and initialize the owner to be this current window
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.imagePreview.getScene().getWindow());
		// Set the scene to the root of the FXML file
		Scene scene = new Scene(loader.getRoot());
		// Set the scene of the stage, and show it!
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	/**
	 * When the delete location is clicked we delete the selected location
	 *
	 * @param actionEvent consumed when the button is clicked
	 */
	public void deleteCurrentLocation(ActionEvent actionEvent)
	{
		Location selected = locationListView.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), request the edit of the location
		if (selected != null)
		{
			SanimalData.getInstance().getLocationList().remove(selected);
		}
		// Otherwise show an alert that no location was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Location Selected");
			alert.setContentText("Please select a location from the location list to remove.");
			alert.showAndWait();
		}
		actionEvent.consume();
	}

	/**
	 * Fired when the import images button is pressed
	 *
	 * @param actionEvent consumed when the button is pressed
	 */
	public void importImages(ActionEvent actionEvent)
	{
		// Create a directory chooser to let the user choose where to get the images from
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select Folder with Images");
		// Set the directory to be in documents
		directoryChooser.setInitialDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
		// Show the dialog
		File file = directoryChooser.showDialog(this.imagePreview.getScene().getWindow());
		// If the file chosen is a file and a directory process it
		if (file != null && file.isDirectory())
		{
			// Convert the file to a recursive image directory data structure
			ImageDirectory directory = DirectoryManager.loadDirectory(file);
			// Remove any directories that are empty and contain no images
			DirectoryManager.removeEmptyDirectories(directory);
			// Check the list of pictures to see if there's any new species that were not there before
			DirectoryManager.detectRegisterAndTagSpecies(directory);
			// Check the list of pictures to see if there's any new locations that need to be added
			DirectoryManager.detectRegisterAndTagLocations(directory);
			// Add the directory to the image tree
			SanimalData.getInstance().getImageTree().addChild(directory);
		}
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * if the delete images button is pressed
	 *
	 * @param actionEvent Button click is consumed
	 */
	public void deleteImages(ActionEvent actionEvent)
	{
		// Grab the selected item
		TreeItem<ImageContainer> item = this.imageTree.getSelectionModel().getSelectedItem();
		// Remove that item from the image tree
		SanimalData.getInstance().getImageTree().removeChildRecursive(item.getValue());
		// Make sure to clear the selection in the tree. This ensures that our left & right arrows will properly hide themselves if no more directories are present
		this.imageTree.getSelectionModel().clearSelection();
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * Reset the image view if the reset button is clicked or a new image is selected
	 *
	 * @param actionEvent consumed if an event is given, otherwise ignored
	 */
	public void resetImageView(ActionEvent actionEvent)
	{
		// Reset the sliders to their default value of 0
		this.sldBrightness.setValue(0);
		this.sldContrast.setValue(0);
		this.sldHue.setValue(0);
		this.sldSaturation.setValue(0);
		// Reset the image preview viewport to its default state
		if (this.imagePreview.getImage() != null)
			//                                                             this.imagePane.getWidth(), this.imagePane.getHeight()));
			this.imagePreview.setViewport(new Rectangle2D(0, 0, this.imagePreview.getImage().getWidth(), this.imagePreview.getImage().getHeight()));
		// Consume the event if possible
		if (actionEvent != null)
			actionEvent.consume();
	}

	/**
	 * Allow the species list to be drag & dropable onto the image view
	 *
	 * @param mouseEvent consumed if a species is selected
	 */
	public void speciesListDrag(MouseEvent mouseEvent)
	{
		// Grab the selected species, make sure it's not null
		Species selected = this.speciesListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			// Can only drag & drop if we have an image selected
			if (this.currentlySelectedImage.getValue() != null)
			{
				// Create a dragboard and begin the drag and drop
				Dragboard dragboard = this.speciesListView.startDragAndDrop(TransferMode.ANY);

				// Create a clipboard and put the species unique ID into that clipboard
				ClipboardContent content = new ClipboardContent();
				content.putString(selected.getUniqueID().toString());
				// Set the dragboard's context, and then consume the event
				dragboard.setContent(content);

				mouseEvent.consume();
			}
		}
	}

	/**
	 * If our mouse hovers over the image pane and we're dragging, we accept the transfer
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void imagePaneDragOver(DragEvent dragEvent)
	{
		// If we started dragging at the species lit view and the dragboard has a string we accept the transfer and consume the event
		if (dragEvent.getGestureSource() == this.speciesListView && dragEvent.getDragboard().hasString())
		{
			dragEvent.acceptTransferModes(TransferMode.COPY);
		}
		dragEvent.consume();
	}

	/**
	 * When the drag from the species list enters the image
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void speciesListDragEntered(DragEvent dragEvent)
	{
		// If we started dragging at the species lit view and the dragboard has a string we play the fade animation and consume the event
		if (dragEvent.getGestureSource() == this.speciesListView && dragEvent.getDragboard().hasString())
			this.fadeAddPanelOut.play();
		dragEvent.consume();
	}

	/**
	 * When the drag from the species list exits the image
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void speciesListDragExited(DragEvent dragEvent)
	{
		// If we started dragging at the species lit view and the dragboard has a string we play the fade animation and consume the event
		if (dragEvent.getGestureSource() == this.speciesListView && dragEvent.getDragboard().hasString())
			this.fadeAddPanelIn.play();
		dragEvent.consume();
	}

	/**
	 * When we drop the species onto the image, we add that species to the list
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void speciesListDragDropped(DragEvent dragEvent)
	{
		// Grab the dragboard
		Dragboard dragboard = dragEvent.getDragboard();
		// Create a flag that will be set to true if everything went well
		Boolean success = false;
		// If our dragboard has a string, grab it
		if (dragboard.hasString())
		{
			String stringID = dragboard.getString();
			// Begin a try catch to make sure we have 1 piece of drag data
			try
			{
				// Grab the id of the species
				Integer ID = Integer.parseInt(stringID);
				// Grab the species with the given ID
				Optional<Species> toAdd = SanimalData.getInstance().getSpeciesList().stream().filter(species -> species.getUniqueID().equals(ID)).findFirst();
				// Add the species to the image
				if (toAdd.isPresent())
					if (currentlySelectedImage.getValue() != null)
					{
						currentlySelectedImage.getValue().addSpecies(toAdd.get(), 1);
						success = true;
					}
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		// Set the success equal to the flag, and consume the event
		dragEvent.setDropCompleted(success);
		dragEvent.consume();
	}

	/**
	 * When the user moves their mouse over the image show the left & right arrows
	 *
	 * @param mouseEvent ignored
	 */
	public void imagePaneMouseEntered(MouseEvent mouseEvent)
	{
		this.fadeLeftIn.play();
		this.fadeRightIn.play();
	}

	/**
	 * When the user moves their mouse over the image hide the left & right arrows
	 *
	 * @param mouseEvent ignored
	 */
	public void imagePaneMouseExited(MouseEvent mouseEvent)
	{
		this.fadeLeftOut.play();
		this.fadeRightOut.play();
	}

	/**
	 * When we move our mouse over the species entry list we play a fade animation
	 *
	 * @param mouseEvent ignored
	 */
	public void onMouseEnteredSpeciesEntryList(MouseEvent mouseEvent)
	{
		fadeSpeciesEntryListOut.play();
	}

	/**
	 * When we move our mouse away from the species entry list we play a fade animation
	 *
	 * @param mouseEvent ignored
	 */
	public void onMouseExitedSpeciesEntryList(MouseEvent mouseEvent)
	{
		fadeSpeciesEntryListIn.play();
	}

	/**
	 * When we click the left arrow we want to advance the picture to the next untagged image
	 *
	 * @param actionEvent ignored
	 */
	public void onLeftArrowClicked(ActionEvent actionEvent)
	{
		this.imageTree.getSelectionModel().selectPrevious();
	}

	/**
	 * When we click the right arrow we want to advance the picture to the next untagged image
	 *
	 * @param actionEvent ignored
	 */
	public void onRightArrowClicked(ActionEvent actionEvent)
	{
		this.imageTree.getSelectionModel().selectNext();
	}

	///
	/// Everything after this point allows the user to scroll the image view. The library used was found here:
	/// https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	///

	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	public void onImagePressed(MouseEvent mouseEvent)
	{
		mouseDown.set(imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY())));
	}

	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	public void onImageDragged(MouseEvent mouseEvent)
	{
		Point2D dragPoint = imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY()));
		shift(this.imagePreview, dragPoint.subtract(mouseDown.get()));
		mouseDown.set(imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY())));
	}

	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	public void onImageClicked(MouseEvent mouseEvent)
	{
		if (mouseEvent.getClickCount() >= 2)
			this.resetImageView(null);
	}

	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	public void onImageScroll(ScrollEvent scrollEvent)
	{
		double delta = -scrollEvent.getDeltaY();
		Rectangle2D viewport = this.imagePreview.getViewport();

		double scale = clamp(Math.pow(1.01, delta),

				// don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
				Math.min(10 / viewport.getWidth(), 10 / viewport.getHeight()),

				// don't scale so that we're bigger than image dimensions:
				Math.max(this.imagePreview.getImage().getWidth() / viewport.getWidth(), this.imagePreview.getImage().getHeight() / viewport.getHeight())

		);

		Point2D mouse = imageViewToImage(imagePreview, new Point2D(scrollEvent.getX(), scrollEvent.getY()));

		double newWidth = viewport.getWidth() * scale;
		double newHeight = viewport.getHeight() * scale;

		// To keep the visual point under the mouse from moving, we need
		// (x - newViewportMinX) / (x - currentViewportMinX) = scale
		// where x is the mouse X coordinate in the image

		// solving this for newViewportMinX gives

		// newViewportMinX = x - (x - currentViewportMinX) * scale

		// we then clamp this value so the image never scrolls out
		// of the imageview:

		double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
				0, this.imagePreview.getImage().getWidth() - newWidth);
		double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
				0, this.imagePreview.getImage().getHeight() - newHeight);

		imagePreview.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
	}

	// convert mouse coordinates in the imageView to coordinates in the actual image:
	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates)
	{
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

		Rectangle2D viewport = imageView.getViewport();
		return new Point2D(
				viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	private void shift(ImageView imageView, Point2D delta)
	{
		Rectangle2D viewport = imageView.getViewport();

		double width = imageView.getImage().getWidth();
		double height = imageView.getImage().getHeight();

		double maxX = width - viewport.getWidth();
		double maxY = height - viewport.getHeight();

		double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

		imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
	}

	// Found here: https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	private double clamp(double value, double min, double max)
	{

		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}
}
