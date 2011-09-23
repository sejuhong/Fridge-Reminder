package edu.calpoly.cpe409.fridgereminder;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FridgeDBAdapter {

	private static class ProductDBOpenHelper extends SQLiteOpenHelper {

		private static final String TAG = "ProductDBOpenHelper";

		public ProductDBOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public void initCategories(SQLiteDatabase db) {
			ContentValues values = new ContentValues();

			values.put(CATEGORY_KEY_ID, 0);
			values.put(CATEGORY_KEY_NAME, "Fruit");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 1);
			values.put(CATEGORY_KEY_NAME, "Vegetables");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 2);
			values.put(CATEGORY_KEY_NAME, "Dairy");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 3);
			values.put(CATEGORY_KEY_NAME, "Meat");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 4);
			values.put(CATEGORY_KEY_NAME, "Fish");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 5);
			values.put(CATEGORY_KEY_NAME, "Bread/Cereal");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 6);
			values.put(CATEGORY_KEY_NAME, "Packaged Food/Mixes");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 7);
			values.put(CATEGORY_KEY_NAME, "Poultry/Eggs");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);

			values.put(CATEGORY_KEY_ID, 8);
			values.put(CATEGORY_KEY_NAME, "N/A");
			db.insert(DATABASE_TABLE_CATEGORIES, null, values);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			arg0.execSQL(FridgeDBAdapter.DATABASE_CREATE_PRODUCTS);
			arg0.execSQL(DATABASE_CREATE_HISTORY);
			arg0.execSQL(FridgeDBAdapter.DATABASE_CREATE_CATEGORIES);
			initCategories(arg0);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			if (oldVersion == 3 && newVersion >= 4) {
				db.execSQL(FridgeDBAdapter.DATABASE_CREATE_CATEGORIES);
				initCategories(db);
				Log.d(TAG, "onUpgrade: Creating Categories Table");
			} else if (oldVersion < 4 && newVersion >= 4) {
				db.execSQL(DATABASE_DROP_PRODUCTS);
				db.execSQL(FridgeDBAdapter.DATABASE_CREATE_PRODUCTS);

				db.execSQL(DATABASE_DROP_CATEGORIES);
				db.execSQL(FridgeDBAdapter.DATABASE_CREATE_CATEGORIES);
				initCategories(db);
				Log.d(TAG,
						"onUpgrade: Re-creating Products and Categories Table");
			}

			if (newVersion >= 5 && oldVersion < 5) {
				db.execSQL(DATABASE_CREATE_HISTORY);
				db.execSQL(DATABASE_CREATE_HISTORY_INDICES);
				Log.d(TAG, "onUpgrade: Creating History Table");
			}

			if (newVersion >= 6 && oldVersion == 5) {
				Log.d(TAG,
						"onUpgrade: Migrating Barcodes table to History table...");
				int count = 0;

				db.execSQL(DATABASE_CREATE_HISTORY);
				db.execSQL(DATABASE_CREATE_HISTORY_INDICES);

				Cursor oldData = db.query(DATABASE_TABLE_BARCODES, null, null,
						null, null, null, null);

				Log.d(TAG,
						"onUpgrade: Barcodes table contains "
								+ oldData.getCount() + " rows");

				if (oldData.moveToFirst()) {
					ContentValues cv = new ContentValues();

					do {
						String newName = oldData.getString(BARCODES_COL_NAME);
						String newBarcode = oldData.getString(BARCODES_COL_ID);
						int newCategory = oldData.getInt(BARCODES_COL_CATEGORY);

						cv.put(HISTORY_KEY_NAME, newName);
						cv.put(HISTORY_KEY_BARCODE_NUM, newBarcode);
						cv.put(HISTORY_KEY_CATEGORY, newCategory);

						if (db.insert(DATABASE_TABLE_HISTORY, null, cv) != -1)
							count++;
					} while (oldData.moveToNext());
				}

				oldData.close();

				Log.d(TAG, "onUpgrade: Migrated " + count + " rows.");

				db.execSQL(DATABASE_DROP_BARCODES);
			}

		}

	}

	// Categories Table
	private static final String DATABASE_TABLE_CATEGORIES = "tbl_categories";

	private static final int CATEGORY_COL_ID = 0;
	private static final String CATEGORY_KEY_ID = "catId";

	private static final int CATEGORY_COL_NAME = 1;
	private static final String CATEGORY_KEY_NAME = "catName";

	// Products Table
	private static final String DATABASE_TABLE_PRODUCT = "tbl_products";
	public static final int PRODUCT_COL_ID = 0;

	public static final String PRODUCT_KEY_ID = "_id";
	public static final int PRODUCT_COL_NAME = 1;

	public static final String PRODUCT_KEY_NAME = "name";
	public static final int PRODUCT_COL_BOUGHTDATE = 2;

	public static final String PRODUCT_KEY_BOUGHTDATE = "bought_date";
	public static final int PRODUCT_COL_EXPIRATIONDATE = 3;

	public static final String PRODUCT_KEY_EXPIRATIONDATE = "expiration_date";
	public static final int PRODUCT_COL_COUNT = 4;

	public static final String PRODUCT_KEY_COUNT = "count";
	public static final int PRODUCT_COL_CATEGORY = 5;

	public static final String PRODUCT_KEY_CATEGORY = "category";

	public static final String DATABASE_CREATE_PRODUCTS = "create table "
			+ DATABASE_TABLE_PRODUCT + " (" + PRODUCT_KEY_ID
			+ " integer primary key autoincrement, " + PRODUCT_KEY_NAME
			+ " text not null, " + PRODUCT_KEY_BOUGHTDATE
			+ " integer not null, " + PRODUCT_KEY_EXPIRATIONDATE
			+ " integer not null, " + PRODUCT_KEY_COUNT + " integer not null, "
			+ PRODUCT_KEY_CATEGORY + " integer not null);";

	public static final String DATABASE_DROP_PRODUCTS = "drop table if exists "
			+ DATABASE_TABLE_PRODUCT;

	public static final String DATABASE_CREATE_CATEGORIES = "create table "
			+ DATABASE_TABLE_CATEGORIES + " (" + CATEGORY_KEY_ID
			+ " integer primary key, " + CATEGORY_KEY_NAME + " text not null);";

	public static final String DATABASE_DROP_CATEGORIES = "drop table if exists "
			+ DATABASE_TABLE_CATEGORIES;

	// Product History Table
	public static final String DATABASE_TABLE_HISTORY = "tbl_history";

	public static final String HISTORY_KEY_ID = "_id";
	public static final int HISTORY_COL_ID = 0;

	public static final String HISTORY_KEY_BARCODE_NUM = "barcode_num";
	public static final int HISTORY_COL_BARCODE_NUM = 1;

	public static final String HISTORY_KEY_NAME = "name";
	public static final int HISTORY_COL_NAME = 2;

	public static final String HISTORY_KEY_CATEGORY = "category";
	public static final int HISTORY_COL_CATEGORY = 3;

	public static final String DATABASE_HISTORY_BARCODE_INDEX = "idx_barcode";
	public static final String DATABASE_HISTORY_NAME_INDEX = "idx_name";

	public static final String DATABASE_CREATE_HISTORY = "create table "
			+ DATABASE_TABLE_HISTORY + " (" + HISTORY_KEY_ID
			+ "  integer primary key autoincrement, " + HISTORY_KEY_BARCODE_NUM
			+ " text, " + HISTORY_KEY_NAME + " text not null, "
			+ HISTORY_KEY_CATEGORY + " integer not null);";

	public static final String DATABASE_CREATE_HISTORY_INDICES = "create index if not exists "
			+ DATABASE_HISTORY_BARCODE_INDEX
			+ " ON "
			+ DATABASE_TABLE_HISTORY
			+ "("
			+ HISTORY_KEY_BARCODE_NUM
			+ ");"
			+ "create index if not exists "
			+ DATABASE_HISTORY_NAME_INDEX
			+ " ON " + DATABASE_TABLE_HISTORY + "(" + HISTORY_KEY_NAME + ");";

	public static final String DATABASE_DROP_HISTORY = "drop table if exists "
			+ DATABASE_TABLE_HISTORY;

	// Old barcodes table
	private static final String DATABASE_TABLE_BARCODES = "tbl_barcodes";

	private static final String BARCODES_KEY_ID = "barcode_num";
	private static final int BARCODES_COL_ID = 0;

	private static final String BARCODES_KEY_NAME = "name";
	private static final int BARCODES_COL_NAME = 1;

	private static final String BARCODES_KEY_CATEGORY = "category";
	private static final int BARCODES_COL_CATEGORY = 2;

	public static final String DATABASE_CREATE_BARCODES = "create table "
			+ DATABASE_TABLE_BARCODES + " (" + BARCODES_KEY_ID
			+ " text primary key, " + BARCODES_KEY_NAME + " text not null, "
			+ BARCODES_KEY_CATEGORY + " integer not null);";

	public static final String DATABASE_DROP_BARCODES = "drop table if exists "
			+ DATABASE_TABLE_BARCODES;

	private static final String DATABASE_NAME = "ProductListDatabase.db";

	private static final int DATABASE_VERSION = 6;

	public final static String DATABASE_GET_PRODUCTS = "select * from "
			+ DATABASE_TABLE_PRODUCT + " join " + DATABASE_TABLE_CATEGORIES
			+ " on " + PRODUCT_KEY_CATEGORY + " = " + CATEGORY_KEY_ID
			+ " order by ";

	private static final String TAG = "ProductDBAdapter";

	public static Product getProductFromCursor(Cursor cursor) {
		Product toReturn = null;
		if (cursor != null && cursor.getCount() > 0) {

			toReturn = parseCursor(cursor);

		}
		return toReturn;
	}

	private static Product parseCursor(Cursor cursor) {
		Product toReturn;

		String name = cursor.getString(PRODUCT_COL_NAME);
		Log.e("ProductDBAdapter",
				"Date to parse: " + cursor.getLong(PRODUCT_COL_BOUGHTDATE));
		Date boughtDate = null;
		Date expirationDate = null;

		boughtDate = new Date(cursor.getLong(PRODUCT_COL_BOUGHTDATE));
		expirationDate = new Date(cursor.getLong(PRODUCT_COL_EXPIRATIONDATE));

		int count = cursor.getInt(PRODUCT_COL_COUNT);
		Category category = Category.parse(cursor.getInt(PRODUCT_COL_CATEGORY));
		int id = cursor.getInt(PRODUCT_COL_ID);

		toReturn = new Product(name, boughtDate, expirationDate, count,
				category, id);
		return toReturn;
	}

	/** Database Instance **/
	private SQLiteDatabase m_db;

	/** Database open/upgrade helper **/
	private ProductDBOpenHelper m_dbHelper;

	public FridgeDBAdapter(Context context) {
		m_dbHelper = new ProductDBOpenHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	public long addProduct(Product product) {
		ContentValues cv = new ContentValues();
		cv.put(FridgeDBAdapter.PRODUCT_KEY_NAME, product.getName());

		Date boughtDate = product.getBoughtDate();
		if (boughtDate != null)
			cv.put(FridgeDBAdapter.PRODUCT_KEY_BOUGHTDATE, boughtDate.getTime());
		else
			cv.put(FridgeDBAdapter.PRODUCT_KEY_BOUGHTDATE, 0);

		cv.put(FridgeDBAdapter.PRODUCT_KEY_EXPIRATIONDATE, product
				.getExpirationDate().getTime());
		cv.put(FridgeDBAdapter.PRODUCT_KEY_COUNT, product.getCount());
		cv.put(FridgeDBAdapter.PRODUCT_KEY_CATEGORY, product.getCategory()
				.ordinal());

		return m_db.insert(FridgeDBAdapter.DATABASE_TABLE_PRODUCT, null, cv);
	}

	public long addHistoryItem(String name, String barcode, int category) {
		ContentValues cv = new ContentValues();
		cv.put(FridgeDBAdapter.HISTORY_KEY_NAME, name);
		cv.put(FridgeDBAdapter.HISTORY_KEY_BARCODE_NUM, barcode);
		cv.put(FridgeDBAdapter.HISTORY_KEY_CATEGORY, category);

		return m_db.insert(FridgeDBAdapter.DATABASE_TABLE_HISTORY, null, cv);
	}

	public void close() {
		m_db.close();
	}

	public void dropProductTable() {
		m_db.execSQL(FridgeDBAdapter.DATABASE_DROP_PRODUCTS);
		m_db.execSQL(FridgeDBAdapter.DATABASE_CREATE_PRODUCTS);
	}

	public Cursor getAllProducts() {
		return getAllProducts(PRODUCT_KEY_EXPIRATIONDATE, "DESC");
	}

	public Cursor getAllProducts(String orderColumn, String sortOrder) {

		orderColumn = orderColumn.equals(PRODUCT_KEY_NAME)
				|| orderColumn.equals(CATEGORY_KEY_NAME) ? "UPPER("
				+ orderColumn + ")" : orderColumn;

		return m_db.rawQuery(DATABASE_GET_PRODUCTS + orderColumn + " "
				+ sortOrder, null);

	}

	public Cursor getAllHistoryItems() {
		String orderBy = "UPPER(" + HISTORY_KEY_NAME + ")";

		return m_db.query(DATABASE_TABLE_HISTORY, null, null, null, null, null,
				orderBy);
	}

	public Product getProduct(long selectionID) {
		String selection = PRODUCT_KEY_ID + "=" + selectionID;
		Product toReturn = null;

		Cursor cursor = m_db.query(DATABASE_TABLE_PRODUCT, new String[] {
				PRODUCT_KEY_ID, PRODUCT_KEY_NAME, PRODUCT_KEY_BOUGHTDATE,
				PRODUCT_KEY_EXPIRATIONDATE, PRODUCT_KEY_COUNT,
				PRODUCT_KEY_CATEGORY }, selection, null, null, null, null);

		if (cursor.moveToNext()) {
			toReturn = parseCursor(cursor);
		} else {
			Log.e("ProductDBAdapter", "No results from getProduct()");
		}

		cursor.close();
		return toReturn;
	}

	public void insertOrUpdateBarcode(BarcodeInfo barcode) {
		ContentValues values = new ContentValues();
		values.put(HISTORY_KEY_BARCODE_NUM, barcode.barcode);
		values.put(HISTORY_KEY_NAME, barcode.name);
		values.put(HISTORY_KEY_CATEGORY, barcode.category);

		long result = m_db.insert(DATABASE_TABLE_HISTORY, null, values);

		Log.d(TAG, "insertOrUpdateBarcode (insert): " + result);

		if (result == -1) {
			String whereClause = HISTORY_KEY_BARCODE_NUM + " = \""
					+ barcode.barcode + "\"";
			result = m_db.update(DATABASE_TABLE_HISTORY, values, whereClause,
					null);
			Log.d(TAG, "insertOrUpdateBarcode (update): " + result);
		}

	}

	public BarcodeInfo lookupBarcode(String barcode) {
		BarcodeInfo toReturn = null;
		String selection = HISTORY_KEY_BARCODE_NUM + " = \"" + barcode + "\"";

		Cursor cursor = m_db.query(DATABASE_TABLE_HISTORY, null, selection,
				null, null, null, null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			toReturn = new BarcodeInfo(barcode,
					cursor.getString(HISTORY_COL_NAME),
					cursor.getInt(HISTORY_COL_CATEGORY));
		}

		cursor.close();
		return toReturn;
	}

	public void open() {
		m_db = m_dbHelper.getWritableDatabase();
	}

	public void printBarcodes() {
		Cursor cursor = m_db.query(DATABASE_TABLE_HISTORY, null, null, null,
				null, null, null);

		String out = "Barcode Contents: " + cursor.getCount();
		out += "\n-----------------------------\n";

		if (cursor.moveToFirst()) {
			do {
				out += "ID: " + cursor.getString(HISTORY_COL_BARCODE_NUM)
						+ ", Name: " + cursor.getString(HISTORY_COL_NAME)
						+ ", Category: "
						+ cursor.getInt(HISTORY_COL_BARCODE_NUM) + "\n";
			} while (cursor.moveToNext());
		}

		cursor.close();
		Log.d(TAG, out);
	}

	public boolean remove(long selectionID) {
		String selection = PRODUCT_KEY_ID + "=" + selectionID;
		return m_db.delete(DATABASE_TABLE_PRODUCT, selection, null) > 0;
	}

	public boolean updateProduct(Product product) {
		String selection = PRODUCT_KEY_ID + "=" + product.getId();

		ContentValues cv = new ContentValues();
		cv.put(FridgeDBAdapter.PRODUCT_KEY_NAME, product.getName());

		Date temp = product.getBoughtDate();
		if (temp != null)
			cv.put(FridgeDBAdapter.PRODUCT_KEY_BOUGHTDATE, temp.getTime());
		else
			cv.put(FridgeDBAdapter.PRODUCT_KEY_BOUGHTDATE, 0);

		cv.put(FridgeDBAdapter.PRODUCT_KEY_EXPIRATIONDATE, product
				.getExpirationDate().getTime());
		cv.put(FridgeDBAdapter.PRODUCT_KEY_COUNT, product.getCount());
		cv.put(FridgeDBAdapter.PRODUCT_KEY_CATEGORY, product.getCategory()
				.ordinal());

		return m_db.update(DATABASE_TABLE_PRODUCT, cv, selection, null) > 0;
	}

	public boolean removeHistoryItem(int id) {
		String whereClause = HISTORY_KEY_ID + " = " + id;
		return m_db.delete(DATABASE_TABLE_HISTORY, whereClause, null) == 1;
	}

	public boolean doesHistoryItemExist(String name) {
		String whereClause = HISTORY_KEY_NAME + " = " + "\"" + name + "\"";
		boolean toReturn = false;

		Cursor cursor = m_db.query(DATABASE_TABLE_HISTORY,
				new String[] { HISTORY_KEY_NAME }, whereClause, null, null,
				null, null);

		toReturn = cursor.getCount() >= 1;
		cursor.close();

		return toReturn;
	}

}
