package edu.calpoly.cpe409.fridgereminder;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This class is the activity for adding a new item to the 
 * Fridge Reminder.
 */
public class AddItemActivity extends Activity {
	private static final int START_DATE_DIALOG = 10;
	private static final int EXPIRATION_DATE_DIALOG = 11;
	private Date purchaseDate;
	private Date expirationDate;
	private Category category;
	private Button purchaseDateButton;
	private Button expirationDateButton;
	private EditText nameEditText;
	private EditText countEditText;
	private Button addButton;
	private Button skipButton;
	private Button okButton;
	private ArrayList<String> batchItems;
	private int batchItemsPos;
	private Iterator<String> batchItemsIter;
	private ArrayList<Product> batchItemResults;
	private Spinner categorySpinner;
	private int productId;
	public static final String ACTION_ADD_NEW_ITEM = "edu.calpoly.cpe409.fridgereminder.action.add_new_item";
	public static final String ACTION_ADD_NEW_ITEM_BATCH = "edu.calpoly.cpe409.fridgereminder.action.add_new_item_batch";
	public static final String ACTION_EDIT_ITEM = "edu.calpoly.cpe409.fridgereminder.action.edit_item";
	public static final String DATA_BATCH_ITEM = "edu.calpoly.cpe409.fridgereminder.data.batch_item_data";
	public static final String PRODUCT_ITEM = "product_item";
	public static final String PRODUCT_ITEMS = "product_items";
	public static final String INIT_PRODUCT_NAME = "edu.calpoly.cpe409.fridgereminder.add_item.init_product_name";
	public static final String INIT_BARCODE = "edu.calpoly.cpe409.fridgereminder.add_item.init_barcode";
	public static final String INIT_CATEGORY_ID = "edu.calpoly.cpe409.fridgereminder.add_item.init_category_id";
	public static final String INIT_SCAN_SUCCESS = "edu.calpoly.cpe409.fridgereminder.add_item.init_scan_success";
	public static final String NEW_ITEM_BARCODE = "edu.calpoly.cpe409.fridgereminder.new_item_barcode";
	private static final String STATE_PURCHASE_DATE = "edu.calpoly.cpe409.fridgereminder.state.purchase_date";
	private static final String STATE_EXPIRATION_DATE = "edu.calpoly.cpe409.fridgereminder.state.expiration_date";
	private Button lookupButton;
	private Button cancelButton;
	private String currentBarcode;

	/**
	 * Creates an item from the product.
	 * @param product the info for the product
	 */
	private void createFormFromProduct(Product product) {
		if (product.getCount() == -1)
			countEditText.setText("");
		else
			countEditText.setText(product.getCount() + "");

		nameEditText.setText(product.getName());

		Calendar cal = new GregorianCalendar();

		long boughtTime = product.getBoughtDate().getTime();
		if (boughtTime != 0) {
			cal.setTimeInMillis(boughtTime);
			purchaseDate = cal.getTime();
		}

		cal.setTimeInMillis(product.getExpirationDate().getTime());
		expirationDate = cal.getTime();

		updateDateButtons();

		category = product.getCategory();
		categorySpinner.setSelection(category.ordinal());

		productId = product.getId();
	}

	/**
	 * Gets the manual input from user and creates a product
	 * @return a product depending on the user input
	 */
	private Product createProductFromForm() {
		int count = -1;

		try {
			if (!countEditText.getText().toString().equals(""))
				count = Integer.parseInt(countEditText.getText().toString());
		} catch (NumberFormatException exc) {
			Toast.makeText(this, R.string.error_add_item_invalid_count,
					Toast.LENGTH_LONG).show();
			return null;
		}

		if (expirationDate == null) {
			Toast.makeText(this, R.string.error_add_item_no_date,
					Toast.LENGTH_LONG).show();
			return null;
		}

		if (purchaseDate != null
				&& expirationDate.getTime() < purchaseDate.getTime()) {
			Toast.makeText(this, R.string.error_add_item_bad_expiration,
					Toast.LENGTH_LONG).show();
			return null;
		}

		Product newProduct = new Product(nameEditText.getText().toString()
				.trim(), purchaseDate, expirationDate, count, category,
				productId);
		return newProduct;
	}

