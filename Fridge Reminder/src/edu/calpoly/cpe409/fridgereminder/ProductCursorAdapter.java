package edu.calpoly.cpe409.fridgereminder;

import edu.calpoly.cpe409.fridgereminder.ProductView.OnProductChangeListener;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;

public class ProductCursorAdapter extends CursorAdapter implements
		OnItemLongClickListener {

	private long m_nSelectedID;
	private OnProductChangeListener m_listener;

	public ProductCursorAdapter(Context context, Cursor c) {
		super(context, c);
		m_listener = null;
		m_nSelectedID = Adapter.NO_SELECTION;
	}

	public long getSelectionID() {
		return m_nSelectedID;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long id) {
		m_nSelectedID = id;
		return false;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Product product = FridgeDBAdapter.getProductFromCursor(cursor);
		ProductView productView = (ProductView) view;
		productView.setProduct(product);
		productView.setOnProductChangeListener(m_listener);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Product product = FridgeDBAdapter.getProductFromCursor(cursor);
		ProductView newView = new ProductView(context, product);
		newView.setOnProductChangeListener(m_listener);
		return newView;
	}

	public void setOnProductChangeListener(OnProductChangeListener listener) {
		m_listener = listener;
		
	}

}
