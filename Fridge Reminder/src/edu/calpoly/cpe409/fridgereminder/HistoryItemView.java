package edu.calpoly.cpe409.fridgereminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryItemView extends RelativeLayout {
	private String name;
	private int category;
	private int id;

	public int getId() {
		return id;
	}

	public HistoryItemView(Context context, String name, int category, int id) {
		super(context);

		((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.history_item_view, this, true);

		this.name = name;
		this.category = category;
		this.id = id;
		setItem(name, category, id);
	}

	public String getName() {
		return name;
	}

	public int getCategory() {
		return category;
	}

	public void setItem(String name, int category, int id) {
		TextView nameTextView = (TextView) findViewById(R.id.HistoryItemName);
		TextView categoryTextView = (TextView) findViewById(R.id.HistoryItemCategory);

		nameTextView.setText(name);
		categoryTextView.setText(Category.parse(category).toString());

		this.name = name;
		this.category = category;
		this.id = id;
	}

}
