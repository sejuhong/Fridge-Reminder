package edu.calpoly.cpe409.fridgereminder;

import android.app.Application;
import android.util.Log;

public class FridgeReminderApp extends Application {
	private final static String TAG = "FridgeReminderApp";
	private static FridgeReminderApp singleton;

	public static FridgeReminderApp getInstance() {
		return singleton;
	}

	private FridgeDBAdapter m_productDB;

	public FridgeDBAdapter getFridgeDB() {
		return m_productDB;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate()");
		singleton = this;

		m_productDB = new FridgeDBAdapter(this);
		m_productDB.open();

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminate()");
		m_productDB.close();
	}

}
