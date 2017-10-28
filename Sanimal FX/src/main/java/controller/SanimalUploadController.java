package controller;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.converter.DefaultStringConverter;
import library.EditCell;
import library.TableColumnHeaderUtil;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.util.FXMLLoaderUtils;
import model.util.FinishableTask;
import org.fxmisc.easybind.EasyBind;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DefaultIntraFileProgressCallbackListener;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for the upload page
 */
public class SanimalUploadController implements Initializable
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<ImageCollection> collectionListView;

	@FXML
	public Button btnNewCollection;
	@FXML
	public Button btnDeleteCollection;


	// The primary split pane
	@FXML
	public SplitPane spnMain;

	// The upload split pane
	@FXML
	public SplitPane spnUpload;

	@FXML
	public ListView<ImageDirectory> lstItemsToUpload;

	///
	/// FXML Bound Fields End
	///

	// The currently selected image collection
	private ObjectProperty<ImageCollection> selectedCollection = new SimpleObjectProperty<>();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// First setup the collection list

		// Grab the global collection list
		SortedList<ImageCollection> collections = new SortedList<>(SanimalData.getInstance().getCollectionList());
		// Set the comparator to be the name of the image collection
		collections.setComparator(Comparator.comparing(ImageCollection::getName));
		// Set the list of items to be the collections
		this.collectionListView.setItems(SanimalData.getInstance().getCollectionList());
		// Set the cell factory to be our custom cell factory
		this.collectionListView.setCellFactory(x -> FXMLLoaderUtils.loadFXML("uploadView/ImageCollectionListEntry.fxml").getController());
		// When we select a new element, set the property
		this.collectionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.selectedCollection.setValue(newValue));

		// If no collection is selected, disable the text fields and buttons
		BooleanBinding nothingSelected = selectedCollection.isNull();

		// Disable this button when we are not the owner of the collection
		this.btnDeleteCollection.disableProperty().bind(EasyBind.monadic(this.selectedCollection).map(collection ->
		{
			String ownerUsername = collection.getOwner();
			return ownerUsername == null || !ownerUsername.equals(SanimalData.getInstance().getUsername());
		}).orElse(nothingSelected));

		this.spnUpload.disableProperty().bind(EasyBind.monadic(this.selectedCollection).map(collection ->
				collection.getPermissions()
						.filtered(perm -> !(perm.getUsername().equals(SanimalData.getInstance().getUsername()) && perm.canUpload())).size() == 1)
				.orElse(nothingSelected));

		this.lstItemsToUpload.setCellFactory(list -> FXMLLoaderUtils.loadFXML("uploadView/ImageUploadListEntry.fxml").getController());
		ObservableList<ImageContainer> containers = SanimalData.getInstance().getImageTree().getChildren();
		ObservableList<ImageDirectory> directories = EasyBind.map(containers.filtered(imageContainer -> imageContainer instanceof ImageDirectory), imageContainer -> (ImageDirectory) imageContainer);
		this.lstItemsToUpload.setItems(directories);
	}

	/**
	 * When we click the new collection button
	 *
	 * @param actionEvent
	 */
	public void newCollectionPressed(ActionEvent actionEvent)
	{
		// Create the collection
		ImageCollection collection = new ImageCollection();
		// Create permissions for the owner
		Permission owner = new Permission();
		// Ensure that the owner has own permissions and then add it to the collection
		owner.setUsername(SanimalData.getInstance().usernameProperty().getValue());
		owner.setRead(true);
		owner.setUpload(true);
		owner.setOwner(true);
		collection.getPermissions().add(owner);
		// Add the collection to the global collection list
		SanimalData.getInstance().getCollectionList().add(collection);
	}

	/**
	 * When we click the delete collection button
	 *
	 * @param actionEvent
	 */
	public void deleteCollectionPressed(ActionEvent actionEvent)
	{
		// Grab the selected collection
		ImageCollection selected = this.collectionListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			// If a collection is selected, show an alert that data may be deleted!
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("Confirmation");
			alert.setHeaderText("Are you sure you want to delete this collection?");
			alert.setContentText("Deleting this collection will result in the permanent removal of all images uploaded to CyVerse to this collection. Are you sure you want to continue?");
			Optional<ButtonType> buttonType = alert.showAndWait();
			if (buttonType.isPresent())
			{
				if (buttonType.get() == ButtonType.OK)
				{
					// Remove the collection on the CyVerse system
					SanimalData.getInstance().getConnectionManager().removeCollection(selected);

					// Remove the selected collection
					SanimalData.getInstance().getCollectionList().remove(selected);
				}
			}
		}
		else
		{
			// If no collection is selected, show an alert
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Collection Selected");
			alert.setContentText("Please select a collection from the collection list to remove.");
			alert.showAndWait();
		}
	}

	public void uploadImages(ActionEvent actionEvent)
	{
		this.lstItemsToUpload.getItems().forEach(imageDirectory ->
		{
			if (imageDirectory.isSelectedForUpload())
			{
				boolean validDirectory = true;
				for (ImageEntry imageEntry : imageDirectory.flattened().filter(imageContainer -> imageContainer instanceof ImageEntry).map(imageContainer -> (ImageEntry) imageContainer).collect(Collectors.toList()))
				{
					if (imageEntry.getLocationTaken() == null || imageEntry.getSpeciesPresent().isEmpty())
					{
						validDirectory = false;
						break;
					}
				}

				if (validDirectory)
				{
					Task<Void> uploadTask = new Task<Void>()
					{
						@Override
						protected Void call() throws Exception
						{
							SanimalData.getInstance().getConnectionManager().uploadImages(selectedCollection.getValue(), imageDirectory, new TransferStatusCallbackListener()
							{
								@Override
								public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) throws JargonException
								{
									System.out.println("XXXXX" + transferStatus.getBytesTransfered());
									return FileStatusCallbackResponse.CONTINUE;
								}

								@Override
								public void overallStatusCallback(TransferStatus transferStatus) throws JargonException
								{

								}

								@Override
								public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection)
								{
									return CallbackResponse.YES_FOR_ALL;
								}
							});
							return null;
						}
					};
					SanimalData.getInstance().getSanimalExecutor().addTask(uploadTask);
				}
				else
				{
					// If an invalid directory is selected, show an alert
					Alert alert = new Alert(Alert.AlertType.WARNING);
					alert.initOwner(this.collectionListView.getScene().getWindow());
					alert.setTitle("Invalid Directory");
					alert.setHeaderText("Invalid Directory (" + imageDirectory.getFile().getName() + ") Selected");
					alert.setContentText("An image in the directory (" + imageDirectory.getFile().getName() + ") you selected does not have a location or species tagged. Please ensure all images are tagged with at least one species and a location!");
					alert.showAndWait();
				}
			}
		});
	}
}
