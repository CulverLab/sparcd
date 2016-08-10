package library;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Library used in moon phase calculations
 * 
 * @author https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=5&ved=0ahUKEwiHgtX7orXOAhUeHGMKHeblD9QQFggzMAQ&url=http%3A%2F%2Fmerganser.
 *         math.gvsu.edu%2Fmyth%2Fmoon%2FMoonCalculator.java&usg=AFQjCNHd1NL2Ssf9XjsqNN8NlvMJF_YpGA&sig2=5LgL0HTnFOQ5kXO2Nz41HA&cad=rja
 */
public class MoonCalculator
{

	static final double EG = 279.403303;
	static final double WG = 282.768422;
	static final double ECCENTRICITY = 0.016713;
	static final double R0 = 1.495985E8;
	static final double THETA0 = 0.533128;
	static final double L0 = 318.351648;
	static final double P0 = 36.340410;
	static final double N0 = 318.510107;
	static final double INCLINATION = 5.145396;
	static final double MOONECCENTRICITY = 0.054900;
	static final double a = 384401;
	static final double MOONTHETA0 = 0.5181;
	static final double PI0 = 0.9507;
	static final double JULIANATEPOCH1990 = 2447891.5;
	static final double JULIANATEPOCH1970 = 2447891.5 - 20 * 365 - 4;
	static final double SYNODICMONTH = 29.5306 / (2 * Math.PI);

	static final String[] phaseLabels =
	{ "Julian Date:        ", "Sun's Distance:     ", "Sun's Angular Size: ", "Moon's Phase:       ", "Moon's Percent:     " };

	public static final int JULIANDATE = 0;
	public static final int SUNDISTANCE = 1;
	public static final int SUNANGULARSIZE = 2;
	public static final int MOONPHASE = 3;
	public static final int MOONPERCENT = 4;
	public static final int PHASEOPTIONS = 5;

	public static double[] getPhase(double jd)
	{
		double[] results = new double[PHASEOPTIONS];
		jd += 50.0 / (60 * 60 * 24);
		results[JULIANDATE] = jd;
		double diff = jd - JULIANATEPOCH1990;

		double N = 360 * diff / 365.242191;
		N = putIntoRange(N);

		double Mdot = putIntoRange(N + EG - WG);
		double EC = 360 * ECCENTRICITY * Math.sin(toRadians(Mdot)) / Math.PI;
		double lambdaDot = putIntoRange(N + EC + EG);

		double nu = Mdot + EC;
		double f = (1 + ECCENTRICITY * Math.cos(toRadians(nu))) / (1 - ECCENTRICITY * ECCENTRICITY);
		double r = R0 / f;
		double theta = f * THETA0;
		results[SUNDISTANCE] = r;
		results[SUNANGULARSIZE] = toDegrees(theta);

		double l = putIntoRange(13.1763966 * diff + L0);
		double Mm = putIntoRange(l - 0.1114041 * diff - P0);

		N = putIntoRange(N0 - 0.0529539 * diff);
		double Enu = 1.2739 * Math.sin(toRadians(2 * (l - lambdaDot) - Mm));

		double Ae = 0.1858 * Math.sin(toRadians(Mdot));
		double A3 = 0.37 * Math.sin(toRadians(Mdot));

		double Mmprime = Mm + Enu - Ae - A3;

		double Ec = 6.2886 * Math.sin(toRadians(Mmprime));
		double A4 = 0.214 * Math.sin(toRadians(2 * Mmprime));
		double lprime = l + Enu + Ec - Ae + A4;

		double V = 0.6583 * Math.sin(toRadians(2 * (lprime - lambdaDot)));
		double lpp = lprime + V;
		double Nprime = N - 0.16 * Math.sin(toRadians(Mdot));
		double y = Math.sin(toRadians(lpp - Nprime)) * Math.cos(toRadians(INCLINATION));
		double x = Math.cos(toRadians(lpp - Nprime));
		double angle = toDegrees(Math.atan2(y, x));
		double lambdam = putIntoRange(angle + Nprime);

		double DD = lpp - lambdaDot;
		results[MOONPHASE] = DD;
		double F = (1 - Math.cos(toRadians(DD))) / 2;
		results[MOONPERCENT] = F;
		return results;
	}

	public static double getJulian(Date date)
	{
		double daysFromEpoch = (double) (date.getTime() / 1000);
		daysFromEpoch /= 60 * 60 * 24;
		return daysFromEpoch + JULIANATEPOCH1970;
	}

	public static double putIntoRange(double d)
	{
		while (d < 0)
			d += 360;
		while (d >= 360)
			d -= 360;
		return d;
	}

	public static long toMillisFromJulian(double jd)
	{
		double dj = jd - JULIANATEPOCH1970;
		return (long) (dj * (24 * 60 * 60 * 1000));
	}

