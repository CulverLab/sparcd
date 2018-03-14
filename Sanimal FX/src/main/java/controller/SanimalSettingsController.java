package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import model.SanimalData;
import org.controlsfx.control.PropertySheet;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SanimalSettingsController implements Initializable
{
	///
	/// FXML bound fields start
	///

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
	public void initialize(URL location, ResourceBundle resources)
	{
		EasyBind.listBind(this.pstSettings.getItems(), SanimalData.getInstance().getSettings().getSettingList());
	}
}