	/**
	 * Creates the layout for the add item activity
	 */
	private void initLayout() {
		purchaseDateButton = (Button) findViewById(R.id.PurchaseDateButton);
		expirationDateButton = (Button) findViewById(R.id.ExpirationDateButton);
		nameEditText = (EditText) findViewById(R.id.ItemNameEditText);
		countEditText = (EditText) findViewById(R.id.ItemCountEditText);
		addButton = (Button) findViewById(R.id.AddButton);
		skipButton = (Button) findViewById(R.id.SkipButton);
		okButton = (Button) findViewById(R.id.OkButton);
		cancelButton = (Button) findViewById(R.id.CancelButton);

		lookupButton = (Button) findViewById(R.id.LookupButton);

		categorySpinner = (Spinner) findViewById(R.id.CategorySpinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.categories, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		categorySpinner.setAdapter(adapter);
		categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				switch (arg2) {
				case 0:
					category = Category.FRUIT;
					break;
				case 1:
					category = Category.VEGETABLES;
					break;
				case 2:
					category = Category.DAIRY;
					break;
				case 3:
					category = Category.MEAT;
					break;
				case 4:
					category = Category.FISH;
					break;
				case 5:
					category = Category.BREADANDCEREAL;
					break;
				case 6:
					category = Category.PACKAGEDFOODANDMIXES;
					break;
				case 7:
					category = Category.POULTRYANDEGGS;
					break;
				case 8:
					category = Category.NONE;
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		purchaseDateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(START_DATE_DIALOG);
			}
		});

		Intent intent = getIntent();

		String initName = intent.getStringExtra(INIT_PRODUCT_NAME);
		currentBarcode = intent.getStringExtra(INIT_BARCODE);
		int initCategory = intent.getIntExtra(INIT_CATEGORY_ID, -1);

		if (initName != null)
			nameEditText.setText(initName);
		if (initCategory != -1)
			categorySpinner.setSelection(initCategory);

		lookupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (purchaseDate != null
						|| nameEditText.getText().toString().length() <= 0) {
					SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, y");

					Calendar calendar = new GregorianCalendar(purchaseDate
							.getYear(), purchaseDate.getMonth(), purchaseDate
							.getDate());

					int days = -1;
					try {
						days = ExpirationDatabase.lookUp(nameEditText.getText()
								.toString().trim(), getApplicationContext());

						if (days != -1) {
							calendar.add(Calendar.DAY_OF_MONTH, days);

							expirationDate = new Date(calendar
									.get(Calendar.YEAR), calendar
									.get(Calendar.MONTH), calendar
									.get(Calendar.DAY_OF_MONTH));

							expirationDateButton.setText(sdf
									.format(expirationDate));

							Toast.makeText(getApplicationContext(),
									"Expiration date guess successful!",
									Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getApplicationContext(),
									"Product not found in database!",
									Toast.LENGTH_LONG).show();
						}
					} catch (UnknownHostException e) {
						Toast.makeText(getApplicationContext(),
								"Problem connecting to server!",
								Toast.LENGTH_LONG).show();
					}

				}
			}
		});

		expirationDateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(EXPIRATION_DATE_DIALOG);
			}
		});

		addButton = (Button) findViewById(R.id.AddButton);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent(null, Uri
						.parse("content://fridgereminder/new"));

				String actionName = getIntent().getAction();
				if (actionName.equals(ACTION_ADD_NEW_ITEM)) {
					Product newProduct = createProductFromForm();

					if (newProduct != null) {
						result.putExtra(PRODUCT_ITEM, newProduct);

						if (currentBarcode != null)
							result.putExtra(NEW_ITEM_BARCODE, currentBarcode);

						setResult(RESULT_OK, result);
						finish();
					}

				} else if (actionName.equals(ACTION_ADD_NEW_ITEM_BATCH)) {
					Product newProduct = createProductFromForm();

					if (newProduct != null) {
						batchItemResults.add(createProductFromForm());

						if (!updateToNextItem()) {
							result.putExtra(PRODUCT_ITEMS, batchItemResults);
							setResult(RESULT_OK, result);
							finish();
						}
					}
				}

			}
		});

		skipButton = (Button) findViewById(R.id.SkipButton);
		skipButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				updateToNextItem();

				if (!batchItemsIter.hasNext()) {
					Intent result = new Intent(null, Uri
							.parse("content://fridgereminder/new"));
					result.putExtra(PRODUCT_ITEMS, batchItemResults);
					setResult(RESULT_OK, result);
					finish();
				}
			}
		});

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent result = new Intent(null, Uri
						.parse("content://fridgereminder/new"));

				Product updatedProduct = createProductFromForm();

				if (updatedProduct != null) {
					result.putExtra(PRODUCT_ITEM, updatedProduct);
					setResult(RESULT_OK, result);
					finish();
				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		lookupButton.setEnabled(false);
		updateDateButtons();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_item);

		initLayout();

		productId = -1;
	}

	/**
	 * Creates an dialog to input the date of either the buy date or expiration date
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		Calendar calendar = Calendar.getInstance();

		Dialog toReturn = null;

		switch (id) {
		case (START_DATE_DIALOG):

			toReturn = new DatePickerDialog(this, new OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					Calendar cal = new GregorianCalendar(year, monthOfYear,
							dayOfMonth);
					purchaseDate = cal.getTime();
					updateDateButtons();
					lookupButton.setEnabled(true);

				}
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));

			break;
		case (EXPIRATION_DATE_DIALOG):

			toReturn = new DatePickerDialog(this, new OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					Calendar cal = new GregorianCalendar(year, monthOfYear,
							dayOfMonth);
					expirationDate = cal.getTime();
					updateDateButtons();

				}
			}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));

			break;
		}

		return toReturn;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		purchaseDate = (Date) savedInstanceState
				.getSerializable(STATE_PURCHASE_DATE);
		expirationDate = (Date) savedInstanceState
				.getSerializable(STATE_EXPIRATION_DATE);

		updateDateButtons();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_PURCHASE_DATE, purchaseDate);
		outState.putSerializable(STATE_EXPIRATION_DATE, expirationDate);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = getIntent();
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION_ADD_NEW_ITEM)) {
				okButton.setVisibility(View.GONE);
				skipButton.setVisibility(View.GONE);
				addButton.setVisibility(View.VISIBLE);
				cancelButton.setVisibility(View.VISIBLE);
			} else if (action.equals(ACTION_ADD_NEW_ITEM_BATCH)) {
				okButton.setVisibility(View.GONE);
				skipButton.setVisibility(View.VISIBLE);
				addButton.setVisibility(View.VISIBLE);
				cancelButton.setVisibility(View.GONE);

				batchItems = (ArrayList<String>) intent.getExtras().get(
						DATA_BATCH_ITEM);
				batchItemResults = new ArrayList<Product>();

				if (batchItems.size() <= 0)
					Log.e("AddItemActivity", "Empty list of batch items");

				batchItemsIter = batchItems.iterator();
				updateToNextItem();
				if (batchItemResults != null)
					batchItemResults.clear();

			} else if (action.equals(ACTION_EDIT_ITEM)) {
				okButton.setVisibility(View.VISIBLE);
				skipButton.setVisibility(View.GONE);
				addButton.setVisibility(View.GONE);
				cancelButton.setVisibility(View.VISIBLE);

				Product temp = (Product) intent.getExtras().get(
						AddItemActivity.PRODUCT_ITEM);

				createFormFromProduct(temp);

			}
		}
	}

	/**
	 * 
	 */
	private void updateDateButtons() {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, y");

		if (purchaseDateButton != null
				&& (purchaseDate == null || purchaseDate.getTime() == 0)) {
			purchaseDateButton.setText("Push to select date");
		} else if (purchaseDate != null) {
			purchaseDateButton.setText(sdf.format(purchaseDate));
		}

		if (expirationDateButton != null
				&& (expirationDate == null || expirationDate.getTime() == 0)) {
			expirationDateButton.setText("Push to select date");
		} else if (expirationDate != null) {
			expirationDateButton.setText(sdf.format(expirationDate));
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean updateToNextItem() {
		if (batchItemsIter.hasNext()) {
			nameEditText.setText(batchItemsIter.next());
			countEditText.setText("");
			purchaseDate = null;
			expirationDate = null;

			updateDateButtons();

			categorySpinner.setSelection(0);

			return true;
		} else
			return false;
	}
}
