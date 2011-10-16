package edu.calpoly.cpe409.fridgereminder;

import java.util.ArrayList;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

public class AddItemHistoryActivity extends Activity {

	private static final int CONTEXT_MENU_ADD_ITEM = 30;
	private static final int CONTEXT_MENU_REMOVE_ITEM = 31;
	private static final int DIALOG_SCAN_ITEM = 300;
	private static final int SCAN_SELECTION_BARCODE = 0;
	private static final int SCAN_SELECTION_RECEIPT = 1;
	private static final int SCAN_ITEM_FROM_RECEIPT_SUB_ACTIVITY = 53;
	private static final int DIALOG_FAILED_BARCODE_SCAN = 200;
	public static final int ADD_ITEM_SUBACTIVITY = 50;

	private static final String TAG = "AddItemHistoryActivity";

	private Cursor historyItems;
	private HistoryItemCursorAdapter adapter;
	private ListView historyList;
	private FrameLayout emptyText;
	private String barcodeContents;
	private String barcodeFormat;
	private String barcodeLookupResult;
	private int barcodeLookupCategory;

	/**
	 * This adds a new item from barcode
	 */
	public void addNewItemFromBarcode() {
		Intent intent = new Intent(this, AddItemActivity.class);
		intent.setAction(AddItemActivity.ACTION_ADD_NEW_ITEM);
		intent.putExtra(AddItemActivity.INIT_BARCODE, barcodeContents);
		intent.putExtra(AddItemActivity.INIT_PRODUCT_NAME, barcodeLookupResult);
		intent.putExtra(AddItemActivity.INIT_CATEGORY_ID, barcodeLookupCategory);

		startActivityForResult(intent, ADD_ITEM_SUBACTIVITY);
	}

	public void addProductAndUpdateHistory(FridgeDBAdapter db, Product temp,
			String barcode) {
		db.addProduct(temp);

		if (!db.doesHistoryItemExist(temp.getName())) {
			db.addHistoryItem(temp.getName(), barcode, temp.getCategory()
					.ordinal());
			historyItems.requery();
		}
	}

	private void checkIfEmpty() {
		if (historyItems.getCount() <= 0)
			emptyText.setVisibility(View.VISIBLE);
		else
			emptyText.setVisibility(View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (ADD_ITEM_SUBACTIVITY):
			if (resultCode == Activity.RESULT_OK) {
				FridgeDBAdapter db = FridgeReminderApp.getInstance()
						.getFridgeDB();

				Product temp = (Product) data.getExtras().get(
						AddItemActivity.PRODUCT_ITEM);
				String barcode = data
						.getStringExtra(AddItemActivity.NEW_ITEM_BARCODE);

				addProductAndUpdateHistory(db, temp, barcode);

				setResult(RESULT_OK);
				finish();
			}

			break;
		case (IntentIntegrator.REQUEST_CODE):
			IntentResult scanResult = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, data);

			if (scanResult != null) {
				Log.d(TAG, "Contents: " + scanResult.getContents()
						+ ", Format: " + scanResult.getFormatName());

				barcodeContents = scanResult.getContents();
				barcodeFormat = scanResult.getFormatName();

				if (barcodeFormat != null) {

					BarcodeInfo bi = FridgeReminderApp.getInstance()
							.getFridgeDB().lookupBarcode(barcodeContents);

					if (bi != null) {
						barcodeLookupResult = bi.name;
						barcodeLookupCategory = bi.category;

						Log.d(TAG, "Local UPC Database Lookup Result: "
								+ barcodeLookupResult);
					} else {
						barcodeLookupResult = UPCDatabaseClient.lookup(
								barcodeContents, barcodeFormat);
						Log.d(TAG, "UPC Database Lookup Result: "
								+ barcodeLookupResult);
					}

					if (barcodeLookupResult == null) {
						barcodeLookupCategory = -1;
						showDialog(DIALOG_FAILED_BARCODE_SCAN);
					} else {
						addNewItemFromBarcode();
					}
				}

			}
			break;

		case (SCAN_ITEM_FROM_RECEIPT_SUB_ACTIVITY):
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<Product> results = (ArrayList<Product>) data
						.getExtras().get(AddItemActivity.PRODUCT_ITEMS);

				if (results != null) {
					FridgeDBAdapter db = FridgeReminderApp.getInstance()
							.getFridgeDB();
					for (Product p : results)
						addProductAndUpdateHistory(db, p, null);

					setResult(RESULT_OK);
					finish();
				}
			}
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		HistoryItemView historyItemView = (HistoryItemView) ((AdapterContextMenuInfo) item
				.getMenuInfo()).targetView;

