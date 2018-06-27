package model.util;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Class containing sanimal settings
 */
public class SettingsData
{
	// A list of settings SANIMAL uses
	private transient ObservableList<CustomPropertyItem<?>> settingList = FXCollections.observableArrayList(item -> new Observable[] { item.getObservableValue().get() });

	// The current setting value
	private ObjectProperty<DateFormat> dateFormat = new SimpleObjectProperty<>(DateFormat.MonthDayYear);
	private ObjectProperty<TimeFormat> timeFormat = new SimpleObjectProperty<>(TimeFormat.Time24Hour);
	private ObjectProperty<LocationFormat> locationFormat = new SimpleObjectProperty<>(LocationFormat.LatLong);
	private ObjectProperty<DistanceUnits> distanceUnits = new SimpleObjectProperty<>(DistanceUnits.Meters);
	private ObjectProperty<Double> popupDelaySec = new SimpleDoubleProperty(10).asObject();
	private BooleanProperty drSandersonDirectoryCompatibility = new SimpleBooleanProperty(false);
	private BooleanProperty drSandersonOutput = new SimpleBooleanProperty(false);
	private BooleanProperty automaticNextImage = new SimpleBooleanProperty(false);
	private BooleanProperty backgroundImageLoading = new SimpleBooleanProperty(false);
	private BooleanProperty disablePopups = new SimpleBooleanProperty(false);

	/**
	 * Constructor adds all settings SANIMAL will use to the dictionary
	 */
	public SettingsData()
	{
		this.setupPropertyPageItems();
	}

	/**
	 * Called if settings are pulled from the cloud and need to be copied into the singleton settings class
	 *
	 * @param otherSettings The settings to copy from
	 */
	public void loadFromOther(SettingsData otherSettings)
	{
		this.dateFormat.setValue(otherSettings.getDateFormat());
		this.timeFormat.setValue(otherSettings.getTimeFormat());
		this.locationFormat.setValue(otherSettings.getLocationFormat());
		this.distanceUnits.setValue(otherSettings.getDistanceUnits());
		this.popupDelaySec.setValue(otherSettings.getPopupDelaySec());
		this.drSandersonDirectoryCompatibility.setValue(otherSettings.getDrSandersonDirectoryCompatibility());
		this.drSandersonOutput.setValue(otherSettings.getDrSandersonOutput());
		this.automaticNextImage.setValue(otherSettings.getAutomaticNextImage());
		this.backgroundImageLoading.setValue(otherSettings.getBackgroundImageLoading());
		this.disablePopups.setValue(otherSettings.getDisablePopups());
	}

	/**
	 * Initializes the settings available
	 */
	private void setupPropertyPageItems()
	{
		settingList.add(new CustomPropertyItem<>("Date Format: ", "DateTime", "The date format to be used when displaying dates", dateFormat, DateFormat.class));
		settingList.add(new CustomPropertyItem<>("Time Format: ", "DateTime", "The time format to be used when displaying dates", timeFormat, TimeFormat.class));
		settingList.add(new CustomPropertyItem<>("Location Format: ", "Location", "The location format to be used when displaying positional information", locationFormat, LocationFormat.class));
		settingList.add(new CustomPropertyItem<>("Distance Units: ", "Units", "The units to be used by the program", distanceUnits, DistanceUnits.class));
		settingList.add(new CustomPropertyItem<>("Popup Hide Delay (in seconds): ", "Options", "How many seconds the popup should wait before disappearing", popupDelaySec, Double.class));
		settingList.add(new CustomPropertyItem<>("Dr. Sanderson's Directory Compatibility: ", "Legacy", "Gives the option to read a directory in Dr. Jim Sanderson's format and automatically tag it", drSandersonDirectoryCompatibility, Boolean.class));
		settingList.add(new CustomPropertyItem<>("Show Dr. Sanderson's Output Replicas: ", "Legacy", "Gives the option to see Dr. Jim Sanderson's Output.txt and AllPictures.txt replicas when querying", drSandersonOutput, Boolean.class));
		settingList.add(new CustomPropertyItem<>("Automatically Select Next Image: ", "Legacy", "Automatically select the next image after tagging one with species", automaticNextImage, Boolean.class));
		settingList.add(new CustomPropertyItem<>("Background Image Loading: ", "Options", "Load images in the background when selecting them, useful for slow hard drives or SD cards", backgroundImageLoading, Boolean.class));
		settingList.add(new CustomPropertyItem<>("Disable Popups: ", "Options", "Lose some program functionality to avoid popups at all costs", disablePopups, Boolean.class));
	}

	/**
	 * Utility method used to format a date with the proper format
	 *
	 * @param date The date to format
	 * @return A string representing the date in the proper format
	 */
	public String formatDate(LocalDate date)
	{
		return this.dateFormat.getValue().format(date);
	}

