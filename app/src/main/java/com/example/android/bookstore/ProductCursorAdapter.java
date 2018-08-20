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
    public void bindView(View view, Context context, final Cursor cursor) {
        //find individual views that we want to modify
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceView = (TextView) view.findViewById(R.id.product_price);
        TextView quantityView = (TextView) view.findViewById(R.id.product_quantity);
        Button sellButton = view.findViewById(R.id.item_list_sell_button);

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

        //setup listener for detail view
        LinearLayout itemContainer = view.findViewById(R.id.list_item_layout);
        itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ID for product
                int idIndex = cursor.getColumnIndex(StoreContract.ProductEntry._ID);
                int id = cursor.getInt(idIndex);

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

        //setup sell button listener
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                //get quantity column index & quantity value
                int quantityIndex = getCursor().getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
                int quantity = getCursor().getInt(quantityIndex);

                //check to see if quantity = 0, if so -> show toast & return
                if (quantity == 0) {
                    Toast.makeText(context, context.getString(R.string.error_product_negative_sell), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // reduce quantity with 1
                    quantity--;
                    //get product ID so we know what to update
                    int idIndex = getCursor().getColumnIndex(StoreContract.ProductEntry._ID);
                    int id = getCursor().getInt(idIndex);
                    //create ContentValues object
                    ContentValues values = new ContentValues();
                    values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

                    //get product URI

                    Uri productUri = Uri.withAppendedPath(StoreContract.ProductEntry.CONTENT_URI, String.valueOf(id));
                    //update product
                    context.getContentResolver().update(productUri, values, null, null);
                    Toast.makeText(context, context.getString(R.string.product_sold_successful), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
