package controller.importView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.SanimalData;
import org.fxmisc.easybind.EasyBind;

import java.net.URL;
import java.time.LocalDateTime;
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

	///
	/// FXML Bound fields end
	///

	// The original calendar
	private LocalDateTime original = LocalDateTime.now();
	// This is the edited date, which updates whenever the spinners chnage
	private LocalDateTime dateToEdit = LocalDateTime.now();

	private boolean dateConfirmed = false;

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
	}

	/**
	 * Takes the original date and calculates the new date based on the spinner positions
	 *
	 * @param ignored
	 */
	private void updateDate(Integer ignored)
	{
		this.dateToEdit = this.original
				.plusYears(this.spnYear.getValue())
				.plusMonths(this.spnMonth.getValue())
				.plusDays(this.spnDay.getValue())
				.plusHours(this.spnHour.getValue())
				.plusMinutes(this.spnMinute.getValue())
				.plusSeconds(this.spnSecond.getValue());

		this.refreshLabel();
	}

	/**
	 * Sets the date label to be the original date + the recalculated date
	 */
	private void refreshLabel()
	{
		this.lblDate.setText(SanimalData.getInstance().getSettings().formatDateTime(this.original, " ") + " -> " + SanimalData.getInstance().getSettings().formatDateTime(this.dateToEdit, " "));
	}

	/**
	 * This should be called once, sets the original date and updates the label
	 *
	 * @param date The original date to be edited
	 */
	public void setDate(LocalDateTime date)
	{
		dateConfirmed = false;
		this.original = date; // No other way to clone?
		this.dateToEdit = date;
		this.spnDay.getValueFactory().setValue(0);
		this.spnHour.getValueFactory().setValue(0);
		this.spnMinute.getValueFactory().setValue(0);
		this.spnSecond.getValueFactory().setValue(0);
		this.spnMonth.getValueFactory().setValue(0);
		this.spnYear.getValueFactory().setValue(0);
		this.refreshLabel();
	}

	/**
	 * Getter for the modified date
	 *
	 * @return The new date
	 */
	public LocalDateTime getDate()
	{
		return this.dateToEdit;
	}

	/**
	 * Test to see if the date was confirmed with the "confirm" button
	 *
	 * @return True if the user pressed confirm, false if he pressed cancel or the X button
	 */
	public boolean dateWasConfirmed()
	{
		return this.dateConfirmed;
	}

	/**
	 * When confirm is pressed, we close the window
	 *
	 * @param mouseEvent consumed
	 */
	public void confirmPressed(MouseEvent mouseEvent)
	{
		dateConfirmed = true;
		((Stage) this.spnDay.getScene().getWindow()).close();
		mouseEvent.consume();
	}

	/**
	 * When cancel is pressed, we set the new date to be the original date, and then close the window
	 *
	 * @param mouseEvent consumed
	 */
	public void cancelPressed(MouseEvent mouseEvent)
	{
		((Stage) this.spnDay.getScene().getWindow()).close();
		mouseEvent.consume();
	}
}
