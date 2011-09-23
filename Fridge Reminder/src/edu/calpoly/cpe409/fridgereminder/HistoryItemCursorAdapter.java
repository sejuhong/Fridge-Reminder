package edu.calpoly.cpe409.fridgereminder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.sax.StartElementListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;

public class HistoryItemCursorAdapter extends CursorAdapter {

	private long m_nSelectedID;
	private Context context;

	public long getSelectionID() {
		return m_nSelectedID;
	}

	public HistoryItemCursorAdapter(Context context, Cursor c) {
		super(context, c);

		this.context = context;
		m_nSelectedID = Adapter.NO_SELECTION;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		HistoryItemView historyView = (HistoryItemView) view;
		historyView.setItem(cursor.getString(FridgeDBAdapter.HISTORY_COL_NAME),
				cursor.getInt(FridgeDBAdapter.HISTORY_COL_CATEGORY),
				cursor.getInt(FridgeDBAdapter.HISTORY_COL_ID));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		String name = cursor.getString(FridgeDBAdapter.HISTORY_COL_NAME);
		int category = cursor.getInt(FridgeDBAdapter.HISTORY_COL_CATEGORY);
		int id = cursor.getInt(FridgeDBAdapter.HISTORY_COL_ID);

		HistoryItemView newView = new HistoryItemView(context, name, category,
				id);
		return newView;
	}

}
