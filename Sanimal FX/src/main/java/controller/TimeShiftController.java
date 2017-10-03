package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
	public Label lblOriginalDate;
	@FXML
	public Label lblNewDate;

	///
	/// FXML Bound fields end
	///

	private Calendar original = Calendar.getInstance();
	private Calendar dateToEdit = Calendar.getInstance();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMMM yyyy HH:mm:ss");

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

		this.lblNewDate.setText("New Date: " + DATE_FORMAT.format(this.dateToEdit.getTime()));
	}

	public void setDate(Date date)
	{
		this.original.setTime((Date) date.clone());
		this.dateToEdit.setTime(this.original.getTime());
		this.lblOriginalDate.setText("Original Date: " + DATE_FORMAT.format(this.original.getTime()));
	}

	public Date getDate()
	{
		return this.dateToEdit.getTime();
	}
}
