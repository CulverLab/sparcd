package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import model.SanimalData;
import org.controlsfx.control.PropertySheet;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.ResourceBundle;

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