	public static void getInformation(Calendar cal)
	{
		Date now = cal.getTime();
		double julianDate = getJulian(now);
		double[] phases = getPhase(julianDate);
		//for (int i = 0; i < PHASEOPTIONS; i++)
		//	System.out.println(phaseLabels[i] + phases[i]);

		double fullmoon = getLunation(julianDate, phases[MOONPHASE], 180);
		long millis = toMillisFromJulian(fullmoon);
		Date fullMoonDate = new Date(millis);
		System.out.println("Full Moon: " + (new SimpleDateFormat("MMMM d, yyyy 'at' HH:mm:ss z")).format(fullMoonDate));
		/*
		System.out.println((new SimpleDateFormat("MMMM d, yyyy 'at' HH:mm:ss z")).format(cal.getTime()));
		*/
	}

	public static void main(String[] args)
	{
		GregorianCalendar cal = new GregorianCalendar();
		getInformation(cal);
		//getInformation(new GregorianCalendar(1979, Calendar.FEBRUARY, 26, 11, 0));
		//getInformation(new GregorianCalendar(2003, Calendar.APRIL, 16, 15, 53));
		//        fromJulian(getJulian(cal));
	}

	public static double getLunation(double julian, double phase, double lunation)
	{
		phase = toRadians(phase);
		lunation = toRadians(lunation);
		double dx = Math.cos(phase) - Math.cos(lunation);
		double dy = Math.sin(phase) - Math.sin(lunation);
		if (dx * dx + dy * dy < 1e-14)
			return julian;

		// Added this
		double dl = 0;
		if (lunation == 0)
			dl = -Math.abs(phase - lunation);
		else
			dl = phase - lunation;
		// Added this (used to just be "double dl = phase - lunation;")

		double newjulian = julian - dl * SYNODICMONTH;
		double[] newphase = getPhase(newjulian);
		//System.out.println(newjulian + " " + newphase[MOONPHASE]);
		return getLunation(newjulian, newphase[MOONPHASE], toDegrees(lunation));
	}

	public static double getJulian(GregorianCalendar cal)
	{
		int year = cal.get(Calendar.YEAR);
		//System.out.println("Year " + year);
		int month = cal.get(Calendar.MONTH) + 1;
		//System.out.println("Month " + month);
		double day = (double) cal.get(Calendar.SECOND);
		//System.out.println("Second " + day);
		double minute = (double) cal.get(Calendar.MINUTE);
		//System.out.println("Minute " + minute);
		day = day / 60 + minute;
		double hour = (double) (cal.get(Calendar.HOUR_OF_DAY) - (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000 * 60 * 60));
		//System.out.println("Hour " + hour);
		day = day / 60 + hour;
		double dofmonth = (double) cal.get(Calendar.DAY_OF_MONTH);
		//System.out.println("Day " + dofmonth);
		day = day / 24 + dofmonth;
		if (month < 3)
		{
			month += 12;
			year -= 1;
		}
		double A = integerPart(year / 100);
		double B = 2 - A + integerPart(A / 4);
		if (cal.before(cal.getGregorianChange()))
			B = 0;
		double C;
		if (year < 0)
			C = integerPart(365.25 * year - 0.75);
		else
			C = integerPart(365.25 * year);
		double D = integerPart(30.6001 * (month + 1));

		return B + C + D + day + 1720994.5;
	}

	public static int[] fromJulian(double jd)
	{
		jd += 0.5;
		int i = (int) Math.floor(jd);
		double F = jd - (double) i;
		int B;
		if (i > 2299160)
		{
			int A = (int) Math.floor((i - 1867216.5) / 36524.25);
			B = i + 1 + A - (int) Math.floor(A / 4);
		}
		else
			B = i;
		int C = B + 1524;
		int D = (int) Math.floor((C - 122.1) / 365.25);
		int E = (int) Math.floor(365.25 * D);
		int G = (int) Math.floor((C - E) / 30.6001);
		double d = C - E + F - Math.floor(30.6001 * G);
		int m;
		if (G < 13.5)
			m = G - 1;
		else
			m = G - 13;
		int y;
		if (m > 2.5)
			y = D - 4716;
		else
			y = D - 4715;
		//System.out.println(d + " " + m + " " + y);
		int day = (int) Math.floor(d);
		double hours = (d - day) * 24;
		int hr = (int) Math.floor(hours);
		double mins = (hours - hr) * 60;
		int min = (int) Math.floor(mins);
		int secs = (int) Math.floor((mins - min) * 60);
		//System.out.println(y + " " + m + " " + day + " " + hr + " " + min + " " + secs);

		return new int[2];
	}

	public static double integerPart(double d)
	{
		if (d >= 0)
			return Math.floor(d);
		return -Math.floor(-d);
	}

	public static double toRadians(double deg)
	{
		return deg * Math.PI / 180;
	}

	public static double toDegrees(double rad)
	{
		return rad * 180 / Math.PI;
	}
}
