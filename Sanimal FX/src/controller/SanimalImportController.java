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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;

public class SanimalImportController implements Initializable
{
	private static final double LIST_CELL_HEIGHT = 25;

	@FXML
	public ImageViewPane imagePreviewPane;

	@FXML
	public ImageView imagePreview;

	@FXML
	public Slider sldBrightness;
	@FXML
	public Slider sldContrast;
	@FXML
	public Slider sldHue;
	@FXML
	public Slider sldSaturation;

	@FXML
	public TextField txtDateTaken;

	@FXML
	public ListView<SpeciesEntry> speciesEntryListView;

	// Will contain ImageEntries and ImageDirectories
	@FXML
	public TreeViewAutomatic<ImageContainer> imageTree;

	@FXML
	public ListView<Location> locationListView;

	@FXML
	public Button btnResetImage;

	@FXML
	private ListView<Species> speciesListView;

	private ObjectProperty<ColorAdjust> colorAdjust = new SimpleObjectProperty<ColorAdjust>(new ColorAdjust());

	private Border tempBorderStorage = null;
	private ObjectProperty<ImageEntry> currentlySelectedImage = new SimpleObjectProperty<ImageEntry>(null);
	private FadeTransition fadeIn;
	private FadeTransition fadeOut;
	private ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<Point2D>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		SortedList<Species> species = new SortedList<Species>(SanimalData.getInstance().getSpeciesList());
		species.setComparator(Comparator.comparing(Species::getName));
		this.speciesListView.setItems(species);
		this.speciesListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("SpeciesListEntry.fxml").getController());
		this.imageTree.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) ->
		{
			if (oldValue != null)
			{
				ImageContainer oldOne = oldValue.getValue();
				if (oldOne instanceof ImageEntry)
					oldValue.setGraphic(new ImageView(oldOne.getTreeIcon()));
				else if (oldOne instanceof ImageDirectory)
				{
					// Ignored
				}
			}

			ImageContainer newOne = newValue.getValue();
			if (newOne instanceof ImageEntry)
				currentlySelectedImage.setValue((ImageEntry) newOne);
			else if (newOne instanceof ImageDirectory)
				currentlySelectedImage.setValue(null);
		}));

		SortedList<Location> locations = new SortedList<Location>(SanimalData.getInstance().getLocationList());
		locations.setComparator(Comparator.comparing(Location::getName));
		this.locationListView.setItems(locations);
		this.locationListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("LocationListEntry.fxml").getController());
		this.locationListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
			if (newValue != null && currentlySelectedImage.getValue() != null)
			{
				currentlySelectedImage.getValue().setLocationTaken(newValue);
			}
		}));

		this.speciesEntryListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("SpeciesEntryListEntry.fxml").getController());

		colorAdjust.getValue().brightnessProperty().bind(this.sldBrightness.valueProperty());
		colorAdjust.getValue().contrastProperty().bind(this.sldContrast.valueProperty());
		colorAdjust.getValue().hueProperty().bind(this.sldHue.valueProperty());
		colorAdjust.getValue().saturationProperty().bind(this.sldSaturation.valueProperty());
		this.imagePreview.effectProperty().bind(this.colorAdjust);

		this.sldSaturation.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldHue.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldContrast.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.sldBrightness.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.btnResetImage.disableProperty().bind(Bindings.createBooleanBinding(() -> currentlySelectedImage.getValue() == null, currentlySelectedImage));
		this.txtDateTaken.textProperty().bind(Bindings.createStringBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getDateTakenFormatted() : "", currentlySelectedImage));
		this.imagePreview.imageProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? new Image(currentlySelectedImage.getValue().getFile().toURI().toString()) : null, currentlySelectedImage));
		this.speciesEntryListView.itemsProperty().bind(Bindings.createObjectBinding(() -> currentlySelectedImage.getValue() != null ? currentlySelectedImage.getValue().getSpeciesPresent() : null, currentlySelectedImage));
		this.currentlySelectedImage.addListener((observable, oldValue, newValue) -> {
			this.resetImageView(null);
			if (this.currentlySelectedImage.getValue() != null && currentlySelectedImage.getValue().getLocationTaken() != null)
				locationListView.getSelectionModel().select(currentlySelectedImage.getValue().getLocationTaken());
			else
				locationListView.getSelectionModel().clearSelection();
		});

		final TreeItem<ImageContainer> ROOT = new TreeItem<ImageContainer>(SanimalData.getInstance().getImageTree());
		this.imageTree.setShowRoot(false);
		this.imageTree.setRoot(ROOT);
		this.imageTree.setItems(SanimalData.getInstance().getImageTree().getChildren());

		this.fadeIn = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeIn.setFromValue(0.9);
		this.fadeIn.setToValue(0.02);
		this.fadeIn.setCycleCount(1);

		this.fadeOut = new FadeTransition(Duration.millis(100), this.speciesEntryListView);
		this.fadeOut.setFromValue(0.02);
		this.fadeOut.setToValue(0.9);
		this.fadeOut.setCycleCount(1);
	}

	public void addNewSpecies(ActionEvent actionEvent)
	{
		Species newSpecies = new Species();
		requestEdit(newSpecies);
		if (!newSpecies.isUninitialized())
			SanimalData.getInstance().getSpeciesList().add(newSpecies);
		actionEvent.consume();
	}

	public void editCurrentSpecies(ActionEvent actionEvent)
	{
		Species selected = speciesListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			requestEdit(selected);
		}
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Species Selected");
			alert.setContentText("Please select a species from the species list to edit.");
			alert.showAndWait();
		}
		actionEvent.consume();
	}

	private void requestEdit(Species species)
	{
		FXMLLoader loader = FXMLLoaderUtils.loadFXML("SpeciesCreator.fxml");
		SpeciesCreatorController controller = loader.<SpeciesCreatorController>getController();
		controller.setSpecies(species);

		Stage dialogStage = new Stage();
		dialogStage.setTitle("Species Creator/Editor");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.imagePreview.getScene().getWindow());
		Scene scene = new Scene(loader.getRoot());
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	public void deleteCurrentSpecies(ActionEvent actionEvent)
	{
		Species selected = speciesListView.getSelectionModel().getSelectedItem();
		SanimalData.getInstance().getSpeciesList().remove(selected);
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
		{
			this.tempBorderStorage = this.imagePreviewPane.getBorder();
			this.imagePreviewPane.setBorder(new Border(new BorderStroke(Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THICK)));
		}
		dragEvent.consume();
	}

	public void speciesListDragExited(DragEvent dragEvent)
	{
		this.imagePreviewPane.setBorder(tempBorderStorage);
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
		//fadeOut.play();
	}

	public void onMouseExitedSpeciesEntryList(MouseEvent mouseEvent)
	{
		//fadeIn.play();
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
