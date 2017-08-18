package model.util;

public class RoundingUtils
{
	public static double round(double number, int decimalPlaces)
	{
		double factor = Math.pow(10, decimalPlaces);
		return Math.round(number * factor) / factor;
	}

	public static double roundLat(double latitude)
	{
		return round(latitude, 4);
	}

	public static double roundLng(double longitude)
	{
		return round(longitude, 4);
	}
}