	/**
	 * Utility method used to format a time with the proper format
	 *
	 * @param time The time to format
	 * @return A string representing the time in the proper format
	 */
	public String formatTime(LocalTime time)
	{
		return this.timeFormat.getValue().format(time);
	}

	/**
	 * Utility method used to format a date & time with the proper format
	 *
	 * @param dateTime The date & time to format
	 * @param delimeter An optional delimeter put between date and time
	 * @return A string representing the date & time in the proper format
	 */
	public String formatDateTime(LocalDateTime dateTime, String delimeter)
	{
		return this.dateFormat.getValue().format(dateTime.toLocalDate()) + delimeter + this.timeFormat.getValue().format(dateTime.toLocalTime());
	}

	/**
	 * Date format specifies the possible date formats to be used
	 */
	public enum DateFormat
	{
		MonthDayYear("Month Day, Year -- January 3, 2011",        "MMMM dd',' yyyy"),
		ShortMonthDayYear("Short Month Day, Year -- Jan 3, 2011", "MMM dd',' yyyy"),
		NumericMonthDayYear("Numeric Month/Day/Year -- 1/3/2011", "M'/'d'/'yyyy"),
		DayMonthYear("Day Month, Year -- 3. January 2011",        "dd'.' MMMM yyyy"),
		ShortDayMonthYear("Day Short Month -- 3. Jan 2011",       "dd'.' MMM yyyy"),
		NumericDayMonthYear("Numeric Day/Month/Year -- 3/1/2011", "d'/'M'/'yyyy"),
		ISO("ISO Local Date -- 2011-1-3",                         DateTimeFormatter.ISO_LOCAL_DATE);

		// The string value is used for displaying purposes
		private String stringValue;
		// The date format actually formats the date
		private DateTimeFormatter formatter;

		/**
		 * Given a display value and a format as a strng, this constructor initializes all fields
		 *
		 * @param stringValue The string display value
		 * @param format The string format
		 */
		DateFormat(String stringValue, String format)
		{
			this.stringValue = stringValue;
			this.formatter = DateTimeFormatter.ofPattern(format);
		}

		/**
		 * Given a display value and a format as a DateTimeFormatter, this constructor initializes all fields
		 *
		 * @param stringValue The string display value
		 * @param formater The date time formatter
		 */
		DateFormat(String stringValue, DateTimeFormatter formater)
		{
			this.stringValue = stringValue;
			this.formatter = formater;
		}

		/**
		 * Returns the name of the setting as a string
		 *
		 * @return The display name of the date
		 */
		@Override
		public String toString()
		{
			return this.stringValue;
		}

		/**
		 * Formats a given date
		 *
		 * @param date The date to format
		 * @return The string representing the formatted date
		 */
		public String format(LocalDate date)
		{
			return date.format(formatter);
		}
	}

	/**
	 * Time format specifies the possible time formats to be used
	 */
	public enum TimeFormat
	{
		Time24Hour("24 hour -- 14:36",                                        "H:mm"),
		Time24HourSeconds("24 hour with seconds -- 14:36:52",                 "H:mm:ss"),
		Time12HourAMPM("12 hour with AM/PM -- 2:36 pm",                       "h:mm a"),
		Time12HourSecondsAMPM("12 hour with AM/PM and seconds -- 2:36:52 pm", "h:mm:ss a");

		// The display name of the time format
		private String stringValue;
		// The actual formatter that formats times into strings
		private DateTimeFormatter formatter;

		/**
		 * Constructor takes a string representation and a time format
		 *
		 * @param stringValue The string representation of the time
		 * @param format The format pattern to be used
		 */
		TimeFormat(String stringValue, String format)
		{
			this.stringValue = stringValue;
			this.formatter = DateTimeFormatter.ofPattern(format);
		}

		/**
		 * Returns the string sample of the time
		 *
		 * @return A string showing what the time format looks like
		 */
		@Override
		public String toString()
		{
			return this.stringValue;
		}

		/**
		 * Formats a time with the formatter
		 *
		 * @param time The time to format
		 * @return The string formatted based on time format
		 */
		public String format(LocalTime time)
		{
			return time.format(formatter);
		}
	}

	/**
	 * Location format used to specify UTM or Lat/Lng
	 */
	public enum LocationFormat
	{
		LatLong("Latitude & Longitude"),
		UTM("UTM");

		// The display name of the format
		private String stringValue;

		/**
		 * Constructor just needs the display name of the location format
		 *
		 * @param stringValue The display name
		 */
		LocationFormat(String stringValue)
		{
			this.stringValue = stringValue;
		}

		/**
		 * Returns the string representation of the location format
		 *
		 * @return The display name of the location
		 */
		@Override
		public String toString()
		{
			return this.stringValue;
		}
	}

