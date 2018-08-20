package com.example.android.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore.data.StoreContract;

public class DetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * codes for updateQuantity
     * 0 = sell
     * 1 = buy
     */
    private static final int QUANTITY_SELL = 0;
    private static final int QUANTITY_BUY = 1;

    /**
     * Uri for product
     */
    private Uri mProductUri;

    /**
     * Private variables for all relevant views that need to load data
     */
    private TextView mNameView;
    private TextView mPriceView;
    private TextView mQuantityView;
    private TextView mSupplierNameView;
    private TextView mSupplierPhoneView;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_detail);

        // extract product Uri from intent
        Intent intent = getIntent();
        mProductUri = intent.getData();
        //if no Uri to product data -> go to mainActivity
        if (mProductUri == null) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        } else {
            //load product
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all relevant views that we want to add data to
        mNameView = findViewById(R.id.detail_name_value);
        mPriceView = findViewById(R.id.detail_price_value);
        mQuantityView = findViewById(R.id.detail_quantity_value);
        mSupplierNameView = findViewById(R.id.detail_supplier_name_value);
        mSupplierPhoneView = findViewById(R.id.detail_supplier_phone_value);
    }

    /**
     * private helper method to update quantity when clicking on buy or sell button
     *
     * @param action Integer that defines if we either increase or decrease quantity
     */
    private void updateQuantity(int action, int quantity) {
        //initialize ContentValues object to pass onto ContentResolver
        ContentValues values = new ContentValues();
        //check action to see if we increase or decrease
        switch (action) {
            case QUANTITY_SELL:
                //check to see if quantity > 0, can't be negative!
                if (quantity > 0) {
                    quantity--;
                    values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    getContentResolver().update(mProductUri, values, null, null);
                    Toast.makeText(this, getString(R.string.detail_quantity_decrease_successful), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.error_quantity_negative), Toast.LENGTH_SHORT).show();
                }
                break;
            case QUANTITY_BUY:
                quantity++;
                values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                getContentResolver().update(mProductUri, values, null, null);
                Toast.makeText(this, getString(R.string.detail_quantity_increase_successful), Toast.LENGTH_SHORT).show();
                break;
            default: //this shouldn't happen
                throw new IllegalArgumentException(getString(R.string.error_quantity_code, action));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu from res/menu/menu_detail.xml file
        //This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_action_delete:
                //pop-up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.detail_action_edit:
                Intent intent = new Intent(this, EditorActivity.class);
                intent.setData(mProductUri);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * private helper method to show confirmation dialog when pressing delete in menu
     */
    private void showDeleteConfirmationDialog() {
        //create AlertDialog.Builder and set message, click listeners for positive&negative
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_message);
        builder.setPositiveButton(R.string.dialog_delete_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user clicked delete on confirmation
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.dialog_delete_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog, user clicked cancel
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //create and show AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * private helper method to delete product after clicking confirm on confirmation dialog
     * after deletion, relaunches main activity (because product is gone -> no more detail view)
     */
    private void deleteProduct() {
        //call ContentResolver to delete product with current Uri
        getContentResolver().delete(mProductUri, null, null);

        //return to mainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //since detail view shows all product attributes, projection = null -> return all columns
        //loader will execute ContentProvider's quary method on background thread
        return new CursorLoader(this,
                mProductUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if cursor doesn't find data
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //go to first (& only) row of cursor & load data
        if (cursor.moveToFirst()) {
            //find columns of product attributes (all)
            //find columns of product attributes (all)
            int nameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            //Extract out the value fromm Cursor for column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String supplier_name = cursor.getString(supplierNameColumnIndex);
            final String supplier_phone = cursor.getString(supplierPhoneColumnIndex);

            //update views with data
            mNameView.setText(name);
            mPriceView.setText(price);
            mQuantityView.setText(quantity);
            mSupplierNameView.setText(supplier_name);
            mSupplierPhoneView.setText(supplier_phone);

            //parse quantity to final int
            final int quantityInt = Integer.parseInt(quantity);

            //set onClickListeners on the buttons
            /*
      private variables for the action buttons
     */
            Button sellButton = findViewById(R.id.detail_action_sell);
            Button orderButton = findViewById(R.id.detail_action_call_supplier);
            Button buyButton = findViewById(R.id.detail_action_buy);
            //on click sell -> update quantity with sell action code & pass current quantity as param
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateQuantity(QUANTITY_SELL, quantityInt);
                }
            });
            //on click buy -> update quantity with buy action code & pass current quantity as param
            buyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateQuantity(QUANTITY_BUY, quantityInt);
                }
            });
            //on click order -> push intent to phone app with phone number as param
            orderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try { // try to launch dial action with given phone number
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        String uri = "tel:" + supplier_phone.trim();
                        intent.setData(Uri.parse(uri));
                        startActivity(intent);
                        finish();
                    } catch (ActivityNotFoundException e) { //if no intent handler found, show toast & do nothing
                        Toast.makeText(v.getContext(), getString(R.string.detail_no_phone_activity), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if loader is invalidated, clear all data from views
        mNameView.setText("");
        mPriceView.setText("");
        mQuantityView.setText("");
        mSupplierNameView.setText("");
        mSupplierPhoneView.setText("");
    }
}