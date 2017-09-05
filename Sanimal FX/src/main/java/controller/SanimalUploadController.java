package controller;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import model.SanimalData;
import model.cyverse.ImageCollection;
import model.cyverse.Permission;
import model.species.Species;
import model.util.FXMLLoaderUtils;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class SanimalUploadController implements Initializable
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public ListView<ImageCollection> collectionListView;

	@FXML
	public TextField txtName;
	@FXML
	public TextField txtOrganization;

	// The actual tableview
	@FXML
	public TableView<Permission> tvwPermissions;
	// All 5 columns
	@FXML
	public TableColumn<Permission, String> clmUser;
	@FXML
	public TableColumn<Permission, Boolean> clmView;
	@FXML
	public TableColumn<Permission, Boolean> clmWrite;
	@FXML
	public TableColumn<Permission, Boolean> clmDelete;
	@FXML
	public TableColumn<Permission, Boolean> clmOwner;

	@FXML
	public Button btnRemoveUser;
	@FXML
	public Button btnAddUser;

	///
	/// FXML Bound Fields End
	///

	// A list of Property<Object> that is used to store weak listeners to avoid early garbage collection. This concept is strange and difficult to
	// understand, here's some articles on it:
	// https://stackoverflow.com/questions/23785816/javafx-beans-binding-suddenly-stops-working
	// https://stackoverflow.com/questions/14558266/clean-javafx-property-listeners-and-bindings-memory-leaks
	// https://stackoverflow.com/questions/26312651/bidirectional-javafx-binding-is-destroyed-by-unrelated-code
	private final List<Property> hardReferences = new ArrayList<>();

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

		// Bind the name property of the current collection to the name text property
		// We cache the property so that it does not get garbage collected early
		this.txtName.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::nameProperty)));
		// Bind the organization property of the current collection to the organization text property
		// We cache the property so that it does not get garbage collected early
		this.txtOrganization.textProperty().bindBidirectional(cache(EasyBind.monadic(selectedCollection).selectProperty(ImageCollection::organizationProperty)));

		this.tvwPermissions.itemsProperty().bind(EasyBind.monadic(selectedCollection).map(ImageCollection::getPermissions));

		this.btnAddUser.disableProperty().bind(selectedCollection.isNull());
		//EasyBind.monadic(this.tvwPermissions.getSelectionModel().selectedItemProperty()).selectProperty(Permission::ownerProperty);
		this.btnRemoveUser.disableProperty().bind(
				selectedCollection.isNull());

		//.or(EasyBind.monadic(this.tvwPermissions.getSelectionModel().selectedItemProperty()).selectProperty(Permission::ownerProperty))

		this.clmUser.setCellValueFactory(param -> param.getValue().usernameProperty());
		this.clmUser.setCellFactory(TextFieldTableCell.forTableColumn());
		this.clmView.setCellValueFactory(param -> param.getValue().viewProperty());
		this.clmView.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmWrite.setCellValueFactory(param -> param.getValue().writeProperty());
		this.clmWrite.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmDelete.setCellValueFactory(param -> param.getValue().deleteProperty());
		this.clmDelete.setCellFactory(param -> new CheckBoxTableCell<>());
		this.clmOwner.setCellValueFactory(param -> param.getValue().ownerProperty());
		this.clmOwner.setCellFactory(param -> new CheckBoxTableCell<>());

		this.tvwPermissions.setEditable(true);
	}

	private <T> Property<T> cache(Property<T> reference)
	{
		this.hardReferences.add(reference);
		return reference;
	}

	public void newCollectionPressed(ActionEvent actionEvent)
	{
		ImageCollection collection = new ImageCollection();
		Permission owner = new Permission();
		owner.setUsername(SanimalData.getInstance().getConnectionManager().usernameProperty().getValue());
		owner.setView(true);
		owner.setWrite(true);
		owner.setDelete(true);
		owner.setOwner(true);
		collection.getPermissions().add(owner);
		SanimalData.getInstance().getCollectionList().add(collection);
	}

	public void deleteCollectionPressed(ActionEvent actionEvent)
	{
		ImageCollection selected = this.collectionListView.getSelectionModel().getSelectedItem();
		if (selected != null)
		{
			SanimalData.getInstance().getCollectionList().remove(selected);
		}
		else
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.initOwner(this.collectionListView.getScene().getWindow());
			alert.setTitle("No Selection");
			alert.setHeaderText("No Collection Selected");
			alert.setContentText("Please select a collection from the collection list to remove.");
			alert.showAndWait();
		}
	}

	public void addNewUser(ActionEvent actionEvent)
	{
		Permission permission = new Permission();
		permission.setDelete(false);
		permission.setWrite(false);
		permission.setView(false);
		permission.setOwner(false);
		permission.setUsername("");
		this.selectedCollection.getValue().getPermissions().add(permission);
		actionEvent.consume();
	}

	public void removeCurrentUser(ActionEvent actionEvent)
	{
		// Grab the selected permission
		Permission selected = this.tvwPermissions.getSelectionModel().getSelectedItem();
		// If it's not null (so something is indeed selected), remove the permission
		if (selected != null)
		{
			selectedCollection.getValue().getPermissions().remove(selected);
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
}
