package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import library.ToggleButtonSelector;
import org.controlsfx.control.SegmentedButton;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

public class TimeShiftController implements Initializable
{
	///
	/// FXML Bound fields start
	///

	@FXML
	public Spinner<Integer> spnYear;
	@FXML
	public Spinner<Integer> spnMonth;
	@FXML
	public Spinner<Integer> spnDay;
	@FXML
	public Spinner<Integer> spnHour;
	@FXML
	public Spinner<Integer> spnMinute;
	@FXML
	public Spinner<Integer> spnSecond;

	@FXML
	public Label lblDate;

	@FXML
	public ToggleButton tbn12Hr;
	@FXML
	public ToggleButton tbn24Hr;

	@FXML
	public ToggleButton tbnDayMonthYear;
	@FXML
	public ToggleButton tbnMonthDayYear;

	///
	/// FXML Bound fields end
	///

	private Calendar original = Calendar.getInstance();
	private Calendar dateToEdit = Calendar.getInstance();
	private DateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy HH:mm:ss");

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.spnYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnSecond.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));

		EasyBind.subscribe(this.spnYear.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnMonth.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnDay.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnHour.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnMinute.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnSecond.valueProperty(), this::updateDate);

		this.tbn12Hr.selectedProperty().addListener((observable, oldValue, newValue) -> this.refreshDateFormat());
		this.tbn24Hr.selectedProperty().addListener((observable, oldValue, newValue) -> this.refreshDateFormat());
		this.tbnDayMonthYear.selectedProperty().addListener((observable, oldValue, newValue) -> this.refreshDateFormat());
		this.tbnMonthDayYear.selectedProperty().addListener((observable, oldValue, newValue) -> this.refreshDateFormat());

		// Ensure that clicking a selected button does not deselect the button otherwise. We do this by registering a click, press, and release event filter
		// for each of the toggle buttons.
		ToggleButtonSelector.makeUnselectable(this.tbn12Hr);
		ToggleButtonSelector.makeUnselectable(this.tbn24Hr);
		ToggleButtonSelector.makeUnselectable(this.tbnDayMonthYear);
		ToggleButtonSelector.makeUnselectable(this.tbnMonthDayYear);
	}

	private void updateDate(Integer ignored)
	{
		this.dateToEdit.setTime(this.original.getTime());
		this.dateToEdit.add(Calendar.YEAR, this.spnYear.getValue());
		this.dateToEdit.add(Calendar.MONTH, this.spnMonth.getValue());
		this.dateToEdit.add(Calendar.DAY_OF_MONTH, this.spnDay.getValue());
		this.dateToEdit.add(Calendar.HOUR_OF_DAY, this.spnHour.getValue());
		this.dateToEdit.add(Calendar.MINUTE, this.spnMinute.getValue());
		this.dateToEdit.add(Calendar.SECOND, this.spnSecond.getValue());

		this.refreshLabel();
	}

	private void refreshDateFormat()
	{
		this.dateFormat = new SimpleDateFormat(String.format("%s yyyy %s", this.tbnDayMonthYear.isSelected() ? "dd MMMMM" : "MMMMM dd", this.tbn12Hr.isSelected() ? "hh:mm:ss aa" : "HH:mm:ss"));
		this.refreshLabel();
	}

	private void refreshLabel()
	{
		this.lblDate.setText(dateFormat.format(this.original.getTime()) + " -> " + dateFormat.format(this.dateToEdit.getTime()));
	}

	public void setDate(Date date)
	{
		this.original.setTime((Date) date.clone());
		this.dateToEdit.setTime(this.original.getTime());
		this.refreshLabel();
	}

	public Date getDate()
	{
		return this.dateToEdit.getTime();
	}

	public void confirmPressed(MouseEvent mouseEvent)
	{
		((Stage) this.tbn12Hr.getScene().getWindow()).close();
	}

	public void cancelPressed(MouseEvent mouseEvent)
	{
		this.dateToEdit = original;
		confirmPressed(mouseEvent);
	}
}
