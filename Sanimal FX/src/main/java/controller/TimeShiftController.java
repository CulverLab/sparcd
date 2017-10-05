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

/**
 * Controller class for the time shift window
 */
public class TimeShiftController implements Initializable
{
	///
	/// FXML Bound fields start
	///

	// Spinners for offsets for the following units: year, month, day, hour, minute, and second
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

	// The label for date on the top
	@FXML
	public Label lblDate;

	// Buttons for swapping date format from 12 hour to 24 hour
	@FXML
	public ToggleButton tbn12Hr;
	@FXML
	public ToggleButton tbn24Hr;

	// Buttons for swapping date format from day-month vs month-day
	@FXML
	public ToggleButton tbnDayMonthYear;
	@FXML
	public ToggleButton tbnMonthDayYear;

	///
	/// FXML Bound fields end
	///

	// The original calendar, this should never be modified after assigned
	private Calendar original = Calendar.getInstance();
	// This is the edited date, which updates whenever the spinners chnage
	private Calendar dateToEdit = Calendar.getInstance();
	// Default date format is day month year 24hour:minute:seconds
	private DateFormat dateFormat = new SimpleDateFormat("dd MMMMM yyyy HH:mm:ss");

	/**
	 * Used to initialize the UI
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// The spinners can have the full range of integers
		this.spnYear.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnMonth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnDay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));
		this.spnSecond.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 0));

		// When any of the spinners change, we recalculate the date labels
		EasyBind.subscribe(this.spnYear.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnMonth.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnDay.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnHour.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnMinute.valueProperty(), this::updateDate);
		EasyBind.subscribe(this.spnSecond.valueProperty(), this::updateDate);

		// When we select a new time or date format, we update the date labels too
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

	/**
	 * Takes the original date and calculates the new date based on the spinner positions
	 *
	 * @param ignored
	 */
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

	/**
	 * Re-calculates the date format based on the state of the toggle buttons and refreshes the label
	 */
	private void refreshDateFormat()
	{
		this.dateFormat = new SimpleDateFormat(String.format("%s yyyy %s", this.tbnDayMonthYear.isSelected() ? "dd MMMMM" : "MMMMM dd", this.tbn12Hr.isSelected() ? "hh:mm:ss aa" : "HH:mm:ss"));
		this.refreshLabel();
	}

	/**
	 * Sets the date label to be the original date + the recalculated date
	 */
	private void refreshLabel()
	{
		this.lblDate.setText(dateFormat.format(this.original.getTime()) + " -> " + dateFormat.format(this.dateToEdit.getTime()));
	}

	/**
	 * This should be called once, sets the original date and updates the label
	 *
	 * @param date The original date to be edited
	 */
	public void setDate(Date date)
	{
		this.original.setTime((Date) date.clone());
		this.dateToEdit.setTime(this.original.getTime());
		this.refreshLabel();
	}

	/**
	 * Getter for the modified date
	 *
	 * @return The new date
	 */
	public Date getDate()
	{
		return this.dateToEdit.getTime();
	}

	/**
	 * When confirm is pressed, we close the window
	 *
	 * @param mouseEvent
	 */
	public void confirmPressed(MouseEvent mouseEvent)
	{
		((Stage) this.tbn12Hr.getScene().getWindow()).close();
	}

	/**
	 * When cancel is pressed, we set the new date to be the original date, and then close the window
	 * @param mouseEvent
	 */
	public void cancelPressed(MouseEvent mouseEvent)
	{
		this.dateToEdit = original;
		confirmPressed(mouseEvent);
	}
}
