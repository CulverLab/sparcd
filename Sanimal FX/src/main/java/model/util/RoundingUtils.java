package model.util;

/**
 * Class used to round doubles to a certain number of decimal places
 */
public class RoundingUtils
{
	/**
	 * Rounds a number to a given number of decimal places
	 *
	 * @param number The number to round
	 * @param decimalPlaces The number of decimal places to round to
	 * @return The rounded double
	 */
	public static double round(double number, int decimalPlaces)
	{
		// Get the factor to multiply the number by to begin rounding. Just multiply 10 by itself decimalPlaces amount of times
		double factor = Math.pow(10, decimalPlaces);
		// Return the rounded value. We do this by multiplying the number by the factor and then dividing truncating the decimal place
		return Math.round(number * factor) / factor;
	}

	/**
	 * Given a latitude we round it to 4 decimal places
	 *
	 * @param latitude The latitude to round
	 * @return The rounded latitude
	 */
	public static double roundLat(double latitude)
	{
		return round(latitude, 4);
	}

	/**
	 * Given a longitude we round it to 4 decimal places
	 *
	 * @param longitude The longitude to round
	 * @return The rounded longitude
	 */
	public static double roundLng(double longitude)
	{
		return round(longitude, 4);
	}
}
