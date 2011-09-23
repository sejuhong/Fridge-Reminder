package edu.calpoly.cpe409.fridgereminder;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemLongClickListener;

public class ProductListAdapter extends BaseAdapter implements OnItemLongClickListener{

	private List<Product> m_productList;
	private Context m_context;
	private int m_nSelectedPosition;

	public ProductListAdapter(Context context, List<Product> products) {
		m_context = context;
		m_productList = products;
	}

	@Override
	public int getCount() {
		return m_productList.size();
	}

	@Override
	public Object getItem(int position) {
		return m_productList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public int getSelectedPosition() {
		return m_nSelectedPosition;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ProductView productView = null;
		Product product = m_productList.get(position);

		if (convertView == null) {
			productView = new ProductView(m_context, product);
		} else {
			productView = (ProductView) convertView;
			productView.setProduct(product);
		}

		return productView;
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
		m_nSelectedPosition = pos;
		return false;
	}

}
