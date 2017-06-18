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
	private ListView<Species> speciesListView;

	private ObjectProperty<ColorAdjust> colorAdjust = new SimpleObjectProperty<ColorAdjust>(new ColorAdjust());

	private Border tempBorderStorage = null;
	private ImageEntry currentlySelectedImage = null;
	private FadeTransition fadeIn;
	private FadeTransition fadeOut;
	private ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<Point2D>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		SortedList<Species> species = new SortedList<Species>(SanimalData.getInstance().getSpeciesList());
		species.setComparator(Comparator.comparing(Species::getName));
		this.speciesListView.setItems(species);
		this.speciesListView.setCellFactory(x ->
				{
					FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SpeciesListEntry.fxml"));

					try
					{
						loader.load();
					}
					catch (IOException exception)
					{
						System.err.println("Could not load the FXML file for the species list entry!");
						exception.printStackTrace();
						return null;
					}

					return loader.getController();
				}
		);
		this.imageTree.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) ->
		{
			ImageContainer oldOne = newValue.getValue();
			if (oldOne != null)
			{
				if (oldOne instanceof ImageEntry)
				{
					ImageEntry imageEntry = (ImageEntry) oldOne;
					txtDateTaken.textProperty().unbind();
				} else if (oldOne instanceof ImageDirectory)
				{
					// Ignored
				}
			}

			ImageContainer newOne = newValue.getValue();
			if (newOne instanceof ImageEntry)
			{
				currentlySelectedImage = (ImageEntry) newOne;
				txtDateTaken.textProperty().bind(Bindings.createStringBinding(currentlySelectedImage::getDateTakenFormatted, currentlySelectedImage.getDateTakenProperty()));
				imagePreview.setImage(new Image(currentlySelectedImage.getFile().toURI().toString()));
				speciesEntryListView.setItems(currentlySelectedImage.getSpeciesPresent());
				this.resetImageView(null);
				this.reset(this.imagePreview, this.imagePreview.getFitWidth(), this.imagePreview.getFitHeight());
			} else if (newOne instanceof ImageDirectory)
			{
				currentlySelectedImage = null;
			}
		}));

		SortedList<Location> locations = new SortedList<Location>(SanimalData.getInstance().getLocationList());
		locations.setComparator(Comparator.comparing(Location::getName));
		this.locationListView.setItems(locations);
		this.locationListView.setCellFactory(x ->
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/LocationListEntry.fxml"));

			try
			{
				loader.load();
			}
			catch (IOException exception)
			{
				System.err.println("Could not load the FXML file for the location list entry!");
				exception.printStackTrace();
				return null;
			}
			return loader.getController();
		});

		this.speciesEntryListView.setCellFactory(x ->
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SpeciesEntryListEntry.fxml"));

			try
			{
				loader.load();
			}
			catch (IOException exception)
			{
				System.err.println("Could not load the FXML file for the species entry list entry!");
				exception.printStackTrace();
				return null;
			}
			return loader.getController();
		});

		colorAdjust.getValue().brightnessProperty().bind(this.sldBrightness.valueProperty());
		colorAdjust.getValue().contrastProperty().bind(this.sldContrast.valueProperty());
		colorAdjust.getValue().hueProperty().bind(this.sldHue.valueProperty());
		colorAdjust.getValue().saturationProperty().bind(this.sldSaturation.valueProperty());
		this.imagePreview.effectProperty().bind(this.colorAdjust);

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
	}

	public void editCurrentSpecies(ActionEvent actionEvent)
	{
		Species selected = speciesListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			requestEdit(selected);
		} else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.imagePreview.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Species Selected");
			alert.setContentText("Please select a species from the species list to edit.");
			alert.showAndWait();
		}
	}

	private void requestEdit(Species species)
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/SpeciesCreator.fxml"));
		GridPane windowHead;
		try
		{
			windowHead = loader.<GridPane>load();
		}
		catch (IOException exception)
		{
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
	}

	private void requestEdit(Location location)
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/LocationCreator.fxml"));
		GridPane windowHead;
		try
		{
			windowHead = loader.<GridPane>load();
		}
		catch (IOException exception)
		{
			System.err.println("Could not load the FXML file for the location list entry!");
			exception.printStackTrace();
			return;
		}
		LocationCreatorController controller = loader.<LocationCreatorController>getController();
		controller.setLocation(location);

		Stage dialogStage = new Stage();
		dialogStage.setTitle("Location Creator/Editor");
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initOwner(this.imagePreview.getScene().getWindow());
		Scene scene = new Scene(windowHead);
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
		item.getParent().getChildren().remove(item);
		actionEvent.consume();
	}

	public void resetImageView(ActionEvent actionEvent)
	{
		this.sldBrightness.setValue(0);
		this.sldContrast.setValue(0);
		this.sldHue.setValue(0);
		this.sldSaturation.setValue(0);
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
					if (currentlySelectedImage != null)
					{
						currentlySelectedImage.addSpecies(toAdd.get(), 1);
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
		fadeOut.play();
	}

	public void onMouseExitedSpeciesEntryList(MouseEvent mouseEvent)
	{
		fadeIn.play();
	}

	public void onImagePressed(MouseEvent mouseEvent)
	{
		mouseDown.set(imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY())));
	}

	public void onImageDragged(MouseEvent mouseEvent)
	{
		Point2D dragPoint = imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY()));
		shift(this.imagePreview, dragPoint.subtract(mouseDown.get()));
		mouseDown.set(imageViewToImage(this.imagePreview, new Point2D(mouseEvent.getX(), mouseEvent.getY())));
	}

	public void onImageClicked(MouseEvent mouseEvent)
	{
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

	// reset to the top left:
	private void reset(ImageView imageView, double width, double height)
	{
		imageView.setViewport(new Rectangle2D(0, 0, width, height));
	}
}
