package edu.calpoly.cpe409.fridgereminder;

import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProductView extends LinearLayout {

	/**
	 * Interface definition for a callback to be invoked when the underlying
	 * Product is changed in this ProductView object.
	 * 
	 */
	public static interface OnProductChangeListener {

		/**
		 * Called when the underlying Joke in a ProductView object changes
		 * state.
		 * 
		 * @param view
		 *            The ProductView in which the Product was changed.
		 * @param product
		 *            The Joke that was changed.
		 */
		public void onProductChanged(ProductView view, Product product);
	}

	private TextView productNameText;
	private TextView productCountText;
	private TextView daysLeftText;
	private TextView categoryText;
	private LinearLayout colorBar;
	private Product product;

	private OnProductChangeListener m_onProductChangeListener;

	public ProductView(Context context, Product product) {
		super(context);

		((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.product_view, this, true);

		setProduct(product);

	}

	public Product getProduct() {
		return product;
	}

	protected void notifyOnProductChangeListener() {
		if (m_onProductChangeListener != null) {
			m_onProductChangeListener.onProductChanged(this, product);
		}
	}

	public void setColor(int color) {
		colorBar.setBackgroundColor(color);
	}

	public void setOnProductChangeListener(OnProductChangeListener m_listener) {
		m_onProductChangeListener = m_listener;

	}

	public void setProduct(Product product) {
		this.product = product;

		productNameText = (TextView) findViewById(R.id.ProductName);
		productCountText = (TextView) findViewById(R.id.ProductCount);
		daysLeftText = (TextView) findViewById(R.id.DaysLeft);
		categoryText = (TextView) findViewById(R.id.ProductCategory);
		colorBar = (LinearLayout) findViewById(R.id.ColorBar);

		productNameText.setText(product.getName());
		categoryText.setText(product.getCategory() + "");

		long daysLeft = DateUtil.daysBetween(new Date(),
				product.getExpirationDate());
		daysLeft = daysLeft < 0 ? 0 : daysLeft;

		daysLeftText.setText("Days Left: " + daysLeft);

		int count = product.getCount();
		if (daysLeft == 0) {
			if (product.getCount() == -1)
				productCountText.setText("");
			else
				productCountText.setText("Quantity: " + product.getCount());
			setColor(Color.RED);
		} else if (count < 0) {
			productCountText.setText("");
			setColor(Color.BLUE);

		} else {
			productCountText.setText("Quantity: " + product.getCount());
			if (count == 0)
				setColor(Color.RED);
			else
				setColor(Color.BLUE);
		}
	}
}
