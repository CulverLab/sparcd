package controller.uploadView;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import library.EditCell;
import library.TableColumnHeaderUtil;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.threading.ErrorTask;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the settings user interface
 */
public class ImageCollectionSettingsController implements Initializable
{
	///
	/// FXML Bound fields start
	///

	// The text fields for collection name, organization, contact information, and description
	@FXML
	public TextField txtName;
	@FXML
	public TextField txtOrganization;
	@FXML
	public TextField txtContactInfo;
	@FXML
	public TextArea tbxDescription;

	// The actual tableview
	@FXML
	public TableView<Permission> tvwPermissions;

	// All 4 columns of the tableview
	@FXML
	public TableColumn<Permission, String> clmUser;
	@FXML
	public TableColumn<Permission, Boolean> clmRead;
	@FXML
	public TableColumn<Permission, Boolean> clmUpload;
	@FXML
	public TableColumn<Permission, Boolean> clmOwner;

	// Buttons to remove a user, save permissions, transfer the collection ownership, and add a new user
	@FXML
	public Button btnRemoveUser;
	@FXML
	public Button btnSave;
	@FXML
	public Button btnAddUser;
	@FXML
	public Button btnTransferOwnership;

	///
	/// FXML Bound fields end
	///

	// A list of Property<Object> that is used to store weak listeners to avoid early garbage collection. This concept is strange and difficult to
	// understand, here's some articles on it:
	// https://stackoverflow.com/questions/23785816/javafx-beans-binding-suddenly-stops-working
	// https://stackoverflow.com/questions/14558266/clean-javafx-property-listeners-and-bindings-memory-leaks
	// https://stackoverflow.com/questions/26312651/bidirectional-javafx-binding-is-destroyed-by-unrelated-code
	private final List<Property> hardReferences = new ArrayList<>();

	// The currently modified image collection
	private ObjectProperty<ImageCollection> clonedCollection = new SimpleObjectProperty<>();
	// The original image collection that we are editing
	private ImageCollection originalCollection;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// TODO: We probably do not need bidirectional binds here

		// Bind the name property of the current collection to the name text property
		// We cache the property so that it does not get garbage collected early
		this.txtName.textProperty().bindBidirectional(cache(EasyBind.monadic(clonedCollection).selectProperty(ImageCollection::nameProperty)));
		// Bind the organization property of the current collection to the organization text property
		// We cache the property so that it does not get garbage collected early
		this.txtOrganization.textProperty().bindBidirectional(cache(EasyBind.monadic(clonedCollection).selectProperty(ImageCollection::organizationProperty)));
		// Bind the contact info property of the current collection to the contact info text property
		// We cache the property so that it does not get garbage collected early
		this.txtContactInfo.textProperty().bindBidirectional(cache(EasyBind.monadic(clonedCollection).selectProperty(ImageCollection::contactInfoProperty)));
		// Bind the description property of the current collection to the description text property
		// We cache the property so that it does not get garbage collected early
		this.tbxDescription.textProperty().bindBidirectional(cache(EasyBind.monadic(clonedCollection).selectProperty(ImageCollection::descriptionProperty)));

		// Bind the permissions to the image collection's permissions
		this.tvwPermissions.itemsProperty().bind(EasyBind.monadic(clonedCollection).map(ImageCollection::getPermissions));

		// Add prompt text to the contact info and description so that users know what the fields are for
		this.txtContactInfo.setPromptText("Email and/or Phone Number preferred");
		this.tbxDescription.setPromptText("Describe the project/collection");

		// If no collection is selected, disable the text fields and buttons
		BooleanBinding nothingSelected = clonedCollection.isNull();

		// Disable this button when the selected permission is the owner
		this.btnRemoveUser.disableProperty().bind(EasyBind.monadic(this.tvwPermissions.getSelectionModel().selectedItemProperty()).selectProperty(Permission::ownerProperty).orElse(nothingSelected));

