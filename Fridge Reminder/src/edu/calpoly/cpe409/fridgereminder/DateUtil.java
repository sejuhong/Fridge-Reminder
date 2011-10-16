package edu.calpoly.cpe409.fridgereminder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.util.Log;

/**
 * Calculates the date between buy date and expiration date
 */
public class DateUtil {
	public static final int MILISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

	/**
	 * Returns the length between two dates
	 * @param start date in Date
	 * @param end date in Date
	 * @return the length between the two dates in long
	 */
	public static long daysBetween(Date start, Date end) {
		Calendar endCal = new GregorianCalendar();
		Calendar startCal = new GregorianCalendar();

		endCal.setTimeInMillis(start.getTime());
		endCal.set(Calendar.HOUR_OF_DAY, 0);
		endCal.set(Calendar.MINUTE, 0);
		endCal.set(Calendar.SECOND, 0);
		endCal.set(Calendar.MILLISECOND, 0);
		
		startCal.setTimeInMillis(end.getTime());
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.MILLISECOND, 0);

		Log.d("DateUtil", startCal.getTimeInMillis()
				- endCal.getTimeInMillis() + "");
		return (startCal.getTimeInMillis()
				- endCal.getTimeInMillis()) / MILISECONDS_PER_DAY;
	}
	
	/**
	 * Returns the length between two dates
	 * @param start date in Calendar
	 * @param end date in Calendar
	 * @return the length between the two dates in long
	 */
	public static long daysBetween(Calendar start, Calendar end) {
		return daysBetween(start.getTime(), end.getTime());
	}
}
