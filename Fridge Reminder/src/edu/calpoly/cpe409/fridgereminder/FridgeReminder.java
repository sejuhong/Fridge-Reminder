package edu.calpoly.cpe409.fridgereminder;

import edu.calpoly.cpe409.fridgereminder.ProductView.OnProductChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class FridgeReminder extends Activity implements
		OnProductChangeListener, OnSharedPreferenceChangeListener {

	private static final int ADD_PRODUCT_MENU = 10;
	private static final int CHECK_FRIDGE_MENU = 12;

	protected static final String PRODUCT_NAME = "edu.calpoly.cpe409.fridgereminder.product_name";
	protected static final String PRODUCT_BOUGHT_DATE = "edu.calpoly.cpe409.fridgereminder.product_bought_date";
	protected static final String PRODUCT_DAYS_LEFT = "edu.calpoly.cpe409.fridgereminder.product_days_left";
	protected static final String PRODUCT_COUNT = "edu.calpoly.cpe409.fridgereminder.product_count";
	protected static final String PRODUCT_CATEGORY = "edu.calpoly.cpe409.fridgereminder.product_cateogry";
	private static final int EDIT_ITEM_SUB_ACTIVITY = 54;

	private static final int PREFERENCES_MENU = 13;
	private static final String TAG = "FridgeReminder";
	private static final int CONTEXT_MENU_ONE_LESS = 300;
	private static final int CONTEXT_MENU_ONE_MORE = 301;
	private static final int CONTEXT_MENU_REMOVE = 302;
	private static final int CONTEXT_NENU_EDIT = 303;

	private Cursor displayedProducts;
	/**
	 * Adapter used to bind an AdapterView to List of Produce.
	 */
	protected ProductCursorAdapter productAdapter;

	private boolean inMainScreen;

	protected Cursor getDisplayedProducts() {
		return displayedProducts;
	}

	public void initDisplayedItems(SharedPreferences sharedPreferences) {
		Resources resources = getResources();

		String sortType = sharedPreferences.getString(
				resources.getString(R.string.pref_display_sort),
				"expiration_date");

		String sortOrder = sharedPreferences.getString(
				resources.getString(R.string.pref_display_sort_order), "DESC");

		displayedProducts = FridgeReminderApp.getInstance().getFridgeDB()
				.getAllProducts(sortType, sortOrder);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case (AddItemHistoryActivity.ADD_ITEM_SUBACTIVITY):
			if (resultCode == Activity.RESULT_OK) {
				displayedProducts.requery();
				updateScreen();
			}

			break;
		case (EDIT_ITEM_SUB_ACTIVITY):
			if (resultCode == Activity.RESULT_OK) {
				Product temp = (Product) data.getExtras().get(
						AddItemActivity.PRODUCT_ITEM);

				boolean success = FridgeReminderApp.getInstance().getFridgeDB()
						.updateProduct(temp);
				Log.i("FridgeReminder", "Update Item: " + success);
				displayedProducts.requery();
				updateScreen();
			}

			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		Product temp;

		switch (item.getItemId()) {
		case CONTEXT_MENU_ONE_LESS:
			temp = FridgeReminderApp.getInstance().getFridgeDB()
					.getProduct(productAdapter.getSelectionID());

			if (temp.getCount() > 0)
				temp.setCount(temp.getCount() - 1);

			onProductChanged(null, temp);
			break;
		case CONTEXT_MENU_ONE_MORE:
			temp = FridgeReminderApp.getInstance().getFridgeDB()
					.getProduct(productAdapter.getSelectionID());

			if (temp.getCount() == -1)
				temp.setCount(1);
			else
				temp.setCount(temp.getCount() + 1);

			onProductChanged(null, temp);
			break;
		case CONTEXT_MENU_REMOVE:
			FridgeReminderApp.getInstance().getFridgeDB()
					.remove(productAdapter.getSelectionID());

			displayedProducts.requery();
			updateScreen();
			break;
		case CONTEXT_NENU_EDIT:

			Intent intent = new Intent(getApplicationContext(),
					AddItemActivity.class);
			intent.setAction(AddItemActivity.ACTION_EDIT_ITEM);

			ProductView productView = (ProductView) ((AdapterContextMenuInfo) item
					.getMenuInfo()).targetView;

			intent.putExtra(AddItemActivity.PRODUCT_ITEM,
					productView.getProduct());

			startActivityForResult(intent, EDIT_ITEM_SUB_ACTIVITY);
			break;
		}

		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FridgeNotificationManager.initAlarm(this);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		initDisplayedItems(prefs);
		startManagingCursor(displayedProducts);

		inMainScreen = false;

		productAdapter = new ProductCursorAdapter(this, displayedProducts);
		productAdapter.setOnProductChangeListener(this);

		updateScreen();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(0, CONTEXT_MENU_ONE_LESS, 0, "One Less");
		menu.add(0, CONTEXT_MENU_ONE_MORE, 0, "One More");
		menu.add(0, CONTEXT_MENU_REMOVE, 0, "Remove");
		menu.add(0, CONTEXT_NENU_EDIT, 0, "Edit");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_PRODUCT_MENU, 0, "New Product").setIcon(
				android.R.drawable.ic_menu_add);

		menu.add(0, CHECK_FRIDGE_MENU, 1, "Check Fridge").setIcon(
				R.drawable.ic_menu_refresh);
		
		menu.add(0, PREFERENCES_MENU, 2, "Settings").setIcon(
				android.R.drawable.ic_menu_preferences);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (ADD_PRODUCT_MENU):
			Intent intent = new Intent(this, AddItemHistoryActivity.class);
			startActivityForResult(intent,
					AddItemHistoryActivity.ADD_ITEM_SUBACTIVITY);
			break;
		case (PREFERENCES_MENU):
			startActivity(new Intent(this, FridgeReminderPreferences.class));
			break;
		case (CHECK_FRIDGE_MENU):
			FridgeNotificationManager.createNotification(this,
					displayedProducts);
			break;
		default:
			Log.e("FridgeReminder", "Bad menu ID");
			break;
		}

		return true;
	}

	@Override
	public void onProductChanged(ProductView view, Product product) {
		if (FridgeReminderApp.getInstance().getFridgeDB()
				.updateProduct(product)) {
			displayedProducts.requery();
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		inMainScreen = savedInstanceState.getBoolean("inMainScreen");

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("inMainScreen", inMainScreen);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Resources resources = getResources();

		if (key.equals(resources.getString(R.string.pref_display_sort))
				|| key.equals(resources
						.getString(R.string.pref_display_sort_order))) {

			initDisplayedItems(sharedPreferences);
			productAdapter.changeCursor(displayedProducts);

		}

	}

	private void updateScreen() {
		if (displayedProducts.getCount() == 0) {
			setContentView(R.layout.main_help);
			inMainScreen = false;
		} else {
			displayedProducts.requery();

			if (!inMainScreen) {
				setContentView(R.layout.main);

				ListView listView = (ListView) findViewById(R.id.ProductListView);
				registerForContextMenu(listView);
				listView.setOnItemLongClickListener(productAdapter);
				listView.setAdapter(productAdapter);

				inMainScreen = true;
			}
		}
	}
}