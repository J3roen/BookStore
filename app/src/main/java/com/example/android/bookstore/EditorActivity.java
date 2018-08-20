package com.example.android.bookstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.bookstore.data.StoreContract.ProductEntry;

/**
 * Allows the user to create a new product or edit an existing one
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * URI for existing product (null if new product)
     */
    private Uri mCurrentProductUri;

    /**
     * field to enter product name
     */
    private EditText mNameEditText;

    /**
     * field to enter product price
     */
    private EditText mPriceEditText;

    /**
     * field to enter product quantity
     */
    private EditText mQuantityEditText;

    /**
     * field to enter product supplier name
     */
    private EditText mSupplierNameEditText;

    /**
     * field to enter product supplier phone
     */
    private EditText mSupplierPhoneEditText;

    /**
     * boolean to keep track if product has been changed
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener to see if user touches on a view, implying product data changed
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine intent used to launch activity
        // in order to figure out of creating new product or editing existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // if NO URI to product data -> creating a new product
        if (mCurrentProductUri == null) {
            //new product -> change app bar to say add a product
            setTitle(getString(R.string.action_add_new_product));

            //invalidate optoins menu, so 'delete' menu optoin can be hidden
            invalidateOptionsMenu();
        } else {
            //otherwise this is an existing product, so change app bar to say 'edit product'
            setTitle(getString(R.string.action_edit_product));

            //initialize a loader to read product data from database
            //and display current valeus in editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all relevant views that we will need user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * private helper method to get user input from fields & save to database
     * returns true if successful
     */
    private boolean saveProduct() {
        //read user input + trim whitespaces
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mSupplierPhoneEditText.getText().toString().trim();

        //check if fields are empty, if so -> do nothing & show toast message
        if (TextUtils.isEmpty(nameString.trim()) || TextUtils.isEmpty(priceString.trim()) ||
                TextUtils.isEmpty(quantityString.trim()) || TextUtils.isEmpty(supplierName.trim())
                || TextUtils.isEmpty(supplierPhone.trim())) {
            Toast.makeText(this, getString(R.string.error_empty_editor_fields), Toast.LENGTH_SHORT).show();
            return false;
        } else {

            //check if this is supposed to be a new product
            //check if all fields in editor are blank
            if (mCurrentProductUri == null
                    && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                    && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierName)
                    && TextUtils.isEmpty(supplierPhone)) {
                //if no fields are modified, return without creating new pet
                return false;
            }

            //Create a ContentValues object where column names are keys & product attributes are values
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);

            //Determine if this is a new or existing product by checking mCurrentProductUri
            if (mCurrentProductUri == null) {
                //this is a NEW product, insert product into provider, returning content URI for new product
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                //show a toast message depending if insertion was successful
                if (newUri == null) {
                    //if URI = null, insertion failed
                    Toast.makeText(this, getString(R.string.error_saving_product), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    //otherwise, insertion was successful
                    Toast.makeText(this, getString(R.string.product_saved_successful), Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else {
                //here is it an EXISTING product, so update with content URI
                // and pass new ContentValues. Pass in null for selection & selection args
                // because URI will already identify correct product
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                //show a toast message depending on update successful
                if (rowsAffected == 0) {
                    //no rows affected
                    Toast.makeText(this, getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    //rows affected, update successful
                    Toast.makeText(this, getString(R.string.product_update_successful, rowsAffected), Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu from res/menu/menu_editor.xml file
        //This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * method is called after invalidateOptionsMenu(), so menu can be updated
     * (some menu items can be hidden or made visible)
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //if this is new product, hide 'delete' menu item
        if (mCurrentProductUri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
        }
        return true;
    }

    /**
     * method is called when menu item is clicked
     *
     * @param item item that has been clicked
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //user clicked on menu option in app bar overflow menu
        switch (item.getItemId()) {
            //respond to "Save"
            case R.id.action_save:
                //save product to database -> true = successful, false = failed
                //finish activity if true, stay if false
                if (saveProduct())
                    finish();
                return true;
            //respond to click on "delete"
            case R.id.action_delete:
                //pop-up confirmation dialog for selection
                showDeleteConfirmationDialog();
                return true;
            //respond to click on "up" arrow button in app bar
            case android.R.id.home:
                //if product hasn't changed, continue with going to parent activity
                if (!mProductHasChanged) {
                    //relaunch detailActivity with product Uri
                    Intent intent = new Intent(this, DetailViewActivity.class);
                    intent.setData(mCurrentProductUri);
                    startActivity(intent);
                    return true;
                }

                //else, if unsaved changes, setup dialog to warn user
                //add clickListener to handle user confirming to discard changes
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //user clicked 'discard', navigate to parent
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                //show dialog that notifies user of unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //create AlertDialog.Builder and set up message + click listeners
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_discard_message);
        builder.setPositiveButton(R.string.dialog_discard_positive, (DialogInterface.OnClickListener) discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_discard_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user clicked keep editing button, dismiss dialog & continue
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
     * private helper method to prompt a confirm to delete pet
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
     * helper method to delete product after confirmation dialog
     */
    private void deleteProduct() {
        //only perform delete if this is an existing product
        if (mCurrentProductUri != null) {
            //Call ContentResolver to delete product at given content URI
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            //show a toest message depending on deletion successful
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.product_delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_delete_successful), Toast.LENGTH_SHORT).show();
            }
        }

        //close activity
        finish();
    }

    /**
     * method is called when back button pressed
     */
    @Override
    public void onBackPressed() {
        //if product hasn't changed, continue
        if (!mProductHasChanged) {
            //relaunch detail activity with given productUri
            Intent intent = new Intent(this, DetailViewActivity.class);
            intent.setData(mCurrentProductUri);
            startActivity(intent);
            return;
        }

        // Otherwise if unsaved changes, setup dialog to warn user
        DialogInterface.OnClickListener discardButtonCLickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user clicked discard, close activity
                finish();
            }

            //show dialog for unsaved changes
        };

        showUnsavedChangesDialog(discardButtonCLickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since editor shows all product attributes, define a projection that contains all columns
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        //this loader will execute the COntentProvider's query method on background thread
        return new CursorLoader(this, //parent activity context
                mCurrentProductUri,        // query the content URI
                projection,                // columns to include in cursor
                null,             //no selection clause
                null,         //no selection arguments
                null             //default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if cursor is null or no rows in cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //proceed with moving to first row of cursor & reading data (should be only row in cursor)
        if (cursor.moveToFirst()) {
            //find columns of product attributes (all)
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            //Extract out the value fromm Cursor for column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String supplier_name = cursor.getString(supplierNameColumnIndex);
            String supplier_phone = cursor.getString(supplierPhoneColumnIndex);

            //Update the views on screen with values from database
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mSupplierNameEditText.setText(supplier_name);
            mSupplierPhoneEditText.setText(supplier_phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //if loader is invalidated, clear all data from input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }
}
