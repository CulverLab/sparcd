package controller;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import model.SanimalData;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the settings tab
 */
public class SanimalSettingsController implements Initializable
{
	///
	/// FXML bound fields start
	///

	// The sheet of sanimal properties
	@FXML
	public PropertySheet pstSettings;

	///
	/// FXML bound fields end
	///

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initialize(URL location, ResourceBundle resources)
	{
		// Bind the sanimal settings list to the property sheet settings because we cant bind the list property...
		EasyBind.listBind(this.pstSettings.getItems(), SanimalData.getInstance().getSettings().getSettingList());

		/*

		@FXML
		public TaskProgressView<Task<?>> tpvTaskProgress;
		FilteredList<Worker<?>> tasks = SanimalData.getInstance().getSanimalExecutor().getImmediateExecutor().getActiveTasks().filtered(worker -> worker instanceof Task<?>);
		EasyBind.listBind(this.tpvTaskProgress.getTasks(), EasyBind.map(tasks, worker -> (Task<?>) worker));

		 */
	}
}
