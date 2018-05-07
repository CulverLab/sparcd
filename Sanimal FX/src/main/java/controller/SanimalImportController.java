package controller;

import controller.importView.LocationCreatorController;
import controller.importView.SpeciesCreatorController;
import controller.importView.SpeciesListEntryController;
import controller.importView.TimeShiftController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import library.ImageViewPane;
import library.TreeViewAutomatic;
import model.SanimalData;
import model.constant.SanimalDataFormats;
import model.image.*;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import model.threading.ErrorTask;
import model.util.FXMLLoaderUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.StatusBar;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

	// Button used to shift the time
	@FXML
	public Button btnTimeShift;

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

	// The button to begin importing images
	@FXML
	public Button btnImportImages;
	// The button to delete imported images
	@FXML
	public Button btnDelete;

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

	// Search field for species entry list
	@FXML
	public TextField txtSpeciesSearch;
	// Reset button for the search field
	@FXML
	public Button btnResetSearch;

	// The main pane holding everything
	@FXML
	public SplitPane mainPane;

	// The image pane used to do the species preview operation
	@FXML
	public StackPane speciesPreviewPane;
	@FXML
	public ImageView imageSpeciesPreview;

	// Top right label containing location name
	@FXML
	public Label lblLocation;
	// Top right hbox containing location info
	@FXML
	public HBox hbxLocation;

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
	private FadeTransition fadeLocationIn;
	private FadeTransition fadeLocationOut;
	private FadeTransition fadeAddPanelIn;
	private FadeTransition fadeAddPanelOut;
	private FadeTransition fadeLeftIn;
	private FadeTransition fadeLeftOut;
	private FadeTransition fadeRightIn;
	private FadeTransition fadeRightOut;
	// A property used to process the image scrolling
	private ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

	// The current image being previewed
	private ObjectProperty<Image> speciesPreviewImage = new SimpleObjectProperty<>(null);

	// The stage containing the time shift controller used to shift dates around
	private Stage timeShiftStage;
	private TimeShiftController timeShiftController;

	/**
	 * Initialize the sanimal import view and data bindings
	 *
	 * @param ignored   ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL ignored, ResourceBundle resources)
	{
		// First we setup the species list

		// Grab the global species list
		SortedList<Species> species = new SortedList<>(SanimalData.getInstance().getSpeciesList());
		// We set the comparator to be the name of the species
		species.setComparator(Comparator.comparing(Species::getName));
		// We create a local wrapper of the species list to filter
		FilteredList<Species> speciesFilteredList = new FilteredList<>(species);
		// Set the filter to update whenever the species search text changes
		speciesFilteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> (speciesToFilter ->
			// Allow any species with a name or scientific name containing the species search text
			(StringUtils.containsIgnoreCase(speciesToFilter.getName(), this.txtSpeciesSearch.getCharacters()) ||
					StringUtils.containsIgnoreCase(speciesToFilter.getScientificName(), this.txtSpeciesSearch.getCharacters()))), this.txtSpeciesSearch.textProperty()));
		// Set the items of the species list view to the newly sorted list
		this.speciesListView.setItems(speciesFilteredList);
		// Set the cell factory to be our custom species list cell
		this.speciesListView.setCellFactory(x -> {
			SpeciesListEntryController controller = FXMLLoaderUtils.loadFXML("importView/SpeciesListEntry.fxml").getController();
			controller.setCurrentImagePreview(this.speciesPreviewImage);
			return controller;
		});
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
		this.locationListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("importView/LocationListEntry.fxml").getController());
		// When we double click the location list view items, we want to edit the location
		this.locationListView.setOnMouseClicked(event -> {
			if (event.getClickCount() >= 2 && this.locationListView.getSelectionModel().getSelectedItem() != null)
				this.requestEdit(this.locationListView.getSelectionModel().getSelectedItem());
		});

		// Setup the species entry list view

		// The species entry list view just needs to have a cell factory
		this.speciesEntryListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("importView/SpeciesEntryListEntry.fxml").getController());

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
		// Setup the image tree cells so that when they get drag & dropped the species & locations can be tagged
		this.imageTree.setCellFactory(x -> FXMLLoaderUtils.loadFXML("importView/ImageTreeCell.fxml").getController());
		// If we select a node that's being uploaded clear the selection
		this.imageTree.setOnKeyPressed(event ->
		{
			// If we're moving up or down on the tree, ensure we're not entering a disabled node
			if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN)
			{
				// Grab the selected tree node
				TreeItem<ImageContainer> selectedItem = this.imageTree.getSelectionModel().getSelectedItem();
				if (selectedItem != null)
				{
					// Grab the next node
					TreeItem<ImageContainer> next = event.getCode() == KeyCode.UP ? selectedItem.previousSibling() : selectedItem.nextSibling();
					// Make sure the next node has a value
					if (next != null && next.getValue() != null)
					{
						// Grab the next tree entries image container
						ImageContainer value = next.getValue();
						// If the image container is a directory being uploaded consome the key press
						if (value instanceof ImageDirectory && ((ImageDirectory) value).getUploadProgress() != -1)
						{
							event.consume();
						}
					}
				}
			}
		});

		// When a new image is selected... we perform a bunch of actions below
		MonadicBinding<ImageContainer> selectedImage = EasyBind.monadic(this.imageTree.getSelectionModel().selectedItemProperty()).map(TreeItem::getValue);
		// Clear the preview pane if there is a preview'd image
		selectedImage.addListener((observable, oldValue, newValue) -> this.speciesPreviewImage.setValue(null));
		// Update the currently selected image and directory
		currentlySelectedImage.bind(selectedImage.map(imageContainer -> (imageContainer instanceof ImageEntry) ? (ImageEntry) imageContainer : null));
		currentlySelectedDirectory.bind(selectedImage.map(imageContainer -> (imageContainer instanceof ImageDirectory) ? (ImageDirectory) imageContainer : null));

		// When we select a cloud image or directory, don't allow clicking delete
		this.btnDelete.disableProperty().bind(
				Bindings.or(Bindings.createBooleanBinding(() -> this.currentlySelectedImage.getValue() instanceof CloudImageEntry, this.currentlySelectedImage),
				Bindings.or(Bindings.createBooleanBinding(() -> this.currentlySelectedDirectory.getValue() instanceof CloudImageDirectory, this.currentlySelectedDirectory),
							this.imageTree.getSelectionModel().selectedIndexProperty().isEqualTo(-1))));

		// Create bindings in the GUI

		// First bind the 4 color adjustment sliders disable property to if an adjustable image is selected
		this.sldSaturation.disableProperty().bind(currentlySelectedImage.isNull());
		this.sldHue.disableProperty().bind(currentlySelectedImage.isNull());
		this.sldContrast.disableProperty().bind(currentlySelectedImage.isNull());
		this.sldBrightness.disableProperty().bind(currentlySelectedImage.isNull());
		// Also bind the date taken text field's disable property if an image is selected
		this.txtDateTaken.disableProperty().bind(currentlySelectedImage.isNull());
		// Also bind the disable button's disable property if an adjustable image is selected
		this.btnResetImage.disableProperty().bind(currentlySelectedImage.isNull());
		// Finally bind the date taken's disable property if an adjustable image is selected
		this.txtDateTaken.textProperty().bind(EasyBind.monadic(currentlySelectedImage).selectProperty(ImageEntry::dateTakenProperty).map(localDateTime -> SanimalData.getInstance().getSettings().formatDateTime(localDateTime, " at ")).orElse(""));
		// Bind the image preview to the selected image from the right side tree view
		this.imagePreview.imageProperty().bind(EasyBind.monadic(currentlySelectedImage).selectProperty(ImageEntry::getFileProperty).map(file -> new Image(file.toURI().toString(), SanimalData.getInstance().getSettings().getBackgroundImageLoading())));
		this.imagePreview.imageProperty().addListener((observable, oldValue, newValue) -> this.resetImageView(null));
		// Bind the species entry list view items to the selected image species present
		this.speciesEntryListView.itemsProperty().bind(EasyBind.monadic(currentlySelectedImage).map(ImageEntry::getSpeciesPresent));
		// Bind the species entry location name to the selected image's location
		this.lblLocation.textProperty().bind(EasyBind.monadic(currentlySelectedImage).selectProperty(ImageEntry::locationTakenProperty).map(Location::getName));
		// Hide the location panel when no location is selected
		this.hbxLocation.visibleProperty().bind(EasyBind.monadic(currentlySelectedImage).selectProperty(ImageEntry::locationTakenProperty).map(location -> true).orElse(false));
		// Hide the progress bar when no tasks remain
		this.sbrTaskProgress.visibleProperty().bind(SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().taskRunningProperty());
		// Bind the progress bar's text property to tasks remaining
		this.sbrTaskProgress.textProperty().bind(SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().messageProperty());
		// Bind the progress bar's progress property to the current task's progress
		this.sbrTaskProgress.progressProperty().bind(SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().progressProperty());
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
		// When we preview an image, bind the image property to the image view
		this.imageSpeciesPreview.imageProperty().bind(this.speciesPreviewImage);
		// When we get a new species to preview, we show the preview pane
		this.speciesPreviewPane.visibleProperty().bind(this.speciesPreviewImage.isNotNull());
		// If we have a folder or image selected, allow time shifting
		this.btnTimeShift.disableProperty().bind(this.currentlySelectedImage.isNull().and(this.currentlySelectedDirectory.isNull()));

		// The listener we will apply to each species entry list
		// Here we use a magic number of 75. This is the height of a list cell. Unfortunately I have no other way of getting the cell height.
		// Possibly this.speciesEntryListView.lookup(".list-cell")? Or new ListCell().getHeight()? These don't seem to work right now.
		final ListChangeListener<SpeciesEntry> listener = change -> this.speciesEntryListView.setMaxHeight(this.speciesEntryListView.getItems() == null ? 0 : this.speciesEntryListView.getItems().size() * 75);

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

		this.currentlySelectedImage.addListener((observable, oldValue, newValue) ->
		{
			// When we select a new image, reset the image viewport to center and zoomed out.
			this.resetImageView(null);
			// We also make sure to pull the image from online if it's a cloud based image
			if (newValue instanceof CloudImageEntry) ((CloudImageEntry) newValue).pullFromCloudIfNotPulled();
		});

		// When we press a key, we want to add the bound species to the species entry
		this.mainPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			// If we have a selected image
			if (this.currentlySelectedImage.getValue() != null)
			{
				// We don't want to trigger keybindings if we're typing into the search box
				if (!this.txtSpeciesSearch.isFocused())
				{
					// Filter the species list by correctly key-bound species, and add them to the current image
					SanimalData.getInstance().getSpeciesList().filtered(boundSpecies -> boundSpecies.getKeyBinding() == event.getCode()).forEach(boundSpecies ->
					{
						this.currentlySelectedImage.getValue().addSpecies(boundSpecies, 1);
						// Automatically select the next image in the image list view if the option is selected
						if (SanimalData.getInstance().getSettings().getAutomaticNextImage())
							this.imageTree.getSelectionModel().selectNext();
					});
					event.consume();
				}
			}
		});

		// Initialize the time shift controller stage

		// Load the FXML file of the editor window
		FXMLLoader timeShiftLoader = FXMLLoaderUtils.loadFXML("importView/TimeShift.fxml");
		// Grab the controller and set the location of that controller
		this.timeShiftController = timeShiftLoader.getController();

		// Create the stage that will have the date editor
		this.timeShiftStage = new Stage();
		// Set the title
		timeShiftStage.setTitle("Date Editor");
		// Set the modality and initialize the owner to be this current window
		timeShiftStage.initModality(Modality.WINDOW_MODAL);
		// Make sure the window is the right size and can't be resized
		timeShiftStage.setResizable(false);
		timeShiftStage.setWidth(950);
		// Set the scene to the root of the FXML file
		Scene timeShiftScene = new Scene(timeShiftLoader.getRoot());
		// Set the scene of the stage, and show it!
		timeShiftStage.setScene(timeShiftScene);

		// Initialize the fade transitions

		// First create a fade-in transition for the species entry list view
		this.fadeSpeciesEntryListIn = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeSpeciesEntryListIn.setFromValue(1);
		this.fadeSpeciesEntryListIn.setToValue(0.4);
		this.fadeSpeciesEntryListIn.setCycleCount(1);

		// First create a fade-out transition for the species entry list view
		this.fadeSpeciesEntryListOut = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeSpeciesEntryListOut.setFromValue(0.4);
		this.fadeSpeciesEntryListOut.setToValue(1);
		this.fadeSpeciesEntryListOut.setCycleCount(1);

		// First create a fade-in transition for the location
		this.fadeLocationIn = new FadeTransition(Duration.millis(100), this.hbxLocation);
		this.fadeLocationIn.setFromValue(1);
		this.fadeLocationIn.setToValue(0.4);
		this.fadeLocationIn.setCycleCount(1);

		// Then create a fade-out transition for the location
		this.fadeLocationOut = new FadeTransition(Duration.millis(100), this.hbxLocation);
		this.fadeLocationOut.setFromValue(0.4);
		this.fadeLocationOut.setToValue(1);
		this.fadeLocationOut.setCycleCount(1);

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

		// Force play all fade ins to start
		this.fadeLocationIn.play();
		this.fadeAddPanelIn.play();
		this.fadeLeftIn.play();
		this.fadeRightIn.play();
		this.fadeSpeciesEntryListIn.play();
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
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("importView/SpeciesCreator.fxml");
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
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("importView/LocationCreator.fxml");
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
			// Grab a list of all images registered in the program
			List<ImageEntry> imageList = SanimalData.getInstance().getAllImages();
			Long locationUsages = imageList
					.stream()
					.filter(imageEntry -> imageEntry.getLocationTaken() == selected).count();

			// If no images contain the location, we're good to delete
			if (locationUsages == 0)
			{
				SanimalData.getInstance().getLocationList().remove(selected);
			}
			// Otherwise prompt the user if they want to untag all images with the location
			else
			{
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.initOwner(this.imagePreview.getScene().getWindow());
				alert.setTitle("Location in Use");
				alert.setHeaderText("Location is already in use");
				alert.setContentText("This location (" + selected.getName() + ") has already been tagged in " + locationUsages + " images.\nYes will untag all images with the location and remove it.");
				Optional<ButtonType> responseOptional = alert.showAndWait();
				responseOptional.ifPresent(response ->
				{
					// If they clicked OK
					if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE)
					{
						// Remove the location and remove each image that has its location set to the selected location
						SanimalData.getInstance().getLocationList().remove(selected);
						imageList.stream().filter(imageEntry -> imageEntry.getLocationTaken() == selected).forEach(imageEntry -> imageEntry.setLocationTaken(null));
					}
				});
			}
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
		// Test if the images should be read as legacy
		Boolean readAsLegacy = false;
		// If Dr. Sanderson's compatibility is enabled, ask
		if (SanimalData.getInstance().getSettings().getDrSandersonDirectoryCompatibility())
		{
			// Ask if the data is legacy
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Legacy Data?");
			alert.setHeaderText("Are you importing legacy data?");
			alert.setContentText("Would you like the directory to be read as legacy data used by Dr. Sanderson's 'Data Analyze' program?");

			// 4 buttons, Yes, No, No don't ask again, Cancel
			ButtonType yes = new ButtonType("Yes, Auto-Tag it");
			ButtonType no = new ButtonType("No");
			ButtonType noDontAsk = new ButtonType("No, don't ask again");

			// Set the button types
			alert.getButtonTypes().setAll(yes, no, noDontAsk, ButtonType.CANCEL);

			// Test for the result of the alert shown
			Optional<ButtonType> result = alert.showAndWait();

			// If cancel is pressed, return
			if (!result.isPresent() || result.get() == ButtonType.CANCEL)
			{
				return;
			}
			// If no is pressed, we are not reading as legacy
			else if (result.get() == no)
			{
				readAsLegacy = false;
			}
			// If yes is pressed, read the data as legacy
			else if (result.get() == yes)
			{
				readAsLegacy = true;
			}
			// If no is pressed, we are not reading as legacy, and update the setting
			else if (result.get() == noDontAsk)
			{
				readAsLegacy = false;
				SanimalData.getInstance().getSettings().setDrSandersonDirectoryCompatibility(false);
			}
		}

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
			this.btnImportImages.setDisable(true);
			Task<ImageDirectory> importTask = new ErrorTask<ImageDirectory>()
			{
				@Override
				protected ImageDirectory call()
				{
					final Long MAX_WORK = 6L;

					this.updateProgress(1, MAX_WORK);
					this.updateMessage("Loading directory...");

					// Grab the current list of species and locations and duplicate it
					List<Species> currentSpecies = new ArrayList<>(SanimalData.getInstance().getSpeciesList());
					List<Location> currentLocations = new ArrayList<>(SanimalData.getInstance().getLocationList());

					// Convert the file to a recursive image directory data structure
					ImageDirectory directory = DirectoryManager.loadDirectory(file, currentLocations, currentSpecies);

					this.updateProgress(2, MAX_WORK);
					this.updateMessage("Removing empty directories...");

					// Remove any directories that are empty and contain no images
					DirectoryManager.removeEmptyDirectories(directory);

					this.updateProgress(3, MAX_WORK);
					this.updateMessage("Detecting species in images...");

					// Diff the new species list and the old one to see if we have new species
					List<Species> newSpecies = ListUtils.subtract(currentSpecies, SanimalData.getInstance().getSpeciesList());
					// If we have new species, show an alert
					if (!newSpecies.isEmpty())
					{
						Platform.runLater(() ->
						{
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.initOwner(SanimalImportController.this.imagePreview.getScene().getWindow());
							alert.setTitle("Species Added");
							alert.setHeaderText("New Species Were Added");
							alert.setContentText("Species found tagged on these images were automatically added to the list.\nThese include: " + newSpecies.stream().map(Species::getName).collect(Collectors.joining(", ")));
							alert.show();
							SanimalData.getInstance().getSpeciesList().addAll(newSpecies);
						});
					}

					this.updateProgress(4, MAX_WORK);
					this.updateMessage("Detecting locations in images...");

					// Diff the new locations list and the old one to see if we have new locations
					List<Location> newLocations = ListUtils.subtract(currentLocations, SanimalData.getInstance().getLocationList());
					// If we have new locations, show an alert
					if (!newLocations.isEmpty())
					{
						Platform.runLater(() ->
						{
							Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
							alert.initOwner(SanimalImportController.this.imagePreview.getScene().getWindow());
							alert.setTitle("Locations Added");
							alert.setHeaderText("New Locations Were Added");
							alert.setContentText("Locations found tagged on these images were automatically added to the list.\nThese include: " + newLocations.stream().map(Location::getName).collect(Collectors.joining(", ")));
							alert.show();
							SanimalData.getInstance().getLocationList().addAll(newLocations);
						});
					}

					this.updateProgress(5, MAX_WORK);
					this.updateMessage("Adding images to the visual tree...");

					this.updateProgress(6, MAX_WORK);
					this.updateMessage("Finished!");

					return directory;
				}
			};
			Boolean finalReadAsLegacy = readAsLegacy;
			importTask.setOnSucceeded(event ->
			{
				// If we're reading non-legacy data, we're done
				if (!finalReadAsLegacy)
				{
					// Add the directory to the image tree
					SanimalData.getInstance().getImageTree().addChild(importTask.getValue());
					this.btnImportImages.setDisable(false);
				}
				// If we're reading legacy data, start a new task to read it
				else
				{
					final ImageDirectory directory = importTask.getValue();
					// If we're reading legacy data, sync legacy data
					Task<Pair<List<Species>, List<Location>>> legacySyncTask = new ErrorTask<Pair<List<Species>, List<Location>>>()
					{
						@Override
						protected Pair<List<Species>, List<Location>> call()
						{
							this.updateProgress(0, 2);
							this.updateMessage("Duplicating the species and location list temporarily...");

							// Grab the current list of species and locations and duplicate it
							List<Species> currentSpecies = new ArrayList<>(SanimalData.getInstance().getSpeciesList());
							List<Location> currentLocations = new ArrayList<>(SanimalData.getInstance().getLocationList());

							this.updateProgress(1, 2);
							this.updateMessage("Reading Dr. Sanderson's Legacy Format");

							// parse dr. sanderson's format
							DirectoryManager.parseLegacyDirectory(directory, currentLocations, currentSpecies);

							this.updateProgress(2, 2);
							this.updateMessage("Finished parsing Dr. Sanderson's Legacy data");

							// A hack to return 2 values...
							return new Pair<>(currentSpecies, currentLocations);
						}
					};

					// If we finished reading legacy data, allow importing images
					legacySyncTask.setOnSucceeded(event2 ->
					{
						// Some locations may not be initialized due to Dr. Sanderson's format so we ask the user to fix them for us
						List<Species> newSpecies = ListUtils.subtract(legacySyncTask.getValue().getKey(), SanimalData.getInstance().getSpeciesList());
						List<Location> newLocations = ListUtils.subtract(legacySyncTask.getValue().getValue(), SanimalData.getInstance().getLocationList());

						// If the species list is not empty, show a popup
						if (!newSpecies.isEmpty())
						{
							SanimalData.getInstance().getErrorDisplay().showPopup(
									Alert.AlertType.INFORMATION,
									SanimalImportController.this.mainPane.getScene().getWindow(),
									"New Species",
									"New Species need to be added",
									newSpecies.size() + " new species were found on the images that were not registered yet. Add any additional species information now.",
									true);

							// Request the edit of each species, because they may not be valid yet
							for (Species species : newSpecies)
								requestEdit(species);

							// Add all new species
							SanimalData.getInstance().getSpeciesList().addAll(newSpecies);
						}

						// If the locations list is not empty, show a popup
						if (!newLocations.isEmpty())
						{
							SanimalData.getInstance().getErrorDisplay().showPopup(
									Alert.AlertType.INFORMATION,
									SanimalImportController.this.mainPane.getScene().getWindow(),
									"New Locations",
									"New Locations need to be added",
									newLocations.size() + " new locations were found on the images that were not registered yet. Please add location latitude/longitude/elevation.",
									true);

							// Request the edit of each locations, because they may not be valid yet
							for (Location location : newLocations)
								requestEdit(location);

							// Add all new locations
							SanimalData.getInstance().getLocationList().addAll(newLocations);
						}

						// Add the directory to the image tree
						SanimalData.getInstance().getImageTree().addChild(directory);
						this.btnImportImages.setDisable(false);
					});

					SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().addTask(legacySyncTask);
				}
			});

			SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().addTask(importTask);
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
		{
			double imageWidth = this.imagePreview.getImage().getWidth();
			double imageHeight = this.imagePreview.getImage().getHeight();
			this.imagePreview.setViewport(new Rectangle2D(0, 0, imageWidth, imageHeight));
		}
		// Consume the event if possible
		if (actionEvent != null)
			actionEvent.consume();
	}

	/**
	 * Shifts the time stamp of the currently selected images
	 * @param mouseEvent consumed
	 */
	public void timeShift(MouseEvent mouseEvent)
	{
		// Grab the first date in the image or image list
		LocalDateTime first = null;
		// If we have an image selected, grab the date of that one image
		if (this.currentlySelectedImage.getValue() != null)
			first = this.currentlySelectedImage.getValue().getDateTaken();
		// If we have a directory selected, grab the date taken of the first image
		else if (this.currentlySelectedDirectory.getValue() != null)
		{
			// Grab the first image's date
			Optional<ImageEntry> firstImage = this.currentlySelectedDirectory.getValue().flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).findFirst();
			if (firstImage.isPresent())
				first = firstImage.get().getDateTaken();
		}

		// If either a date from the directory or image was detected, process it
		if (first != null)
		{
			timeShiftController.setDate(first);
			if (timeShiftStage.getOwner() == null)
				timeShiftStage.initOwner(this.imagePreview.getScene().getWindow());
			timeShiftStage.showAndWait();

			// Grab the new date from the dialog stage
			// If a new date was created...
			if (timeShiftController.dateWasConfirmed())
			{
				LocalDateTime newDate = timeShiftController.getDate();
				// If just an image was selected, set the date taken of that specific image
				if (this.currentlySelectedImage.getValue() != null)
					this.currentlySelectedImage.getValue().setDateTaken(newDate);
				// If a directory was selected...
				else if (this.currentlySelectedDirectory.getValue() != null)
				{
					// Calculate the time between the first date and the newly created date
					long timeBetween = ChronoUnit.MILLIS.between(first, newDate);
					// If the offset is non 0, offset the date of every image in the directory by the offset
					if (timeBetween != 0)
						this.currentlySelectedDirectory.getValue().flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).forEach(imageEntry -> {
							imageEntry.setDateTaken(imageEntry.getDateTaken().plus(timeBetween, ChronoUnit.MILLIS));
						});
				}
			}
		}

		mouseEvent.consume();
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
			// Create a dragboard and begin the drag and drop
			Dragboard dragboard = this.speciesListView.startDragAndDrop(TransferMode.ANY);

			// Create a clipboard and put the species unique ID into that clipboard
			ClipboardContent content = new ClipboardContent();
			content.put(SanimalDataFormats.SPECIES_NAME_FORMAT, selected.getName());
			content.put(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT, selected.getScientificName());
			// Set the dragboard's context, and then consume the event
			dragboard.setContent(content);

			mouseEvent.consume();
		}
	}

	/**
	 * Allow the location list to be drag & dropable onto the image view
	 *
	 * @param mouseEvent consumed if a location is selected
	 */
	public void locationListDrag(MouseEvent mouseEvent)
	{
		// Grab the selected location, make sure it's not null
		Location selected = this.locationListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			// Create a dragboard and begin the drag and drop
			Dragboard dragboard = this.locationListView.startDragAndDrop(TransferMode.ANY);

			// Create a clipboard and put the location unique ID into that clipboard
			ClipboardContent content = new ClipboardContent();
			content.put(SanimalDataFormats.LOCATION_NAME_FORMAT, selected.getName());
			content.put(SanimalDataFormats.LOCATION_ID_FORMAT, selected.getId());
			// Set the dragboard's context, and then consume the event
			dragboard.setContent(content);

			mouseEvent.consume();
		}
	}

	/**
	 * If our mouse hovers over the image pane and we're dragging, we accept the transfer
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void imagePaneDragOver(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(SanimalDataFormats.LOCATION_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.LOCATION_ID_FORMAT)) || (dragboard.hasContent(SanimalDataFormats.SPECIES_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT) && this.currentlySelectedImage.getValue() != null))
			dragEvent.acceptTransferModes(TransferMode.COPY);
		dragEvent.consume();
	}

	/**
	 * When the drag from the species or location list enters the image
	 *
	 * @param dragEvent The event that means we are dragging over the image pane
	 */
	public void imagePaneDragEntered(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(SanimalDataFormats.LOCATION_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.LOCATION_ID_FORMAT)) || (dragboard.hasContent(SanimalDataFormats.SPECIES_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT) && this.currentlySelectedImage.getValue() != null))
			this.fadeAddPanelOut.play();
		dragEvent.consume();
	}

	/**
	 * When the drag from the species or location list exits the image
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void imagePaneDragExited(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		// If we started dragging at the species or location view and the dragboard has a string we play the fade animation and consume the event
		if ((dragboard.hasContent(SanimalDataFormats.LOCATION_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.LOCATION_ID_FORMAT)) || (dragboard.hasContent(SanimalDataFormats.SPECIES_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT) && this.currentlySelectedImage.getValue() != null))
			this.fadeAddPanelIn.play();
		dragEvent.consume();
	}

	/**
	 * When we drop the species or location onto the image, we add that species or location to the list
	 *
	 * @param dragEvent The event that means we are dragging away from the image pane
	 */
	public void imagePaneDragDropped(DragEvent dragEvent)
	{
		// Create a flag that will be set to true if everything went well
		Boolean success = false;
		// Grab the dragboard
		Dragboard dragboard = dragEvent.getDragboard();
		// If our dragboard has a string we have data which we need
		if (dragboard.hasContent(SanimalDataFormats.SPECIES_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT) && this.currentlySelectedImage.getValue() != null)
		{
			String commonName = (String) dragboard.getContent(SanimalDataFormats.SPECIES_NAME_FORMAT);
			String scientificName = (String) dragboard.getContent(SanimalDataFormats.SPECIES_SCIENTIFIC_NAME_FORMAT);
			// Grab the species with the given ID
			Optional<Species> toAdd = SanimalData.getInstance().getSpeciesList().stream().filter(species -> species.getScientificName().equals(scientificName) && species.getName().equals(commonName)).findFirst();
			// Add the species to the image
			if (toAdd.isPresent())
				if (currentlySelectedImage.getValue() != null)
				{
					currentlySelectedImage.getValue().addSpecies(toAdd.get(), 1);
					// Automatically select the next image in the image list view if the option is selected
					if (SanimalData.getInstance().getSettings().getAutomaticNextImage())
						this.imageTree.getSelectionModel().selectNext();
					// We request focus after a drag and drop so that arrow keys will continue to move the selected image down or up
					this.imageTree.requestFocus();
					success = true;
				}
		}
		else if (dragboard.hasContent(SanimalDataFormats.LOCATION_NAME_FORMAT) && dragboard.hasContent(SanimalDataFormats.LOCATION_ID_FORMAT))
		{
			String locationName = (String) dragboard.getContent(SanimalDataFormats.LOCATION_NAME_FORMAT);
			String locationId = (String) dragboard.getContent(SanimalDataFormats.LOCATION_ID_FORMAT);
			// Grab the species with the given ID
			Optional<Location> toAdd = SanimalData.getInstance().getLocationList().stream().filter(location -> location.getName().equals(locationName) && location.getId().equals(locationId)).findFirst();
			// Add the species to the image
			if (toAdd.isPresent())
				// Check if we have a selected image or directory to update!
				if (currentlySelectedImage.getValue() != null)
				{
					currentlySelectedImage.getValue().setLocationTaken(toAdd.get());
					// We request focus after a drag and drop so that arrow keys will continue to move the selected image down or up
					this.imageTree.requestFocus();
					success = true;
				}
				else if (currentlySelectedDirectory.getValue() != null)
				{
					currentlySelectedDirectory.getValue().setLocationTaken(toAdd.get());
					// We request focus after a drag and drop so that arrow keys will continue to move the selected image down or up
					this.imageTree.requestFocus();
					success = true;
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
	 * When we move our mouse over the location we play a fade animation
	 *
	 * @param mouseEvent ignored
	 */
	public void onMouseEnteredLocation(MouseEvent mouseEvent)
	{
		fadeLocationOut.play();
	}

	/**
	 * When we move our mouse away from the location we play a fade animation
	 *
	 * @param mouseEvent ignored
	 */
	public void onMouseExitedLocation(MouseEvent mouseEvent)
	{
		fadeLocationIn.play();
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

	/**
	 * When we click the X button we want to reset the species search box
	 *
	 * @param actionEvent ignored
	 */
	public void resetSpeciesSearch(ActionEvent actionEvent)
	{
		this.txtSpeciesSearch.clear();
	}

	/**
	 * When we click the species clear preview button in the top right
	 *
	 * @param actionEvent ignored
	 */
	public void clearSpeciesPreview(ActionEvent actionEvent)
	{
		this.speciesPreviewImage.setValue(null);
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