		switch (item.getItemId()) {
		case CONTEXT_MENU_ADD_ITEM:
			Intent intent = new Intent(getApplicationContext(),
					AddItemActivity.class);

			intent.setAction(AddItemActivity.ACTION_ADD_NEW_ITEM);
			intent.putExtra(AddItemActivity.INIT_PRODUCT_NAME,
					historyItemView.getName());
			intent.putExtra(AddItemActivity.INIT_CATEGORY_ID,
					historyItemView.getCategory());

			startActivityForResult(intent, ADD_ITEM_SUBACTIVITY);
			break;
		case CONTEXT_MENU_REMOVE_ITEM:
			FridgeReminderApp.getInstance().getFridgeDB()
					.removeHistoryItem(historyItemView.getId());

			historyItems.requery();
			checkIfEmpty();
			break;
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_item_history);

		Button newItemButton = (Button) findViewById(R.id.NewItemButton);
		Button scanItemButton = (Button) findViewById(R.id.NewItemScanButton);
		historyList = (ListView) findViewById(R.id.HistoryListView);
		emptyText = (FrameLayout) findViewById(R.id.HistoryEmptyText);

		newItemButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						AddItemActivity.class);
				intent.setAction(AddItemActivity.ACTION_ADD_NEW_ITEM);
				startActivityForResult(intent, ADD_ITEM_SUBACTIVITY);
			}
		});

		scanItemButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SCAN_ITEM);
			}
		});

		historyItems = FridgeReminderApp.getInstance().getFridgeDB()
				.getAllHistoryItems();
		startManagingCursor(historyItems);

		checkIfEmpty();

		adapter = new HistoryItemCursorAdapter(this, historyItems);
		historyList.setAdapter(adapter);

		registerForContextMenu(historyList);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(0, CONTEXT_MENU_ADD_ITEM, 0, "Add This Product");
		menu.add(0, CONTEXT_MENU_REMOVE_ITEM, 1, "Delete");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		Dialog toReturn = null;
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_SCAN_ITEM:
			final Activity thisActivity = this;

			ab.setTitle("Scan Product");
			ab.setItems(R.array.scan_types,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case SCAN_SELECTION_BARCODE:
								IntentIntegrator.initiateScan(thisActivity);
								break;
							case SCAN_SELECTION_RECEIPT:
								startActivityForResult(new Intent(
										getApplicationContext(),
										ScannerActivity.class),
										SCAN_ITEM_FROM_RECEIPT_SUB_ACTIVITY);
								break;
							}
						}
					});

			toReturn = ab.create();
			break;

		case (DIALOG_FAILED_BARCODE_SCAN):
			ab = new AlertDialog.Builder(this);
			ab.setTitle("Identification Failure");
			ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					addNewItemFromBarcode();
				}
			});

			ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing
				}
			});

			ab.setMessage("I haven't seen this barcode before. Do you want to manually add the item so I can remember it?");
			toReturn = ab.create();
			break;
		}

		return toReturn;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		barcodeContents = savedInstanceState.getString("barcodeContents");
		barcodeLookupResult = savedInstanceState
				.getString("barcodeLookupResult");
		barcodeLookupCategory = savedInstanceState
				.getInt("barcodeLookupCategory");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("barcodeContents", barcodeContents);
		outState.putString("barcodeLookupResult", barcodeLookupResult);
		outState.putInt("barcodeLookupCategory", barcodeLookupCategory);

		super.onSaveInstanceState(outState);
	}
}