	/**
	 * Two different distance units are feet and meters
	 */
	public enum DistanceUnits
	{
		Feet(1 / 0.3048, 1D, "ft"),
		Meters(1D, 0.3048D, "m");

		private Double toMeters;
		private Double toFeet;
		private String symbol;

		/**
		 * Constructor takes in 3 parameters, X->meters, X->feet, and a symbol
		 *
		 * @param toMeters The conversion constant from X -> meters
		 * @param toFeet The conversion constant from X -> feet
		 * @param symbol The symbol of the distance unit
		 */
		DistanceUnits(Double toMeters, Double toFeet, String symbol)
		{
			this.toMeters = toMeters;
			this.toFeet = toFeet;
			this.symbol = symbol;
		}

		/**
		 * Formats feet into the given type
		 *
		 * @param value The value in feet to format
		 * @return The formatted value
		 */
		public Double formatToFeet(Double value)
		{
			return this.toFeet * value;
		}

		/**
		 * Formats meters into the given type
		 *
		 * @param value The value in meters to format
		 * @return The formatted value
		 */
		public Double formatToMeters(Double value)
		{
			return this.toMeters * value;
		}

		/**
		 * Gets the symbol for the distance units
		 *
		 * @return The symbol for the unit type
		 */
		public String getSymbol()
		{
			return this.symbol;
		}
	}

	///
	/// Getters/Setters
	///

	public ObservableList<CustomPropertyItem<?>> getSettingList()
	{
		return this.settingList;
	}

	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormat.set(dateFormat);
	}

	public DateFormat getDateFormat()
	{
		return dateFormat.get();
	}

	public ObjectProperty<DateFormat> dateFormatProperty()
	{
		return dateFormat;
	}

	public void setTimeFormat(TimeFormat timeFormat)
	{
		this.timeFormat.set(timeFormat);
	}

	public TimeFormat getTimeFormat()
	{
		return timeFormat.get();
	}

	public ObjectProperty<TimeFormat> timeFormatProperty()
	{
		return timeFormat;
	}

	public void setLocationFormat(LocationFormat locationFormat)
	{
		this.locationFormat.set(locationFormat);
	}

	public LocationFormat getLocationFormat()
	{
		return locationFormat.get();
	}

	public ObjectProperty<LocationFormat> locationFormatProperty()
	{
		return locationFormat;
	}

	public void setDistanceUnits(DistanceUnits distanceUnits)
	{
		this.distanceUnits.set(distanceUnits);
	}

	public DistanceUnits getDistanceUnits()
	{
		return distanceUnits.get();
	}

	public ObjectProperty<DistanceUnits> distanceUnitsProperty()
	{
		return distanceUnits;
	}

	public void setDrSandersonDirectoryCompatibility(boolean drSandersonDirectoryCompatibility)
	{
		this.drSandersonDirectoryCompatibility.set(drSandersonDirectoryCompatibility);
	}

	public boolean getDrSandersonDirectoryCompatibility()
	{
		return this.drSandersonDirectoryCompatibility.get();
	}

	public BooleanProperty drSandersonDirectoryCompatibilityProperty()
	{
		return drSandersonDirectoryCompatibility;
	}

	public void setAutomaticNextImage(boolean automaticNextImage)
	{
		this.automaticNextImage.set(automaticNextImage);
	}

	public boolean getAutomaticNextImage()
	{
		return automaticNextImage.get();
	}

	public BooleanProperty automaticNextImageProperty()
	{
		return automaticNextImage;
	}

	public void setBackgroundImageLoading(boolean backgroundImageLoading)
	{
		this.backgroundImageLoading.set(backgroundImageLoading);
	}

	public boolean getBackgroundImageLoading()
	{
		return backgroundImageLoading.get();
	}

	public BooleanProperty backgroundImageLoadingProperty()
	{
		return backgroundImageLoading;
	}

	public void setDrSandersonOutput(boolean drSandersonOutput)
	{
		this.drSandersonOutput.set(drSandersonOutput);
	}

	public boolean getDrSandersonOutput()
	{
		return drSandersonOutput.get();
	}

	public BooleanProperty drSandersonOutputProperty()
	{
		return drSandersonOutput;
	}

	public void setPopupDelaySec(Double popupDelaySec)
	{
		this.popupDelaySec.set(popupDelaySec);
	}

	public Double getPopupDelaySec()
	{
		return popupDelaySec.get();
	}

	public ObjectProperty<Double> popupDelaySecProperty()
	{
		return popupDelaySec;
	}

	public void setDisablePopups(boolean disablePopups)
	{
		this.disablePopups.set(disablePopups);
	}

	public Boolean getDisablePopups()
	{
		return this.disablePopups.get();
	}

	public BooleanProperty disablePopupsProperty()
	{
		return disablePopups;
	}
}
