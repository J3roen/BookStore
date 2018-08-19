package com.example.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstore.data.StoreContract;

/**
 * {@link #ProductCursorAdapter(Context, Cursor)} is an adapter for a list or grid view that
 * uses a @{@link Cursor} of pet data as its data source
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs new ProductCursorAdapter
     * @param context
     * @param c
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    /**
     * make a new blank list item view, no data is set to the views yet
     *
     * @param context app context
     * @param cursor cursor from which to get the data, already in right position
     * @param parent parent to which the new view is attached to
     * @return newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Binds product data (in current row pointed by cursor) to the given list item layout.
     *
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //find individual views that we want to modify
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity);

        //find columns of store attributes that we want
        int nameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        //read store attributes from cursor <- values can't be empty due to data validation
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);

        //update the textviews with the attributes for current store
        nameTextView.setText(productName);
        priceView.setText(context.getString(R.string.list_price_label, productPrice));
        quantityView.setText(context.getString(R.string.list_quantity_label, productQuantity));
    }
}
