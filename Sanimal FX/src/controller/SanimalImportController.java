package controller;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import model.image.ImageImporter;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URL;
import java.util.Comparator;
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

	// The list view containing the species
	@FXML
	private ListView<Species> speciesListView;

	///
	/// FXML bound fields end
	///

	// The color adjust property is used to adjust the image preview's color FX
	private ObjectProperty<ColorAdjust> colorAdjust = new SimpleObjectProperty<ColorAdjust>(new ColorAdjust());

	// Fields to hold the currently selected image entry and image directory
	private ObjectProperty<ImageEntry> currentlySelectedImage = new SimpleObjectProperty<ImageEntry>(null);
	private ObjectProperty<ImageDirectory> currentlySelectedDirectory = new SimpleObjectProperty<ImageDirectory>(null);
	// Use fade transitions to fade the species list in and out
	private FadeTransition fadeSpeciesEntryListIn;
	private FadeTransition fadeSpeciesEntryListOut;
	private FadeTransition fadeAddPanelIn;
	private FadeTransition fadeAddPanelOut;
	// A property used to process the image scrolling
	private ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<Point2D>();

	/**
	 * Initialize the sanimal import view and data bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// First we setup the species list

		// Grab the global species list
		SortedList<Species> species = new SortedList<Species>(SanimalData.getInstance().getSpeciesList());
		// We set the comparator to be the name of the species
		species.setComparator(Comparator.comparing(Species::getName));
		// Set the items of the species list view to the newly sorted list
		this.speciesListView.setItems(species);
		// Set the cell factory to be our custom species list cell
		this.speciesListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("SpeciesListEntry.fxml").getController());

		// Then we setup the locations list in a similar manner

		// Grab the global location list
		SortedList<Location> locations = new SortedList<Location>(SanimalData.getInstance().getLocationList());
		// Set the comparator to be the name of the location
		locations.setComparator(Comparator.comparing(Location::getName));
		// Set the items of the location list view to the newly sorted list
		this.locationListView.setItems(locations);
		// Set the cell factory to be our custom location list cell
		this.locationListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("LocationListEntry.fxml").getController());
		// When we select a location, we need to set the location on the selected image or directory of images
		this.locationListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
			// Make sure we got a new value
			if (newValue != null)
				// Check if we have a selected image or directory to update!
				if (currentlySelectedImage.getValue() != null)
					currentlySelectedImage.getValue().setLocationTaken(newValue);
				else if (currentlySelectedDirectory.getValue() != null)
					this.setContainerLocation(currentlySelectedDirectory.getValue(), newValue);
		}));

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

		// Create bindings in the GUI

		// First bind the 4 color adjustment sliders disable property to if an adjustable image is selected
		this.sldSaturation.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldHue.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldContrast.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldBrightness.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		// Also bind the disable button's disable property if an adjustable image is selected
		this.btnResetImage.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		// Finally bind the date taken's disable property if an adjustable image is selected
		this.txtDateTaken.textProperty().bind(Bindings.createStringBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getDateTakenFormatted() : "", currentlySelectedImage));
		// Bind the image preview to the selected image from the right side tree view
		this.imagePreview.imageProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? new Image(currentlySelectedImage.getValue().getFile().toURI().toString()) : null, currentlySelectedImage));
		// Bind the species entry list view items to the selected image species present
		this.speciesEntryListView.itemsProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getSpeciesPresent() : null, currentlySelectedImage));
		// When we select a new image, reset the image viewport to center and zoomed out. We also check the location that the image has selected
		this.currentlySelectedImage.addListener((observable, oldValue, newValue) -> {
			this.resetImageView(null);
			if (this.currentlySelectedImage.getValue() != null && currentlySelectedImage.getValue().getLocationTaken() != null)
				locationListView.getSelectionModel().select(currentlySelectedImage.getValue().getLocationTaken());
			else
				locationListView.getSelectionModel().clearSelection();
		});

		// Initialize root of the right side directory/image tree and make the root invisible
		// This is because a treeview must have ONE root.

		// Create a fake invisible root node whos children
		final TreeItem<ImageContainer> ROOT = new TreeItem<ImageContainer>(SanimalData.getInstance().getImageTree());
		// Hide the fake invisible root
		this.imageTree.setShowRoot(false);
		// Set the fake invisible root
		this.imageTree.setRoot(ROOT);
		// Set the items of the tree to be the children of the fake invisible root
		this.imageTree.setItems(SanimalData.getInstance().getImageTree().getChildren());
		// When a new image is selected...
		this.imageTree.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) ->
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
		}));

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

	}

	/**
	 * Recursively set the location of all images in the given container
	 *
	 * @param container The container to set the location of, if it's a directory recursively set the location on all its children
	 * @param location The location to recursively set
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
		SpeciesCreatorController controller = loader.<SpeciesCreatorController>getController();
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
			SanimalData.getInstance().getSpeciesList().remove(selected);
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

	public void addNewLocation(ActionEvent actionEvent)
	{
		Location newLocation = new Location();
		requestEdit(newLocation);
		if (newLocation.locationValid())
			SanimalData.getInstance().getLocationList().add(newLocation);
		actionEvent.consume();
	}

	public void editCurrentLocation(ActionEvent actionEvent)
	{
		Location selected = locationListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			requestEdit(selected);
		} else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Location Selected");
			alert.setContentText("Please select a location from the location list to edit.");
			alert.showAndWait();
		}
		actionEvent.consume();
	}

	private void requestEdit(Location location)
	{
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("LocationCreator.fxml");
		LocationCreatorController controller = loader.<LocationCreatorController>getController();
		controller.setLocation(location);

		Stage dialogStage = new Stage();
		dialogStage.setTitle("Location Creator/Editor");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.imagePreview.getScene().getWindow());
		Scene scene = new Scene(loader.getRoot());
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	public void deleteCurrentLocation(ActionEvent actionEvent)
	{
		Location selected = locationListView.getSelectionModel().getSelectedItem();
		SanimalData.getInstance().getLocationList().remove(selected);
		actionEvent.consume();
	}

	public void importImages(ActionEvent actionEvent)
	{
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

	public void deleteImages(ActionEvent actionEvent)
	{
		TreeItem<ImageContainer> item = this.imageTree.getSelectionModel().getSelectedItem();
		SanimalData.getInstance().getImageTree().removeChildRecursive(item.getValue());
		actionEvent.consume();
	}

	public void resetImageView(ActionEvent actionEvent)
	{
		this.sldBrightness.setValue(0);
		this.sldContrast.setValue(0);
		this.sldHue.setValue(0);
		this.sldSaturation.setValue(0);
		if (this.imagePreview.getImage() != null)
			this.imagePreview.setViewport(new Rectangle2D(0, 0, this.imagePreview.getImage().getWidth(), this.imagePreview.getImage().getHeight()));
		if (actionEvent != null)
			actionEvent.consume();
	}

	public void speciesListDrag(MouseEvent mouseEvent)
	{
		Species selected = this.speciesListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			Dragboard dragboard = this.speciesListView.startDragAndDrop(TransferMode.ANY);

			ClipboardContent content = new ClipboardContent();
			content.putString(selected.getUniqueID().toString());
			dragboard.setContent(content);

			mouseEvent.consume();
		}
	}

	public void imagePaneDragOver(DragEvent dragEvent)
	{
		if (dragEvent.getGestureSource() == this.speciesListView && dragEvent.getDragboard().hasString())
		{
			dragEvent.acceptTransferModes(TransferMode.COPY);
		}
		dragEvent.consume();
	}

	public void speciesListDragEntered(DragEvent dragEvent)
	{
		if (dragEvent.getGestureSource() == this.speciesListView && dragEvent.getDragboard().hasString())
			this.fadeAddPanelOut.play();
		dragEvent.consume();
	}

	public void speciesListDragExited(DragEvent dragEvent)
	{
		this.fadeAddPanelIn.play();
		dragEvent.consume();
	}

	public void speciesListDragDropped(DragEvent dragEvent)
	{
		Dragboard dragboard = dragEvent.getDragboard();
		Boolean success = false;
		if (dragboard.hasString())
		{
			String stringID = dragboard.getString();
			try
			{
				Integer ID = Integer.parseInt(stringID);
				Optional<Species> toAdd = SanimalData.getInstance().getSpeciesList().stream().filter(species -> species.getUniqueID().equals(ID)).findFirst();
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
		dragEvent.setDropCompleted(success);
		dragEvent.consume();
	}

	public void onMouseEnteredSpeciesEntryList(MouseEvent mouseEvent)
	{
		fadeSpeciesEntryListOut.play();
	}

	public void onMouseExitedSpeciesEntryList(MouseEvent mouseEvent)
	{
		fadeSpeciesEntryListIn.play();
	}

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
