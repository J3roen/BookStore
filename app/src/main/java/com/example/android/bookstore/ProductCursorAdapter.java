package com.example.android.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore.data.StoreContract;

/**
 * {@link #ProductCursorAdapter(Context, Cursor)} is an adapter for a list or grid view that
 * uses a @{@link Cursor} of pet data as its data source
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * inner ViewHolder class to reduce use of findViewById
     */
    private static class ViewHolder {
        TextView mNameView;
        TextView mPriceView;
        TextView mQuantityView;
        Button mSellButton;
        LinearLayout mItemContainer;

    }

    /**
     * Constructs new ProductCursorAdapter
     *
     * @param context
     * @param c
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * make a new blank list item view, no data is set to the views yet
     *
     * @param context app context
     * @param cursor  cursor from which to get the data, already in right position
     * @param parent  parent to which the new view is attached to
     * @return newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        //load views into ViewHolder
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mNameView = view.findViewById(R.id.product_name);
        viewHolder.mPriceView = view.findViewById(R.id.product_price);
        viewHolder.mQuantityView = view.findViewById(R.id.product_quantity);
        viewHolder.mSellButton = view.findViewById(R.id.item_list_sell_button);
        viewHolder.mItemContainer = view.findViewById(R.id.list_item_layout);
        view.setTag(R.string.tag_item_viewHolder, viewHolder);

        return view;
    }

    /**
     * Binds product data (in current row pointed by cursor) to the given list item layout.
     *
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        //get viewHolder from view tag
        ViewHolder viewHolder = (ViewHolder) view.getTag(R.string.tag_item_viewHolder);

        //find columns of store attributes that we want
        int nameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        //read store attributes from cursor <- values can't be empty due to data validation
        String productName = cursor.getString(nameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);

        //update the textviews with the attributes for current store
        viewHolder.mNameView.setText(productName);
        viewHolder.mPriceView.setText(context.getString(R.string.list_price_label, productPrice));
        viewHolder.mQuantityView.setText(context.getString(R.string.list_quantity_label, productQuantity));

        //get ID for product
        int idIndex = cursor.getColumnIndex(StoreContract.ProductEntry._ID);
        final int id = cursor.getInt(idIndex);

        //setup listener for detail view
        viewHolder.mItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), DetailViewActivity.class);

                //Form content URI that represents specific product that was clicked on, by appending "id" (passed as input to method) into the
                //@PetEntry#CONTENT_URI
                Uri currentProductURi = ContentUris.withAppendedId(StoreContract.ProductEntry.CONTENT_URI, id);

                //set URI on the data field of intent
                intent.setData(currentProductURi);

                //Launch the @EditorActivity to display data for the current pet.
                v.getContext().startActivity(intent);
            }
        });

        //load data from cursor outside of listener, else you always read data from last item in cursor
        //get quantity column index & quantity value
        int quantityIndex = getCursor().getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int quantity = getCursor().getInt(quantityIndex);
        //pass quantity as tag with button
        viewHolder.mSellButton.setTag(R.string.tag_sellButton_quantity, quantity);
        //pass id as tag with button
        viewHolder. mSellButton.setTag(R.string.tag_sellButton_id, id);
        //setup sell button listener
        viewHolder.mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSellAction(v);
            }
        });
    }

    private void performSellAction(View v) {
        Context context = v.getContext();
        //get quantity value from view tag
        int quantity = (Integer) v.getTag(R.string.tag_sellButton_quantity);
        //check to see if quantity = 0, if so -> show toast & return
        if (quantity == 0) {
            Toast.makeText(context, context.getString(R.string.error_product_negative_sell), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // reduce quantity with 1
            quantity--;

            //create ContentValues object
            ContentValues values = new ContentValues();
            values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

            //get product URI
            Uri productUri = Uri.withAppendedPath(StoreContract.ProductEntry.CONTENT_URI, String.valueOf(v.getTag(R.string.tag_sellButton_id)));

            //update product
            context.getContentResolver().update(productUri, values, null, null);
            Toast.makeText(context, context.getString(R.string.product_sold_successful), Toast.LENGTH_SHORT).show();
        }
    }
}
