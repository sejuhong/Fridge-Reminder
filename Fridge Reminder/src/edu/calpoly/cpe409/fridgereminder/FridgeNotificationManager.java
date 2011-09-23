package edu.calpoly.cpe409.fridgereminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

public class FridgeNotificationManager extends BroadcastReceiver {
	private static final String ACTION_CHECK_FRIDGE = "edu.calpoly.cpe409.fridgereminder.action.UPDATE_ITEMS";
	private static final int EXPIRED_NOTIFICATION = 100;
	private static final int WARNING_NOTIFICATION = 110;
	private static final String UPDATE_DATE_PREF = "edu.calpoly.cpe409.fridgereminder.pref.update_date";
	private static final String ALREADY_RAN_KEY = "edu.calpoly.cpe409.fridgereminder.key.ran_once_b3";
	private static final String FRIDGE_REMINDER_SHARED_PREFS = "edu.calpoly.cpe409.fridgereminder.shared_preferences";
	private static final String TAG = "FridgeNotificationManager";

	public static void createNotification(Context context, Cursor products) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification expirationNotification = new Notification(R.drawable.icon,
				"Expired food", System.currentTimeMillis());
		Notification warningNotification = new Notification(R.drawable.icon,
				"Food is about to expire", System.currentTimeMillis());

		ArrayList<Product> expiredProducts = new ArrayList<Product>();
		ArrayList<Product> warningProducts = new ArrayList<Product>();
		Date today = new Date();

		int daysBefore = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						context.getResources().getString(
								R.string.pref_days_before), "1"));

		if (products.moveToFirst()) {
			do {
				Product product = FridgeDBAdapter
						.getProductFromCursor(products);

				long daysLeft = DateUtil.daysBetween(today,
						product.getExpirationDate());
				daysLeft = daysLeft < 0 ? 0 : daysLeft;

				if (daysLeft <= 0)
					expiredProducts.add(product);
				else if (daysLeft == daysBefore)
					warningProducts.add(product);

			} while (products.moveToNext());
		}

		// Process expiration notifications
		String message = null;
		int numExpired = expiredProducts.size();

		if (numExpired == 1) {
			message = expiredProducts.get(0).getName().trim() + " has expired!";
			expirationNotification.number = 1;
		} else if (numExpired > 1) {
			message = "Food has expired!";
			expirationNotification.number = numExpired;
		}

		if (numExpired > 0) {
			expirationNotification.setLatestEventInfo(context, "Expired Food!",
					message, PendingIntent.getActivity(context, 0, new Intent(
							context, FridgeReminder.class), 0));

			expirationNotification.defaults = Notification.DEFAULT_ALL;
			expirationNotification.flags |= Notification.FLAG_AUTO_CANCEL;

			mNotificationManager.notify(WARNING_NOTIFICATION,
					expirationNotification);
		}

		// Process warning notifications
		message = null;
		int numAboutToExpire = warningProducts.size();

		if (numAboutToExpire == 1) {
			message = warningProducts.get(0).getName().trim()
					+ " will expire in " + daysBefore + " day(s)!";
			warningNotification.number = 1;
		} else if (numExpired > 1) {
			message = "Food about to expire!";
			warningNotification.number = numAboutToExpire;
		}

		if (numAboutToExpire > 0) {
			warningNotification.setLatestEventInfo(context, "Food going bad!",
					message, PendingIntent.getActivity(context, 0, new Intent(
							context, FridgeReminder.class), 0));

			warningNotification.defaults = Notification.DEFAULT_ALL;
			warningNotification.flags |= Notification.FLAG_AUTO_CANCEL;

			mNotificationManager.notify(EXPIRED_NOTIFICATION,
					warningNotification);
		}
	}

	public static void doProductCheck(Context context) {
		FridgeDBAdapter pDB = new FridgeDBAdapter(context);
		pDB.open();
		Cursor products = pDB.getAllProducts();

		createNotification(context, products);

		products.requery();
		products.close();
		pDB.close();
	}

	public static void initAlarm(Context context) {
		SharedPreferences settings = context.getSharedPreferences(
				FRIDGE_REMINDER_SHARED_PREFS, Activity.MODE_PRIVATE);
		boolean firstRun = settings.getBoolean(ALREADY_RAN_KEY, false);

		if (!firstRun) {
			startAlarm(context, 0);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(ALREADY_RAN_KEY, true).commit();
		}
	}

	protected static void startAlarm(Context context, int hourOfDay) {
		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(ACTION_CHECK_FRIDGE);
		PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
				0);

		alarms.cancel(pIntent);

		// Set alarm based on user settings
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		alarms.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				DateUtil.MILISECONDS_PER_DAY, pIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(ACTION_CHECK_FRIDGE)) {
			Log.d(TAG, "Checking fridge...");
			doProductCheck(context);
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(TAG, "Recreating alarm");

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);

			int hourOfDay = Integer.parseInt(prefs.getString(context
					.getResources().getString(R.string.pref_hour_of_day), "0"));

			FridgeNotificationManager.startAlarm(context, hourOfDay);
		}

	}
}