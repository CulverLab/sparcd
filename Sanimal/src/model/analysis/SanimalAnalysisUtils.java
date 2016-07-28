/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model.analysis;

import java.time.temporal.ChronoUnit;
import java.util.Date;

public class SanimalAnalysisUtils
{
	public static long daysBetween(Date date1, Date date2)
	{
		if (date1 != null && date2 != null)
			return ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant());
		else
			return 0;
	}
	
	public static double distanceBetween(double lat1, double lng1, double lat2, double lng2)
	{
		//		var lat1Rad = lat1.toRadians(), lat2Rad = lat2.toRadians(), delta = (lon2-lon1).toRadians(), R = 6371e3; // gives d in metres
		//	    var d = Math.acos( Math.sin(lat1Rad)*Math.sin(lat2Rad) + Math.cos(lat1Rad)*Math.cos(lat2Rad) * Math.cos(delta) ) * R;
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double delta = Math.toRadians(lng2 - lng1);
		double R = 6371.000;
		return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(delta)) * R;
	}
}