		// Each column is bound to a different permission
		this.clmUser.setCellValueFactory(param -> param.getValue().usernameProperty());
		this.clmUser.setCellFactory(x -> new EditCell<>(new DefaultStringConverter()));
		this.clmRead.setCellValueFactory(param -> param.getValue().readProperty());
		this.clmRead.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmUpload.setCellValueFactory(param -> param.getValue().uploadProperty());
		this.clmUpload.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmOwner.setCellValueFactory(param -> param.getValue().ownerProperty());
		this.clmOwner.setCellFactory(param -> new CheckBoxTableCell<>());
		// We make the header of the owner column wrap because it is very long
		TableColumnHeaderUtil.makeHeaderWrappable(this.clmOwner);
		// We ensure we cant' set the owner, otherwise you might accidently transfer the collection ownership to another user
		this.clmOwner.setEditable(false);

		// Upon double clicking an empty cell, add a new user
		this.tvwPermissions.setRowFactory(table -> {
			TableRow<Permission> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2)
					if (row.isEmpty())
						createUser();
			});
			return row;
		});

		// Ensure that the table view is editable
		this.tvwPermissions.setEditable(true);
	}

	/**
	 * This is purely used so that the action listeners are not garbage collected early
	 *
	 * @param reference The reference to cache
	 * @param <T> The type of the property, can be anything
	 * @return Returns the property passed in purely for convenience
	 */
	private <T> Property<T> cache(Property<T> reference)
	{
		// Add the reference and return it
		this.hardReferences.add(reference);
		return reference;
	}

	/**
	 * Set the collection that we will edit
	 *
	 * @param collection The collection to clone and edit
	 */
	public void setCollectionToEdit(ImageCollection collection)
	{
		// Clone the collection first
		ImageCollection clone = new ImageCollection();
		// Initialize the new collection's parameters
		clone.setName(collection.getName());
		clone.setContactInfo(collection.getContactInfo());
		clone.setOrganization(collection.getOrganization());
		clone.setDescription(collection.getDescription());
		// Clone each permission (so we don't just copy references) and add it to the clone's permission list
		clone.getPermissions().addAll(collection.getPermissions().stream().map(Permission::clone).collect(Collectors.toList()));
		// Update the clone'd collection and the original collection
		this.clonedCollection.setValue(clone);
		originalCollection = collection;
	}

	/**
	 * When we click the add new user button
	 *
	 * @param actionEvent consumed
	 */
	public void addNewUser(ActionEvent actionEvent)
	{
		// Just create a new user with permissions
		createUser();
		actionEvent.consume();
	}

	/**
	 * Creates a new user without a name and no permissions
	 */
	private void createUser()
	{
		// Create the permission
		Permission permission = new Permission();
		// If the selected collection is not null, add it to the collection
		if (this.clonedCollection.getValue() != null)
			this.clonedCollection.getValue().getPermissions().add(permission);
	}

	/**
	 * Revokes permission from a given user
	 *
	 * @param actionEvent consumed
	 */
	public void removeCurrentUser(ActionEvent actionEvent)
	{
		// Grab the selected permission
		Permission selected = this.tvwPermissions.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), remove the permission
		if (selected != null)
		{
			clonedCollection.getValue().getPermissions().remove(selected);
		}
		// Otherwise show an alert that no permission was selected
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.tvwPermissions.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Permission Selected");
			alert.setContentText("Please select a permission from the permissions list to edit.");
			alert.showAndWait();
		}
		// Consume the event
		actionEvent.consume();
	}

	/**
	 * Updates the current collection settings on CyVerse and the internal program
	 *
	 * @param actionEvent consumed
	 */
	public void saveCollection(ActionEvent actionEvent)
	{
		// Disable the save button so that we don't click it twice
		btnSave.setDisable(true);
		ImageCollection currentlySelected = this.clonedCollection.getValue();
		// Ensure that the permissions are valid first
		for (Permission permission : currentlySelected.getPermissions())
		{
			// Double check that each username entered is valid
			if (!SanimalData.getInstance().getConnectionManager().isValidUsername(permission.getUsername()))
			{
				// If it is not, show the save button again, and show an error message
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Invalid User");
				alert.setHeaderText("Username entered invalid");
				alert.setContentText("The username (" + permission.getUsername() + ") you entered was not found on the CyVerse system. Reminder: permissions are expecting usernames, not real names.");
				alert.showAndWait();
				btnSave.setDisable(false);
				// Just return if there is an invalid username
				return;
			}
		}
		// Grab the cloned collection and update the original based on the new settings
		originalCollection.setName(currentlySelected.getName());
		originalCollection.setContactInfo(currentlySelected.getContactInfo());
		originalCollection.setOrganization(currentlySelected.getOrganization());
		originalCollection.setDescription(currentlySelected.getDescription());
		// Here we can just update references, because we're done editing
		originalCollection.getPermissions().setAll(currentlySelected.getPermissions());

		// Create a task used to thread off the saving process
		Task<Void> saveTask = new ErrorTask<Void>()
		{
			@Override
			protected Void call()
			{
				// We have done no work yet, so our progress is 0
				this.updateProgress(0, 1);
				this.updateMessage("Saving the collection: " + originalCollection.getName());

				// We use this message updater to change the message string when the connection manager reports progress
				StringProperty messageUpdater = new SimpleStringProperty("");
				messageUpdater.addListener((observable, oldValue, newValue) -> this.updateMessage(newValue));

				SanimalData.getInstance().getConnectionManager().pushLocalCollection(originalCollection, messageUpdater);

				this.updateProgress(1, 1);
				return null;
			}
		};

		// Perform the task
		SanimalData.getInstance().getSanimalExecutor().getQueuedExecutor().addTask(saveTask);

		// Close the edit window, since we're done with the edit
		((Stage) this.tvwPermissions.getScene().getWindow()).close();

		actionEvent.consume();
	}

	/**
	 * Transfers ownership from one user to another
	 *
	 * @param actionEvent consumed
	 */
	public void transferOwnership(ActionEvent actionEvent)
	{
		// Show a dialog asking for the username to transfer to
		TextInputDialog input = new TextInputDialog();
		input.setTitle("Transfer Ownership");
		input.setContentText("Enter the username of the user to transfer ownership to");

		// Ensure that the new username is valid in a loop
		String newOwner;
		Boolean gotValidUsername = false;
		while (!gotValidUsername)
		{
			// Grab a username
			Optional<String> inputValue = input.showAndWait();
			if (inputValue.isPresent())
			{
				// Grab the owner string
				newOwner = inputValue.get();
				// Test if it is valid
				gotValidUsername = SanimalData.getInstance().getConnectionManager().isValidUsername(newOwner);
				if (!gotValidUsername)
				{
					// If we didn't get a valid name, show an alert, and ask for a new username
					Alert alert = new Alert(Alert.AlertType.WARNING);
					alert.setTitle("Invalid User");
					alert.setHeaderText("Username entered invalid");
					alert.setContentText("The username you entered was not found on the CyVerse system, please try again...");
					alert.showAndWait();
				}
			}
			else
				return;
		}

		// We want to confirm that the user wants to transfer the collection, because this may result in full loss of permissions
		Alert transferConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
		transferConfirmation.setTitle("Transfer Confirmation");
		transferConfirmation.setHeaderText("Continue finalizing transfer?");
		transferConfirmation.setContentText("Once the owner has been set, you will no longer be able to edit collection permissions, description, title, or any other settings. Are you sure you want to continue?");
		Optional<ButtonType> buttonTypeSelected = transferConfirmation.showAndWait();
		buttonTypeSelected.ifPresent(buttonType ->
		{
			if (buttonType == ButtonType.OK)
			{
				// For now, this is not supported
			}
		});

		actionEvent.consume();
	}
}
